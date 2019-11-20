package br.edu.ifro.agroplace.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.adapter.IntroViewPagerAdapter;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.IntroScreenItem;

public class IntroActivity extends AppCompatActivity {

    private ViewPager introScreenPager;
    private IntroViewPagerAdapter adapter;
    private TabLayout tabIndicator;
    private Button btnNext;
    private Button btnGettingStarted;
    private TextView btnSkip;
    private int position = 0;
    private Animation btnAnim ;
    private Preferencias preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);


        preferencias = new Preferencias(this);

        if (preferencias.isIntroOpened()) {
            Intent intent = new Intent(this, MainActivity.class );
            startActivity(intent);
            finish();
        }

        tabIndicator = findViewById(R.id.tabLayoutIndicator);
        btnNext = findViewById(R.id.btn_next);
        btnGettingStarted = findViewById(R.id.btn_getting_started);
        btnSkip = findViewById(R.id.btn_skip);

        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation);

        List<IntroScreenItem> list = new ArrayList<>();
        list.add(new IntroScreenItem("Olá!", "Temos algumas instruções para você, por que não da uma olhada?", R.drawable.intro_item1));
        list.add(new IntroScreenItem("Como postar um\nnovo produto?", "Para postar um novo produto basta ir até a aba meus produtos e pressionar o botão com sinal de \"+\"", R.drawable.intro_item2));
        list.add(new IntroScreenItem("Como pesquisar\num produto?", "Para pesquisar um produto basta clicar no ícone de lupa presente na toolbar, melhore os filtros da pesquisa ao selecionar uma categoria específica", R.drawable.intro_item3));
        list.add(new IntroScreenItem("Como visualizar detalhes\nde um produto?", "Para visualizar detalhes de um produto basta pressionar qualquer parte do cartão", R.drawable.intro_item4));
        list.add(new IntroScreenItem("Como visualizar\nconversas?", "Para visualizar as conversas basta pressionar no ícone de mensagem", R.drawable.intro_item5));
        list.add(new IntroScreenItem("Como saber se tenho\nmensagens não lidas?", "O ícone de mensagem passa a ter uma \"!\" para sinalizar mensagens não lidas", R.drawable.intro_item6));
        list.add(new IntroScreenItem("Você já está pronto!", "", R.drawable.intro_item7));

        introScreenPager = findViewById(R.id.intro_screen_pager);
        adapter = new IntroViewPagerAdapter(this, list);
        introScreenPager.setAdapter(adapter);

        tabIndicator.setupWithViewPager(introScreenPager);

        btnNext.setOnClickListener(v -> {
            position = introScreenPager.getCurrentItem();
            if (position < list.size()) {
                position++;
                introScreenPager.setCurrentItem(position);
            }
            if (position ==  list.size()) {
                loadLastItem();
            }
        });

        btnGettingStarted.setOnClickListener(view -> {
            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivity);
            preferencias.setIntroOpenend();
            finish();
        });

        tabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == list.size()-1) {
                    loadLastItem();
                } else {
                    unloadLastItem();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnSkip.setOnClickListener(v -> {
            introScreenPager.setCurrentItem(list.size());
        });
    }

    private void loadLastItem() {
        btnNext.setVisibility(View.INVISIBLE);
        btnGettingStarted.setVisibility(View.VISIBLE);
        tabIndicator.setVisibility(View.INVISIBLE);
        btnSkip.setVisibility(View.INVISIBLE);
        btnGettingStarted.setAnimation(btnAnim);
        btnGettingStarted.animate();
    }

    private void unloadLastItem() {
        btnNext.setVisibility(View.VISIBLE);
        btnGettingStarted.setVisibility(View.INVISIBLE);
        tabIndicator.setVisibility(View.VISIBLE);
        btnSkip.setVisibility(View.VISIBLE);
        btnGettingStarted.clearAnimation();
    }
}
