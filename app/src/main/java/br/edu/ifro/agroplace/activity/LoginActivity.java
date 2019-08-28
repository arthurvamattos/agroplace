package br.edu.ifro.agroplace.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Base64Custom;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private TextInputEditText emailField;
    private TextInputEditText senhaField;
    private Button loginBtn;
    private DocumentReference instance;
    private Usuario usuario;
    private String identificadorUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        verficarUsuarioLogado();

        emailField = findViewById(R.id.login_username);
        senhaField = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_btn);
        //cadastro

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!emailField.getText().toString().trim().equals("") && !senhaField.getText().toString().trim().equals("")) {
                    usuario = new Usuario();
                    usuario.setEmail(emailField.getText().toString());
                    usuario.setSenha(senhaField.getText().toString());
                    logarUsuario();
                } else {
                    Snackbar.make(findViewById(R.id.login_id), "Por favor informe todos os campos!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void verficarUsuarioLogado() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if(autenticacao.getCurrentUser() != null)
            abrirMain();
    }

    private void logarUsuario() {
        bloqueiaCampos();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        identificadorUsuarioLogado = Base64Custom.codificarBase64(emailField.getText().toString());

                        instance = ConfiguracaoFirebase.getInstance().collection("usuarios").document(identificadorUsuarioLogado);
                        instance.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Usuario user = documentSnapshot.toObject(Usuario.class);
                                Preferencias preferencias = new Preferencias(LoginActivity.this);
                                preferencias.salvarDados(identificadorUsuarioLogado, user.getNome());
                                abrirMain();
                            }
                        });
                    } else {
                        desbloqueiaCampos();
                        Snackbar.make(findViewById(R.id.login_id), "Usuário ou senha inválidos!", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void abrirMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int resultado : grantResults){
            if (resultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões negadas");
        builder.setMessage("Para utilizar este app é necessário aceitar as permissões");
        builder.setPositiveButton("CONFIRMAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void abrirCadastroUsuario(View view) {
        Intent intent = new Intent(LoginActivity.this, CadastroUsuarioActivity.class);
        startActivity(intent);
    }

    private void bloqueiaCampos() {
        emailField.setEnabled(false);
        senhaField.setEnabled(false);
    }

    private void desbloqueiaCampos() {
        emailField.setEnabled(true);
        senhaField.setEnabled(true);
    }
}
