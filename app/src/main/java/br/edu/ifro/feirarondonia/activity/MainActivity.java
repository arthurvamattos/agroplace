package br.edu.ifro.feirarondonia.activity;

import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.adapter.MainTabAdapter;
import br.edu.ifro.feirarondonia.config.Categorias;
import br.edu.ifro.feirarondonia.helper.HorizontalListView;
import br.edu.ifro.feirarondonia.helper.Preferencias;
import br.edu.ifro.feirarondonia.helper.SlidingTabLayout;

public class MainActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private HorizontalListView listViewCategorias;
    private ArrayAdapter adapterCategorias;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("AGROPLACE");
        setSupportActionBar(toolbar);

        slidingTabLayout = findViewById(R.id.main_stl_tabs);
        viewPager = findViewById(R.id.vp_main);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(this, R.color.colorPrimary));
        MainTabAdapter tabAdapter = new MainTabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);
        slidingTabLayout.setViewPager(viewPager);

        listViewCategorias = findViewById(R.id.categorias_lisview);
        adapterCategorias = new ArrayAdapter(this, android.R.layout.simple_list_item_1, Categorias.getCategoriasLista());
        listViewCategorias.setAdapter(adapterCategorias);


        listViewCategorias = findViewById(R.id.categorias_lisview);
        listViewCategorias.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Preferencias preferencias = new Preferencias(MainActivity.this);
                preferencias.trocarCategoria(adapterCategorias.getItem(position).toString());
            }
        });


    }
}

