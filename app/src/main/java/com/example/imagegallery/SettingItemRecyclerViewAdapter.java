package com.example.imagegallery;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagegallery.databinding.DialogLanguageSelectionBinding;
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

        int themeState = SharedPreferencesManager.loadThemeState(holder.itemView.getContext());
        int languageState = SharedPreferencesManager.loadLanguageState(holder.itemView.getContext());

        if(position == 0) {
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
            switch (languageState) {
                case 0:
                    holder.tvValue.setText(holder.itemView.getContext().getString(R.string.en_us));
                    break;
                case 1:
                    holder.tvValue.setText(holder.itemView.getContext().getString(R.string.vie));
                    break;
            }

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

                    View dialogLangView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_language_selection, null);
                    AlertDialog.Builder askingLanguageDialogBuilder = new AlertDialog.Builder(v.getContext());
                    askingLanguageDialogBuilder.setView(dialogLangView);
                    askingLanguageDialogBuilder.setNegativeButton(v.getContext().getString(R.string.cancel), (dialog, which) -> {
                        dialog.dismiss();
                    });

                    DialogLanguageSelectionBinding bindingLang = DialogLanguageSelectionBinding.bind(dialogLangView);

                    String[] languages = {
                            v.getContext().getString(R.string.en_us),
                            v.getContext().getString(R.string.vie)
                    };

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_single_choice, languages);
                    bindingLang.lvLanguageList.setAdapter(adapter);
                    bindingLang.lvLanguageList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

                    // set checked item
                    if(languageState == 0){
                        bindingLang.lvLanguageList.setItemChecked(0, true);
                    } else if(languageState == 1){
                        bindingLang.lvLanguageList.setItemChecked(1, true);
                    }



                    // set positive button
                    askingLanguageDialogBuilder.setPositiveButton(v.getContext().getString(R.string.ok), (dialog, which) -> {
                        int selectedPosition = bindingLang.lvLanguageList.getCheckedItemPosition();
                        if(selectedPosition == 0){
                            ((SettingActivity) v.getContext()).onLanguageChanged("English");
                        } else if(selectedPosition == 1){
                            ((SettingActivity) v.getContext()).onLanguageChanged("Vietnamese");
                        }
                    });


                    // create and show the dialog
                    AlertDialog askingLanguageDialog = askingLanguageDialogBuilder.create();
                    askingLanguageDialog.show();

                    break;

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