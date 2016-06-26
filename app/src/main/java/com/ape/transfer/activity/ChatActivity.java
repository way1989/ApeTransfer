package com.ape.transfer.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ape.emoji.keyboard.KeyboardStatusListener;
import com.ape.emoji.keyboard.emoji.EmojiKeyboard;
import com.ape.filepicker.Intents;
import com.ape.photopicker.ImageInfo;
import com.ape.photopicker.PhotoPickActivity;
import com.ape.transfer.R;
import com.ape.transfer.util.Files;
import com.ape.transfer.util.KeyboardHelper;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.Screen;
import com.ape.transfer.util.ViewUtils;
import com.ape.transfer.util.audio.VoiceCaptureActor;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends BaseActivity implements TextWatcher {

    private static final int RESULT_REQUEST_PICK_FILE = 1002;
    private static final int RESULT_REQUEST_PICK_PHOTO = 1003;
    private static final String TAG = "ChatActivity";
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 7;
    protected EmojiKeyboard emojiKeyboard;
    @BindView(R.id.et_message)
    EditText etMessage;
    @BindView(R.id.ib_attach)
    ImageView ibAttach;
    @BindView(R.id.attachAnchor)
    View attachAnchor;
    @BindView(R.id.ib_emoji)
    ImageView ibEmoji;
    @BindView(R.id.ib_send)
    ImageView ibSend;
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
    @BindView(R.id.messagesFragment)
    FrameLayout messagesFragment;
    @BindView(R.id.share_container)
    LinearLayout shareContainer;
    private KeyboardHelper mKeyboardHelper;
    private int SLIDE_LIMIT = (int) (Screen.getDensity() * 180);
    private boolean isAudioVisible;
    private int slideStart;
    private String audioFile;
    private VoiceCaptureActor voiceCaptureActor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        mKeyboardHelper = new KeyboardHelper(getApplicationContext());
        emojiKeyboard = new EmojiKeyboard(this);
        emojiKeyboard.setKeyboardStatusListener(new KeyboardStatusListener() {

            @Override
            public void onDismiss() {
                ibEmoji.setImageResource(R.drawable.ic_emoji);
            }

            @Override
            public void onShow() {
                shareContainer.setVisibility(View.GONE);
                ibEmoji.setImageResource(R.drawable.ic_emoji_checked);
            }

        });
        etMessage.addTextChangedListener(this);
        recordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isAudioVisible) {
                            showAudio();
                            slideStart = (int) event.getX();
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (isAudioVisible) {
                            hideAudio(false);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isAudioVisible) {
                            int slide = slideStart - (int) event.getX();
                            if (slide < 0) {
                                slide = 0;
                            }
                            if (slide > SLIDE_LIMIT) {
                                hideAudio(true);
                            } else {
                                slideAudio(slide);
                            }

                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @OnClick({R.id.ib_attach, R.id.ib_emoji, R.id.ib_send, R.id.share_gallery, R.id.share_file})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_attach:
                if (shareContainer.getVisibility() == View.GONE) {
                    mKeyboardHelper.setImeVisibility(etMessage, false);
                    if (emojiKeyboard.isShowing()) emojiKeyboard.dismiss();
                    shareContainer.setVisibility(View.VISIBLE);
                } else {
                    shareContainer.setVisibility(View.GONE);
                    if (emojiKeyboard.isShowing()) emojiKeyboard.dismiss();
                    mKeyboardHelper.setImeVisibility(etMessage, true);
                }
                break;
            case R.id.ib_emoji:
                emojiKeyboard.toggle(etMessage);
                break;
            case R.id.ib_send:
                break;
            case R.id.share_gallery:
                photo();
                break;
            case R.id.share_file:
                startActivityForResult(Intents.pickFile(this), RESULT_REQUEST_PICK_FILE);
                break;
            default:
                break;
        }
    }

    public void photo() {
        Intent intent = new Intent(this, PhotoPickActivity.class);
        startActivityForResult(intent, RESULT_REQUEST_PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_REQUEST_PICK_FILE) {
                ArrayList<String> selectedItems = data.getStringArrayListExtra("picked");
                for (String file : selectedItems) {
                    Log.i(TAG, "selected file = " + file);
                }
            } else if (requestCode == RESULT_REQUEST_PICK_PHOTO) {
                ArrayList<ImageInfo> pickPhots = (ArrayList<ImageInfo>) data.getSerializableExtra("data");
                for (ImageInfo item : pickPhots) {
                    Uri uri = Uri.parse(item.path);
                    Log.i(TAG, "selected photo = " + item.path);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Destroy emoji keyboard
        emojiKeyboard.destroy();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        checkSendButton(s.length() > 0);
    }

    public void checkSendButton() {
        checkSendButton(etMessage.getText().length() > 0);
    }

    public void checkSendButton(boolean hasText) {
        if (hasText) {
            ibSend.setEnabled(true);
            ViewUtils.zoomInView(ibSend);
            ViewUtils.zoomOutView(recordBtn);
        } else {
            ibSend.setEnabled(false);
            ViewUtils.zoomInView(recordBtn);
            ViewUtils.zoomOutView(ibSend);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        voiceCaptureActor = new VoiceCaptureActor(getApplicationContext(), new VoiceCaptureActor.VoiceCaptureCallback() {
            @Override
            public void onRecordProgress(final long time) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        audioTimer.setText(formatDuration((int) (time / 1000)));
                    }
                });
            }

            @Override
            public void onRecordCrash() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideAudio(true);
                    }
                });
            }

            @Override
            public void onRecordStop(long time) {
                if (time < 1200) {
                    //Cancel
                } else {
                    //send message
                    //messenger().sendVoice(peer, (int) progress, audioFile);
                }
            }
        });
    }

    public String formatDuration(int duration) {
        if (duration < 60) {
            return formatTwoDigit(0) + ":" + formatTwoDigit(duration);
        } else if (duration < 60 * 60) {
            return formatTwoDigit(duration / 60) + ":" + formatTwoDigit(duration % 60);
        } else {
            return formatTwoDigit(duration / 3600) + ":" + formatTwoDigit(duration / 60) + ":" + formatTwoDigit(duration % 60);
        }
    }

    public String formatTwoDigit(int v) {
        if (v < 0) {
            return "00";
        } else if (v < 10) {
            return "0" + v;
        } else if (v < 100) {
            return "" + v;
        } else {
            String res = "" + v;
            return res.substring(res.length() - 2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void slideAudio(int value) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(audioSlide, "translationX", audioSlide.getX(), -value);
        oa.setDuration(0);
        oa.start();
    }

    private void showAudio() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permissions", "recordAudio - no permission :c");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
            return;
        }

        if (isAudioVisible) {
            return;
        }
        isAudioVisible = true;

        ViewUtils.hideView(ibAttach);
        ViewUtils.hideView(etMessage);
        ViewUtils.hideView(ibEmoji);
        ViewUtils.hideView(sendContainer);

        audioFile = Files.getInternalTempFile("voice_msg", ".amr");


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        long id = VoiceCaptureActor.LAST_ID.incrementAndGet();
        voiceCaptureActor.onStartMessage(audioFile);

        slideAudio(0);
        audioTimer.setText("00:00");

        TranslateAnimation animation = new TranslateAnimation(Screen.getWidth(), 0, 0, 0);
        animation.setDuration(160);
        audioContainer.clearAnimation();
        audioContainer.setAnimation(animation);
        audioContainer.animate();
        audioContainer.setVisibility(View.VISIBLE);


        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0.2f);
        alphaAnimation.setDuration(800);
        alphaAnimation.setRepeatMode(AlphaAnimation.REVERSE);
        alphaAnimation.setRepeatCount(AlphaAnimation.INFINITE);
        recordPoint.clearAnimation();
        recordPoint.setAnimation(alphaAnimation);
        recordPoint.animate();
    }

    private void hideAudio(boolean cancel) {
        if (!isAudioVisible) {
            return;
        }
        isAudioVisible = false;

        ViewUtils.showView(ibAttach);
        ViewUtils.showView(etMessage);
        ViewUtils.showView(ibEmoji);
        ViewUtils.showView(sendContainer);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        voiceCaptureActor.onStopMessage(cancel);
        TranslateAnimation animation = new TranslateAnimation(0, Screen.getWidth(), 0, 0);
        animation.setDuration(160);
        audioContainer.clearAnimation();
        audioContainer.setAnimation(animation);
        audioContainer.animate();
        audioContainer.setVisibility(View.GONE);
        etMessage.requestFocus();

    }
}
