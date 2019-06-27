package br.edu.ifro.agroplace.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import br.edu.ifro.agroplace.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView logo;
    private TextView textLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.splash_logo);
        textLogo = findViewById(R.id.splash_text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

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
