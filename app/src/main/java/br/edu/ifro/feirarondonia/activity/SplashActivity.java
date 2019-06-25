package br.edu.ifro.feirarondonia.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import br.edu.ifro.feirarondonia.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView logo;
    private TextView textLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.splash_logo);
        textLogo = findViewById(R.id.splash_text);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        },2000);

        Animation animacao = AnimationUtils.loadAnimation(this,R.anim.splash_animation);
        logo.startAnimation(animacao);
        textLogo.startAnimation(animacao);
    }
}
