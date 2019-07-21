package br.edu.ifro.agroplace.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.adapter.MensagemAdapter;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Base64Custom;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.helper.WhatsAppHelper;
import br.edu.ifro.agroplace.model.Contato;
import br.edu.ifro.agroplace.model.Conversa;
import br.edu.ifro.agroplace.model.Mensagem;
import br.edu.ifro.agroplace.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editMensagem;
    private ImageButton btnEnviar;
    private DatabaseReference firebase;
    private FirebaseAuth auth;
    private ListView listView;
    private ArrayList<Mensagem> mensagens;
    private ArrayAdapter<Mensagem> adapter;
    private ValueEventListener valueEventListenerMensagem;

    //dados do destinatário
    private String nomeUsuarioDestinatario;
    private String idUsuarioDestinatario;

    //dados do remetente
    private String idUsuarioRemetente;

    private String identificadorContato;
    private DatabaseReference mensagensDestinatario;
    private ValueEventListener mensagensVisualiazadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);

        toolbar = findViewById(R.id.tb_conversa);
        editMensagem = findViewById(R.id.edit_mensagem);
        btnEnviar = findViewById(R.id.btn_enviar);
        listView = findViewById(R.id.lv_conversas);

        //dados do usuário logado
        Preferencias preferencias = new Preferencias(ConversaActivity.this);
        idUsuarioRemetente = preferencias.getIdentificador();

        Bundle extra = getIntent().getExtras();
        if (extra != null){
            nomeUsuarioDestinatario = extra.getString("nome");
            String emailDestinatario = extra.getString("email");
            idUsuarioDestinatario = Base64Custom.codificarBase64(emailDestinatario);
        }

        //Configurar a toobar
        configurarAToobar();

        //Montar a listview e adapter
        mensagens = new ArrayList<>();

        //Adapter customizado
        adapterCustomizado();

        //Recuperar as mensagens do firebase
        recuperarMensagens();

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

        valueEventListenerMensagem = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mensagens.clear();
                for (DataSnapshot dado : dataSnapshot.getChildren()){
                    Mensagem mensagem = dado.getValue(Mensagem.class);
                    mensagens.add(mensagem);
                }
                adapter.notifyDataSetChanged();
                if (mensagens.size() > 0) marcarConversaComoVisualizada();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        firebase.addValueEventListener(valueEventListenerMensagem);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textoMensagem = editMensagem.getText().toString();
                if (textoMensagem.isEmpty()){
                    Toast.makeText(ConversaActivity.this, "Digite uma mensagem para enviar!", Toast.LENGTH_SHORT).show();
                } else{
                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioRemetente);
                    mensagem.setMensagem(textoMensagem);
                    //Salvar mensagem para o remetente
                    mensagem.setVisualizada(false);
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
                    Conversa conversa = new Conversa();
                    conversa.setIdUsuario(idUsuarioDestinatario);
                    conversa.setNome(nomeUsuarioDestinatario);
                    conversa.setMensagem(textoMensagem);
                    conversa.setVisualizada(true);
                    Boolean retornoConversaRemetente = salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, conversa);

                    if (!retornoMensagemRemetente) {
                        Snackbar.make(findViewById(R.id.conversa_id), "Problema ao salvar conversa, tente novamente!", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Preferencias preferencias = new Preferencias(ConversaActivity.this);
                        conversa = new Conversa();
                        conversa.setIdUsuario(idUsuarioRemetente);
                        conversa.setNome(preferencias.getNome());
                        conversa.setMensagem(textoMensagem);
                        Boolean retornoConversaDestinatario = salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, conversa);
                        if (!retornoConversaDestinatario) {
                            Snackbar.make(findViewById(R.id.conversa_id), "Problema ao salvar conversa para o destinatário, tente novamente!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    editMensagem.setText("");
                }
            }
        });
        marcarMensagensComoVisualizadas();
        buscarFotoPerfil();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mensagensDestinatario.addValueEventListener(mensagensVisualiazadas);
    }

    private void recuperarMensagens() {
        firebase = ConfiguracaoFirebase.getFirebase().child("mensagens").child(idUsuarioRemetente).child(idUsuarioDestinatario);
        firebase.keepSynced(true);
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
        DatabaseReference firebase = ConfiguracaoFirebase.getFirebase().child("usuarios").child(idUsuarioDestinatario);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuarioRecuperdado = dataSnapshot.getValue(Usuario.class);
                if (usuarioRecuperdado.getUrlImagem() != null) {
                    CircleImageView campoFoto = findViewById(R.id.toolbar_foto);
                    Picasso.get().load(usuarioRecuperdado.getUrlImagem()).fit().centerCrop().into(campoFoto);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


    }

    private void marcarConversaComoVisualizada() {
        firebase = ConfiguracaoFirebase.getFirebase().child("conversas");
        firebase.child(idUsuarioRemetente).child(idUsuarioDestinatario).child("visualizada").setValue(Boolean.TRUE);
        firebase.keepSynced(true);
    }

    private void marcarMensagensComoVisualizadas() {
        final List<String> mensagens = new ArrayList<>();
        mensagens.clear();

        mensagensVisualiazadas = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mensagens.clear();
                for (DataSnapshot dado : dataSnapshot.getChildren()) {
                    String id = dado.getKey();
                    if (!id.equals(idUsuarioRemetente) && !id.equals(idUsuarioDestinatario))
                        mensagens.add(dado.getKey());
                }

                for (String key : mensagens) {
                    DatabaseReference mensagensDestinatario = ConfiguracaoFirebase.getFirebase().child("mensagens")
                            .child(idUsuarioDestinatario).child(idUsuarioRemetente).child(key).child("visualizada");
                    mensagensDestinatario.setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mensagensDestinatario = ConfiguracaoFirebase.getFirebase().child("mensagens").child(idUsuarioDestinatario).child(idUsuarioRemetente);
    }

    private boolean salvarMensagem(String idRemetente, String idDestinatario, Mensagem mensagem) {
        try {
            firebase = ConfiguracaoFirebase.getFirebase().child("mensagens");
            firebase.child(idRemetente).child(idDestinatario).push().setValue(mensagem);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean salvarConversa(String idRemetente, String idDestinatario, Conversa conversa) {
        try {
            firebase = ConfiguracaoFirebase.getFirebase().child("conversas").child(idRemetente).child(idDestinatario);
            firebase.setValue(conversa);
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
                firebase = ConfiguracaoFirebase.getFirebase().child("usuarios").child(identificadorContato);
                firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //Recuperar dados do contato
                        Usuario usuarioContato = dataSnapshot.getValue(Usuario.class);

                        //Recuperar identificador do usuário logado (base64)
                        Preferencias preferencias = new Preferencias(ConversaActivity.this);
                        String identificadorUsuarioLogado =  preferencias.getIdentificador();

                        firebase = ConfiguracaoFirebase.getFirebase().child("contatos")
                                        .child(identificadorUsuarioLogado)
                                        .child(identificadorContato);

                        Contato contato = new Contato();
                        contato.setIdentificadorUsuario(identificadorContato);
                        contato.setEmail(usuarioContato.getEmail());
                        contato.setNome(usuarioContato.getNome());

                        firebase.setValue(contato);
                        Snackbar.make(findViewById(R.id.conversa_id), contato.getNome()+" está na sua lista de contatos", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            case R.id.menu_conversa_whatsapp:
                identificadorContato = idUsuarioDestinatario;
                firebase = ConfiguracaoFirebase.getFirebase().child("usuarios").child(identificadorContato);
                firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //Recuperar dados do contato
                        Usuario usuarioContato = dataSnapshot.getValue(Usuario.class);

                        //Recuperar identificador do usuário logado (base64)
                        Preferencias preferencias = new Preferencias(ConversaActivity.this);
                        String identificadorUsuarioLogado =  preferencias.getIdentificador();

                        firebase = ConfiguracaoFirebase.getFirebase().child("contatos")
                                .child(identificadorUsuarioLogado)
                                .child(identificadorContato);

                        if (usuarioContato.getTelefone() != null)
                        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(WhatsAppHelper.gerarLinkAPI(usuarioContato.getTelefone()))));
                        else
                            Snackbar.make(findViewById(R.id.conversa_id), "Este contato ainda não cadastrou o whatsapp, por favor use este chat para entrar em contato", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListenerMensagem);
        mensagensDestinatario.removeEventListener(mensagensVisualiazadas);
    }
}
