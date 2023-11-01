package com.example.imagegallery;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
    //private String mParam2;

    public ImageFragment() {
        // Required empty public constructor
    }


    public static ImageFragment newInstance(ArrayList<ImageObject> images, String fragmentName) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, images);
        args.putString(ARG_PARAM2, fragmentName);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView recyclerView;
    private TextView tvTitle;
    private ImageButton btnChangeGrid;
    private ImageView btnSort, btnOptions;
    private int[] colNumbers = {2, 3, 4};
    private static int colNumberIndex = 0;

    private static boolean ascending = false;

    private enum SortType {
        DATE, NAME
    }
    private static SortType sortType = SortType.DATE;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            if(msg.what == 0)
            {
                getActivity().recreate();
            }
        };
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            images = getArguments().getParcelableArrayList(ARG_PARAM1);
            fragmentName = getArguments().getString(ARG_PARAM2);
        }
        if(images == null)
            images = new ArrayList<ImageObject>();

        if(images.size() > 0) {
            for (ImageObject image : images) {
                ArrayList<String> albumNames = SharedPreferencesManager.loadImageAlbumInfo(getContext(), image.getFilePath());
                if (albumNames != null)
                    image.setAlbumNames(getContext(), albumNames);
                else
                    image.setAlbumNames(getContext(), new ArrayList<String>());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View imageFragment = inflater.inflate(R.layout.fragment_image, container, false);
        recyclerView = imageFragment.findViewById(R.id.rv_items);
        tvTitle = imageFragment.findViewById(R.id.tvTitle);

        tvTitle.setText(fragmentName);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(imageFragment.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        ImageObject.sortByDate(images, ascending);

        MyAdapter adapter = new MyAdapter(images);
        adapter.setColNumber(colNumbers[colNumberIndex]);

        recyclerView.setAdapter(adapter);


        recyclerView.setLayoutManager(new GridLayoutManager(imageFragment.getContext(), colNumbers[colNumberIndex]));
        recyclerView.scrollToPosition(SharedPreferencesManager.loadCurrentItemPosition(getContext()));

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
        btnChangeGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colNumberIndex = (colNumberIndex + 1) % colNumbers.length;
                adapter.setColNumber(colNumbers[colNumberIndex]);
                recyclerView.setLayoutManager(new GridLayoutManager(imageFragment.getContext(), adapter.getColNumber()));
            }
        });

        btnOptions = imageFragment.findViewById(R.id.btnOptions);
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(imageFragment.getContext(), btnOptions);

                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                if(fragmentName.equals("Search") || fragmentName.equals("Trash"))
                {
                    popupMenu.getMenu().findItem(R.id.menu_delete_duplitate).setVisible(false);
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
                                        handler.sendEmptyMessage(0);
                                        Toast.makeText(getContext(), "Downloaded successful", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                            downloadThread.start();

                        }
                        else if(id == R.id.menu_delete_duplitate)
                        {
                            Thread deleteThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ImageObject.deleteDuplicateImage(getContext(), images);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    handler.sendEmptyMessage(0);
                                }

                            });
                            deleteThread.start();
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
            });

        return imageFragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        SharedPreferencesManager.saveCurrentItemPosition(getContext(), firstVisiblePosition);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferencesManager.saveCurrentItemPosition(getContext(), 0);
    }
}