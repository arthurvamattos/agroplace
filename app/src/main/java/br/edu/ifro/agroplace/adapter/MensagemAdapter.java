package br.edu.ifro.agroplace.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Base64Custom;
import br.edu.ifro.agroplace.model.Mensagem;

public class MensagemAdapter extends ArrayAdapter<Mensagem> {

    private Context context;
    private ArrayList<Mensagem> mensagens;

    public MensagemAdapter(Context c, ArrayList<Mensagem> objects) {
        super(c, 0, objects);
        this.context = c;
        this.mensagens = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        //Verifica se a lista esta preenchida
        if (mensagens != null){

            //Recupera dados do usuario remetente
            FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
            String idUsuarioRemetente = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());


            //Inicializa objeto para montagem do layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            //Recupera a mensagem
            Mensagem mensagem = mensagens.get(position);

            //Monta a view a partir do xml
            if (idUsuarioRemetente.equals( mensagem.getIdUsuario() )){
                view = inflater.inflate(R.layout.item_mensagem_direita, parent, false);
            } else {
                view = inflater.inflate(R.layout.item_mensagem_esquerda, parent, false);
            }

            //Recupera elemento para exibição
            TextView textoMensagem = (TextView) view.findViewById(R.id.tv_mensgagem);
            textoMensagem.setText(mensagem.getMensagem());

        }

        return view;
    }
}
