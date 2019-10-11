package br.edu.ifro.agroplace.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;
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
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
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
public class MeusProdutosFragment extends Fragment implements CategoriaObserver {


    private ArrayList<Produto> produtos;
    private boolean searchViewOpened;

    private ProductsAdapter productsAdapter;
    private EventListener<QuerySnapshot> eventListener;
    private ListenerRegistration productListener;
    private com.google.firebase.firestore.Query productsRef;

    private static List<SearchViewObserver> observers = new ArrayList<SearchViewObserver>();

    private RecyclerView productsRecyclerView;
    private LinearLayout icEmptyView;

    private CollectionReference conversasRef;

    private Handler handler;

    public MeusProdutosFragment() {
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
        View view = inflater.inflate(R.layout.fragment_meus_produtos, container, false);

        produtos = new ArrayList();
        produtos.clear();
        handler = new Handler();

        productsRecyclerView = view.findViewById(R.id.myproducts_recycler_view);
        icEmptyView = view.findViewById(R.id.ic_empty_view);

        productsAdapter = new ProductsAdapter(getContext(), produtos);
        productsRecyclerView.setAdapter(productsAdapter);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventListener = (queryDocumentSnapshots, e) -> {

            if (queryDocumentSnapshots != null) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    produtos.clear();
                    produtos.addAll(queryDocumentSnapshots.toObjects(Produto.class));
                    Collections.reverse(produtos);
                }
                if (!produtos.isEmpty()) {
                    productsRecyclerView.setVisibility(View.VISIBLE);
                    icEmptyView.setVisibility(View.GONE);
                } else {
                    productsRecyclerView.setVisibility(View.GONE);
                    icEmptyView.setVisibility(View.VISIBLE);
                }
                productsAdapter.notifyDataSetChanged();
            }
        };

        Preferencias preferencias = new Preferencias(getActivity());
        productsRef = ConfiguracaoFirebase.getInstance().collection("produtos").whereEqualTo("idVendedor", preferencias.getIdentificador());
        productListener = productsRef.addSnapshotListener(eventListener);

        FloatingActionButton btnNovaVenda = view.findViewById(R.id.btn_nova_venda);
        btnNovaVenda.setOnClickListener(v -> abrirFormularioVenda());

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_green, menu);
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
                if (count > anterior) {
                    productsAdapter.getFilter().filter(newText);
                } else {
                    productsRef.addSnapshotListener(eventListener);
                    handler.postDelayed(() -> productsAdapter.getFilter().filter(newText), 100);
                }
                if (count == 0) {
                    productsRef.addSnapshotListener(eventListener);
                }
                return false;
            }
        });
        searchView.setOnSearchClickListener(view -> {
            searchViewOpened = true;
            notifyObservers();
        });

        searchView.setOnCloseListener(() -> {
            searchViewOpened = false;
            notifyObservers();
            return false;
        });
        verificarConversasNaoLidas(menu);
    }

    public void verificarConversasNaoLidas(final Menu menu) {
        Preferencias preferencias = new Preferencias(getActivity());
        conversasRef = ConfiguracaoFirebase.getInstance().collection("conversas").document(preferencias.getIdentificador())
                .collection("contatos");
        final MenuItem menuConversa = menu.findItem(R.id.menu_main_conversas);
        conversasRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) return;
            int icon = R.drawable.ic_message_green;
            for (Conversa c : queryDocumentSnapshots.toObjects(Conversa.class)) {
                if (!c.isVisualizada()) {
                    icon = R.drawable.ic_announcement_green;
                }
            }
            menuConversa.setIcon(icon);
        });
    }


    private void configurarSearchView(SearchView searchView) {
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        ImageView icon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        ImageView iconClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        iconClose.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        searchView.setMaxWidth(Integer.MAX_VALUE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_sair:
                deslogarUsuario();
                return true;
            case R.id.menu_main_nova_venda:
                abrirFormularioVenda();
                return true;
            case R.id.menu_main_perfil:
                Preferencias preferencias = new Preferencias(getActivity());
                DocumentReference instance = ConfiguracaoFirebase.getInstance().collection("usuarios").document(preferencias.getIdentificador());
                instance.get().addOnSuccessListener(documentSnapshot -> {
                    Usuario user = documentSnapshot.toObject(Usuario.class);
                    abrirPerfil(user);
                });
                return true;
            case R.id.menu_main_conversas:
                irParaConversas();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    private void deslogarUsuario() {
        FirebaseAuth usuarioAutenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAutenticacao.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }


    public void abrirFormularioVenda() {
        Intent intent = new Intent(getActivity(), FormularioVendaActivity.class);
        startActivity(intent);
    }

    @Override
    public void update(final String categoria) {
        if (categoria.equals(Categorias.getCategoriasLista()[0])) {
            productsRef.addSnapshotListener(eventListener);
        } else {
            productsRef.addSnapshotListener(eventListener);
            handler.postDelayed(() -> productsAdapter.getFilterCategory().filter(categoria), 100);
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
