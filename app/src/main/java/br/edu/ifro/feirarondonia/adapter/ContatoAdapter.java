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
import br.edu.ifro.feirarondonia.model.Contato;

public class ContatoAdapter extends BaseAdapter {
    private List<Contato> contatos;
    private Activity activity;

    public ContatoAdapter(List<Contato> contatos, Activity activity) {
        this.contatos = contatos;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return contatos.size();
    }

    @Override
    public Object getItem(int position) {
        return contatos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = activity.getLayoutInflater().inflate(R.layout.lista_contato, parent,false);

        ImageView campoFoto = view.findViewById(R.id.lista_contato_personalizado_foto);
        TextView campoNome = view.findViewById(R.id.lista_contato_personalizado_nome);

        Contato contato = contatos.get(position);
        campoNome.setText(contato.getNome());

        if (contato.getCaminhoFoto() != null){
            Bitmap bitmap = BitmapFactory.decodeFile(contato.getCaminhoFoto());
            Bitmap bitmapReduzido = Bitmap.createScaledBitmap(bitmap, 70, 70, true);
            campoFoto.setImageBitmap(bitmapReduzido);
            campoFoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            campoFoto.setTag(contato.getCaminhoFoto());
        }

        return view;
    }
}

