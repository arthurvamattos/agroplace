package br.edu.ifro.feirarondonia.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.config.ConfiguracaoFirebase;
import br.edu.ifro.feirarondonia.model.Contato;
import br.edu.ifro.feirarondonia.model.Usuario;
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

        Contato contato = contatos.get(position);
        campoNome.setText(contato.getNome());

        if (contato.getCaminhoFoto() != null){
            Bitmap bitmap = BitmapFactory.decodeFile(contato.getCaminhoFoto());
            Bitmap bitmapReduzido = Bitmap.createScaledBitmap(bitmap, 70, 70, true);
            campoFoto.setImageBitmap(bitmapReduzido);
            campoFoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            campoFoto.setTag(contato.getCaminhoFoto());
        }

        DatabaseReference firebase = ConfiguracaoFirebase.getFirebase().child("usuarios").child(contato.getIdentificadorUsuario());
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuarioRecuperdado = dataSnapshot.getValue(Usuario.class);
                if (usuarioRecuperdado.getUrlImagem() != null) {
                    Picasso.get().load(usuarioRecuperdado.getUrlImagem()).fit().centerCrop().into(campoFoto);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        return view;
    }
}

