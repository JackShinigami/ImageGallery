package com.example.imagegallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
        int resID = album.getThumbnailPath();

        holder.albumThumbnail.setImageResource(resID);

        if(album.isDefault()){
            holder.moreMenu.setVisibility(View.INVISIBLE);
        }
        else{
            holder.moreMenu.setVisibility(View.VISIBLE);
        }

        holder.moreMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.moreMenu, position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ImageObject> images = album.getImages();

                ImageFragment imageFragment = ImageFragment.newInstance(images, album.getAlbumName());
                FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, imageFragment);
                fragmentTransaction.commit();

                ((MainActivity) context).setCurrentFragment(MainActivity.FragmentType.ALBUM_IMAGE_FRAGMENT);
                ((MainActivity) context).setCurrentImages(images);
                ((MainActivity) context).setCurrentFragmentName(album.getAlbumName());
                SharedPreferencesManager.saveCurrentName(context, album.getAlbumName());
            }
        });
    }

    private void showPopupMenu(View itemView, int position) {
        AlbumData currentAlbum = albums.get(position);

        Context context = itemView.getContext();
        PopupMenu popupMenu = new PopupMenu(context, itemView);
        popupMenu.inflate(R.menu.album_item_popup_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if(R.id.edit_album == itemId){
                    EditAlbumName(itemView, position);
                }
                else if(R.id.delete_album == itemId){
                    SharedPreferencesManager.deleteAlbumData(context, albums.get(position).getAlbumName());
                    albums.remove(position);
                    notifyDataSetChanged();
                }
                return true;
            }


        });
        popupMenu.show();

    }

    private void EditAlbumName(View itemView, int position) {
        AlbumData currentAlbum = albums.get(position);
        String oldName = currentAlbum.getAlbumName();
        Context context = itemView.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.add_album, null);
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        EditText txtName = view.findViewById(R.id.edit_album_name);

        txtTitle.setText("Edit Album Name");
        txtName.setText(oldName);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = txtName.getText().toString();
                if(newName.length() != 0) {
                    if(!oldName.equals(newName)) {
                        for(AlbumData album : albums) {
                            if(album.getAlbumName().equals(newName)) {
                                Toast.makeText(context, "Album name already exists", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        currentAlbum.setAlbumName(newName);
                        notifyDataSetChanged();
                        dialog.dismiss();

                        SharedPreferencesManager.deleteAlbumData(context, oldName);
                    }
                }
                else {
                    Toast.makeText(context, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create();
        builder.show();

    }


    @Override
    public int getItemCount() {
        return albums.size();
    }

    public void addAlbum(AlbumData album){
        albums.add(album);
        notifyDataSetChanged();
    }
}
