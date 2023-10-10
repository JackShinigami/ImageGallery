package com.example.imagegallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumViewHolder>{

    Context context;
    ArrayList<AlbumData> albums;
    RecyclerView recyclerView;
    MyAdapter adapter;

    private int[] colNumbers = {2, 3, 4};
    private int colNumberIndex = 0;

    public AlbumAdapter(Context context, ArrayList<AlbumData> albums){
        this.context = context;
        this.albums = albums;
    }
    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album_layout, parent, false);
        return new AlbumViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        AlbumData album = albums.get(position);
        holder.tvAlbumName.setText(album.getAlbumName());
        holder.tvAlbumSize.setText(album.getImages().size() + " images");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ImageObject> images = album.getImages();
                adapter = new MyAdapter(images);
                AlbumActivity activity = (AlbumActivity) context;
                recyclerView = activity.findViewById(R.id.rv_albums);


                adapter = new MyAdapter(images);
                recyclerView.setAdapter(adapter);

                adapter.setColNumber(colNumbers[colNumberIndex]);

                recyclerView.setLayoutManager(new GridLayoutManager(context, adapter.getColNumber()));


            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
}
