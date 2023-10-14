package com.example.imagegallery;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
    ArrayList<ImageObject> data;
    private int colNumber = 3;


    public MyAdapter(ArrayList<ImageObject> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ImageObject imageObject = data.get(position);

        ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
        layoutParams.width = WindowSize.getWidth() / colNumber;
        layoutParams.height = layoutParams.width;

        imageObject.loadImage(holder.imageView.getContext(), holder.imageView, layoutParams.width/3, layoutParams.height/3);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                intent.putExtra("imageObject", imageObject);
                v.getContext().startActivity(intent);
            }
        });

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivity mainActivity = (MainActivity) v.getContext();
                PopupMenu popupMenu = new PopupMenu(v.getContext(), holder.imageView);
                popupMenu.inflate(R.menu.item_image_popup_menu);

                if(mainActivity.getCurrentFragementName().equals("Gallery")){
                    popupMenu.getMenu().findItem(R.id.remove_from_album).setVisible(false);
                }
                else{
                    popupMenu.getMenu().findItem(R.id.remove_from_album).setVisible(true);
                }


                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if(R.id.add_to_album == itemId){
                            AlbumHelper.addImgaeToAlbum(v.getContext(), imageObject);
                        }
                        else if(R.id.remove_from_album == itemId){
                            AlbumHelper.removeImageFromAlbum(v.getContext(), imageObject);
                            data = SharedPreferencesManager.loadAlbumData(mainActivity, mainActivity.getCurrentFragementName()).getImages();
                            notifyDataSetChanged();
                        }
                        return  true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setColNumber(int colNumber) {
        this.colNumber = colNumber;
    }

    public int getColNumber() {
        return colNumber;
    }


}