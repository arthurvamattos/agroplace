package br.edu.ifro.agroplace.activity;

import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.adapter.MainTabAdapter;
import br.edu.ifro.agroplace.config.Categorias;
import br.edu.ifro.agroplace.fragment.MeusProdutosFragment;
import br.edu.ifro.agroplace.fragment.ProdutosFragment;
import br.edu.ifro.agroplace.helper.CategoriaObserver;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.helper.SearchViewObserver;
import br.edu.ifro.agroplace.helper.SlidingTabLayout;

public class MainActivity extends AppCompatActivity implements SearchViewObserver {

    private Toolbar toolbar;
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private Spinner spinnerCategorias;
    private ImageView toolbarLogo;

    private static List<CategoriaObserver> observers = new ArrayList<CategoriaObserver>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("AGROPLACE");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ProdutosFragment.adicionarObserver(this);
        MeusProdutosFragment.adicionarObserver(this);

        slidingTabLayout = findViewById(R.id.main_stl_tabs);
        viewPager = findViewById(R.id.vp_main);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(this, R.color.colorPrimary));
        MainTabAdapter tabAdapter = new MainTabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);
        slidingTabLayout.setViewPager(viewPager);

        toolbarLogo = findViewById(R.id.toolbar_logo);

        spinnerCategorias = findViewById(R.id.categorias_spinner);
        ArrayAdapter adapterCategorias = new ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, Categorias.getCategoriasLista());
        spinnerCategorias.setAdapter(adapterCategorias);

        spinnerCategorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Preferencias preferencias = new Preferencias(MainActivity.this);
                preferencias.trocarCategoria(Categorias.getCategoriasLista()[i]);
                notifyObservers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.main_id), "Mostrando "+categoria, Snackbar.LENGTH_SHORT);
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


    @Override
    public void update(boolean opened) {
        if (opened) {
            toolbarLogo.setVisibility(View.INVISIBLE);
        } else {
            toolbarLogo.setVisibility(View.VISIBLE);
        }
    }
}

