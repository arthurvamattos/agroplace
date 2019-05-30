package br.edu.ifro.feirarondonia.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.adapter.MensagemAdapter;
import br.edu.ifro.feirarondonia.config.ConfiguracaoFirebase;
import br.edu.ifro.feirarondonia.helper.Base64Custom;
import br.edu.ifro.feirarondonia.helper.Preferencias;
import br.edu.ifro.feirarondonia.model.Contato;
import br.edu.ifro.feirarondonia.model.Conversa;
import br.edu.ifro.feirarondonia.model.Mensagem;
import br.edu.ifro.feirarondonia.model.Usuario;

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
        toolbar.setTitle(nomeUsuarioDestinatario);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);

        //Montar a listview e adapter
        mensagens = new ArrayList<>();

        //Adapter customizado
        adapter = new MensagemAdapter(ConversaActivity.this, mensagens);
        listView.setDivider(null);
        listView.setAdapter(adapter);
        //Recuperar as mensagens do firebase
        firebase = ConfiguracaoFirebase.getFirebase().child("mensagens").child(idUsuarioRemetente).child(idUsuarioDestinatario);
        valueEventListenerMensagem = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mensagens.clear();
                for (DataSnapshot dado : dataSnapshot.getChildren()){
                    Mensagem mensagem = dado.getValue(Mensagem.class);
                    mensagens.add(mensagem);
                }
                adapter.notifyDataSetChanged();
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
                    Boolean retornoMensagemRemetente =  salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                    if (!retornoMensagemRemetente) {
                        Snackbar.make(findViewById(R.id.conversa_id), "Problema ao salvar mensagem, tente novamente!", Snackbar.LENGTH_SHORT).show();
                    } else {
                        //Salvar mensagem para o destinatário
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
            firebase = ConfiguracaoFirebase.getFirebase().child("conversas");
            firebase.child(idRemetente).child(idDestinatario).setValue(conversa);
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
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListenerMensagem);
    }
}
