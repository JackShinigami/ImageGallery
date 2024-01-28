package com.example.imagegallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewImageActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private ImageView edit, iv_love, more, share, imgDelete;
    private ViewImageAdapter viewImageAdapter;

    private ImagesViewModel imagesViewModel;

    private ImageObject obj ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        this.imagesViewModel = new ViewModelProvider(this).get(ImagesViewModel.class);

        if (SharedPreferencesManager.loadCurrentName(this).equals("Gallery"))
            viewImageAdapter = new ViewImageAdapter(this, imagesViewModel.getImagesList().getValue());
        else {
            if (SharedPreferencesManager.loadCurrentName(this).equals("Search"))
                viewImageAdapter = new ViewImageAdapter(this, imagesViewModel.getImagesSearch().getValue());
            else
                viewImageAdapter = new ViewImageAdapter(this, imagesViewModel.getImagesBackup().getValue());
        }

        viewPager2 = findViewById(R.id.viewPager2);

        viewPager2.setAdapter(viewImageAdapter);
        viewPager2.setCurrentItem(getIntent().getBundleExtra("positionBundle").getInt("position"), false);
        obj = viewImageAdapter.getCurrentItem(viewPager2.getCurrentItem());

        edit = findViewById(R.id.imgEdit);
        iv_love = findViewById(R.id.iv_love);

        if (SharedPreferencesManager.loadCurrentName(this).equals("Trash"))
            iv_love.setVisibility(View.GONE);
        if (obj.isLoved(this))
            iv_love.setImageResource(R.drawable.ic_loved);
        else
            iv_love.setImageResource(R.drawable.ic_not_loved);

        iv_love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (obj.isLoved(view.getContext())) {
                    obj.setLoved(view.getContext(), false);
                    iv_love.setImageResource(R.drawable.ic_not_loved);
                } else {
                    obj.setLoved(view.getContext(), true);
                    iv_love.setImageResource(R.drawable.ic_loved);
                }
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // This method will be invoked when a new page becomes selected.
                obj = viewImageAdapter.getCurrentItem(viewPager2.getCurrentItem());
                if (obj.isLoved(viewPager2.getContext()))
                    iv_love.setImageResource(R.drawable.ic_loved);
                else
                    iv_love.setImageResource(R.drawable.ic_not_loved);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                // This method will be invoked when the current page is scrolled.
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                // Called when the scroll state changes.
                // Possible states are SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING.
            }
        });



        edit.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("imageObject", viewImageAdapter.getCurrentItem(viewPager2.getCurrentItem()));
            startActivity(intent);
        });

        share = findViewById(R.id.iv_share);
        share.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            MediaScannerConnection.scanFile(this,
                    new String[]{obj.getFilePath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            share.putExtra(Intent.EXTRA_STREAM, uri);
                            startActivity(Intent.createChooser(share, "Select"));
                        }
                    });
        });
        imgDelete = findViewById(R.id.imgDelete);
        imgDelete.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_save_edited_image);
            TextView txtTitle = dialog.findViewById(R.id.tv_message_dialog);
            txtTitle.setText(R.string.delete_images_confirm);
            Button btnYes = dialog.findViewById(R.id.btn_save);
            Button btnNo = dialog.findViewById(R.id.btn_cancel);
            btnYes.setText(R.string.delete);
            btnNo.setText(R.string.cancel);
            dialog.show();
            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    obj.deleteToTrash(getApplicationContext());
                    if (SearchActivity.isSearchActivityRunning()) {
                        SearchActivity.addDeleteImage(obj);
                    }
                    finish();
                    dialog.dismiss();
                }
            });
            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        });
    }
}