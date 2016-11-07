package com.ape.transfer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.ape.transfer.adapter.HistoryAdapter;
import com.ape.transfer.fragment.loader.BaseLoader;
import com.ape.transfer.fragment.loader.TaskLoader;
import com.ape.transfer.model.HistoryTransfer;
import com.ape.transfer.model.TransferEvent;
import com.ape.transfer.model.TransferTaskStartEvent;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.RxBus;
import com.trello.rxlifecycle.android.FragmentEvent;
import com.trello.rxlifecycle.components.support.RxFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by android on 16-7-5.
 */
public class HistoryFragment extends RxFragment implements LoaderManager.LoaderCallbacks<BaseLoader.Result> {
    private static final String TAG = "HistoryFragment";
    private static final String ARG_DIRECTION = "direction";
    private static final int LOAD_ID = 0x001;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDirection = getArguments().getInt(ARG_DIRECTION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, rootView);
        RxBus.getInstance().toObservable(TransferEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<TransferEvent>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<TransferEvent>() {
                    @Override
                    public void call(TransferEvent event) {
                        //do some thing
                        onTransferChange(event);
                    }
                });
        RxBus.getInstance().toObservable(TransferTaskStartEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<TransferTaskStartEvent>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<TransferTaskStartEvent>() {
                    @Override
                    public void call(TransferTaskStartEvent event) {
                        //do some thing
                        if (event.getDirection() == mDirection) {
                            getLoaderManager().restartLoader(LOAD_ID, null, HistoryFragment.this);
                        }
                    }
                });
        return rootView;
    }


    public void onTransferChange(TransferEvent event) {
        Log.i(TAG, "onEventMainThread收到了消息：" + event.getTransferFile());
        mAdapter.updateItem(new HistoryTransfer(event.getPeer(), event.getTransferFile()));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new HistoryAdapter(getContext(), mDirection);
        historyList.setLayoutManager(new LinearLayoutManager(getContext()));
        historyList.setAdapter(mAdapter);
        getLoaderManager().initLoader(LOAD_ID, null, this);
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
            mAdapter.setData(data.lists);
        } else {
            historyList.setVisibility(View.INVISIBLE);
            rlEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<BaseLoader.Result> loader) {
        mAdapter.reset();
    }
}
