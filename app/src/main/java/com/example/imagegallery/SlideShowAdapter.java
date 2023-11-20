package com.example.imagegallery;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class SlideShowAdapter extends  RecyclerView.Adapter<SlideShowAdapter.SlideShowViewHolder>{

    private Context context;
    private ArrayList<ImageObject> imageObjects;

    public SlideShowAdapter(Context context, ArrayList<ImageObject> imageObjects) {
        this.context = context;
        this.imageObjects = imageObjects;
    }

    @NonNull
    @Override
    public SlideShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new SlideShowViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideShowViewHolder holder, int position) {
        holder.imageView.setImageURI(imageObjects.get(position).getImageUri());
    }

    @Override
    public int getItemCount() {
        return imageObjects.size();
    }

    public class SlideShowViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public SlideShowViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;

        }
    }

}
