package br.edu.ifro.feirarondonia.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.activity.FormularioVendaActivity;
import br.edu.ifro.feirarondonia.activity.ProdutoActivity;
import br.edu.ifro.feirarondonia.adapter.ProdutoAdapter;
import br.edu.ifro.feirarondonia.config.ConfiguracaoFirebase;
import br.edu.ifro.feirarondonia.helper.Preferencias;
import br.edu.ifro.feirarondonia.model.Produto;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProdutosFragment extends Fragment {

    private ListView listView;
    private ProdutoAdapter adapter;
    private ArrayList<Produto> produtos;

    private DatabaseReference firebase;
    private ValueEventListener valueEventListenerContatos;

    public ProdutosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        firebase.addValueEventListener(valueEventListenerContatos);
    }

    @Override
    public void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListenerContatos);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_produtos, container, false);

        produtos = new ArrayList();

        adapter = new ProdutoAdapter(produtos, getActivity());
        listView = view.findViewById(R.id.produtos_listview);
        listView.setAdapter(adapter);
        firebase = ConfiguracaoFirebase.getFirebase().child("produtos");
        firebase = ConfiguracaoFirebase.getFirebase().child("produtos");

        valueEventListenerContatos = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                produtos.clear();
                for (DataSnapshot dado : dataSnapshot.getChildren()){
                    for (DataSnapshot produtosSnap : dado.getChildren()){
                        produtos.add(produtosSnap.getValue(Produto.class));
                    }
                }
                Collections.sort(produtos, new Comparator<Produto>() {
                    @Override
                    public int compare(Produto prod1, Produto prod2) {
                        return prod2.getDataPublicacao().compareTo(prod1.getDataPublicacao());
                    }
                });
//                Collections.reverse(produtos);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               Produto produto = produtos.get(position);
               Preferencias preferencias = new Preferencias(getActivity());
               if (produto.getIdVendedor().equals(preferencias.getIdentificador())){
                   Intent intent = new Intent(getActivity(), FormularioVendaActivity.class);
                   intent.putExtra("produto", produto);
                   startActivity(intent);
               } else {
                   Intent intent = new Intent(getActivity(), ProdutoActivity.class);
                   intent.putExtra("produto", produto);
                   startActivity(intent);
               }
            }
        });

        return  view;
    }

}
