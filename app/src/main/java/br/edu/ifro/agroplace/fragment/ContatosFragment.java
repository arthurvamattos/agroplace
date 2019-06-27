package br.edu.ifro.agroplace.fragment;


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
import java.util.List;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.activity.ConversaActivity;
import br.edu.ifro.agroplace.adapter.ContatoAdapter;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.Contato;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {


    private ListView listView;
    private ContatoAdapter adapter;
    private List contatos;
    private DatabaseReference firebase;
    private ValueEventListener valueEventListenerContatos;

    public ContatosFragment() {
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

        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        contatos = new ArrayList();
        adapter = new ContatoAdapter(contatos, getActivity());
        listView = view.findViewById(R.id.contatos_listview);
        listView.setDivider(null);
        listView.setAdapter((ListAdapter) adapter);

        Preferencias preferencias = new Preferencias(getActivity());
        firebase = ConfiguracaoFirebase.getFirebase().child("contatos").child(preferencias.getIdentificador());
        firebase.keepSynced(true);

        valueEventListenerContatos = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contatos.clear();
                for (DataSnapshot dado : dataSnapshot.getChildren()){
                    Contato contato = dado.getValue(Contato.class);
                    contatos.add(contato);
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
                Intent intent = new Intent(getContext(), ConversaActivity.class);

                //Recuperar dados a serem passados
                Contato contato = (Contato) contatos.get(position);
                intent.putExtra("nome", contato.getNome());
                intent.putExtra("email", contato.getEmail());
                startActivity(intent);
            }
        });

        return view;
    }
}
