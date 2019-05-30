package br.edu.ifro.feirarondonia.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.firebase.auth.FirebaseAuth;
import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.adapter.MainTabAdapter;
import br.edu.ifro.feirarondonia.config.ConfiguracaoFirebase;
import br.edu.ifro.feirarondonia.helper.SlidingTabLayout;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FirebaseAuth usuarioAutenticacao;

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Feira Rondônia");
        setSupportActionBar(toolbar);

        slidingTabLayout = findViewById(R.id.main_stl_tabs);
        viewPager = findViewById(R.id.vp_main);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(this, R.color.colorPrimary));
        MainTabAdapter tabAdapter = new MainTabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);
        slidingTabLayout.setViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_sair:
                deslogarUsuario();
                return true;
            case R.id.menu_main_nova_venda:
                abrirFormularioVenda();
                return true;
            case R.id.menu_main_pesquisa:
                return true;
            case R.id.menu_main_conversas:
                irParaConversas();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void irParaConversas() {
        Intent intent = new Intent(MainActivity.this, ConversasActivity.class);
        startActivity(intent);
    }

    public void abrirFormularioVenda() {
        Intent intent = new Intent(MainActivity.this, FormularioVendaActivity.class);
        startActivity(intent);
    }

    private void deslogarUsuario() {
        usuarioAutenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAutenticacao.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

