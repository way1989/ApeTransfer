package com.ape.transfer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.adapter.FileItemAdapter;
import com.ape.transfer.fragment.loader.BaseLoader;
import com.ape.transfer.fragment.loader.FileItemLoader;
import com.ape.transfer.model.FileItem;
import com.ape.transfer.util.FileCategoryHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by android on 16-6-28.
 */
public class FileFragment extends Fragment implements LoaderManager.LoaderCallbacks<BaseLoader.Result>, FileItemAdapter.OnItemClickListener {
    private static final String FILE_CATEGORY = "fileCategory";
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.empty)
    TextView empty;
    @BindView(R.id.empty_container)
    FrameLayout emptyContainer;
    private FileItemAdapter mMusicItemAdapter;
    private FileCategoryHelper.FileCategory mFileCategory;

    public static FileFragment newInstance(FileCategoryHelper.FileCategory fileCategory) {
        FileFragment fragment = new FileFragment();
        Bundle args = new Bundle();
        args.putSerializable(FILE_CATEGORY, fileCategory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileCategory = (FileCategoryHelper.FileCategory) getArguments().getSerializable(FILE_CATEGORY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMusicItemAdapter = new FileItemAdapter(getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mMusicItemAdapter);
        getLoaderManager().initLoader(0, null, FileFragment.this);
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        empty.setVisibility(View.INVISIBLE);
    }


    @Override
    public Loader<BaseLoader.Result> onCreateLoader(int id, Bundle args) {
        return new FileItemLoader(getContext(), mFileCategory);
    }

    @Override
    public void onLoadFinished(Loader<BaseLoader.Result> loader, BaseLoader.Result data) {

        if (data.lists != null) {
            progressBar.setVisibility(View.INVISIBLE);
            empty.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            mMusicItemAdapter.setDatas(data.lists);
        }else {
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            empty.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<BaseLoader.Result> loader) {
        mMusicItemAdapter.reset();
    }

    @Override
    public void onItemClick(View v) {
        FileItem item = (FileItem) v.getTag();
        boolean isSelected = item.selected;
        item.selected = !isSelected;
        mMusicItemAdapter.notifyDataSetChanged();
    }
}