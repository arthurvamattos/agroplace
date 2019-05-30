package br.edu.ifro.feirarondonia.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.activity.ConversaActivity;
import br.edu.ifro.feirarondonia.adapter.ConversasAdapter;
import br.edu.ifro.feirarondonia.config.ConfiguracaoFirebase;
import br.edu.ifro.feirarondonia.helper.Base64Custom;
import br.edu.ifro.feirarondonia.helper.Preferencias;
import br.edu.ifro.feirarondonia.model.Contato;
import br.edu.ifro.feirarondonia.model.Conversa;

public class ConversasFragment extends Fragment {

    private ListView listView;
    private ConversasAdapter adapter;
    private ArrayList<Conversa> conversas;

    private DatabaseReference firebase;
    private ValueEventListener valueEventListenerContatos;

    public ConversasFragment() {
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
        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        conversas = new ArrayList();
        adapter = new ConversasAdapter(conversas, getActivity());
        listView = view.findViewById(R.id.conversas_listview);
        listView.setDivider(null);
        listView.setAdapter(adapter);

        Preferencias preferencias = new Preferencias(getActivity());
        firebase = ConfiguracaoFirebase.getFirebase().child("conversas").child(preferencias.getIdentificador());

        valueEventListenerContatos = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                conversas.clear();
                for (DataSnapshot dado : dataSnapshot.getChildren()){
                    Conversa conversa = dado.getValue(Conversa.class);
                    conversas.add(conversa);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Conversa conversa = conversas.get(position);
                Intent intent = new Intent(getActivity(), ConversaActivity.class);
                intent.putExtra("nome", conversa.getNome());
                intent.putExtra("email", Base64Custom.decodificarBase64(conversa.getIdUsuario()));
                startActivity(intent);
            }
        });

        return view;
    }

}
