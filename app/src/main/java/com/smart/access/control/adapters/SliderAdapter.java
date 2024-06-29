package com.smart.access.control.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.smart.access.control.R;

import java.util.List;

public class SliderAdapter extends PagerAdapter {
    private final Context context;
    private final List<Integer> slider;

    public SliderAdapter(Context context, List<Integer> slider) {
        this.context = context;
        this.slider = slider;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.slider_item, container, false);
        ImageView imageView = itemView.findViewById(R.id.imageView);

        imageView.setImageResource(slider.get(position));
        container.addView(itemView);
        return itemView;
    }

    @Override
    public int getCount() {
        return slider.size();
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

