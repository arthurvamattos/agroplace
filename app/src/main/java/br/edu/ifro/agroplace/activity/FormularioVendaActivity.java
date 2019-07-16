package br.edu.ifro.agroplace.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Date;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.config.Categorias;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.Produto;

public class FormularioVendaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private static final int REQUISICAO_IMAGEM = 86;
    private FloatingActionButton btnFoto;
    private ImageView imageView;
    private TextInputEditText nomeField;
    private TextInputEditText valorField;
    private TextInputEditText descricaoField;
    private Preferencias preferencias;
    private StorageReference referenciaStorage;
    private DatabaseReference firebase;
    private Uri localImagemRecuperada;
    private Produto produto;

    private StorageTask tarefaUpload;
    private Bundle extra;
    private Spinner categoriasSpinner;
    private ArrayAdapter adapterCategorias;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_venda);

        preferencias = new Preferencias(FormularioVendaActivity.this);


        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Nova venda");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.formulario_foto);
        nomeField = findViewById(R.id.formulario_nome_produto);

        valorField = findViewById(R.id.formulario_valor);
        SimpleMaskFormatter smf = new SimpleMaskFormatter("R$ NN.NN");
        MaskTextWatcher mtw = new MaskTextWatcher(valorField, smf);
        valorField.addTextChangedListener(mtw);

        descricaoField = findViewById(R.id.formulario_descricao);

        btnFoto = findViewById(R.id.formulario_btn_foto);
        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirSeletorDeImagens();
            }
        });

        categoriasSpinner = findViewById(R.id.formulario_categorias);
        adapterCategorias = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Categorias.getCategoriasCasdastro());
        categoriasSpinner.setAdapter(adapterCategorias);

        extra = getIntent().getExtras();
        if (extra != null){
            produto = (Produto) extra.getSerializable("produto");
        }
        if (produto != null) {
            Picasso.get().load(produto.getUrlImagem()).fit().centerCrop().into(imageView);
            nomeField.setText(produto.getNome());
            valorField.setText(produto.getValor());
            descricaoField.setText(produto.getDescricao());
            toolbar.setTitle(produto.getNome());
        }

    }

    private void abrirSeletorDeImagens() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUISICAO_IMAGEM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == REQUISICAO_IMAGEM && data != null && data.getData() != null) {
                //recuperar local do recurso
                localImagemRecuperada = data.getData();
                Picasso.get().load(localImagemRecuperada).fit().centerCrop().into(imageView);
            }
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private boolean publicarVenda(){
        final boolean[] retorno = {false};
        try {
            if (localImagemRecuperada != null) {
                referenciaStorage = ConfiguracaoFirebase.getFirebaseStorage().child("produtos").child(System.currentTimeMillis()+"."+getFileExtension(localImagemRecuperada));
                tarefaUpload = referenciaStorage.putFile(localImagemRecuperada);
                Task<Uri> urlTask = tarefaUpload.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return referenciaStorage.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            finish();
                            if (validaCampos()){
                                retorno[0] = true;
                                Produto produto = montaProduto(task);
                                salvarProduto(produto);
                                Toast.makeText(FormularioVendaActivity.this, "Venda publicada com sucesso", Toast.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(findViewById(R.id.formulario_id), "Por favor, informe todos os campos!", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            Snackbar.make(findViewById(R.id.formulario_id), "Erro ao salvar venda, tente novamente!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (produto.getUrlImagem() != null) {
                bloqueiaMenuSalvar();
                salvarProduto(montaProduto());
                retorno[0] = true;
                finish();
                Toast.makeText(FormularioVendaActivity.this, "Venda alterada com sucesso", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e){
            Snackbar.make(findViewById(R.id.formulario_id), "Por favor, escolha uma foto para o produto!", Snackbar.LENGTH_SHORT).show();
        }
        return retorno[0];

    }

    private void bloqueiaMenuSalvar() {
        MenuItem menuItem = findViewById(R.id.menu_formulario_salvar);
        menuItem.setEnabled(false);
    }

    private void salvarProduto(Produto produto) {
        firebase = ConfiguracaoFirebase.getFirebase().child("produtos");
        firebase.child(produto.getId()).setValue(produto);
    }

    private Produto montaProduto(Task<Uri> task) {
        Produto produto = new Produto();
        produto.setId(String.valueOf(System.currentTimeMillis()));
        produto.setNome(nomeField.getText().toString());
        produto.setValor(valorField.getText().toString());
        produto.setDescricao(descricaoField.getText().toString());
        produto.setUrlImagem(task.getResult().toString());
        Preferencias preferencias = new Preferencias(FormularioVendaActivity.this);
        produto.setVendedor(preferencias.getNome());
        produto.setIdVendedor(preferencias.getIdentificador());
        Date dataAutal = new Date();
        produto.setDataPublicacao(dataAutal.toString());
        produto.setCategoria(categoriasSpinner.getSelectedItem().toString());
        return produto;
    }

    private Produto montaProduto() {
        Produto produto = new Produto();
        produto.setId(this.produto.getId());
        produto.setNome(nomeField.getText().toString());
        produto.setValor(valorField.getText().toString());
        produto.setDescricao(descricaoField.getText().toString());
        produto.setUrlImagem(this.produto.getUrlImagem());
        Preferencias preferencias = new Preferencias(FormularioVendaActivity.this);
        produto.setVendedor(preferencias.getNome());
        produto.setIdVendedor(preferencias.getIdentificador());
        Date dataAutal = new Date();
        produto.setDataPublicacao(dataAutal.toString());
        produto.setCategoria(categoriasSpinner.getSelectedItem().toString());
        return produto;
    }

    private boolean validaCampos() {
        return !nomeField.getText().toString().trim().equals("") && !valorField.getText().toString().trim().equals("") && !descricaoField.getText().toString().trim().equals("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (extra == null)
            inflater.inflate(R.menu.menu_formulario, menu);
        else
            inflater.inflate(R.menu.menu_formulario_edicao, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_formulario_salvar:
                if (tarefaUpload != null && tarefaUpload.isInProgress()){
                    Snackbar.make(findViewById(R.id.formulario_id), "Estamos publicando sua venda, por favor aguarde!", Snackbar.LENGTH_SHORT).show();
                } else {
                    publicarVenda();
                }
                return true;
            case R.id.menu_formulario_deletar:
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_warning)
                        .setTitle("Deletar Venda")
                        .setMessage("Você tem certeza que deseja remover esta venda?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                firebase = ConfiguracaoFirebase.getFirebase().child("produtos").child(preferencias.getIdentificador()).child(produto.getId());
                                firebase.removeValue();
                                Toast.makeText(FormularioVendaActivity.this, "Venda deletada com sucesso!", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        })
                        .setNegativeButton("Não", null)
                        .show();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
