package com.ape.filepicker;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.ape.filepicker.items.ExplorerItem;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by kiolt_000 on 15/09/2014.
 */
public abstract class BasePickerActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    protected ArrayList<String> selectedItems = new ArrayList<String>();
    protected Fragment currentFragment;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_picker);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.picker_fragment_explorer_welcome_enter, R.animator.picker_fragment_explorer_welcome_exit)
                    .add(R.id.container, getWelcomeFragment())
                    .commit();
        }
        mActionBar = getSupportActionBar();
        if (mActionBar != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    protected void returnResult() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("picked", selectedItems);
        setResult(RESULT_OK, returnIntent);
        save();
        finish();
    }

    protected abstract Fragment getWelcomeFragment();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentFragment != null) {
            currentFragment.onCreateOptionsMenu(menu, getMenuInflater());
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem saveItem = menu.findItem(R.id.menu_pick);
        if (saveItem != null) {
            saveItem.setEnabled(!selectedItems.isEmpty());
            String size = selectedItems.isEmpty() ? "" : (selectedItems.size() + "/9");
            saveItem.setTitle(getString(android.R.string.ok) + size);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_pick) {
            returnResult();
            return true;
        } else {
        }
        if (currentFragment != null)
            return currentFragment.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        // currentFragment = fragment;
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    protected abstract void save();

    public boolean selectItem(String path) {
        boolean selected = !selectedItems.contains(path);
        if (selected) {
            if (selectedItems.size() > 9) {
                Toast.makeText(this, "You can pick only 10 items.", Toast.LENGTH_SHORT).show();
                return false;
            }
            selectedItems.add(path);
        } else {
            selectedItems.remove(path);
        }
        updateCounter();
        return selected;
    }

    public void selectItem(ExplorerItem item, View itemView) {
        item.setSelected(selectItem(item.getPath()));
        item.bindData(itemView);
    }

    public void updateCounter() {
        invalidateOptionsMenu();
    }

    public boolean isSelected(String path) {
        return selectedItems.contains(path);
    }

    public boolean isSelected(File file) {
        return isSelected(file.getPath());
    }

    public void setFragment(Fragment fragment) {
        this.currentFragment = fragment;
    }
}
