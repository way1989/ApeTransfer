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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.adapter.FileItemAdapter;
import com.ape.transfer.adapter.HistoryAdapter;
import com.ape.transfer.fragment.loader.BaseLoader;
import com.ape.transfer.fragment.loader.TaskLoader;
import com.ape.transfer.model.FileEvent;
import com.ape.transfer.model.FileItem;
import com.ape.transfer.model.P2PFileInfoEvent;
import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by android on 16-7-5.
 */
public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<BaseLoader.Result>,
        HistoryAdapter.OnItemClickListener {
    private static final String ARG_DIRECTION = "direction";
    @BindView(R.id.history_list)
    RecyclerView historyList;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.rl_empty)
    RelativeLayout rlEmpty;
    private int mDirection;
    private HistoryAdapter mAdapter;
    public HistoryFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDirection = getArguments().getInt(ARG_DIRECTION);
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static HistoryFragment newInstance(int sectionNumber) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DIRECTION, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, rootView);
        EventBus.getDefault().register(this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private static final String TAG = "HistoryFragment";
    @Subscribe
    public void onEventMainThread(P2PFileInfoEvent event) {
        Log.i(TAG, "onEventMainThread收到了消息：" + event.getMsg());
        P2PFileInfo fileInfo = event.getMsg();
        mAdapter.updateItem(fileInfo);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new HistoryAdapter(getContext(), mDirection, this);
        historyList.setLayoutManager(new LinearLayoutManager(getContext()));
        historyList.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
        rlEmpty.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<BaseLoader.Result> onCreateLoader(int id, Bundle args) {
        return new TaskLoader(getContext(), mDirection);
    }

    @Override
    public void onLoadFinished(Loader<BaseLoader.Result> loader, BaseLoader.Result data) {
        if (!data.lists.isEmpty()) {
            rlEmpty.setVisibility(View.INVISIBLE);
            historyList.setVisibility(View.VISIBLE);
            mAdapter.setDatas(data.lists);
        } else {
            historyList.setVisibility(View.INVISIBLE);
            rlEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<BaseLoader.Result> loader) {
        mAdapter.reset();
    }

    @Override
    public void onItemClick(View v) {

    }
}
