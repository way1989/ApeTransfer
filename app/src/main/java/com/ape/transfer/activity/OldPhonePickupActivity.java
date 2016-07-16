package com.ape.transfer.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ape.backuprestore.PersonalItemData;
import com.ape.backuprestore.modules.AppBackupComposer;
import com.ape.backuprestore.modules.CalendarBackupComposer;
import com.ape.backuprestore.modules.Composer;
import com.ape.backuprestore.modules.ContactBackupComposer;
import com.ape.backuprestore.modules.MmsBackupComposer;
import com.ape.backuprestore.modules.MusicBackupComposer;
import com.ape.backuprestore.modules.NoteBookBackupComposer;
import com.ape.backuprestore.modules.PictureBackupComposer;
import com.ape.backuprestore.modules.SmsBackupComposer;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.Utils;
import com.ape.transfer.R;
import com.ape.transfer.adapter.OldPhonePickupAdapter;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.TDevice;
import com.ape.transfer.widget.MobileDataWarningContainer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by android on 16-7-13.
 */
public class OldPhonePickupActivity extends BaseActivity implements OldPhonePickupAdapter.OnItemClickListener {
    private static final String TAG = "OldPhonePickupActivity";
    @BindView(R.id.mobile_data_warning)
    MobileDataWarningContainer mobileDataWarning;
    @BindView(R.id.rv_data_category)
    RecyclerView rvDataCategory;
    @BindView(R.id.storageView)
    TextView storageView;
    @BindView(R.id.btn_sure)
    Button btnSure;
    private List<String> mMessageEnable = new ArrayList<>();
    private OldPhonePickupAdapter mAdapter;
    private InitPersonalDataTask mInitDataTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_phone_pickup);
        ButterKnife.bind(this);
        if (TDevice.hasInternet()) {
            mobileDataWarning.setVisibility(View.VISIBLE);
        }
        mAdapter = new OldPhonePickupAdapter(getApplicationContext(), this);
        rvDataCategory.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        rvDataCategory.setAdapter(mAdapter);
        mInitDataTask = new InitPersonalDataTask();
        mInitDataTask.execute();
    }

    private int getModulesCount(Composer... composers) {
        int count = 0;
        for (Composer composer : composers) {
            if (composer.init()) {
                count += composer.getCount();
                composer.onEnd();
            }
        }
        Log.i(TAG, "getModulesCount : " + count);
        return count;
    }

    public void setButtonsEnable(boolean isEnabled) {
        btnSure.setEnabled(isEnabled);
    }

    private void updateData(ArrayList<PersonalItemData> mBackupDataList) {
        Log.i(TAG, "updateData... mBackupDataList.size = " + mBackupDataList.size());
        mAdapter.setDatas(mBackupDataList);
    }

    private void showLoadingContent(boolean isShow) {
    }

    @Override
    public void onItemClick(View v) {
    }

    private class InitPersonalDataTask extends AsyncTask<Void, Void, Long> {
        private static final String TASK_TAG = "InitPersonalDataTask";
        ArrayList<PersonalItemData> mBackupDataList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, TASK_TAG + "---onPreExecute");
            showLoadingContent(true);
            setTitle(R.string.backup_personal_data);
            setButtonsEnable(false);
        }

        @Override
        protected void onPostExecute(Long arg0) {
            showLoadingContent(false);
            setButtonsEnable(true);
            updateData(mBackupDataList);
            //setOnBackupStatusListener(mBackupListener);
            Log.i(TAG,
                    "---onPostExecute----getTitle " + OldPhonePickupActivity.this.getTitle());
            super.onPostExecute(arg0);
        }

        @Override
        protected Long doInBackground(Void... arg0) {
            try {
                mMessageEnable.clear();
                mBackupDataList = new ArrayList<>();
                int types[] = new int[]{
                        ModuleType.TYPE_CONTACT,
                        ModuleType.TYPE_MESSAGE,
                        ModuleType.TYPE_PICTURE,
                        ModuleType.TYPE_CALENDAR,
                        ModuleType.TYPE_MUSIC,
                        ModuleType.TYPE_APP
                };

                int num = types.length;
                boolean skipAppend;
                for (int i = 0; i < num; i++) {
                    skipAppend = false;
                    int count = 0;
                    Composer composer;
                    switch (types[i]) {
                        case ModuleType.TYPE_CONTACT:
                            count = getModulesCount(new ContactBackupComposer(
                                    OldPhonePickupActivity.this));
                            break;
                        case ModuleType.TYPE_MESSAGE:
                            int countSMS = 0;
                            int countMMS = 0;
                            composer = new SmsBackupComposer(OldPhonePickupActivity.this);
                            if (composer.init()) {
                                countSMS = composer.getCount();
                                composer.onEnd();
                            }
                            if (countSMS != 0) {
                                mMessageEnable.add(getString(R.string.message_sms));
                            }
                            composer = new MmsBackupComposer(OldPhonePickupActivity.this);
                            if (composer.init()) {
                                countMMS = composer.getCount();
                                composer.onEnd();
                            }
                            count = countSMS + countMMS;
                            Log.i(TAG, "countSMS = " + countSMS + ", countMMS = " + countMMS);
                            if (countMMS != 0) {
                                mMessageEnable.add(getString(R.string.message_mms));
                            }
                            break;
                        case ModuleType.TYPE_PICTURE:
                            count = getModulesCount(new PictureBackupComposer(
                                    OldPhonePickupActivity.this));
                            break;
                        case ModuleType.TYPE_CALENDAR:
                            count = getModulesCount(new CalendarBackupComposer(
                                    OldPhonePickupActivity.this));
                            break;
                        case ModuleType.TYPE_APP:
                            count = getModulesCount(new AppBackupComposer(
                                    OldPhonePickupActivity.this));
                            break;
                        case ModuleType.TYPE_MUSIC:
                            count = getModulesCount(new MusicBackupComposer(
                                    OldPhonePickupActivity.this));

                            break;
                        case ModuleType.TYPE_NOTEBOOK:
                            count = getModulesCount(new NoteBookBackupComposer(
                                    OldPhonePickupActivity.this));
                            break;
//                    case ModuleType.TYPE_BOOKMARK:
//                        count = getModulesCount(new BookmarkBackupComposer(
//                                OldPhonePickupActivity.this));
//                        break;
                        default:
                            Log.i(TAG, "Unknown module type: " + types[i]);
                            break;
                    }
                    composer = null;
                    PersonalItemData item = new PersonalItemData(types[i], count);
                    if (!skipAppend) {
                        Log.i(TAG, "Add module type: " + types[i]);
                        mBackupDataList.add(item);
                    } else {
                        Log.i(TAG, "Skip module type: " + types[i]);
                    }
                }
            } catch (java.lang.SecurityException e) {
                Log.i(TAG, "Permission not satisified");
                Utils.exitLockTaskModeIfNeeded(OldPhonePickupActivity.this);
                finish();
            }

            return null;
        }
    }
}
