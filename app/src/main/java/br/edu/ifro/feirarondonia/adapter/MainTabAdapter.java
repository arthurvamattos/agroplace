package br.edu.ifro.feirarondonia.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import br.edu.ifro.feirarondonia.fragment.MeusProdutosFragment;
import br.edu.ifro.feirarondonia.fragment.ProdutosFragment;

public class MainTabAdapter extends FragmentStatePagerAdapter {

    private String[] tituloAbas = {"À VENDA", "MEUS PRODUTOS"};

    public MainTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new ProdutosFragment();
                break;
            case 1:
                fragment = new MeusProdutosFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return tituloAbas.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tituloAbas[position];
    }
}
