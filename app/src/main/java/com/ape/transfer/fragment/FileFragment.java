package com.ape.transfer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ape.transfer.App;
import com.ape.transfer.R;
import com.ape.transfer.adapter.FileItemAdapter;
import com.ape.transfer.fragment.loader.BaseLoader;
import com.ape.transfer.fragment.loader.FileItemLoader;
import com.ape.transfer.model.FileEvent;
import com.ape.transfer.model.FileItem;
import com.ape.transfer.p2p.util.Constant;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.RxBus;
import com.ape.transfer.util.Screen;
import com.ape.transfer.widget.SimpleListDividerDecorator;
import com.ape.transfer.widget.SpaceGridItemDecoration;
import com.weavey.loading.lib.LoadingLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by android on 16-6-28.
 */
public class FileFragment extends Fragment implements LoaderManager.LoaderCallbacks<BaseLoader.Result>, FileItemAdapter.OnItemClickListener {
    private static final String TAG = "FileFragment";
    private static final String FILE_CATEGORY = "fileCategory";
    private static final int LOADER_ID = 0;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading_layout)
    LoadingLayout mLoadingLayout;

    private FileItemAdapter mAdapter;
    private int mFileCategory;
    private OnFileItemChangeListener mListener;
    protected CompositeDisposable mDisposable = new CompositeDisposable();

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
        //do some thing
        mDisposable.add(RxBus.getInstance().toObservable(FileEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onFileChange));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDisposable.clear();
    }

    public void onFileChange(FileEvent event) {
        Log.i(TAG, "onEventMainThread收到了消息：" + event.getFileItemList());
        ArrayList<FileItem> lists = event.getFileItemList();
        mAdapter.unChecked(lists);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        loadData();
    }

    private void loadData() {
        if (isAdded() && getUserVisibleHint()
                && getLoaderManager().getLoader(LOADER_ID) == null) {
            getLoaderManager().initLoader(0, null, FileFragment.this);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLoadingLayout.setStatus(LoadingLayout.Loading);
        mAdapter = new FileItemAdapter(App.getApp(), mFileCategory, this);
        switch (mFileCategory) {
            case Constant.TYPE.APP:
            case Constant.TYPE.PIC:
            case Constant.TYPE.VIDEO:
                mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
                mRecyclerView.addItemDecoration(new SpaceGridItemDecoration(Screen.dp(2)));
                break;
            default:
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat
                        .getDrawable(getContext(), R.drawable.list_divider_h), true));
                break;
        }
        mRecyclerView.setAdapter(mAdapter);
        loadData();
    }


    @Override
    public Loader<BaseLoader.Result> onCreateLoader(int id, Bundle args) {
        return new FileItemLoader(getContext(), mFileCategory);
    }

    @Override
    public void onLoadFinished(Loader<BaseLoader.Result> loader, BaseLoader.Result data) {
        if (!data.lists.isEmpty()) {
            mLoadingLayout.setStatus(LoadingLayout.Success);
            mAdapter.setDatas(data.lists);
        } else {
            mLoadingLayout.setStatus(LoadingLayout.Empty);
        }

    }

    @Override
    public void onLoaderReset(Loader<BaseLoader.Result> loader) {
        mAdapter.reset();
    }

    @Override
    public void onItemClick(View v) {
        FileItem item = (FileItem) v.getTag();
//        boolean isSelected = item.selected;
//        item.selected = !isSelected;
//        mAdapter.notifyDataSetChanged();
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
