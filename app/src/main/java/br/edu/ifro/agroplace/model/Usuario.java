package br.edu.ifro.agroplace.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import br.edu.ifro.agroplace.config.ConfiguracaoFirebase;

public class Usuario implements Serializable {
    private String id;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private String urlImagem;

    public Usuario (){
        this.setUrlImagem("https://firebasestorage.googleapis.com/v0/b/agroplace-project.appspot.com/o/no-img.png?alt=media");
    }

    public void salvar(){
        FirebaseFirestore db = ConfiguracaoFirebase.getInstance();
        Map<String, Object> user = montarMapUser();
        db.collection("usuarios").document(getId()).set(user);
    }

    private Map<String, Object> montarMapUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", this.id);
        user.put("nome", this.nome);
        user.put("email", this.email);
        user.put("telefone", this.telefone);
        user.put("urlImagem", this.urlImagem);
        return user;
    }


    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }
}
