package br.edu.ifro.agroplace.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.activity.ConversaActivity;
import br.edu.ifro.agroplace.adapter.ConversasAdapter;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.Base64Custom;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.Conversa;

public class ConversasFragment extends Fragment {

    private ListView listView;
    private ConversasAdapter adapter;
    private ArrayList<Conversa> conversas;
    private CollectionReference conversasRef;
    private EventListener<QuerySnapshot> eventListener;
    private ListenerRegistration conversasListener;

    private LinearLayout icEmptyView;

    public ConversasFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        conversasListener = conversasRef.addSnapshotListener(eventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasListener.remove();
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

        icEmptyView = view.findViewById(R.id.ic_empty_view);

        Preferencias preferencias = new Preferencias(getActivity());
        conversasRef = ConfiguracaoFirebase.getInstance().collection("conversas").document(preferencias.getIdentificador())
            .collection("contatos");

        eventListener = (queryDocumentSnapshots, e) -> {
            conversas.clear();
            if (!queryDocumentSnapshots.isEmpty()) {
                conversas.addAll(queryDocumentSnapshots.toObjects(Conversa.class));
                listView.setVisibility(View.VISIBLE);
                icEmptyView.setVisibility(View.GONE);
            } else {
                listView.setVisibility(View.GONE);
                icEmptyView.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        };

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick (AdapterView < ? > parent, View view,int position, long id){

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
