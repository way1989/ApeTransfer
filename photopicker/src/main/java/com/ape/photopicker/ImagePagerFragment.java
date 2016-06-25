package com.ape.photopicker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;


/**
 * Created by chaochen on 2014-9-7.
 */
public class ImagePagerFragment extends Fragment {

    ViewGroup rootLayout;
    SubsamplingScaleImageView image;
    String uri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uri = getArguments().getString("uri");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.activity_image_pager_item, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootLayout = (ViewGroup) view.findViewById(R.id.rootLayout);
        showPhoto();
    }

    @Override
    public void onDestroyView() {
        if (image != null) {
        }
        super.onDestroyView();
    }

    private void showPhoto() {
        if (!isAdded()) {
            return;
        }

        image = new SubsamplingScaleImageView(getContext());
        rootLayout.addView(image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        image.setImage(ImageSource.uri(uri));
//      scaleImageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_OUTSIDE);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
