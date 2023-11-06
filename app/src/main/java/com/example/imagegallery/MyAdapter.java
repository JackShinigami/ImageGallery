package com.example.imagegallery;

import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
    ArrayList<ImageObject> data;
    private int colNumber = 3;
    private boolean isSelectMode;
    private SparseBooleanArray selectedItems ;



    public MyAdapter(ArrayList<ImageObject> data) {
        this.data = data;
        isSelectMode = false;
        selectedItems = new SparseBooleanArray();
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
        AlbumHelper albumHelper = AlbumHelper.getInstance();

        ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
        layoutParams.width = WindowSize.getWidth() / colNumber;
        layoutParams.height = layoutParams.width;

        imageObject.loadImage(holder.imageView.getContext(), holder.imageView, layoutParams.width/3, layoutParams.height/3);

        if(isSelectMode && selectedItems.get(position)){
            holder.imageView.setAlpha(0.5f);
        }
        else{
            holder.imageView.setAlpha(1f);
        }

        if(isSelectMode){
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedItems.get(position)){
                        selectedItems.put(position, false);
                        holder.imageView.setAlpha(1f);
                    }else{
                        selectedItems.put(position, true);
                        holder.imageView.setAlpha(0.5f);
                    }
                }
            });
        }
        else{
            selectedItems.clear();
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



                    if(albumHelper.isDefaultAlbum(mainActivity.getCurrentFragementName())){
                        popupMenu.getMenu().findItem(R.id.remove_from_album).setVisible(false);
                    }

                    if(mainActivity.getCurrentFragementName().equals("Gallery")){
                        popupMenu.getMenu().findItem(R.id.remove_from_album).setVisible(false);
                    }

                    if(mainActivity.getCurrentFragementName().equals("Trash")){
                        popupMenu.getMenu().findItem(R.id.add_to_album).setVisible(false);
                    }




                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int itemId = item.getItemId();
                            if(R.id.add_to_album == itemId){
                                albumHelper.addImageToAlbum(v.getContext(), imageObject);
                            }
                            else if(R.id.remove_from_album == itemId){
                                albumHelper.removeImageFromAlbum(v.getContext(), imageObject);
                                data = SharedPreferencesManager.loadAlbumData(mainActivity, mainActivity.getCurrentFragementName()).getImages();
                                notifyDataSetChanged();
                            }
                            else if(R.id.upload == itemId){
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        BackupImage.uploadImage(v.getContext(), imageObject);
                                    }
                                });

                                thread.start();
                            } else if (R.id.delete_to_trash == itemId) {
                                imageObject.deleteToTrash(v.getContext());
                                data.remove(imageObject);
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

    public void setSelectMode(boolean selectMode) {
        isSelectMode = selectMode;
        notifyDataSetChanged();
    }

    public ArrayList<ImageObject> getSelectedImages(){
        ArrayList<ImageObject> selectedImages = new ArrayList<>();
        for(int i = 0; i < data.size(); i++){
            if(selectedItems.get(i)){
                selectedImages.add(data.get(i));
            }
        }
        return selectedImages;
    }

    public void SelectAll(){
        for(int i = 0; i < data.size(); i++){
            selectedItems.put(i, true);
            notifyDataSetChanged();
        }
    }


}