package br.edu.ifro.agroplace.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;

public class EsqueceuASenhaActivity extends AppCompatActivity {


    private FirebaseAuth auth;
    private TextInputEditText emailField;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esqueceu_asenha);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emailField = findViewById(R.id.esqueceu_asenha_email);
        button = findViewById(R.id.esqueceu_asenha_btn);

        button.setOnClickListener(view -> {
            if (!emailField.getText().toString().trim().equals("")) {
                emailField.setEnabled(false);
                button.setEnabled(false);
                String email = emailField.getText().toString();
                auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Snackbar.make(findViewById(R.id.esqueceu_asenha), "E-mail enviado com sucesso!", Snackbar.LENGTH_SHORT).show();

                                new Handler().postDelayed(() -> {
                                    Intent intent = new Intent(EsqueceuASenhaActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                },1000);
                            } else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidUserException e){
                                    Snackbar.make(findViewById(R.id.esqueceu_asenha), "Verifique o e-mail digitado ou tente usar uma conta do google!", Snackbar.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            emailField.setEnabled(true);
                            button.setEnabled(true);
                        });
            } else {
                Snackbar.make(findViewById(R.id.esqueceu_asenha), "Por favor informe um e-mail!", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    public void abrirlogin(View view) {
        Intent intent = new Intent(EsqueceuASenhaActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
