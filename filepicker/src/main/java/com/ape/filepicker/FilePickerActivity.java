package com.ape.filepicker;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.ape.filepicker.items.BackItem;
import com.ape.filepicker.items.ExplorerItem;
import com.ape.filepicker.util.HistoryDatabase;


public class FilePickerActivity extends BasePickerActivity {

    @Override
    protected Fragment getWelcomeFragment() {
        return new ExplorerFragment();
    }

    @Override
    protected void save() {
        HistoryDatabase.save(this, selectedItems);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {

        ExplorerItem item = (ExplorerItem) parent.getItemAtPosition(position);

        if (item instanceof BackItem) {
            onBackPressed();
            return;
        }

        if (item.isDirectory()) {
            String path = item.getPath();
            Bundle bundle = new Bundle();
            bundle.putString("path", path);

            Fragment fragment = new ExplorerFragment();
            fragment.setArguments(bundle);
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.picker_fragment_explorer_enter, R.animator.picker_fragment_explorer_exit,
                            R.animator.picker_fragment_explorer_return, R.animator.picker_fragment_explorer_out)
                    .replace(R.id.container, fragment)
                    .addToBackStack(path)
                    .commit();
        } else {
            selectItem(item, itemView);
            //returnResult();
            Adapter adapter = parent.getAdapter();
            if (adapter instanceof ExplorerAdapter) {
                ((ExplorerAdapter) adapter).notifyDataSetChanged();
            } else if (adapter instanceof WelcomeExplorerAdapter) {
                ((WelcomeExplorerAdapter) adapter).notifyDataSetChanged();

            }
        }
    }

}