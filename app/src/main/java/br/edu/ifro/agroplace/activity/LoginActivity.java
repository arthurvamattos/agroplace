package br.edu.ifro.agroplace.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


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

    //google sign in
    public static int RC_SIGN_IN = 101;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton googleBtn;

    @Override
    protected void onStart() {
        super.onStart();
        verficarUsuarioLogado();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailField = findViewById(R.id.login_username);
        senhaField = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_btn);

        googleBtn = findViewById(R.id.sign_in_button);

        loginBtn.setOnClickListener(v -> {
            if (!emailField.getText().toString().trim().equals("") && !senhaField.getText().toString().trim().equals("")) {
                usuario = new Usuario();
                usuario.setEmail(emailField.getText().toString());
                usuario.setSenha(senhaField.getText().toString());
                logarUsuario();
            } else {
                Snackbar.make(findViewById(R.id.login_id), "Por favor informe todos os campos!", Snackbar.LENGTH_SHORT).show();
            }
        });

        //google sigin
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleBtn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void verficarUsuarioLogado() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if(autenticacao.getCurrentUser() != null)
            abrirMain();
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        autenticacao.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = autenticacao.getCurrentUser();
                            identificadorUsuarioLogado = Base64Custom.codificarBase64(emailField.getText().toString());
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Log.w("Error", "Google sign in failed");
                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("Error", "Google sign in failed", e);
                    // ...
                }
            }
    }

    private void logarUsuario() {
        bloqueiaCampos();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    identificadorUsuarioLogado = Base64Custom.codificarBase64(emailField.getText().toString());

                    instance = ConfiguracaoFirebase.getInstance().collection("usuarios").document(identificadorUsuarioLogado);
                    instance.get().addOnSuccessListener(documentSnapshot -> {
                        Usuario user = documentSnapshot.toObject(Usuario.class);
                        Preferencias preferencias = new Preferencias(LoginActivity.this);
                        preferencias.salvarDados(identificadorUsuarioLogado, user.getNome());
                        abrirMain();
                    });
                } else {
                    desbloqueiaCampos();
                    Snackbar.make(findViewById(R.id.login_id), "Usuário ou senha inválidos!", Snackbar.LENGTH_SHORT).show();
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
        builder.setPositiveButton("CONFIRMAR", (dialog, which) -> finish());
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
