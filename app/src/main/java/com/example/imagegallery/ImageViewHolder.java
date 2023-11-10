package com.example.imagegallery;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

public class ImageViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;

    public ImageViewHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);

    }
}