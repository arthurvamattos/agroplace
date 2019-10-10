package br.edu.ifro.agroplace.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Base64Custom;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.Usuario;

public class CadastroUsuarioActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private TextInputEditText emailField, nomeField, senhaField, confirmarsenhaField, telefoneField;
    private Button btnCadastrar;
    private Usuario usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_usuario);


        //Recuperando do XML
        emailField = findViewById(R.id.cadastro_usuario_email);
        nomeField = findViewById(R.id.cadastro_usuario_nome);
        senhaField = findViewById(R.id.cadastro_usuario_password);
        confirmarsenhaField = findViewById(R.id.cadastro_usuario_oonfirm_password);
        telefoneField = findViewById(R.id.cadastro_usuario_telefone);
        btnCadastrar = findViewById(R.id.cadastro_usuario_btn);

        SimpleMaskFormatter mascaraTelefone = new SimpleMaskFormatter("(NN) NNNNN-NNNN");
        MaskTextWatcher mascaraTelefoneWatcher = new MaskTextWatcher(telefoneField, mascaraTelefone);
        telefoneField.addTextChangedListener(mascaraTelefoneWatcher);

        //Cadastro de usuário
        btnCadastrar.setOnClickListener(v -> {
            if (!nomeField.getText().toString().trim().equals("") && !emailField.getText().toString().trim().equals("")
            && !senhaField.getText().toString().trim().equals("") && !confirmarsenhaField.getText().toString().trim().equals("")){
                usuario = new Usuario();
                usuario.setNome(nomeField.getText().toString());
                usuario.setEmail(emailField.getText().toString());
                if (senhaField.getText().toString().equals(confirmarsenhaField.getText().toString())){
                    usuario.setSenha(senhaField.getText().toString());
                    if (!telefoneField.getText().toString().trim().equals("")){
                        usuario.setTelefone(telefoneField.getText().toString());
                    } else {
                        usuario.setTelefone("");
                    }
                    cadastrarUsuario();
                } else {
                    Snackbar.make(findViewById(R.id.cadastro_usuario_id), "As senhas informadas são diferentes!", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.cadastro_usuario_id), "Por favor informe todos os campos!", Snackbar.LENGTH_SHORT).show();
            }

        });
    }

    private void cadastrarUsuario() {
        bloqueiaCampos();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        String identificadoUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setId(identificadoUsuario);
                        usuario.salvar();
                        String identificadorUsuario = Base64Custom.codificarBase64( usuario.getEmail() );
                        Preferencias preferencias = new Preferencias(CadastroUsuarioActivity.this);
                        preferencias.salvarDados(identificadorUsuario, usuario.getNome());
                        abrirMain();
                    } else {
                        desbloqueiaCampos();
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
                            mensagemDeErro = "Falha ao cadastrar usuário!";
                        }
                        Snackbar.make(findViewById(R.id.cadastro_usuario_id), mensagemDeErro, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void abrirMain() {
        Intent intent = new Intent(CadastroUsuarioActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void abrirLogin(View view) {
        finish();
    }

    private void bloqueiaCampos() {
        nomeField.setEnabled(false);
        emailField.setEnabled(false);
        senhaField.setEnabled(false);
        confirmarsenhaField.setEnabled(false);
        telefoneField.setEnabled(false);
    }

    private void desbloqueiaCampos() {
        emailField.setEnabled(true);
        senhaField.setEnabled(true);
        nomeField.setEnabled(true);
        confirmarsenhaField.setEnabled(true);
        telefoneField.setEnabled(true);
    }
}
