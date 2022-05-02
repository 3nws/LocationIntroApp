package com.example.location_intro_app;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<String> images, highResImages;

    ViewPagerAdapter(Context context, ArrayList<String> images, ArrayList<String> highResImages) {
        this.context = context;
        this.images = images;
        this.highResImages = highResImages;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageButton imageView = new ImageButton(context);
        Picasso.get()
                .load(images.get(position))
                .fit()
                .centerCrop()
                .into(imageView);
        container.addView(imageView);

        imageView.setOnClickListener(view -> {
            Intent i = new Intent(context, ImageActivity.class);
            i.putExtra("image", highResImages.get(position));
            context.startActivity(i);
        });
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}