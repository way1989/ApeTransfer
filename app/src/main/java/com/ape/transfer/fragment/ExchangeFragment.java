package com.ape.transfer.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ape.transfer.R;
import com.ape.transfer.activity.InviteFriendActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExchangeFragment extends Fragment {


    public ExchangeFragment() {
        // Required empty public constructor
    }

    public static ExchangeFragment newInstance() {
        ExchangeFragment fragment = new ExchangeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exchange, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.rl_invite, R.id.mainSendBtn, R.id.mainReceiveBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_invite:
                startActivity(new Intent(getActivity(), InviteFriendActivity.class));
                break;
            case R.id.mainSendBtn:
                break;
            case R.id.mainReceiveBtn:
                break;
        }
    }
}
