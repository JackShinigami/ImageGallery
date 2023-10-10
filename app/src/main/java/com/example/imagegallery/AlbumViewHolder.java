package com.example.imagegallery;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumViewHolder extends RecyclerView.ViewHolder{

    TextView tvAlbumName;
    TextView tvAlbumSize;

    public AlbumViewHolder(@NonNull View itemView) {
        super(itemView);
        tvAlbumName = itemView.findViewById(R.id.tv_album_name);
        tvAlbumSize = itemView.findViewById(R.id.tv_album_size);
    }
}
