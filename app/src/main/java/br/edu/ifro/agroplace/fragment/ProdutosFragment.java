package br.edu.ifro.agroplace.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.activity.ConversasActivity;
import br.edu.ifro.agroplace.activity.FormularioUsuarioActivity;
import br.edu.ifro.agroplace.activity.FormularioVendaActivity;
import br.edu.ifro.agroplace.activity.LoginActivity;
import br.edu.ifro.agroplace.activity.MainActivity;
import br.edu.ifro.agroplace.adapter.ProductsAdapter;
import br.edu.ifro.agroplace.config.Categorias;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.CategoriaObserver;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.helper.SearchViewObserver;
import br.edu.ifro.agroplace.model.Conversa;
import br.edu.ifro.agroplace.model.Produto;
import br.edu.ifro.agroplace.model.Usuario;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProdutosFragment extends Fragment implements CategoriaObserver {

    private ArrayList<Produto> produtos;
    private boolean searchViewOpened;
    private ProductsAdapter productsAdapter;
    private Handler handler;

    private static List<SearchViewObserver> observers = new ArrayList<SearchViewObserver>();
    private EventListener<QuerySnapshot> eventListener;
    private ListenerRegistration productListener;
    private Query productsRef;

    public ProdutosFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        productListener = productsRef.addSnapshotListener(eventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        productListener.remove();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        MainActivity.adicionarObserver(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_produtos, container, false);

        produtos = new ArrayList();
        produtos.clear();
        handler = new Handler();

        RecyclerView productsRecyclerView = view.findViewById(R.id.products_recycler_view);
        productsAdapter = new ProductsAdapter(getContext(), produtos);
        productsRecyclerView.setAdapter(productsAdapter);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventListener = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                produtos.clear();
                produtos.addAll(queryDocumentSnapshots.toObjects(Produto.class));
                productsAdapter.notifyDataSetChanged();
            }
        };

        productsRef = ConfiguracaoFirebase.getInstance().collection("produtos").orderBy("dataPublicacao");
        productListener = productsRef.addSnapshotListener(eventListener);

        return  view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_main_pesquisa);
        SearchView searchView = (SearchView) searchItem.getActionView();
        configurarSearchView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            int count = 0;
            int anterior = 0;
            @Override
            public boolean onQueryTextChange(final String newText) {
                anterior = count;
                count = newText.length();
                if (count > anterior){
                    productsAdapter.getFilter().filter(newText);
                } else {
                    productsRef.addSnapshotListener(eventListener);
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            productsAdapter.getFilter().filter(newText);
                        }
                    }, 100);
                }
                if (count == 0){
                    productsRef.addSnapshotListener(eventListener);
                }
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchViewOpened = true;
                notifyObservers();
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchViewOpened = false;
                notifyObservers();
                return false;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        verificarConversasNaoLidas(menu);
    }

    public void verificarConversasNaoLidas(final Menu menu){
        final boolean[] viewed = {true};
        Preferencias preferencias = new Preferencias(getActivity());
        DatabaseReference referenciaConversas = ConfiguracaoFirebase.getFirebase().child("conversas").child(preferencias.getIdentificador());
        referenciaConversas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Conversa conversa = data.getValue(Conversa.class);
                    if (!conversa.isVisualizada()){
                        viewed[0] = false;
                        MenuItem menuConversa = menu.findItem(R.id.menu_main_conversas);
                        menuConversa.setIcon(R.drawable.ic_announcement);
                    }
                }
                if (viewed[0]) {
                    MenuItem menuConversa = menu.findItem(R.id.menu_main_conversas);
                    menuConversa.setIcon(R.drawable.ic_message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void configurarSearchView(SearchView searchView) {
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        ImageView icon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        icon.setColorFilter(Color.WHITE);
        ImageView iconClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        iconClose.setColorFilter(Color.WHITE);
        searchView.setMaxWidth(Integer.MAX_VALUE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_sair:
                deslogarUsuario();
                return true;
            case R.id.menu_main_nova_venda:
                abrirFormularioVenda();
                return true;
            case R.id.menu_main_perfil:
                Preferencias preferencias = new Preferencias(getActivity());
                DocumentReference instance = ConfiguracaoFirebase.getInstance().collection("usuarios").document(preferencias.getIdentificador());
                instance.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Usuario user = documentSnapshot.toObject(Usuario.class);
                        abrirPerfil(user);
                    }
                });
                return true;
            case R.id.menu_main_conversas:
                irParaConversas();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void abrirPerfil(Usuario user) {
        Intent intent = new Intent(getActivity(), FormularioUsuarioActivity.class);
        intent.putExtra("usuario", user);
        startActivity(intent);
    }

    private void irParaConversas() {
        Intent intent = new Intent(getActivity(), ConversasActivity.class);
        startActivity(intent);
    }

    public void abrirFormularioVenda() {
        Intent intent = new Intent(getActivity(), FormularioVendaActivity.class);
        startActivity(intent);
    }

    private void deslogarUsuario() {
        FirebaseAuth usuarioAutenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAutenticacao.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void update(final String categoria) {
        if (categoria.equals(Categorias.getCategoriasLista()[0])){
            productsRef.addSnapshotListener(eventListener);
        } else {
            productsRef.addSnapshotListener(eventListener);
            handler.postDelayed(new Runnable() {
                public void run() {
                    productsAdapter.getFilterCategory().filter(categoria);
                }
            }, 100);
        }
    }

    public static void adicionarObserver(SearchViewObserver obs) {
        observers.add(obs);
    }

    private void notifyObservers() {
        for (SearchViewObserver observer : this.observers) {
            observer.update(searchViewOpened);
        }
    }


}
