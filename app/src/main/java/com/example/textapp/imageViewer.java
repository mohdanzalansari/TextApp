package com.example.textapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class imageViewer extends AppCompatActivity {

    private ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);


        Toolbar toolbar=findViewById(R.id.image_viewer_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String imageURI = getIntent().getStringExtra("imageURI");

        image=findViewById(R.id.imageViewerBOX);

        Glide.with(getApplicationContext()).load(Uri.parse(imageURI)).into(image);

    }
}
