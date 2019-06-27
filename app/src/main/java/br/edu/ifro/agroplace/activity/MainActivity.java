package br.edu.ifro.agroplace.activity;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.adapter.MainTabAdapter;
import br.edu.ifro.agroplace.config.Categorias;
import br.edu.ifro.agroplace.helper.CategoriaObserver;
import br.edu.ifro.agroplace.helper.HorizontalListView;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.helper.SlidingTabLayout;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private HorizontalListView listViewCategorias;
    private ArrayAdapter adapterCategorias;

    private static List<CategoriaObserver> observers = new ArrayList<CategoriaObserver>();

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
                notifyObservers();
            }
        });

    }

    public static void adicionarObserver(CategoriaObserver obs) {
        observers.add(obs);
    }

    private void notifyObservers() {
        Preferencias preferencias = new Preferencias(MainActivity.this);
        String categoria = preferencias.getCategoria();
        for (CategoriaObserver observer : this.observers) {
            observer.update(categoria);
        }
        if (preferencias.getCategoria().equals(Categorias.getCategoriasLista()[0]))
            Snackbar.make(findViewById(R.id.main_id), "Mostrando todos os produtos", Snackbar.LENGTH_SHORT).show();
        else{
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.main_id), "Mostrando "+categoria, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Mostrar todos", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (CategoriaObserver observer : observers) {
                        observer.update(Categorias.getCategoriasLista()[0]);
                    }
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }
    }


}

