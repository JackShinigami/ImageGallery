package com.example.imagegallery;

import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder>{
    ArrayList<ImageObject> data;
    private int colNumber = 3;
    private boolean isSelectMode;
    private SparseBooleanArray selectedItems ;
    private String fragmentName;



    public ImageAdapter(ArrayList<ImageObject> data, String fragmentName) {
        this.data = data;
        isSelectMode = false;
        selectedItems = new SparseBooleanArray();
        this.fragmentName = fragmentName;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_layout, parent, false);
        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
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
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), holder.imageView);
                    popupMenu.inflate(R.menu.item_image_popup_menu);



                    if(albumHelper.isDefaultAlbum(fragmentName)){
                        popupMenu.getMenu().findItem(R.id.remove_from_album).setVisible(false);
                    }

                    if(fragmentName.equals("Gallery") || fragmentName.equals("Search")){
                        popupMenu.getMenu().findItem(R.id.remove_from_album).setVisible(false);
                    }

                    if(fragmentName.equals("Trash")){
                        popupMenu.getMenu().findItem(R.id.add_to_album).setVisible(false);
                        popupMenu.getMenu().findItem(R.id.remove_from_album).setVisible(false);
                        popupMenu.getMenu().findItem(R.id.upload).setVisible(false);
                        popupMenu.getMenu().findItem(R.id.delete_to_trash).setVisible(false);
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
                                data = SharedPreferencesManager.loadAlbumData(v.getContext(), fragmentName).getImages();
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
                                if(SearchActivity.isSearchActivityRunning())
                                {
                                    SearchActivity.addDeleteImage(imageObject);
                                    ((SearchActivity)v.getContext()).onResume();
                                }
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
        }
        notifyDataSetChanged();
    }


}