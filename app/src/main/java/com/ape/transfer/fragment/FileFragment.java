package com.ape.transfer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
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
import com.ape.transfer.model.FileEvent;
import com.ape.transfer.model.FileItem;
import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by android on 16-6-28.
 */
public class FileFragment extends Fragment implements LoaderManager.LoaderCallbacks<BaseLoader.Result>, FileItemAdapter.OnItemClickListener {
    private static final String TAG = "FileFragment";
    private static final String FILE_CATEGORY = "fileCategory";
    private static final int LOADER_ID = 0;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.empty)
    TextView empty;
    @BindView(R.id.empty_container)
    FrameLayout emptyContainer;
    private FileItemAdapter mMusicItemAdapter;
    private int mFileCategory;
    private OnFileItemChangeListener mListener;

    public static FileFragment newInstance(int fileCategory) {
        FileFragment fragment = new FileFragment();
        Bundle args = new Bundle();
        args.putInt(FILE_CATEGORY, fileCategory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileCategory = getArguments().getInt(FILE_CATEGORY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file, container, false);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEventMainThread(FileEvent event) {
        Log.i(TAG, "onEventMainThread收到了消息：" + event.getMsg());
        ArrayList<FileItem> lists = event.getMsg();
        mMusicItemAdapter.unChecked(lists);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser
                && getLoaderManager().getLoader(LOADER_ID) == null){
            getLoaderManager().initLoader(0, null, FileFragment.this);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMusicItemAdapter = new FileItemAdapter(getContext(), mFileCategory, this);
        recyclerView.setLayoutManager(getLayoutManager());
        recyclerView.setAdapter(mMusicItemAdapter);
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        empty.setVisibility(View.INVISIBLE);
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        switch (mFileCategory) {
            case P2PConstant.TYPE.APP:
            case P2PConstant.TYPE.PIC:
            case P2PConstant.TYPE.VIDEO:
                return new GridLayoutManager(getContext(), 4);
            default:
                return new LinearLayoutManager(getContext());
        }
    }

    @Override
    public Loader<BaseLoader.Result> onCreateLoader(int id, Bundle args) {
        return new FileItemLoader(getContext(), mFileCategory);
    }

    @Override
    public void onLoadFinished(Loader<BaseLoader.Result> loader, BaseLoader.Result data) {
        if (!data.lists.isEmpty()) {
            progressBar.setVisibility(View.INVISIBLE);
            empty.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            mMusicItemAdapter.setDatas(data.lists);
        } else {
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
        mListener.onFileItemChange(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFileItemChangeListener) {
            mListener = (OnFileItemChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFileItemChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFileItemChangeListener {
        void onFileItemChange(FileItem item);
    }
}
