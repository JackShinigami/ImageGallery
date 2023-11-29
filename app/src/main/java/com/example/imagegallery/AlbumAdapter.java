package com.example.imagegallery;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, @SuppressLint("RecyclerView") int position) {
        AlbumHelper albumHelper = AlbumHelper.getInstance();
        AlbumData album = albums.get(position);
        String albumName = album.getAlbumName();

        if(albumName.equals("Trash"))
            holder.tvAlbumName.setText(context.getString(R.string.trash));
        else if(albumName.equals("Favorites"))
            holder.tvAlbumName.setText(context.getString(R.string.favorites));
        else
            holder.tvAlbumName.setText(albumName);

        if(album.getImages().size() == 1 || album.getImages().size() == 0){
            String imageUnit = context.getString(R.string.image_unit_1);
            holder.tvAlbumSize.setText(album.getImages().size() + " " + imageUnit);
        }
        else{
            String imageUnit = context.getString(R.string.image_unit_n);
            holder.tvAlbumSize.setText(album.getImages().size() + " " + imageUnit);
        }
        int resID = album.getThumbnailPath();

        holder.albumThumbnail.setImageResource(resID);
        holder.moreMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPopupMenu(holder.moreMenu, position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                albumHelper.checkAlbumPassword(context, album.getAlbumName(), new PasswordCheckCallBack() {
                    @Override
                    public void onPasswordChecked(boolean isPasswordCorrect) {
                        if(isPasswordCorrect){
                            ArrayList<ImageObject> images = album.getImages();

                            ImageFragment imageFragment = ImageFragment.newInstance(images, album.getAlbumName());
                            ((MainActivity) context).setImageFragment(imageFragment);
                            FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, imageFragment, album.getAlbumName());
                            fragmentTransaction.addToBackStack("MainStack");
                            fragmentTransaction.commit();

                            ((MainActivity) context).setCurrentFragment(MainActivity.FragmentType.ALBUM_IMAGE_FRAGMENT);
                            ((MainActivity) context).setCurrentImages(images);
                            ((MainActivity) context).setCurrentFragmentName(album.getAlbumName());
                            ((MainActivity) context).updateButtonInAlbum();
                        }
                    }
                });
            }
        });
    }

    private void showPopupMenu(View itemView, int position) {
        AlbumData currentAlbum = albums.get(position);
        AlbumHelper albumHelper = AlbumHelper.getInstance();

        Context context = itemView.getContext();
        PopupMenu popupMenu = new PopupMenu(context, itemView);
        popupMenu.inflate(R.menu.album_item_popup_menu);

        if(albumHelper.isDefaultAlbum(currentAlbum.getAlbumName())){
            popupMenu.getMenu().findItem(R.id.delete_album).setVisible(false);
            popupMenu.getMenu().findItem(R.id.edit_album).setVisible(false);

        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if(R.id.edit_album == itemId){
                    EditAlbumName(itemView, position);
                }
                else if(R.id.delete_album == itemId){
                    SharedPreferencesManager.deleteAlbumData(context, albums.get(position).getAlbumName());
                    for(ImageObject image : albums.get(position).getImages()){
                        image.removeAlbumName(context, albums.get(position).getAlbumName());
                    }
                    SharedPreferencesManager.deleteAlbumPassword(context, albums.get(position).getAlbumName());
                    albums.remove(position);
                    notifyDataSetChanged();
                }
                else if(R.id.set_password == itemId){
                    albumHelper.setAlbumPassword(context, currentAlbum.getAlbumName());
                }
                return true;
            }
        });// to implement on click event on items of menu
        popupMenu.show();
    }//showPopupMenu

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

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = txtName.getText().toString();
                if(newName.length() != 0) {
                    if(!oldName.equals(newName)) {
                        ArrayList<String> albumNames = new ArrayList<>();
                        for(AlbumData album : albums) {
                            if(album.getAlbumName().equals(newName)) {
                                String message = context.getString(R.string.album_name_exists);
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            albumNames.add(album.getAlbumName());
                        }

                        for (ImageObject image : currentAlbum.getImages()) {
                            image.updateAlbumName(context, oldName, newName);
                        }

                        albumNames.remove(oldName);
                        albumNames.add(newName);
                        SharedPreferencesManager.saveAlbumNameList(context, albumNames);

                        currentAlbum.setAlbumName(newName);
                        SharedPreferencesManager.updateAlbumNameInPassword(context, oldName, newName);
                        SharedPreferencesManager.saveAlbumData(context, currentAlbum);
                        SharedPreferencesManager.deleteAlbumData(context, oldName);

                        notifyDataSetChanged();
                        dialog.dismiss();
                    }
                }
                else {
                    String message = context.getString(R.string.album_name_empty);
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            }
        });//positive button

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });//negative button

        builder.create();
        builder.show();

    }//EditAlbumName


    @Override
    public int getItemCount() {
        return albums.size();
    }

    public void addAlbum(AlbumData album){
        albums.add(album);
        ArrayList<String> albumNameList = SharedPreferencesManager.loadAlbumNameList(context);
        if(albumNameList == null){
            albumNameList = new ArrayList<>();
        }
        albumNameList.add(album.getAlbumName());
        SharedPreferencesManager.saveAlbumNameList(context, albumNameList);
        SharedPreferencesManager.saveAlbumData(context, album);
        notifyDataSetChanged();
    }//addAlbum
}
