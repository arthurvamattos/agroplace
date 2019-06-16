package br.edu.ifro.feirarondonia.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public final class Categorias {

    public static String[] getCategoriasLista(){
        String[] categorias = {"Mostrar todos","Frutas","Verduras","Organicos","PANC","Sementes"};
        return categorias;
    }

    public static String[] getCategoriasCasdastro(){
        String[] categorias = {"Frutas","Verduras","Organicos","PANC","Sementes"};
        return categorias;
    }


}
