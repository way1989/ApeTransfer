package com.ape.transfer.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ape.transfer.R;
import com.ape.transfer.adapter.MusicItemAdapter;
import com.ape.transfer.util.FileCategoryHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by android on 16-6-28.
 */
public class MusicFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    RecyclerView recyclerView;
    private MusicItemAdapter mMusicItemAdapter;

    public static MusicFragment newInstance() {
        MusicFragment fragment = new MusicFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mMusicItemAdapter = new MusicItemAdapter(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mMusicItemAdapter);
        getLoaderManager().initLoader(0, null, MusicFragment.this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = FileCategoryHelper.getContentUriByCategory(FileCategoryHelper.FileCategory.Music);
        String[] projection = FileCategoryHelper.getProjection();
        String selection = "";
        String[] selectionArgs = null;
        String sortOrder = FileCategoryHelper.buildSortOrder(FileCategoryHelper.SortMethod.name);
        return new CursorLoader(
                getContext(), uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMusicItemAdapter.setDatas(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMusicItemAdapter.reset();
    }
}
