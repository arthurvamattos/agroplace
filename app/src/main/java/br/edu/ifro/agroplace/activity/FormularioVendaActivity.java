package br.edu.ifro.agroplace.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.config.Categorias;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.IsoStringDate;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.Produto;
import br.edu.ifro.agroplace.model.Usuario;

public class FormularioVendaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private static final int REQUISICAO_IMAGEM = 86;
    private static final int REQUISICAO_CAMERA = 87;
    private FloatingActionButton btnFoto;
    private ImageView imageView;
    private TextInputEditText nomeField;
    private TextInputEditText valorField;
    private TextInputEditText descricaoField;
    private Preferencias preferencias;
    private StorageReference referenciaStorage;
    private Uri localImagemRecuperada;
    private Produto produto;
    private FirebaseFirestore db;
    private StorageTask<UploadTask.TaskSnapshot> tarefaUpload;
    private Bundle extra;
    private Spinner categoriasSpinner;
    private ArrayAdapter<String> adapterCategorias;
    private String caminhoFoto;
    private String sellerUrl;
    private String nomeVendedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_venda);

        db = ConfiguracaoFirebase.getInstance();

        preferencias = new Preferencias(FormularioVendaActivity.this);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Nova venda");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_green);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.formulario_foto);
        nomeField = findViewById(R.id.formulario_nome_produto);

        valorField = findViewById(R.id.formulario_valor);
        SimpleMaskFormatter smf = new SimpleMaskFormatter("R$ NN.NN");
        MaskTextWatcher mtw = new MaskTextWatcher(valorField, smf);
        valorField.addTextChangedListener(mtw);

        descricaoField = findViewById(R.id.formulario_descricao);

        btnFoto = findViewById(R.id.formulario_btn_foto);
        btnFoto.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(FormularioVendaActivity.this);
            builder.setTitle("Foto do produto")
                    .setMessage("Foto nova ou existente?")
                    .setNegativeButton("Usar a câmera", (dialog, id) -> abrirCamera())
                    .setPositiveButton("Abrir a galeria", (dialogInterface, i) -> abrirSeletorDeImagens()).show();
        });

        categoriasSpinner = findViewById(R.id.formulario_categorias);
        adapterCategorias = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Categorias.getCategoriasCasdastro());
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
            CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
            collapsingToolbarLayout.setTitle(produto.getNome());
        }
        getSellerData();

    }

    private void abrirCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        caminhoFoto = getExternalFilesDir(null)+"/agroplace"+System.currentTimeMillis()+".jpg";
        File arquivoFoto = new File(caminhoFoto);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(arquivoFoto));

        //Não faço ideia, mas funciona
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        startActivityForResult(intent, REQUISICAO_CAMERA);
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
            if (requestCode == REQUISICAO_CAMERA && caminhoFoto != null) {
                File foto = new File(caminhoFoto);
                localImagemRecuperada = Uri.fromFile(foto);
                Picasso.get().load(localImagemRecuperada).fit().centerCrop().into(imageView);
            }
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void publicarVenda(){
        try {
            if (localImagemRecuperada != null) {
                bloqueiaCampos();
                Snackbar snake = Snackbar.make(findViewById(R.id.formulario_id), "Estamos publicando sua venda, só mais um segundo", Snackbar.LENGTH_INDEFINITE);
                snake.show();
                referenciaStorage = ConfiguracaoFirebase.getFirebaseStorage().child(System.currentTimeMillis()+"."+getFileExtension(localImagemRecuperada));
                tarefaUpload = referenciaStorage.putFile(localImagemRecuperada);
                tarefaUpload.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return referenciaStorage.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        finish();
                        if (validaCampos()){
                            String id = "";
                            if (produto != null) {
                                id = produto.getIdProduto();
                            }
                            Produto produto = montaProduto(task);
                            if (!id.equals("")) {
                                produto.setIdProduto(id);
                            }
                            salvarProduto(produto);
                            Toast.makeText(FormularioVendaActivity.this, "Venda publicada com sucesso", Toast.LENGTH_SHORT).show();
                        } else {
                            desbloqueiaCampos();
                            Snackbar.make(findViewById(R.id.formulario_id), "Por favor, informe todos os campos!", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        desbloqueiaCampos();
                        Snackbar.make(findViewById(R.id.formulario_id), "Erro ao salvar venda, tente novamente!", Snackbar.LENGTH_SHORT).show();
                    }
                });
            } else if (produto.getUrlImagem() != null) {
                bloqueiaCampos();
                Snackbar snake = Snackbar.make(findViewById(R.id.formulario_id), "Estamos publicando sua venda, só mais um segundo", Snackbar.LENGTH_INDEFINITE);
                snake.show();
                salvarProduto(montaProduto());
                finish();
                Toast.makeText(FormularioVendaActivity.this, "Venda alterada com sucesso", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e){
            desbloqueiaCampos();
            Snackbar.make(findViewById(R.id.formulario_id), "Por favor, escolha uma foto para o produto!", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void bloqueiaCampos() {
        btnFoto.setClickable(false);
        nomeField.setEnabled(false);
        valorField.setEnabled(false);
        descricaoField.setEnabled(false);
        categoriasSpinner.setEnabled(false);
    }

    private void desbloqueiaCampos() {
        btnFoto.setClickable(true);
        nomeField.setEnabled(true);
        valorField.setEnabled(true);
        descricaoField.setEnabled(true);
        categoriasSpinner.setEnabled(true);
    }

    private void salvarProduto(Produto produto) {
        Map<String, Object> prod = montaMap(produto);
        db.collection("produtos").document(produto.getIdProduto()).set(prod);
    }

    private Map<String, Object> montaMap(Produto prod) {
        Map<String, Object> produto = new HashMap<>();
        produto.put("categoria", prod.getCategoria());
        produto.put("dataPublicacao", prod.getDataPublicacao());
        produto.put("descricao", prod.getDescricao());
        produto.put("idProduto", prod.getIdProduto());
        produto.put("idVendedor", prod.getIdVendedor());
        produto.put("nome", prod.getNome());
        produto.put("urlImagem", prod.getUrlImagem());
        produto.put("urlFotoVendedor", prod.getUrlFotoVendedor());
        produto.put("valor", prod.getValor());
        produto.put("vendedor", prod.getVendedor());
        return produto;
    }

    private Produto montaProduto(Task<Uri> task) {
        Produto produto =  montaBaseProduto();
        produto.setIdProduto(produto.getNome()+System.currentTimeMillis());
        produto.setUrlImagem(task.getResult().toString());
        return produto;
    }

    private Produto montaBaseProduto() {
        Produto produto = new Produto();
        produto.setNome(nomeField.getText().toString());
        produto.setValor(valorField.getText().toString());
        produto.setDescricao(descricaoField.getText().toString());
        produto.setVendedor(nomeVendedor);
        produto.setIdVendedor(preferencias.getIdentificador());
        produto.setDataPublicacao(IsoStringDate.getIsoStringDate());
        produto.setCategoria(categoriasSpinner.getSelectedItem().toString());
        produto.setUrlFotoVendedor(sellerUrl);
        return produto;
    }


    private Produto montaProduto() {
        Produto produto =  montaBaseProduto();
        produto.setIdProduto(this.produto.getIdProduto());
        produto.setUrlImagem(this.produto.getUrlImagem());
        return produto;
    }

    private void getSellerData() {
        DocumentReference userRef = ConfiguracaoFirebase.getInstance().collection("usuarios")
                .document(preferencias.getIdentificador());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Usuario user = documentSnapshot.toObject(Usuario.class);
            sellerUrl = user.getUrlImagem();
            nomeVendedor = user.getNome();
        });
    }


    private boolean validaCampos() {
        return !nomeField.getText().toString().trim().equals("") && !valorField.getText().toString().trim().equals("")
                && !descricaoField.getText().toString().trim().equals("");
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
                        .setPositiveButton("Sim", (dialog, which) -> {
                            bloqueiaCampos();
                            db.collection("produtos").document(produto.getIdProduto()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(FormularioVendaActivity.this, "Venda deletada com sucesso!", Toast.LENGTH_SHORT).show();
                                finish();
                                    })
                            .addOnFailureListener(e -> {
                                desbloqueiaCampos();
                                Toast.makeText(FormularioVendaActivity.this, "Erro ao deletar venda, tente novamente mais tarde!", Toast.LENGTH_SHORT).show();
                            });
                    })
                        .setNegativeButton("Não", null)
                        .show();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
