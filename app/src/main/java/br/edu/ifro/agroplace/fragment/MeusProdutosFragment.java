package br.edu.ifro.agroplace.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.activity.ConversasActivity;
import br.edu.ifro.agroplace.activity.FormularioUsuarioActivity;
import br.edu.ifro.agroplace.activity.FormularioVendaActivity;
import br.edu.ifro.agroplace.activity.LoginActivity;
import br.edu.ifro.agroplace.activity.MainActivity;
import br.edu.ifro.agroplace.adapter.ProdutoAdapter;
import br.edu.ifro.agroplace.config.Categorias;
import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;
import br.edu.ifro.agroplace.helper.CategoriaObserver;
import br.edu.ifro.agroplace.helper.Preferencias;
import br.edu.ifro.agroplace.model.Produto;
import br.edu.ifro.agroplace.model.Usuario;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeusProdutosFragment extends Fragment implements CategoriaObserver {


    private ListView listView;
    private ProdutoAdapter adapter;
    private ArrayList<Produto> produtos;

    private FirebaseAuth usuarioAutenticacao;
    private Query firebase;
    private ValueEventListener valueEventListener;
    private FloatingActionButton btnNovaVenda;

    private Handler handler;

    public MeusProdutosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        firebase.addValueEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListener);
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

        adapter = new ProdutoAdapter(produtos, getActivity());
        listView = view.findViewById(R.id.produtos_listview);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setFooterDividersEnabled(false);

        Preferencias preferencias = new Preferencias(getActivity());
        firebase = ConfiguracaoFirebase.getFirebase().child("produtos").orderByChild("idVendedor").equalTo(preferencias.getIdentificador());
        firebase.keepSynced(true);

        valueEventListener = new ValueEventListener() {
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
                    adapter.getFilter().filter(newText);
                } else {
                    firebase.addValueEventListener(valueEventListener);
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            adapter.getFilter().filter(newText);
                        }
                    }, 100);
                }
                if (count == 0){
                    firebase.addValueEventListener(valueEventListener);
                }
                return false;
            }
        });
    }

    private void configurarSearchView(SearchView searchView) {
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        ImageView icon = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        icon.setColorFilter(Color.WHITE);
        ImageView iconClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
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
                firebase = ConfiguracaoFirebase.getFirebase().child("usuarios").child(preferencias.getIdentificador());
                firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Usuario usuarioRecuperdado = dataSnapshot.getValue(Usuario.class);
                        abrirPerfil(usuarioRecuperdado);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
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

    private void deslogarUsuario() {
        usuarioAutenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
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
        if (categoria.equals(Categorias.getCategoriasLista()[0])){
            firebase.addValueEventListener(valueEventListener);
        } else {
            firebase.addValueEventListener(valueEventListener);

            handler.postDelayed(new Runnable() {
                public void run() {
                    adapter.getFilterCategory().filter(categoria);
                }
            }, 300);
        }
    }


}
