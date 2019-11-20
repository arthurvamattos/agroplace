package br.edu.ifro.agroplace.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

import br.edu.ifro.agroplace.R;
import br.edu.ifro.agroplace.model.IntroScreenItem;

public class IntroViewPagerAdapter extends PagerAdapter {

    private Context context;
    private List<IntroScreenItem> list;

    public IntroViewPagerAdapter(Context context, List<IntroScreenItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layoutScreen = inflater.inflate(R.layout.layout_intro_screen, null);

        ImageView img = layoutScreen.findViewById(R.id.intro_image);
        TextView title = layoutScreen.findViewById(R.id.intro_title);
        TextView description = layoutScreen.findViewById(R.id.intro_description);

        title.setText(list.get(position).getTitle());
        description.setText(list.get(position).getDescription());
        img.setImageResource(list.get(position).getScreenImg());

        if (position == 0) {
            title.setTextSize(60);
            img.setVisibility(View.GONE);
        }

        container.addView(layoutScreen);

        return layoutScreen;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
