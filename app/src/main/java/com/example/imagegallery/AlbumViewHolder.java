package com.example.imagegallery;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumViewHolder extends RecyclerView.ViewHolder{

    TextView tvAlbumName;
    TextView tvAlbumSize;

    ImageView albumThumbnail;
    CardView cardView;


    public AlbumViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.card_view_album);
        tvAlbumName = itemView.findViewById(R.id.tv_album_name);
        tvAlbumSize = itemView.findViewById(R.id.tv_album_size);
        albumThumbnail = itemView.findViewById(R.id.album_thumbnail);
    }
}
