package br.edu.ifro.agroplace.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Base64Custom;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class FormularioUsuarioActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private static final int REQUISICAO_IMAGEM = 86;
    private FloatingActionButton btnFoto;
    private CircleImageView imageView;
    private TextInputEditText nomeField;
    private TextInputEditText emailField;
    private TextInputEditText senhaField;
    private TextInputEditText confirmarSenhaField;
    private TextInputEditText telefoneField;
    private Preferencias preferencias;
    private StorageReference referenciaStorage;
    private Uri localImagemRecuperada;
    private Usuario usuario;
    private FirebaseAuth autenticacao;

    private StorageTask tarefaUpload;
    private Bundle extra;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_usuario);

        preferencias = new Preferencias(FormularioUsuarioActivity.this);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Alterar Perfil");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.formulario_usuario_foto);
        nomeField = findViewById(R.id.formulario_usuario_nome);
        emailField = findViewById(R.id.formulario_usuario_email);
        senhaField = findViewById(R.id.formulario_usuario_password);
        confirmarSenhaField = findViewById(R.id.formulario_usuario_confirm_password);
        telefoneField = findViewById(R.id.formulario_usuario_telefone);
        btnFoto = findViewById(R.id.formulario_usuario_btn_foto);

        SimpleMaskFormatter mascaraTelefone = new SimpleMaskFormatter("(NN) NNNNN-NNNN");
        MaskTextWatcher mascaraTelefoneWatcher = new MaskTextWatcher(telefoneField, mascaraTelefone);
        telefoneField.addTextChangedListener(mascaraTelefoneWatcher);

        extra = getIntent().getExtras();
        if (extra != null){
            usuario = (Usuario) extra.getSerializable("usuario");
        }
        if (usuario != null) {
            Picasso.get().load(usuario.getUrlImagem()).fit().centerCrop().into(imageView);
            nomeField.setText(usuario.getNome());
            emailField.setText(usuario.getEmail());
            toolbar.setTitle(usuario.getNome());
            if (usuario.getTelefone() != null)
                telefoneField.setText(usuario.getTelefone());
        }

        btnFoto = findViewById(R.id.formulario_usuario_btn_foto);
        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirSeletorDeImagens();
            }
        });
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

    private void realizaAlteracao(){
        if (senhaField.getText().toString().trim().equals(confirmarSenhaField.getText().toString().trim()) && validaCampos()) {
            if (localImagemRecuperada != null) {
                referenciaStorage = ConfiguracaoFirebase.getFirebaseStorage().child("usuarios").child(System.currentTimeMillis() + "." + getFileExtension(localImagemRecuperada));
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
                            if (validaCampos()) {
                                usuario.setUrlImagem(task.getResult().toString());
                                salvarUsuario();
                                preferencias.salvarDados(usuario.getId(), usuario.getNome());
                                Toast.makeText(FormularioUsuarioActivity.this, "Usuario alterado com sucesso", Toast.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(findViewById(R.id.formulario_usuario_id), "Por favor, informe todos os campos!", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            Snackbar.make(findViewById(R.id.formulario_usuario_id), "Erro ao salvar novos dados, tente novamente!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                salvarUsuario();
                preferencias.salvarDados(usuario.getId(), usuario.getNome());
                finish();
                Toast.makeText(FormularioUsuarioActivity.this, "Usuário alterado com sucesso", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (!validaCampos()){
                Snackbar.make(findViewById(R.id.formulario_usuario_id), "Por favor, informe todos os campos", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(findViewById(R.id.formulario_usuario_id), "As senhas informadas são diferentes!", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void salvarUsuario() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        FirebaseUser firebaseUser = autenticacao.getCurrentUser();
        firebaseUser.updateEmail(emailField.getText().toString());
        firebaseUser.updatePassword(senhaField.getText().toString());
        autenticacao.updateCurrentUser(firebaseUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    String identificadoUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setId(identificadoUsuario);
                    usuario.setNome(nomeField.getText().toString());
                    usuario.setEmail(emailField.getText().toString());
                    if (!telefoneField.getText().toString().trim().equals(""))
                        usuario.setTelefone(telefoneField.getText().toString());
                    usuario.salvar();
                    String identificadorUsuario = Base64Custom.codificarBase64( usuario.getEmail() );
                    Preferencias preferencias = new Preferencias(FormularioUsuarioActivity.this);
                    preferencias.salvarDados(identificadorUsuario, usuario.getNome());
                    finish();
                } else {
                    String mensagemDeErro = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e){
                        mensagemDeErro = "Por favor digite uma senha mais forte!";
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        mensagemDeErro = "Por favor digite um e-mail válido!";
                    } catch (FirebaseAuthUserCollisionException e){
                        mensagemDeErro = "O e-mail informado já está em uso!";
                    } catch (Exception e) {
                        mensagemDeErro = "Falha ao alterar usuário!";
                    }
                    Snackbar.make(findViewById(R.id.formulario_usuario_id), mensagemDeErro, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validaCampos() {
        return !nomeField.getText().toString().trim().equals("") && !emailField.getText().toString().trim().equals("")
                && !senhaField.getText().toString().trim().equals("") && !confirmarSenhaField.getText().toString().trim().equals("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_formulario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_formulario_salvar:
                if (tarefaUpload != null && tarefaUpload.isInProgress()){
                    Snackbar.make(findViewById(R.id.formulario_usuario_id), "Estamos salvando seus dados, por favor aguarde!", Snackbar.LENGTH_SHORT).show();
                } else {
                    realizaAlteracao();
                }
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

}
