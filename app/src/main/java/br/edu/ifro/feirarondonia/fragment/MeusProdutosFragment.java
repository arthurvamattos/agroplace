package br.edu.ifro.feirarondonia.fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;

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
public class MeusProdutosFragment extends Fragment {


    private ListView listView;
    private ProdutoAdapter adapter;
    private ArrayList<Produto> produtos;

    private DatabaseReference firebase;
    private ValueEventListener valueEventListenerContatos;
    FloatingActionButton btnNovaVenda;

    public MeusProdutosFragment() {
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
        View view = inflater.inflate(R.layout.fragment_meus_produtos, container, false);

        produtos = new ArrayList();

        adapter = new ProdutoAdapter(produtos, getActivity());
        listView = view.findViewById(R.id.produtos_listview);
        listView.setAdapter(adapter);

        Preferencias preferencias = new Preferencias(getActivity());
        firebase = ConfiguracaoFirebase.getFirebase().child("produtos").child(preferencias.getIdentificador());

        valueEventListenerContatos = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                produtos.clear();
                for (DataSnapshot dado : dataSnapshot.getChildren()){
                    Produto produto = dado.getValue(Produto.class);
                    produtos.add(produto);
                }
                Collections.reverse(produtos);
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
            }
        });

        btnNovaVenda = view.findViewById(R.id.btn_nova_venda);
        btnNovaVenda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirFormularioVenda();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Produto produto = produtos.get(position);

                Intent intent = new Intent(getActivity(), FormularioVendaActivity.class);
                intent.putExtra("produto", produto);
                startActivity(intent);
            }
        });

        return view;
    }

    public void abrirFormularioVenda() {
        Intent intent = new Intent(getActivity(), FormularioVendaActivity.class);
        startActivity(intent);
    }


}
