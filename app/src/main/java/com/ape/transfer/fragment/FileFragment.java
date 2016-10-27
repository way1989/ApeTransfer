package com.ape.transfer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ape.transfer.R;
import com.ape.transfer.adapter.FileItemAdapter;
import com.ape.transfer.fragment.loader.BaseLoader;
import com.ape.transfer.fragment.loader.FileItemLoader;
import com.ape.transfer.model.FileEvent;
import com.ape.transfer.model.FileItem;
import com.ape.transfer.p2p.util.Constant;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.RxBus;
import com.ape.transfer.widget.LoadingEmptyContainer;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by android on 16-6-28.
 */
public class FileFragment extends RxFragment implements LoaderManager.LoaderCallbacks<BaseLoader.Result>, FileItemAdapter.OnItemClickListener {
    private static final String TAG = "FileFragment";
    private static final String FILE_CATEGORY = "fileCategory";
    private static final int LOADER_ID = 0;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.loading_empty_container)
    LoadingEmptyContainer loadingEmptyContainer;

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
        RxBus.getInstance().toObservable(FileEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<FileEvent>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<FileEvent>() {
                    @Override
                    public void call(FileEvent event) {
                        //do some thing
                        onFileChange(event);
                    }
                });
        return view;
    }

    public void onFileChange(FileEvent event) {
        Log.i(TAG, "onEventMainThread收到了消息：" + event.getFileItemList());
        ArrayList<FileItem> lists = event.getFileItemList();
        mMusicItemAdapter.unChecked(lists);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        loadData();
    }

    private void loadData() {
        if (isAdded() && getUserVisibleHint()
                && getLoaderManager().getLoader(LOADER_ID) == null) {
            loadingEmptyContainer.showLoading();
            getLoaderManager().initLoader(0, null, FileFragment.this);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMusicItemAdapter = new FileItemAdapter(getContext().getApplicationContext(),
                mFileCategory, this);
        recyclerView.setLayoutManager(getLayoutManager());
        recyclerView.setAdapter(mMusicItemAdapter);
        loadData();
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        switch (mFileCategory) {
            case Constant.TYPE.APP:
            case Constant.TYPE.PIC:
            case Constant.TYPE.VIDEO:
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
            loadingEmptyContainer.hideAll();
            mMusicItemAdapter.setDatas(data.lists);
        } else {
            loadingEmptyContainer.showNoResults();
        }

    }

    @Override
    public void onLoaderReset(Loader<BaseLoader.Result> loader) {
        mMusicItemAdapter.reset();
    }

    @Override
    public void onItemClick(View v) {
        FileItem item = (FileItem) v.getTag();
//        boolean isSelected = item.selected;
//        item.selected = !isSelected;
//        mMusicItemAdapter.notifyDataSetChanged();
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
