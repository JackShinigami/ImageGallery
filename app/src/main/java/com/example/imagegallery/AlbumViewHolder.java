package com.example.imagegallery;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumViewHolder extends RecyclerView.ViewHolder{

    TextView tvAlbumName;
    TextView tvAlbumSize;

    ImageView moreMenu;

    public AlbumViewHolder(@NonNull View itemView) {
        super(itemView);
        tvAlbumName = itemView.findViewById(R.id.tv_album_name);
        tvAlbumSize = itemView.findViewById(R.id.tv_album_size);
        moreMenu = itemView.findViewById(R.id.iv_more);
    }
}
