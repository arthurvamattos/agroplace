package br.edu.ifro.agroplace.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

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
    private List<Contato> contatos;
    private CollectionReference contatosRef;
    private EventListener<QuerySnapshot> eventListener;
    private ListenerRegistration contatosListener;

    private LinearLayout icEmptyView;

    public ContatosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        contatosListener = contatosRef.addSnapshotListener(eventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        contatosListener.remove();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        contatos = new ArrayList();
        adapter = new ContatoAdapter(contatos, getActivity());
        listView = view.findViewById(R.id.contatos_listview);
        listView.setDivider(null);
        listView.setAdapter(adapter);

        icEmptyView = view.findViewById(R.id.ic_empty_view);

        Preferencias preferencias = new Preferencias(getActivity());
        contatosRef = ConfiguracaoFirebase.getInstance().collection("contatos").document(preferencias.getIdentificador())
                .collection("pessoas");

        eventListener = (queryDocumentSnapshots, e) -> {
            contatos.clear();
            if (!queryDocumentSnapshots.isEmpty()) {

                ArrayList<Contato> contatosBD = new ArrayList<>();
                contatosBD.addAll(queryDocumentSnapshots.toObjects(Contato.class));
                for (Contato contato: contatosBD) {
                    if (contato.getNome() != null) {
                        contatos.add(contato);
                    }
                }
                listView.setVisibility(View.VISIBLE);
                icEmptyView.setVisibility(View.GONE);
            } else {
                listView.setVisibility(View.GONE);
                icEmptyView.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        };

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Intent intent = new Intent(getContext(), ConversaActivity.class);

            //Recuperar dados a serem passados
            Contato contato = contatos.get(position);
            intent.putExtra("nome", contato.getNome());
            intent.putExtra("email", contato.getEmail());
            startActivity(intent);
        });

        return view;
    }
}
