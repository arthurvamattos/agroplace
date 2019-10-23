package br.edu.ifro.agroplace.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.adapter.MensagemAdapter;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Base64Custom;
import br.edu.ifro.agroplace.helper.IsoStringDate;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.helper.WhatsAppHelper;
import br.edu.ifro.agroplace.model.Contato;
import br.edu.ifro.agroplace.model.Conversa;
import br.edu.ifro.agroplace.model.Mensagem;
import br.edu.ifro.agroplace.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText messageField;
    private ImageButton btnEnviar;
    private ListView listView;
    private CircleImageView campoFoto;
    private TextView tituloToolbar;

    private String idUsuarioDestinatario;
    private String idUsuarioRemetente;
    private String identificadorContato;

    private Usuario destinario;
    private Usuario remetente;

    private ArrayList<Mensagem> mensagens;
    private ArrayAdapter<Mensagem> adapter;
    private Preferencias preferencias;


    private EventListener<QuerySnapshot> eventListener;
    private ListenerRegistration messageListener;
    private com.google.firebase.firestore.Query messagesRef;

    @Override
    protected void onStart() {
        super.onStart();
        messageListener = messagesRef.addSnapshotListener(eventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        messageListener.remove();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);

        mensagens = new ArrayList<>();
        mensagens.clear();

        toolbar = findViewById(R.id.tb_conversa);
        messageField = findViewById(R.id.edit_mensagem);
        btnEnviar = findViewById(R.id.btn_enviar);
        campoFoto = findViewById(R.id.toolbar_foto);
        tituloToolbar = findViewById(R.id.toolbar_nome);
        listView = findViewById(R.id.lv_conversas);

        preferencias = new Preferencias(this);
        idUsuarioRemetente = preferencias.getIdentificador();

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            String emailDestinatario = extra.getString("email");
            idUsuarioDestinatario = Base64Custom.codificarBase64(emailDestinatario);
            ConfiguracaoFirebase.getInstance().collection("usuarios").document(idUsuarioDestinatario).get()
                    .addOnSuccessListener(
                            documentSnapshot -> {
                                destinario = documentSnapshot.toObject(Usuario.class);
                                toolbarConfig();
                            });
        }

        ConfiguracaoFirebase.getInstance().collection("usuarios").document(idUsuarioRemetente).get()
                .addOnSuccessListener(documentSnapshot -> remetente = documentSnapshot.toObject(Usuario.class));


        eventListener = (queryDocumentSnapshots, e) -> {
            mensagens.clear();
            if  (queryDocumentSnapshots != null){
                mensagens.addAll(queryDocumentSnapshots.toObjects(Mensagem.class));
                marcarMensagensComoVisualizadas();
                adapter.notifyDataSetChanged();
            }
        };

        messagesRef = ConfiguracaoFirebase.getInstance().collection("mensagens")
                .document(idUsuarioRemetente).collection(idUsuarioDestinatario)
                .orderBy("dataCriacao");
        messageListener = messagesRef.addSnapshotListener(eventListener);

        adapter = new MensagemAdapter(ConversaActivity.this, mensagens);
        listView.setDivider(null);
        listView.setAdapter(adapter);

        viewMessageStatusOnClick();
        btnEnviar.setOnClickListener(view -> sendMenssage());
        marcarConversaComoVisualizada();

    }


    private void sendMenssage() {
        String textoMensagem = messageField.getText().toString();
        AtomicBoolean sendError = new AtomicBoolean(false);
        if (textoMensagem.trim().equals("")) {
            Snackbar.make(findViewById(R.id.conversa_id), "Por favor digite uma mensagem", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Mensagem mensagem = getMessageObject(textoMensagem);
        //salvando para o rementene
        ConfiguracaoFirebase.getInstance().collection("mensagens")
                .document(remetente.getId()).collection(destinario.getId()).add(mensagem)
                .addOnSuccessListener(documentReference -> {
                    //salvado para o destinatário
                    ConfiguracaoFirebase.getInstance().collection("mensagens")
                            .document(destinario.getId()).collection(remetente.getId()).add(mensagem)
                            .addOnSuccessListener(documentReference2 -> {
                                //salvado para o remetente
                                Conversa conversa = getConversationObject(textoMensagem);
                                conversa.setVisualizada(true);
                                ConfiguracaoFirebase.getInstance().collection("conversas")
                                        .document(remetente.getId()).collection("contatos")
                                        .document(destinario.getId()).set(conversa)
                                        .addOnSuccessListener(docRef -> {
                                            Map<String, String> mapId = new HashMap<>();
                                            mapId.put("id", remetente.getId());
                                            ConfiguracaoFirebase.getInstance().collection("conversas")
                                                    .document(remetente.getId()).set(mapId);
                                            conversa.setIdUsuario(remetente.getId());
                                            conversa.setNome(remetente.getNome());
                                            conversa.setUrlImagem(remetente.getUrlImagem());
                                            conversa.setVisualizada(false);
                                            ConfiguracaoFirebase.getInstance().collection("conversas")
                                                    .document(destinario.getId()).collection("contatos")
                                                    .document(remetente.getId()).set(conversa)
                                                    .addOnFailureListener(err2 -> sendError.set(true));
                                        }).addOnSuccessListener(aVoid -> {
                                            Map<String, String> mapId = new HashMap<>();
                                            mapId.put("id", remetente.getId());
                                            ConfiguracaoFirebase.getInstance().collection("conversas")
                                                .document(remetente.getId()).set(mapId);})
                                        .addOnFailureListener(err3 -> sendError.set(true));
                            })
                            .addOnFailureListener(e -> sendError.set(true));
                })
                .addOnFailureListener(e -> sendError.set(true));

        if (sendError.get()) {
            Snackbar.make(findViewById(R.id.conversa_id), "Algo deu errado, por favor tente novamente em alguns instantes", Snackbar.LENGTH_SHORT).show();
        }
        messageField.setText("");
    }


    private Conversa getConversationObject(String textoMensagem) {
        Conversa conversa = new Conversa();
        conversa.setIdUsuario(destinario.getId());
        conversa.setNome(destinario.getNome());
        conversa.setMensagem(textoMensagem);
        conversa.setUrlImagem(destinario.getUrlImagem());
        return conversa;
    }

    private void marcarMensagensComoVisualizadas() {
        final ArrayList<String> listaIds = new ArrayList<>();
        CollectionReference destinatarioRef = ConfiguracaoFirebase.getInstance().collection("mensagens")
                .document(idUsuarioDestinatario).collection(idUsuarioRemetente);
        destinatarioRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots){
                listaIds.add(doc.getId());
                Mensagem mensagemAtualizda = doc.toObject(Mensagem.class);
                mensagemAtualizda.setVisualizada(true);
                destinatarioRef.document(doc.getId()).set(mensagemAtualizda);
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void goToProfile() {
        Intent intent = new Intent(ConversaActivity.this, PerfilActivity.class);
        intent.putExtra("idVendedor", destinario.getId());
        intent.putExtra("nome", destinario.getNome());
        startActivity(intent);
    }

    private Mensagem getMessageObject(String textoMensagem) {
        Mensagem mensagem = new Mensagem();
        mensagem.setIdUsuario(idUsuarioRemetente);
        mensagem.setMensagem(textoMensagem);
        mensagem.setDataCriacao(IsoStringDate.getIsoStringDate());
        return mensagem;
    }

    private void viewMessageStatusOnClick() {
        listView.setOnItemClickListener((adapterView, view, i, l) -> {

            Mensagem mensagem = mensagens.get(i);
            if (idUsuarioRemetente.equals(mensagem.getIdUsuario())) {
                final TextView indicadorVisualizacao = view.findViewById(R.id.indicador_visualizacao);
                if (mensagem.isVisualizada()) {
                    indicadorVisualizacao.setText("Visualizada");
                }
                if (indicadorVisualizacao.getVisibility() == View.VISIBLE) {
                    indicadorVisualizacao.setVisibility(View.INVISIBLE);
                    indicadorVisualizacao.setTextSize(0);
                } else {
                    indicadorVisualizacao.setVisibility(View.VISIBLE);
                    indicadorVisualizacao.setTextSize(14);
                }
            }
        });
    }

    private void marcarConversaComoVisualizada() {
        DocumentReference conversasRef = ConfiguracaoFirebase.getInstance().collection("conversas")
                .document(idUsuarioRemetente).collection("contatos")
                .document(idUsuarioDestinatario);
        conversasRef.get().addOnSuccessListener(documentSnapshot -> {
            Conversa conversaAtualizda = documentSnapshot.toObject(Conversa.class);
            if (conversaAtualizda == null) return;
            conversaAtualizda.setVisualizada(true);
            conversasRef.set(conversaAtualizda);
        });
    }

    private void toolbarConfig() {
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_green);
        tituloToolbar.setText(destinario.getNome());
        Picasso.get().load(destinario.getUrlImagem()).fit().centerCrop().into(campoFoto);
        setSupportActionBar(toolbar);

        tituloToolbar.setOnClickListener((View v) -> {
            goToProfile();
        });
        campoFoto.setOnClickListener((View v) -> {
            goToProfile();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_conversa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_conversa_contato:
                identificadorContato = idUsuarioDestinatario;

                //Montar contato
                DocumentReference usuarioContatoRef = ConfiguracaoFirebase.getInstance().collection("usuarios").document(identificadorContato);
                usuarioContatoRef.get().addOnSuccessListener(documentSnapshot -> {
                    Usuario usuarioContato = documentSnapshot.toObject(Usuario.class);

                    Contato contato = new Contato();
                    contato.setIdentificadorUsuario(identificadorContato);
                    contato.setUrlImagem(usuarioContato.getUrlImagem());
                    contato.setEmail(usuarioContato.getEmail());
                    contato.setNome(usuarioContato.getNome());

                    //Salvar na lista de contatos
                    DocumentReference contatoRef = ConfiguracaoFirebase.getInstance()
                            .collection("contatos/").document(preferencias.getIdentificador());
                    contatoRef.collection("pessoas").document(identificadorContato).set(contato);

                    contatoRef.set(contato);

                    Snackbar.make(findViewById(R.id.conversa_id), contato.getNome() + " está na sua lista de contatos", Snackbar.LENGTH_SHORT).show();
                });
                return true;
            case R.id.menu_conversa_whatsapp:
                identificadorContato = idUsuarioDestinatario;
                DocumentReference userRef = ConfiguracaoFirebase.getInstance().collection("usuarios").document(identificadorContato);
                userRef.get().addOnSuccessListener(documentSnapshot -> {
                    Usuario usuarioContato = documentSnapshot.toObject(Usuario.class);

                    if (usuarioContato.getTelefone() != null)
                        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(WhatsAppHelper.gerarLinkAPI(usuarioContato.getTelefone()))));
                    else
                        Snackbar.make(findViewById(R.id.conversa_id), "Este contato ainda não cadastrou o whatsapp, por favor use este chat para entrar em contato", Snackbar.LENGTH_SHORT).show();
                });
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
