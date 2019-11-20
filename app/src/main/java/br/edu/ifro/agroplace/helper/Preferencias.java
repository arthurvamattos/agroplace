package br.edu.ifro.agroplace.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferencias {

    private Context contexto;
    private SharedPreferences preferences;
    private final String NOME_ARQUIVO = "agroplace.preferencias";
    private final int MODE = 0;
    private SharedPreferences.Editor editor;

    private final String CHAVE_IDENTIFICADOR = "identificadorUsuarioLogado";
    private final String CHAVE_NOME = "nomeUsuarioLogado";
    private final String CHAVE_INTRO = "isIntroOpened";

    private final String CHAVE_CATEGORIA = "categoriaSelecionada";

    public Preferencias( Context contextoParametro){
        contexto = contextoParametro;
        preferences = contexto.getSharedPreferences(NOME_ARQUIVO, MODE );
        editor = preferences.edit();

    }

    public void setIntroOpenend() {
        editor.putBoolean(CHAVE_INTRO, true);
        editor.commit();
    }

    public void salvarDados(String identificadorUsuario, String nomeUsuarioLogado){
        editor.putString(CHAVE_IDENTIFICADOR, identificadorUsuario);
        editor.putString(CHAVE_NOME, nomeUsuarioLogado);
        editor.commit();
    }

    public void trocarCategoria(String categoria){
        editor.putString(CHAVE_CATEGORIA, categoria);
        editor.commit();
    }

    public String getIdentificador(){
        return preferences.getString(CHAVE_IDENTIFICADOR, null);
    }
    public String getNome(){
        return preferences.getString(CHAVE_NOME, null);
    }
    public String getCategoria(){
        return preferences.getString(CHAVE_CATEGORIA, "Mostrar todos");
    }
    public boolean isIntroOpened(){
        return preferences.getBoolean(CHAVE_INTRO, false);
    }

}

