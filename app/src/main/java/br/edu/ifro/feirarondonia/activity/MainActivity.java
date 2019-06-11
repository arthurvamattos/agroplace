package br.edu.ifro.feirarondonia.activity;

import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import br.edu.ifro.feirarondonia.R;
import br.edu.ifro.feirarondonia.adapter.MainTabAdapter;
import br.edu.ifro.feirarondonia.helper.SlidingTabLayout;

public class MainActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Feira Rondônia");
        setSupportActionBar(toolbar);

        slidingTabLayout = findViewById(R.id.main_stl_tabs);
        viewPager = findViewById(R.id.vp_main);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(this, R.color.colorPrimary));
        MainTabAdapter tabAdapter = new MainTabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);
        slidingTabLayout.setViewPager(viewPager);
    }
}

