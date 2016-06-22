package com.ape.transfer.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ape.emoji.keyboard.KeyboardStatusListener;
import com.ape.emoji.keyboard.emoji.EmojiKeyboard;
import com.ape.transfer.R;
import com.ape.transfer.widget.RecyclerListView;
import com.ape.transfer.widget.TintImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends BaseActivity {

    @BindView(R.id.mentionsList)
    RecyclerListView mentionsList;
    @BindView(R.id.et_message)
    EditText etMessage;
    @BindView(R.id.ib_attach)
    ImageView ibAttach;
    @BindView(R.id.attachAnchor)
    View attachAnchor;
    @BindView(R.id.ib_emoji)
    ImageView ibEmoji;
    @BindView(R.id.ib_send)
    TintImageView ibSend;
    @BindView(R.id.sendContainer)
    FrameLayout sendContainer;
    @BindView(R.id.record_btn)
    ImageView recordBtn;
    @BindView(R.id.record_point)
    ImageView recordPoint;
    @BindView(R.id.audioTimer)
    TextView audioTimer;
    @BindView(R.id.audioSlide)
    TextView audioSlide;
    @BindView(R.id.audioContainer)
    LinearLayout audioContainer;
    @BindView(R.id.fl_send_panel)
    FrameLayout flSendPanel;
    protected EmojiKeyboard emojiKeyboard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        emojiKeyboard = new EmojiKeyboard(this);
        emojiKeyboard.setKeyboardStatusListener(new KeyboardStatusListener() {

            @Override
            public void onDismiss() {
                ibEmoji.setImageResource(R.drawable.ic_emoji);
            }

            @Override
            public void onShow() {
                ibEmoji.setImageResource(R.drawable.ic_emoji_checked);
            }

        });
    }

    @OnClick({R.id.ib_attach, R.id.ib_emoji, R.id.ib_send, R.id.record_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_attach:
                break;
            case R.id.ib_emoji:
                emojiKeyboard.toggle(etMessage);
                break;
            case R.id.ib_send:
                break;
            case R.id.record_btn:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Destroy emoji keyboard
        emojiKeyboard.destroy();
    }

}
