package br.edu.ifro.feirarondonia.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.config.ConfiguracaoFirebase;
import br.edu.ifro.feirarondonia.helper.Base64Custom;
import br.edu.ifro.feirarondonia.model.Produto;

public class ProdutoActivity extends AppCompatActivity {

    private FirebaseAuth usuarioAutenticacao;

    private Toolbar toolbar;
    private ImageView imageView;
    private TextView nomeField;
    private TextView valorField;
    private TextView descricaoField;
    private TextView vendedorField;
    private TextView categoriaField;
    private Button btnContato;
    private LinearLayout linkVendedor;

    private Produto produto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);


        imageView = findViewById(R.id.produto_foto);
        nomeField = findViewById(R.id.produto_nome);
        valorField = findViewById(R.id.produto_valor);
        descricaoField = findViewById(R.id.produto_descricao);
        vendedorField = findViewById(R.id.produto_vendedor);
        categoriaField = findViewById(R.id.produto_categoria);
        btnContato = findViewById(R.id.produto_btn);
        linkVendedor = findViewById(R.id.produto_vendedor_link);

        Bundle extra = getIntent().getExtras();
        if (extra != null){
            produto = (Produto) extra.getSerializable("produto");
        }
        if (produto != null) {
            Picasso.get().load(produto.getUrlImagem()).fit().centerCrop().into(imageView);
            nomeField.setText(produto.getNome());
            valorField.setText(produto.getValor());
            descricaoField.setText(produto.getDescricao());
            vendedorField.setText(produto.getVendedor());
            categoriaField.setText(produto.getCategoria());
            setTitle(produto.getNome());
        }

        btnContato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProdutoActivity.this, ConversaActivity.class);
                intent.putExtra("nome", produto.getVendedor());
                intent.putExtra("email", Base64Custom.decodificarBase64(produto.getIdVendedor()));
                startActivity(intent);
            }
        });

        linkVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirPerfil();
            }
        });
    }

    private void abrirPerfil() {
        Intent intent =  new Intent(ProdutoActivity.this, PerfilActivity.class);
        intent.putExtra("idVendedor",produto.getIdVendedor());
        intent.putExtra("nome", produto.getVendedor());
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_produto, menu);
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
            case R.id.menu_main_conversas:
                irParaConversas();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
    private void irParaConversas() {
        Intent intent = new Intent(ProdutoActivity.this, ConversasActivity.class);
        startActivity(intent);
    }

    public void abrirFormularioVenda() {
        Intent intent = new Intent(ProdutoActivity.this, FormularioVendaActivity.class);
        startActivity(intent);
    }

    private void deslogarUsuario() {
        usuarioAutenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAutenticacao.signOut();
        Intent intent = new Intent(ProdutoActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
