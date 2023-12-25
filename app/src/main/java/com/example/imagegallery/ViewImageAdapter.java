package com.example.imagegallery;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ViewImageAdapter  extends  RecyclerView.Adapter<ViewImageAdapter.ViewImageViewHolder>{
private ViewImageActivity context;
    private ImageObject imageObject;
    private ImagesViewModel imagesViewModel;
    private ArrayList<ImageObject> imageObjects;
    public ViewImageAdapter(ViewImageActivity context) {
        this.context = context;

        this.imagesViewModel = new ViewModelProvider(this.context).get(ImagesViewModel.class);
        imageObjects = imagesViewModel.getImagesList().getValue();
        Log.d("imageviewmodel: ", "ViewImageAdapter: " + imageObjects.size());
    }

    @Override
    public ViewImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new ViewImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(ViewImageViewHolder holder, int position) {
        holder.imageView.setImageURI(imageObjects.get(position).getImageUri());
    }

    @Override
    public int getItemCount() {
        return imageObjects.size();
    }

    public ImageObject getCurrentItem(int position) {
        return imageObjects.get(position);
    }

    public class ViewImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ViewImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}
