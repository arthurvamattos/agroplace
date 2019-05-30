package br.edu.ifro.feirarondonia.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.model.Conversa;
import br.edu.ifro.feirarondonia.model.Produto;

public class ProdutoAdapter extends BaseAdapter {
    private List<Produto> produtos;
    private Activity activity;

    public ProdutoAdapter(List<Produto> produtos, Activity activity) {
        this.produtos = produtos;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return produtos.size();
    }

    @Override
    public Object getItem(int position) {
        return produtos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = activity.getLayoutInflater().inflate(R.layout.imagem_produto, parent,false);

        ImageView campoFoto = view.findViewById(R.id.imagem_produto_foto);
        TextView campoNome = view.findViewById(R.id.imagem_produto_nome);
        TextView campoNomeVendedor = view.findViewById(R.id.imagem_produto_vendedor_nome);

        Produto produto = produtos.get(position);
        campoNome.setText(produto.getNome());
        campoNomeVendedor.setText(produto.getVendedor());

        if (produto.getUrlImagem() != null){
            Picasso.get().load(produto.getUrlImagem()).fit().centerCrop().into(campoFoto);
        }

        return view;
    }
}