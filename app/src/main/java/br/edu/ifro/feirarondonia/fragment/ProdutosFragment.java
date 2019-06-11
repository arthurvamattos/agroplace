package br.edu.ifro.feirarondonia.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.activity.ConversasActivity;
import br.edu.ifro.feirarondonia.activity.FormularioUsuarioActivity;
import br.edu.ifro.feirarondonia.activity.FormularioVendaActivity;
import br.edu.ifro.feirarondonia.activity.LoginActivity;
import br.edu.ifro.feirarondonia.activity.ProdutoActivity;
import br.edu.ifro.feirarondonia.adapter.ProdutoAdapter;
import br.edu.ifro.feirarondonia.config.ConfiguracaoFirebase;
import br.edu.ifro.feirarondonia.helper.Preferencias;
import br.edu.ifro.feirarondonia.model.Produto;
import br.edu.ifro.feirarondonia.model.Usuario;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProdutosFragment extends Fragment {

    private ListView listView;
    private ProdutoAdapter adapter;
    private ArrayList<Produto> produtos;
    private ArrayList<Produto> produtosPesquisa;

    private FirebaseAuth usuarioAutenticacao;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
                produtosPesquisa = produtos;
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_main_pesquisa);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        ImageView icon = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        icon.setColorFilter(Color.WHITE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            int count = 0;
            int anterior = 0;
            @Override
            public boolean onQueryTextChange(String newText) {
                anterior = count;
                count = newText.length();
                if (count > anterior){
                    adapter.getFilter().filter(newText);
                } else {
                    firebase.addValueEventListener(valueEventListenerContatos);
                    adapter.getFilter().filter(newText);
                }
                if (count == 0){
                    firebase.addValueEventListener(valueEventListenerContatos);
                }
                return false;
            }
        });
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

    public void abrirFormularioVenda() {
        Intent intent = new Intent(getActivity(), FormularioVendaActivity.class);
        startActivity(intent);
    }

    private void deslogarUsuario() {
        usuarioAutenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAutenticacao.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

}
