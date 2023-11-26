package com.example.imagegallery;

import static android.provider.Settings.System.getString;

import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagegallery.databinding.DialogThemeSelectionBinding;
import com.example.imagegallery.databinding.SettingListItemBinding;
import com.example.imagegallery.placeholder.SettingPlaceholderContent.SettingPlaceholderItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SettingPlaceholderItem}.
 */
public class SettingItemRecyclerViewAdapter extends RecyclerView.Adapter<SettingItemRecyclerViewAdapter.SettingViewHolder> {

    private final List<SettingPlaceholderItem> properties;

    public SettingItemRecyclerViewAdapter(List<SettingPlaceholderItem> items) {
        properties = items;
    }

    public List<SettingPlaceholderItem> getProperties() {
        return properties;
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SettingViewHolder(SettingListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final SettingViewHolder holder, int position) {
        holder.settingItem = properties.get(position);
        holder.tvProperty.setText(properties.get(position).property);
        holder.tvValue.setText(properties.get(position).value);

        if(position == 0) {
            int themeState = SharedPreferencesManager.loadThemeState(holder.itemView.getContext());
            switch (themeState) {
                case 0:
                    holder.tvValue.setText(holder.itemView.getContext().getString(R.string.dark));
                    break;
                case 1:
                    holder.tvValue.setText(holder.itemView.getContext().getString(R.string.light));
                    break;
                case 2:
                    holder.tvValue.setText(holder.itemView.getContext().getString(R.string.defaultTheme));
                    break;
            }
        } else if(position == 1){
            // TODO: Make language factory

        }

//        switch (position) {
//            case 0:
//                if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO){
//                    // holder.tvValue.setText in string resource
//                    holder.tvValue.setText(holder.itemView.getContext().getString(R.string.light));
//                }
//                break;
//        }


        holder.itemView.setOnClickListener(v -> {
            switch (position) {
                case 0:
                    // send message to the activity
                    // title is a string that get from R.string.theme

                    View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_theme_selection, null);
                    AlertDialog.Builder askingThemeDialogBuilder = new AlertDialog.Builder(v.getContext());
                    askingThemeDialogBuilder.setView(dialogView);
                    askingThemeDialogBuilder.setNegativeButton(v.getContext().getString(R.string.cancel), (dialog, which) -> {
                        dialog.dismiss();
                    });
                    DialogThemeSelectionBinding binding = DialogThemeSelectionBinding.bind(dialogView);
                    if (SharedPreferencesManager.loadThemeState(v.getContext()) == 0) {
                        binding.radioDark.setChecked(true);
                    } else if (SharedPreferencesManager.loadThemeState(v.getContext()) == 1) {
                        binding.radioLight.setChecked(true);
                    } else if (SharedPreferencesManager.loadThemeState(v.getContext()) == 2) {
                        binding.radioSameSystem.setChecked(true);
                    }

                    askingThemeDialogBuilder.setPositiveButton(v.getContext().getString(R.string.ok), (dialog, which) -> {


                        // change theme of the app
                        if (binding.radioDark.isChecked()) {
                            holder.tvValue.setText(v.getContext().getString(R.string.dark));
                            ((SettingActivity) v.getContext()).onThemeChanged("Dark");
                        } else if (binding.radioLight.isChecked()) {
                            holder.tvValue.setText(v.getContext().getString(R.string.light));
                            ((SettingActivity) v.getContext()).onThemeChanged("Light");
                        } else if (binding.radioSameSystem.isChecked()) {
                            holder.tvValue.setText(v.getContext().getString(R.string.defaultTheme));
                            ((SettingActivity) v.getContext()).onThemeChanged("SameSystem");
                        }

                    });

                    // create and show the dialog
                    AlertDialog askingThemeDialog = askingThemeDialogBuilder.create();
                    askingThemeDialog.show();

                    break;
                case 1:

            }
        });
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    public static class SettingViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvProperty;
        public final TextView tvValue;
        public SettingPlaceholderItem settingItem;


        public SettingViewHolder(SettingListItemBinding binding) {
            super(binding.getRoot());
            tvProperty = binding.tvProperty;
            tvValue = binding.tvValue;
        }
    }
}