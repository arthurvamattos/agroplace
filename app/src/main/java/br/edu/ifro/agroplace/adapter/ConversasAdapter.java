package br.edu.ifro.agroplace.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.model.Conversa;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversasAdapter extends BaseAdapter {
    private List<Conversa> conversas;
    private Activity activity;

    public ConversasAdapter(List<Conversa> contatos, Activity activity) {
        this.conversas = contatos;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return conversas.size();
    }

    @Override
    public Object getItem(int position) {
        return conversas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = activity.getLayoutInflater().inflate(R.layout.lista_conversas, parent,false);

        TextView campoNome = view.findViewById(R.id.lista_conversa_personalizada_nome);
        TextView campoMensagem = view.findViewById(R.id.lista_conversa_personalizada_mensagem);
        final CircleImageView campoFoto = view.findViewById(R.id.lista_conversa_personalizada_foto);
        ImageView campoVizualizacao = view.findViewById(R.id.lista_contato_personalizado_vizualizacao);

        Conversa conversa = conversas.get(position);
        campoNome.setText(conversa.getNome());
        campoMensagem.setText(conversa.getMensagem());

        if (conversa.isVisualizada()) {
            campoVizualizacao.setVisibility(View.INVISIBLE);
        }

        Picasso.get().load(conversa.getUrlImagem()).fit().centerCrop().into(campoFoto);

        return view;
    }
}

