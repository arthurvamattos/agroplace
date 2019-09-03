package br.edu.ifro.agroplace.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.adapter.ProductsAdapter;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Base64Custom;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.helper.WhatsAppHelper;
import br.edu.ifro.agroplace.model.Produto;
import br.edu.ifro.agroplace.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private CircleImageView imageView;
    private TextView nomeField;
    private TextView contato;
    private RecyclerView recyclerView;
    private ProductsAdapter adapter;
    private ArrayList<Produto> produtos;

    private EventListener<QuerySnapshot> eventListener;
    private ListenerRegistration productListener;
    private com.google.firebase.firestore.Query productsRef;

    private String idVendedor;
    private String nome;

    private Usuario user = new Usuario();

    @Override
    public void onStart() {
        super.onStart();
        productListener = productsRef.addSnapshotListener(eventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        productListener.remove();
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
        recyclerView = findViewById(R.id.perfil_lista);
        contato = findViewById(R.id.perfil_contato);

        Bundle extra = getIntent().getExtras();
        if (extra != null){
            nome = extra.getString("nome");
            idVendedor = extra.getString("idVendedor");
            nomeField.setText(nome);
            setTitle(nome);
        }

        produtos = new ArrayList();

        adapter = new ProductsAdapter(PerfilActivity.this, produtos);
        adapter = new ProductsAdapter(PerfilActivity.this, produtos);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(PerfilActivity.this));

        eventListener = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                produtos.clear();
                produtos.addAll(queryDocumentSnapshots.toObjects(Produto.class));
                adapter.notifyDataSetChanged();
            }
        };

        Preferencias preferencias = new Preferencias(PerfilActivity.this);
        productsRef = ConfiguracaoFirebase.getInstance().collection("produtos").whereEqualTo("idVendedor", preferencias.getIdentificador());
        productListener = productsRef.addSnapshotListener(eventListener);

        contato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PerfilActivity.this);
                builder.setTitle("Contato")
                        .setMessage("Por onde deseja contatar o vendedor?")
                        .setNegativeButton("Pelo aplicativo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(PerfilActivity.this, ConversaActivity.class);
                                intent.putExtra("nome", nome);
                                intent.putExtra("email", Base64Custom.decodificarBase64(idVendedor));
                                startActivity(intent);
                                finish();
                            }
                        });

                if (user.getTelefone() != null){
                    builder.setPositiveButton("WhatsApp", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(WhatsAppHelper.gerarLinkAPI(user.getTelefone()))));
                        }
                    });
                }

                builder.show();
            }
        });
    }
}
