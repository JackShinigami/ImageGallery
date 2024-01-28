package com.example.imagegallery;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
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
        AlbumHelper albumHelper = AlbumHelper.getInstance(holder.imageView.getContext());

        ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
        layoutParams.width = WindowSize.getWidth() / colNumber;
        layoutParams.height = layoutParams.width;

        imageObject.loadImage(holder.imageView.getContext(), holder.imageView, layoutParams.width, layoutParams.height);

        if(isSelectMode && selectedItems.get(position)){
            holder.imageView.setAlpha(0.5f);
            holder.imageView.setScaleX(0.9f);
            holder.imageView.setScaleY(0.9f);
            holder.textView.setVisibility(View.VISIBLE);
        }
        else{
            holder.imageView.setAlpha(1f);
            holder.imageView.setScaleX(1f);
            holder.imageView.setScaleY(1f);
            holder.textView.setVisibility(View.GONE);
        }

        if(isSelectMode){
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedItems.get(position)){
                        selectedItems.put(position, false);
                        holder.imageView.setAlpha(1f);
                        holder.imageView.setScaleX(1f);
                        holder.imageView.setScaleY(1f);
                        holder.textView.setVisibility(View.GONE);
                    }
                    else{
                        if(countSeleted() < 100){
                            selectedItems.put(position, true);
                            holder.imageView.setAlpha(0.5f);
                            holder.imageView.setScaleX(0.9f);
                            holder.imageView.setScaleY(0.9f);
                            holder.textView.setVisibility(View.VISIBLE);
                        }
                        else{
                            Toast.makeText(v.getContext(), R.string.over_100_images, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
        else{
            selectedItems.clear();
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImagesViewModel imagesViewModel;
                    try {
                        imagesViewModel = new ViewModelProvider((MainActivity) v.getContext()).get(ImagesViewModel.class);
                        imagesViewModel.setImagesBackup(data);
                    }
                    catch (Exception e){
                        imagesViewModel = new ViewModelProvider((SearchActivity) v.getContext()).get(ImagesViewModel.class);
                        imagesViewModel.setImagesSearch(data);
                    }
                    Intent intent = new Intent(v.getContext(), ViewImageActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    intent.putExtra("positionBundle", bundle);
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
                                Dialog dialog = new Dialog(v.getContext());
                                dialog.setContentView(R.layout.dialog_save_edited_image);
                                TextView txtTitle = dialog.findViewById(R.id.tv_message_dialog);
                                txtTitle.setText(R.string.delete_image_confirm);
                                Button btnYes = dialog.findViewById(R.id.btn_save);
                                Button btnNo = dialog.findViewById(R.id.btn_cancel);
                                btnYes.setText(R.string.delete);
                                btnNo.setText(R.string.cancel);
                                btnYes.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        imageObject.deleteToTrash(v.getContext());
                                        data.remove(imageObject);
                                        if(SearchActivity.isSearchActivityRunning())
                                        {
                                            try {
                                                SearchActivity.addDeleteImage(imageObject);
                                                Context context = v.getContext();
                                                while (!(context instanceof SearchActivity)) {
                                                    context = ((ContextWrapper)context).getBaseContext();
                                                }
                                                ((SearchActivity) context).onResume();
                                            }
                                            catch(Exception e){
                                                Log.e("Error", e.toString());
                                            }
                                        } else {
                                            try {
                                                Context context = v.getContext();
                                                while (!(context instanceof MainActivity)) {
                                                    context = ((ContextWrapper) context).getBaseContext();
                                                }
                                                ((MainActivity) context).handler.sendEmptyMessage(1);
                                            } catch (Exception e) {
                                                Log.e("Error", e.toString());
                                            }
                                        }
                                        dialog.dismiss();
                                        notifyDataSetChanged();
                                    }
                                });
                                btnNo.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();

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
            if(i == 99){
                break;
            }
        }
        notifyDataSetChanged();
    }

    public int countSeleted(){
        int count = 0;
        for(int i = 0; i < data.size(); i++){
            if(selectedItems.get(i)){
                count++;
            }
        }
        return count;
    }


}