package com.ape.transfer.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ape.transfer.R;
import com.ape.transfer.util.PreferenceUtil;

import java.nio.charset.Charset;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserInfoActivity extends BaseActivity implements TextWatcher {
    public static final int[] HEAD = {R.drawable.head_default0, R.drawable.head_default1,
            R.drawable.head_default2, R.drawable.head_default3};
    @BindView(R.id.iv_avatar)
    ImageView ivAvatar;
    @BindView(R.id.iv_edit)
    ImageView ivEdit;
    @BindView(R.id.ll_head0)
    RelativeLayout llHead0;
    @BindView(R.id.ll_head1)
    RelativeLayout llHead1;
    @BindView(R.id.ll_head2)
    RelativeLayout llHead2;
    @BindView(R.id.ll_head3)
    RelativeLayout llHead3;
    @BindView(R.id.editText)
    TextInputEditText editText;

    private int mCurrentHead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ivEdit.setVisibility(View.GONE);
        editText.addTextChangedListener(this);
        editText.setText(Build.MODEL);
        mCurrentHead = PreferenceUtil.getInstance(getApplicationContext()).getHead();
        ivAvatar.setImageResource(HEAD[mCurrentHead]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem saveItem = menu.findItem(R.id.action_save);
        String alias = editText.getText().toString();
        saveItem.setEnabled(isValidate(alias));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
                preferenceUtil.setAlias(editText.getText().toString());
                preferenceUtil.setHead(mCurrentHead);
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_user_info;
    }

    @OnClick({R.id.iv_edit, R.id.ll_head0, R.id.ll_head1, R.id.ll_head2, R.id.ll_head3})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_edit:
                //Todo
                break;
            case R.id.ll_head0:
                selectHead(0);
                break;
            case R.id.ll_head1:
                selectHead(1);
                break;
            case R.id.ll_head2:
                selectHead(2);
                break;
            case R.id.ll_head3:
                selectHead(3);
                break;
        }
    }

    private void selectHead(int position) {
        mCurrentHead = position;
        ivAvatar.setImageResource(HEAD[position]);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        validate();
    }

    private boolean isValidate(String alias) {
        return !TextUtils.isEmpty(alias) &&
                Charset.forName("UTF-8").encode(alias).limit() <= 16;
    }

    private void validate() {
        String alias = editText.getText().toString();
        if (TextUtils.isEmpty(alias)) {
            editText.setError(getString(R.string.hint_input_username_empty));
        } else if (!isValidate(alias)) {
            editText.setError(getString(R.string.hint_input_username));
        }
        invalidateOptionsMenu();
    }

}
