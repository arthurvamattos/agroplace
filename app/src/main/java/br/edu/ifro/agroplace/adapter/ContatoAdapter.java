package br.edu.ifro.agroplace.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.model.Contato;
import br.edu.ifro.agroplace.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

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

        final CircleImageView campoFoto = view.findViewById(R.id.lista_contato_personalizado_foto);
        TextView campoNome = view.findViewById(R.id.lista_contato_personalizado_nome);

        campoFoto.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_transition_animation));
        campoNome.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_scale_animation));


        Contato contato = contatos.get(position);
        campoNome.setText(contato.getNome());
        Picasso.get().load(contato.getCaminhoFoto()).fit().centerCrop().into(campoFoto);

        return view;
    }
}

