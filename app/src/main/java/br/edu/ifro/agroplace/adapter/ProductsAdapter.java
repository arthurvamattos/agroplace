package br.edu.ifro.agroplace.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.activity.FormularioVendaActivity;
import br.edu.ifro.agroplace.activity.ProdutoActivity;
import br.edu.ifro.agroplace.helper.OnClickListener;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.Produto;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductsHolder> {

    Context context;
    List<Produto> products;

    public ProductsAdapter(Context context, List<Produto> products) {
        this.context = context;
        this.products = products;
    }


    @NonNull
    @Override
    public ProductsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layout;
        layout = LayoutInflater.from(context).inflate(R.layout.item_product, viewGroup, false);
        return new ProductsHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsHolder productsHolder, int positon) {

        productsHolder.sellerImage.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition_animation));
        productsHolder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_scale_animation));

        Produto product = products.get(positon);

        productsHolder.productName.setText(product.getNome());
        productsHolder.productSeller.setText(product.getVendedor());
        Picasso.get().load(product.getUrlImagem()).fit().centerCrop().into(productsHolder.productImage);
        Picasso.get().load(product.getUrlFotoVendedor()).fit().centerCrop().into(productsHolder.sellerImage);

        productsHolder.bind(product, new OnClickListener() {
            @Override
            public void onItemClick(Produto produto) {
               Preferencias preferencias = new Preferencias(context);
               if (produto.getIdVendedor().equals(preferencias.getIdentificador())){
                   Intent intent = new Intent(context, FormularioVendaActivity.class);
                   intent.putExtra("produto", produto);
                   context.startActivity(intent);
               } else {
                   Intent intent = new Intent(context, ProdutoActivity.class);
                   intent.putExtra("produto", produto);
                   context.startActivity(intent);
               }
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    public class ProductsHolder extends  RecyclerView.ViewHolder {

        TextView productName, productSeller;
        CircleImageView sellerImage;
        ImageView productImage;
        RelativeLayout container;


        public ProductsHolder(@NonNull View itemView){
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productSeller = itemView.findViewById(R.id.product_seller);
            sellerImage = itemView.findViewById(R.id.product_seller_image);
            productImage = itemView.findViewById(R.id.product_image);
            container = itemView.findViewById(R.id.product_container);
        }

        public void bind(final Produto produto, final OnClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override public void onClick(View v) {
                    listener.onItemClick(produto);
                }
            });
        }
    }


    //filtro de pesquisa
    public Filter getFilter() {
        return filtroProdutos;
    }

    private Filter filtroProdutos = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence restricao) {
            List<Produto> listaFiltrada = new ArrayList<>();

            if (restricao == null || restricao.length() == 0) {
                listaFiltrada.addAll(products);
            } else {
                String filtroPattern = restricao.toString().toLowerCase().trim();

                for (Produto produto : products) {
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
            products.clear();
            products.addAll((List) results.values);
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
                listaFiltrada.addAll(products);
            } else {
                String filtroPattern = restricao.toString().trim();

                for (Produto produto : products) {
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
            products.clear();
            products.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
