package com.example.imagegallery;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "ArrayList<ImageObject>";
    private static final String ARG_PARAM2 = "fragmentName";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private ArrayList<ImageObject> images;
    private String fragmentName;
    boolean isSelectMode = false;
    AlbumHelper albumHelper;
    //private String mParam2;

    public ImageFragment() {
        // Required empty public constructor
    }


    public static ImageFragment newInstance(String fragmentName) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM2, fragmentName);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private TextView tvTitle;
    private ImageButton btnChangeGrid;
    private ImageView btnSort, btnOptions;
    private ImageButton btnSelect;
    private int[] colNumbers = {2, 3, 4};
    private static int colNumberIndex = 1;

    private static boolean ascending = false;

    private enum SortType {
        DATE, NAME
    }
    private static SortType sortType = SortType.DATE;

    ImagesViewModel imagesViewModel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imagesViewModel = new ViewModelProvider(requireActivity()).get(ImagesViewModel.class);
            fragmentName = getArguments().getString(ARG_PARAM2);
            if(fragmentName.equals("Gallery"))
                images = imagesViewModel.getImagesList().getValue();
            else
                images = imagesViewModel.getImagesAlbum().getValue();
        }
        if(images == null)
            images = new ArrayList<ImageObject>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        try {
            ((MainActivity) getContext()).setCurrentFragmentName(fragmentName);
            ((MainActivity) getContext()).setImageFragment(this);
            if (fragmentName.equals("Gallery"))
                ((MainActivity) getContext()).setCurrentFragment(MainActivity.FragmentType.IMAGE_FRAGMENT);
            else
                ((MainActivity) getContext()).setCurrentFragment(MainActivity.FragmentType.ALBUM_IMAGE_FRAGMENT);
        }
        catch (Exception e)
        {
            //Ignore if context is not MainActivity
        }

        View imageFragment = inflater.inflate(R.layout.fragment_image, container, false);
        recyclerView = imageFragment.findViewById(R.id.rv_items);
        tvTitle = imageFragment.findViewById(R.id.tvTitle);

        switch (fragmentName){
            case "Gallery":
                tvTitle.setText(getString(R.string.gallery));
                break;
            case "Album":
                tvTitle.setText(getString(R.string.album));
                break;
            case "Search":
                tvTitle.setText(getString(R.string.search));
                break;
            case "Trash":
                tvTitle.setText(getString(R.string.trash));
                break;
            case "Favorite":
                tvTitle.setText(getString(R.string.favorite));
                break;
            default:
                tvTitle.setText(fragmentName);
                break;
        }


        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(30);

        if(SortType.DATE == sortType)
            ImageObject.sortByDate(images, ascending);
        else if(SortType.NAME == sortType)
            ImageObject.sortByFileName(images, ascending);

        adapter = new ImageAdapter(images, fragmentName);
        adapter.setColNumber(colNumbers[colNumberIndex]);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(imageFragment.getContext(), colNumbers[colNumberIndex]));
        recyclerView.scrollToPosition(SharedPreferencesManager.loadCurrentItemPosition(getContext()));

        albumHelper = AlbumHelper.getInstance(getContext());

        btnSort = imageFragment.findViewById(R.id.btnSort);
        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ascending) {
                    ascending = false;
                    if(SortType.DATE == sortType)
                        ImageObject.sortByDate(images, ascending);
                    else if(SortType.NAME == sortType)
                        ImageObject.sortByFileName(images, ascending);
                    btnSort.setImageResource(R.drawable.ic_arrow_up);
                }
                else {
                    ascending = true;
                    if(SortType.DATE == sortType)
                        ImageObject.sortByDate(images, ascending);
                    else if(SortType.NAME == sortType)
                        ImageObject.sortByFileName(images, ascending);
                    btnSort.setImageResource(R.drawable.ic_arrow_down);
                }

                adapter.notifyDataSetChanged();
            }
        });

        if(ascending)
            btnSort.setImageResource(R.drawable.ic_arrow_down);
        else
            btnSort.setImageResource(R.drawable.ic_arrow_up);

        btnChangeGrid = imageFragment.findViewById(R.id.btnChangeGrid);
        PopupMenu popupMenu = new PopupMenu(getContext(), btnChangeGrid);
        popupMenu.getMenuInflater().inflate(R.menu.number_columns_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if(id == R.id.two_columns){
                colNumberIndex = 0;

            }
            else if(id == R.id.three_columns){
                colNumberIndex = 1;
            }
            else if(id == R.id.four_columns){
                colNumberIndex = 2;
            }
            adapter.setColNumber(colNumbers[colNumberIndex]);
            recyclerView.setLayoutManager(new GridLayoutManager(imageFragment.getContext(), adapter.getColNumber()));
            return false;
        });

        btnChangeGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu.show();
            }
        });

        btnOptions = imageFragment.findViewById(R.id.btnOptions);
        setBtnOptionsClick();


        btnSelect = imageFragment.findViewById(R.id.btnSelect);
        reload(false);


        return imageFragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        reload(false);
        setBtnOptionsClick();

        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        SharedPreferencesManager.saveCurrentItemPosition(getContext(), firstVisiblePosition);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferencesManager.saveCurrentItemPosition(getContext(), 0);
    }

    public void setFragmentAdapter(ArrayList<ImageObject> images, Context context) {
        this.images = images;

        if(SortType.DATE == sortType)
            ImageObject.sortByDate(images, ascending);
        else if(SortType.NAME == sortType)
            ImageObject.sortByFileName(images, ascending);

        adapter = new ImageAdapter(images, fragmentName);
        adapter.setColNumber(colNumbers[colNumberIndex]);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), colNumbers[colNumberIndex]));
        recyclerView.scrollToPosition(SharedPreferencesManager.loadCurrentItemPosition(context));
    }

    public void reload(boolean selectMode){
        setSelectMode(selectMode);
        setBtnOptionsClick();
        //reload fragment
        if(isSelectMode){
            enterSelectMode();
        }

        else{
            adapter.setSelectMode(false);
            btnSelect.setImageResource(R.drawable.ic_multiselect);
            btnSort.setVisibility(View.VISIBLE);
            btnChangeGrid.setVisibility(View.VISIBLE);
            btnOptions.setVisibility(View.VISIBLE);
            btnSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isSelectMode = true;
                    reload(true);
                }
            });
        }

    }

    public void setSelectMode(boolean selectMode){
        isSelectMode = selectMode;
    }

    public void enterSelectMode(){
        btnSelect.setImageResource(R.drawable.ic_multiselect_menu);
        btnSort.setVisibility(View.INVISIBLE);
        btnChangeGrid.setVisibility(View.INVISIBLE);
        btnOptions.setVisibility(View.INVISIBLE);
        adapter.setSelectMode(true);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectMenu(v.getContext());
            }
        });
    }

    void setBtnOptionsClick(){
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), btnOptions);

                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                if(fragmentName.equals("Search") || fragmentName.equals("Trash"))
                {
                    popupMenu.getMenu().findItem(R.id.menu_delete_duplitate).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.menu_download_backup).setVisible(false);
                }



                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if(id == R.id.menu_date)
                        {
                            sortType = SortType.DATE;
                            ImageObject.sortByDate(images, ascending);
                            adapter.notifyDataSetChanged();
                        }
                        else if(id == R.id.menu_name)
                        {
                            sortType = SortType.NAME;
                            ImageObject.sortByFileName(images, ascending);
                            adapter.notifyDataSetChanged();
                        }
                        else if (id == R.id.menu_download_backup) {
                            Thread downloadThread = new Thread(new Runnable() {
                                TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
                                @Override
                                public void run() {

                                    try {
                                        BackupImage.downloadImage(getContext(), taskCompletionSource);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    taskCompletionSource.getTask().addOnCompleteListener(task -> {
                                        MainActivity mainActivity = (MainActivity) getContext();
                                        mainActivity.handler.sendEmptyMessage(1);
                                        Toast.makeText(getContext(), R.string.download_successful, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                            downloadThread.start();

                        }
                        else if(id == R.id.menu_delete_duplitate)
                        {
                            TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
                            final int[] count = {0};
                            Thread deleteThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        count[0] = ImageObject.deleteDuplicateImage(getContext(), images, taskCompletionSource);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    Log.d("Delete duplicate", "Done");
                                }

                            });
                            deleteThread.start();
                            taskCompletionSource.getTask().addOnCompleteListener(task -> {
                                try {
                                    MainActivity mainActivity = (MainActivity) getContext();
                                    mainActivity.handler.sendEmptyMessage(1);
                                    Toast.makeText(getContext(), getString(R.string.delete_duplicate_done) + ": " + count[0] + " " + getString(R.string.images), Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e)
                                {
                                    //Ignore if context is not MainActivity
                                }
                            });
                            taskCompletionSource.getTask().addOnFailureListener(task -> {
                                try {
                                    Toast.makeText(getContext(), getString(R.string.delete_duplicate_error), Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e)
                                {
                                    //Ignore if context is not MainActivity
                                }
                            });
                        } else if (id == R.id.setting_menu){
                            // Open setting activity
                            Intent intent = new Intent(getContext(), SettingActivity.class);
                            startActivity(intent);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    void deleteSelectedImages(){
        ArrayList<ImageObject> selectedImages = adapter.getSelectedImages();
        if(selectedImages.size() > 0)
        {
            for(ImageObject imageObject : selectedImages){
                if(SearchActivity.isSearchActivityRunning())
                {
                    SearchActivity.addDeleteImage(imageObject);
                }
                imageObject.deleteToTrash(getContext());
                images.remove(imageObject);
            }
            if(SearchActivity.isSearchActivityRunning())
            {
                try {
                    ((SearchActivity) getContext()).onResume();
                }
                catch (Exception e)
                {
                    //Ignore if context is not SearchActivity
                }
            }
        }
        else
            Toast.makeText(getContext(), R.string.no_image_selected, Toast.LENGTH_SHORT).show();
        try {
            ((MainActivity) getContext()).handler.sendEmptyMessage(1);
        }
        catch(Exception e){

        }
        reload(false);
    }

    void deleteTrashImages(){
        ArrayList<ImageObject> selectedImages = adapter.getSelectedImages();
        if(selectedImages.size() > 0)
        {
            for(ImageObject imageObject : selectedImages){
                imageObject.deleteFile(getContext());
                images.remove(imageObject);
            }
        }
        else
            Toast.makeText(getContext(), R.string.no_image_selected, Toast.LENGTH_SHORT).show();

        try {
            ((MainActivity) getContext()).handler.sendEmptyMessage(1);
        }
        catch(Exception e){

        }
        reload(false);
    }

    void showSelectMenu(Context context){
        PopupMenu popupMenu = new PopupMenu(context, btnSelect);
        popupMenu.getMenuInflater().inflate(R.menu.select_images_menu, popupMenu.getMenu());

        if( fragmentName.equals("Trash"))
        {
            popupMenu.getMenu().findItem(R.id.add_to_album).setVisible(false);
            popupMenu.getMenu().findItem(R.id.delete_images).setVisible(false);
            popupMenu.getMenu().findItem(R.id.upload_images).setVisible(false);
            popupMenu.getMenu().findItem(R.id.restore_images).setVisible(true);
            popupMenu.getMenu().findItem(R.id.delete_trash).setVisible(true);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();

                if(id == R.id.add_to_album)
                {
                    ArrayList<ImageObject> selectedImages = adapter.getSelectedImages();
                    if(selectedImages.size() > 0)
                    {
                        albumHelper.addImagesToAlbum(getContext(), selectedImages);
                    }
                    else
                        Toast.makeText(getContext(), R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                    adapter.setSelectMode(false);
                    reload(false);
                }
                else if(id == R.id.delete_images)
                {
                    Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.dialog_save_edited_image);
                    TextView txtTitle = dialog.findViewById(R.id.tv_message_dialog);
                    txtTitle.setText(R.string.delete_images_confirm);
                    Button btnYes = dialog.findViewById(R.id.btn_save);
                    Button btnNo = dialog.findViewById(R.id.btn_cancel);
                    btnYes.setText(R.string.delete);
                    btnNo.setText(R.string.cancel);
                    btnYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteSelectedImages();
                            dialog.dismiss();
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
                else if(id == R.id.upload_images)
                {
                    ArrayList<ImageObject> selectedImages = adapter.getSelectedImages();
                    if(selectedImages.size() > 0)
                    {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for(ImageObject imageObject : selectedImages){
                                    BackupImage.uploadImage(getContext(), imageObject);
                                }
                            }
                        });
                        thread.start();
                    }
                    else
                        Toast.makeText(getContext(), R.string.no_image_selected, Toast.LENGTH_SHORT).show();

                    reload(false);
                }
                else if(id == R.id.select_all){
                    adapter.SelectAll();
                } else if (id == R.id.cancel_action) {
                    reload(false);
                } else if (id == R.id.delete_trash) {
                    Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.dialog_save_edited_image);
                    TextView txtTitle = dialog.findViewById(R.id.tv_message_dialog);
                    txtTitle.setText(R.string.delete_trashes_confirm);
                    Button btnYes = dialog.findViewById(R.id.btn_save);
                    Button btnNo = dialog.findViewById(R.id.btn_cancel);
                    btnYes.setText(R.string.delete);
                    btnNo.setText(R.string.cancel);
                    btnYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteTrashImages();
                            dialog.dismiss();
                        }
                    });
                    btnNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else if (id == R.id.restore_images) {
                    ArrayList<ImageObject> selectedImages = adapter.getSelectedImages();
                    if(selectedImages.size() > 0)
                    {
                        for(ImageObject imageObject : selectedImages){
                            imageObject.restoreFile(getContext());
                            images.remove(imageObject);
                        }
                    }
                    else
                        Toast.makeText(getContext(), R.string.no_image_selected, Toast.LENGTH_SHORT).show();

                    reload(false);

                    try {
                        ((MainActivity) getContext()).handler.sendEmptyMessage(1);
                    }
                    catch(Exception e){

                    }
                } else if (id == R.id.slide_show)
                {
                    ArrayList<ImageObject> selectedImages = adapter.getSelectedImages();
                    if(selectedImages.size() > 0)
                    {
                        Intent intent = new Intent(getContext(), SlideShowActivity.class);
                        intent.putParcelableArrayListExtra("images", selectedImages);
                        startActivity(intent);
                    }
                    else
                        Toast.makeText(getContext(), R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                    reload(false);
                }

                return false;
            }
        });

        popupMenu.show();
    }
}