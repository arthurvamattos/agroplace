package br.edu.ifro.agroplace.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public final class ConfiguracaoFirebase {

    private static FirebaseFirestore referenciaFirestore;
    private static FirebaseAuth autenticacao;
    private static StorageReference storage;

    public static FirebaseFirestore getInstance(){
        if (referenciaFirestore == null){
            referenciaFirestore = FirebaseFirestore.getInstance();
            //Permitir a pesistência em cache
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            referenciaFirestore.setFirestoreSettings(settings);
        }
        return referenciaFirestore;
    }


    public static FirebaseAuth getFirebaseAutenticacao(){
        if (autenticacao == null)
            autenticacao = FirebaseAuth.getInstance();
        return autenticacao;
    }

    public static StorageReference getFirebaseStorage(){
        if (storage == null)
        storage = FirebaseStorage.getInstance().getReference();
        return storage;
    }

}
