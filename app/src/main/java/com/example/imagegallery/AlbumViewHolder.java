package com.example.imagegallery;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumViewHolder extends RecyclerView.ViewHolder{

    TextView tvAlbumName;
    TextView tvAlbumSize;

    ImageView albumThumbnail;

    ImageView moreMenu;

    public AlbumViewHolder(@NonNull View itemView) {
        super(itemView);
        tvAlbumName = itemView.findViewById(R.id.tv_album_name);
        tvAlbumSize = itemView.findViewById(R.id.tv_album_size);
        albumThumbnail = itemView.findViewById(R.id.album_thumbnail);
        moreMenu = itemView.findViewById(R.id.iv_more);
    }
}
