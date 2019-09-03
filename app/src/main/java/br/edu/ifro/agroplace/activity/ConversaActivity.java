package br.edu.ifro.agroplace.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.adapter.MensagemAdapter;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Base64Custom;
import br.edu.ifro.agroplace.helper.IsoStringDate;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.Contato;
import br.edu.ifro.agroplace.model.Conversa;
import br.edu.ifro.agroplace.model.Mensagem;
import br.edu.ifro.agroplace.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editMensagem;
    private ImageButton btnEnviar;
    private ListView listView;
    private ArrayList<Mensagem> mensagens;
    private ArrayAdapter<Mensagem> adapter;

    private EventListener<QuerySnapshot> eventListenerMensagem;
    private ListenerRegistration messageListener;

    //dados do destinatário
    private String nomeUsuarioDestinatario;
    private String idUsuarioDestinatario;

    //dados do remetente
    private String idUsuarioRemetente;
    private String identificadorContato;

    private CollectionReference mensagensRef;
    private Preferencias preferencias;
    private Query buscarMensagensRef;
    private CollectionReference destinatarioRef;
    private String caminhoFotoDestinatario, caminhoFotoRemetente;

    @Override
    protected void onStart() {
        super.onStart();
        messageListener = buscarMensagensRef.addSnapshotListener(eventListenerMensagem);
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
        preferencias = new Preferencias(ConversaActivity.this);

        configuraRemetente();
        configuraDestinatario();

        mensagensRef = ConfiguracaoFirebase.getInstance().collection("mensagens");
        buscarMensagensRef = ConfiguracaoFirebase.getInstance().collection("mensagens").document(idUsuarioRemetente)
                .collection(idUsuarioDestinatario).orderBy("dataCriacao");

        destinatarioRef = ConfiguracaoFirebase.getInstance().collection("mensagens")
                .document(idUsuarioDestinatario).collection(idUsuarioRemetente);

        criarEventListener();
        marcarConversaComoVisualizada();
        recuperarItensXML();
        configurarAToobar();
        adapterCustomizado();
        recuperarMensagens();
        configurarListView();
        buscarFotoPerfil();
        enviarMensagem();
    }

    private void enviarMensagem() {
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textoMensagem = editMensagem.getText().toString();
                if (textoMensagem.isEmpty()){
                    Toast.makeText(ConversaActivity.this, "Digite uma mensagem para enviar!", Toast.LENGTH_SHORT).show();
                } else{
                    Mensagem mensagem = getMensagem(textoMensagem);
                    mensagem.setVisualizada(false);
                    //Salvar mensagem para o remetente
                    Boolean retornoMensagemRemetente =  salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                    if (!retornoMensagemRemetente) {
                        Snackbar.make(findViewById(R.id.conversa_id), "Problema ao salvar mensagem, tente novamente!", Snackbar.LENGTH_SHORT).show();
                    } else {
                        //Salvar mensagem para o destinatário
                        mensagem.setVisualizada(true);
                        Boolean retornoMensagemDestinatario = salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);
                        if (!retornoMensagemDestinatario) {
                            Snackbar.make(findViewById(R.id.conversa_id), "Problema ao salvar mensagem para o destinatário, tente novamente!", Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    //Salvamos a conversa para o remetente
                    Conversa conversa = getConversa(textoMensagem);
                    conversa.setVisualizada(true);
                    Boolean retornoConversaRemetente = salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, conversa);

                    if (!retornoMensagemRemetente) {
                        Snackbar.make(findViewById(R.id.conversa_id), "Problema ao salvar conversa, tente novamente!", Snackbar.LENGTH_SHORT).show();
                    } else {
                        conversa = new Conversa();
                        conversa.setIdUsuario(idUsuarioRemetente);
                        conversa.setNome(preferencias.getNome());
                        conversa.setMensagem(textoMensagem);
                        conversa.setCaminhoFoto(caminhoFotoRemetente);
                        Boolean retornoConversaDestinatario = salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, conversa);
                        if (!retornoConversaDestinatario) {
                            Snackbar.make(findViewById(R.id.conversa_id), "Problema ao salvar conversa para o destinatário, tente novamente!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    editMensagem.setText("");
                }
            }
        });
    }

    private Conversa getConversa(String textoMensagem) {
        Conversa conversa = new Conversa();
        conversa.setIdUsuario(idUsuarioDestinatario);
        conversa.setNome(nomeUsuarioDestinatario);
        conversa.setMensagem(textoMensagem);
        conversa.setCaminhoFoto(caminhoFotoDestinatario);
        return conversa;
    }

    private void configuraRemetente() {
        idUsuarioRemetente = preferencias.getIdentificador();
    }

    private void configuraDestinatario() {
        Bundle extra = getIntent().getExtras();
        if (extra != null){
            nomeUsuarioDestinatario = extra.getString("nome");
            String emailDestinatario = extra.getString("email");
            idUsuarioDestinatario = Base64Custom.codificarBase64(emailDestinatario);
        }
    }

    private void criarEventListener() {
        eventListenerMensagem = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                mensagens.clear();
                if (queryDocumentSnapshots.isEmpty()) return;
                mensagens.addAll(queryDocumentSnapshots.toObjects(Mensagem.class));
                adapter.notifyDataSetChanged();
                if (mensagens.size() > 0) {
                    marcarConversaComoVisualizada();
                    marcarMensagensComoVisualizadas();
                }
            }
        };
    }

    private Mensagem getMensagem(String textoMensagem) {
        Mensagem mensagem = new Mensagem();
        mensagem.setIdUsuario(idUsuarioRemetente);
        mensagem.setMensagem(textoMensagem);
        mensagem.setDataCriacao(IsoStringDate.getIsoStringDate());
        return mensagem;
    }

    private void recuperarItensXML() {
        toolbar = findViewById(R.id.tb_conversa);
        editMensagem = findViewById(R.id.edit_mensagem);
        btnEnviar = findViewById(R.id.btn_enviar);
        listView = findViewById(R.id.lv_conversas);
    }

    private void configurarListView() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Mensagem mensagem = mensagens.get(i);
                if (idUsuarioRemetente.equals( mensagem.getIdUsuario() )){
                    final TextView indicadorVisualizacao = view.findViewById(R.id.indicador_visualizacao);
                    if (mensagem.isVisualizada()) {
                        indicadorVisualizacao.setText("Visualizada");
                    }
                    if (indicadorVisualizacao.getVisibility() == View.VISIBLE){
                        indicadorVisualizacao.setVisibility(View.INVISIBLE);
                        indicadorVisualizacao.setTextSize(0);
                    } else {
                        indicadorVisualizacao.setVisibility(View.VISIBLE);
                        indicadorVisualizacao.setTextSize(14);
                    }
                }
            }
        });
    }

    private void recuperarMensagens() {
        buscarMensagensRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mensagens.clear();
                mensagens.addAll(queryDocumentSnapshots.toObjects(Mensagem.class));
                adapter.notifyDataSetChanged();
                if (mensagens.size() > 0) {
                    marcarConversaComoVisualizada();
                    marcarMensagensComoVisualizadas();
                }
            }
        });
        messageListener = mensagensRef.addSnapshotListener(eventListenerMensagem);
    }

    private void adapterCustomizado() {
        adapter = new MensagemAdapter(ConversaActivity.this, mensagens);
        listView.setDivider(null);
        listView.setAdapter(adapter);
    }

    private void configurarAToobar() {
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        TextView tituloToolbar = findViewById(R.id.toolbar_nome);
        tituloToolbar.setText(nomeUsuarioDestinatario);
        setSupportActionBar(toolbar);
    }

    private void buscarFotoPerfil() {
        caminhoFotoDestinatario = caminhoFotoRemetente = "";
        DocumentReference userRef = ConfiguracaoFirebase.getInstance().collection("usuarios").document(idUsuarioDestinatario);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Usuario usuario = documentSnapshot.toObject(Usuario.class);
                CircleImageView campoFoto = findViewById(R.id.toolbar_foto);
                Picasso.get().load(usuario.getUrlImagem()).fit().centerCrop().into(campoFoto);
                caminhoFotoDestinatario = usuario.getUrlImagem();
            }
        });

        DocumentReference userRef2 = ConfiguracaoFirebase.getInstance().collection("usuarios").document(idUsuarioRemetente);
        userRef2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Usuario user = documentSnapshot.toObject(Usuario.class);
               caminhoFotoRemetente = user.getUrlImagem();
            }
        });
    }

    private void marcarConversaComoVisualizada() {
        final DocumentReference conversasRef = ConfiguracaoFirebase.getInstance().collection("conversas")
            .document(idUsuarioRemetente).collection("contatos").document(idUsuarioDestinatario);
        conversasRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Conversa conversaAtualizda = documentSnapshot.toObject(Conversa.class);
                if (conversaAtualizda == null) return;
                conversaAtualizda.setVisualizada(true);
                conversasRef.set(conversaAtualizda);
            }
        });
    }

    private void marcarMensagensComoVisualizadas() {
        final ArrayList<String> listaIds = new ArrayList<>();
        destinatarioRef = ConfiguracaoFirebase.getInstance().collection("mensagens")
                .document(idUsuarioDestinatario).collection(idUsuarioRemetente);
        destinatarioRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
           @Override
           public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
               for (DocumentSnapshot doc : queryDocumentSnapshots){
                   listaIds.add(doc.getId());
               }
               for (int i = 0; i < listaIds.size() && mensagens.size() > 0; i++){
                   if (mensagens.get(i).getMensagem() != null) {
                       Mensagem novaMensagem = mensagens.get(i);
                       novaMensagem.setVisualizada(true);
                       destinatarioRef.document(listaIds.get(i)).set(novaMensagem);
                   }
               }
           }
        });
    }

    private boolean salvarMensagem(String idRemetente, String idDestinatario, Mensagem mensagem) {
        try {
            mensagensRef.document(idRemetente).collection(idDestinatario).add(mensagem);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean salvarConversa(String idRemetente, String idDestinatario, Conversa conversa) {
        try {
            CollectionReference conversasRef = ConfiguracaoFirebase.getInstance().collection("conversas");
            conversasRef.document(idRemetente).collection("contatos").document(idDestinatario).set(conversa);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_conversa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_conversa_contato:
                identificadorContato = idUsuarioDestinatario;

                //Montar contato
                DocumentReference usuarioContatoRef = ConfiguracaoFirebase.getInstance().collection("usuarios").document(identificadorContato);
                usuarioContatoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Usuario usuarioContato = documentSnapshot.toObject(Usuario.class);

                        Contato contato = new Contato();
                        contato.setIdentificadorUsuario(identificadorContato);
                        contato.setCaminhoFoto(usuarioContato.getUrlImagem());
                        contato.setEmail(usuarioContato.getEmail());
                        contato.setNome(usuarioContato.getNome());

                        //Salvar na lista de contatos
                        DocumentReference contatoRef = ConfiguracaoFirebase.getInstance()
                                .collection("contatos/").document(preferencias.getIdentificador());
                        contatoRef.collection("pessoas").document(identificadorContato).set(contato);

                        Snackbar.make(findViewById(R.id.conversa_id), contato.getNome()+" está na sua lista de contatos", Snackbar.LENGTH_SHORT).show();
                    }
                });
                return true;
            case R.id.menu_conversa_whatsapp:
                identificadorContato = idUsuarioDestinatario;
//                firebase = ConfiguracaoFirebase.getFirebase().child("usuarios").child(identificadorContato);
//                firebase.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                        //Recuperar dados do contato
//                        Usuario usuarioContato = dataSnapshot.getValue(Usuario.class);
//
//                        //Recuperar identificador do usuário logado (base64)
//                        String identificadorUsuarioLogado =  idUsuarioRemetente;
//
//                        firebase = ConfiguracaoFirebase.getFirebase().child("contatos")
//                                .child(identificadorUsuarioLogado)
//                                .child(identificadorContato);
//
//                        if (usuarioContato.getTelefone() != null)
//                        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(WhatsAppHelper.gerarLinkAPI(usuarioContato.getTelefone()))));
//                        else
//                            Snackbar.make(findViewById(R.id.conversa_id), "Este contato ainda não cadastrou o whatsapp, por favor use este chat para entrar em contato", Snackbar.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

}
