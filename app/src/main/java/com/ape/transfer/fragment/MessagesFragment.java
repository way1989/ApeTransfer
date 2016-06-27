package com.ape.transfer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ape.transfer.R;
import com.ape.transfer.widget.ChatRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by android on 16-6-26.
 */
public class MessagesFragment extends Fragment {
    @BindView(R.id.chatBackgroundView)
    ImageView chatBackgroundView;
    @BindView(R.id.collection)
    ChatRecyclerView collection;

    public static MessagesFragment newInstance() {
        MessagesFragment fragment = new MessagesFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        ButterKnife.bind(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
