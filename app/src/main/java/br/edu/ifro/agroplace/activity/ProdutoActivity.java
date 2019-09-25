package br.edu.ifro.agroplace.activity;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Base64Custom;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.Conversa;
import br.edu.ifro.agroplace.model.Produto;

public class ProdutoActivity extends AppCompatActivity {

    private FirebaseAuth usuarioAutenticacao;

    private Toolbar toolbar;
    private ImageView imageView;
    private TextView valorField;
    private TextView descricaoField;
    private TextView vendedorField;
    private TextView categoriaField;
    private Button btnContato;
    private LinearLayout linkVendedor;


    private CollectionReference conversasRef;

    private Produto produto;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_green);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.produto_foto);
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
            montaVisualizacaoProduto();
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

    private void montaVisualizacaoProduto() {
        Picasso.get().load(produto.getUrlImagem()).fit().centerCrop().into(imageView);
        valorField.setText(produto.getValor());
        descricaoField.setText(produto.getDescricao());
        vendedorField.setText(produto.getVendedor());
        categoriaField.setText(produto.getCategoria());
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(produto.getNome());
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
        verificarConversasNaoLidas(menu);
        return true;
    }

    public void verificarConversasNaoLidas(final Menu menu){
        Preferencias preferencias = new Preferencias(ProdutoActivity.this);
        conversasRef = ConfiguracaoFirebase.getInstance().collection("conversas").document(preferencias.getIdentificador())
                .collection("contatos");
        final MenuItem menuConversa = menu.findItem(R.id.menu_main_conversas);
        conversasRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) return;
                int icon = R.drawable.ic_message_green;
                for (Conversa c : queryDocumentSnapshots.toObjects(Conversa.class)) {
                    if (!c.isVisualizada()){
                        icon = R.drawable.ic_announcement_green;
                    }
                }
                menuConversa.setIcon(icon);
            }
        });
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
