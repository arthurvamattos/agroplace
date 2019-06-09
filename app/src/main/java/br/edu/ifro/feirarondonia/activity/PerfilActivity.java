package br.edu.ifro.feirarondonia.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.adapter.ProdutoAdapter;
import br.edu.ifro.feirarondonia.config.ConfiguracaoFirebase;
import br.edu.ifro.feirarondonia.helper.Preferencias;
import br.edu.ifro.feirarondonia.model.Produto;
import br.edu.ifro.feirarondonia.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private CircleImageView imageView;
    private TextView nomeField;
    private ListView listView;
    private ProdutoAdapter adapter;
    private ArrayList<Produto> produtos;

    private DatabaseReference firebase;
    private ValueEventListener valueEventListenerProdutos;

    private String idVendedor;
    private String nome;


    @Override
    public void onStart() {
        super.onStart();
        firebase.addValueEventListener(valueEventListenerProdutos);
    }

    @Override
    public void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListenerProdutos);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.perfil_foto);
        nomeField = findViewById(R.id.perfil_nome);
        listView = findViewById(R.id.perfil_lista);

        Bundle extra = getIntent().getExtras();
        if (extra != null){
            nome = extra.getString("nome");
            idVendedor = extra.getString("idVendedor");
            toolbar.setTitle(nome);
            nomeField.setText(nome);
        }

        produtos = new ArrayList();
        produtos.clear();

        adapter = new ProdutoAdapter(produtos, PerfilActivity.this);
        listView = findViewById(R.id.perfil_lista);
        listView.setAdapter(adapter);

        firebase = ConfiguracaoFirebase.getFirebase().child("produtos").child(idVendedor);

        valueEventListenerProdutos = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                produtos.clear();
                for (DataSnapshot dado : dataSnapshot.getChildren()){
                    Produto produto = dado.getValue(Produto.class);
                    produtos.add(produto);
                }
                if (produtos != null)
                    Collections.reverse(produtos);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Produto produto = produtos.get(position);
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Produto produto = produtos.get(position);

                Intent intent = new Intent(PerfilActivity.this, FormularioVendaActivity.class);
                intent.putExtra("produto", produto);
                startActivity(intent);
                finish();
            }
        });

        DatabaseReference firebaseFoto = ConfiguracaoFirebase.getFirebase().child("usuarios").child(idVendedor);
        firebaseFoto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuarioRecuperdado = dataSnapshot.getValue(Usuario.class);
                if (usuarioRecuperdado.getUrlImagem() != null) {
                    Picasso.get().load(usuarioRecuperdado.getUrlImagem()).fit().centerCrop().into(imageView);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


    }
}
