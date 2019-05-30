package br.edu.ifro.feirarondonia.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.model.Conversa;

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

        ImageView campoFoto = view.findViewById(R.id.lista_conversa_personalizada_foto);
        TextView campoNome = view.findViewById(R.id.lista_conversa_personalizada_nome);
        TextView campoMensagem = view.findViewById(R.id.lista_conversa_personalizada_mensagem);

        Conversa conversa = conversas.get(position);
        campoNome.setText(conversa.getNome());
        campoMensagem.setText(conversa.getMensagem());

        if (conversa.getCaminhoFoto() != null){
            Bitmap bitmap = BitmapFactory.decodeFile(conversa.getCaminhoFoto());
            Bitmap bitmapReduzido = Bitmap.createScaledBitmap(bitmap, 70, 70, true);
            campoFoto.setImageBitmap(bitmapReduzido);
            campoFoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            campoFoto.setTag(conversa.getCaminhoFoto());
        }

        return view;
    }
}

