package br.edu.ifro.agroplace.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import br.edu.ifro.agroplace.fragment.MeusProdutosFragment;
import br.edu.ifro.agroplace.fragment.ProdutosFragment;

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
