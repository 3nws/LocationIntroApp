package com.example.location_intro_app;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageActivity extends AppCompatActivity {

    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        String imgUrl = getIntent().getStringExtra("image");
        iv = findViewById(R.id.imageView1);
        Picasso.get()
                .load(imgUrl)
                .fit()
                .rotate(90)
                .centerCrop()
                .into(iv);
    }
}