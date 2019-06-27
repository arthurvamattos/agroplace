package br.edu.ifro.agroplace.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import android.widget.BaseAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.model.Produto;
import br.edu.ifro.agroplace.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

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
        View view = activity.getLayoutInflater().inflate(R.layout.card_produto, parent,false);

        ImageView campoFoto = view.findViewById(R.id.imagem_produto_foto);
        TextView campoNome = view.findViewById(R.id.imagem_produto_nome);
        TextView campoNomeVendedor = view.findViewById(R.id.imagem_produto_vendedor_nome);
        final CircleImageView campoFotoVendedor = view.findViewById(R.id.imagem_produto_vendedor_foto);

        Produto produto = produtos.get(position);
        campoNome.setText(produto.getNome());
        campoNomeVendedor.setText(produto.getVendedor());

        if (produto.getUrlImagem() != null){
            Picasso.get().load(produto.getUrlImagem()).fit().centerCrop().into(campoFoto);
        }

        DatabaseReference firebase = ConfiguracaoFirebase.getFirebase().child("usuarios").child(produto.getIdVendedor());
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuarioRecuperdado = dataSnapshot.getValue(Usuario.class);
                if (usuarioRecuperdado.getUrlImagem() != null)
                    Picasso.get().load(usuarioRecuperdado.getUrlImagem()).fit().centerCrop().into(campoFotoVendedor);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        return view;
    }

    public Filter getFilter() {
        return filtroProdutos;
    }

    private Filter filtroProdutos = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence restricao) {
            List<Produto> listaFiltrada = new ArrayList<>();

            if (restricao == null || restricao.length() == 0) {
                listaFiltrada.addAll(produtos);
            } else {
                String filtroPattern = restricao.toString().toLowerCase().trim();

                for (Produto produto : produtos) {
                    if (produto.getNome().toLowerCase().contains(filtroPattern)) {
                        listaFiltrada.add(produto);
                    }
                }
            }

            FilterResults resultados = new FilterResults();
            resultados.values = listaFiltrada;

            return resultados;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            produtos.clear();
            produtos.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public Filter getFilterCategory() {
        return filtroProdutosCategoria;
    }

    private Filter filtroProdutosCategoria = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence restricao) {
            List<Produto> listaFiltrada = new ArrayList<>();
            if (restricao == null || restricao.length() == 0) {
                listaFiltrada.addAll(produtos);
            } else {
                String filtroPattern = restricao.toString().trim();

                for (Produto produto : produtos) {
                    if (produto.getCategoria().trim().equals(filtroPattern)) {
                        listaFiltrada.add(produto);
                    }
                }
            }

            FilterResults resultados = new FilterResults();
            resultados.values = listaFiltrada;

            return resultados;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            produtos.clear();
            produtos.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}