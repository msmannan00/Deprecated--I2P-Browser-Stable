package com.darkweb.appManager.settingManager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.darkweb.constants.status;
import com.darkweb.appManager.activityContextManager;
import com.darkweb.appManager.homeManager.homeController;
import com.darkweb.constants.constants;
import com.darkweb.constants.enums;
import com.darkweb.constants.keys;
import com.darkweb.constants.strings;
import com.darkweb.dataManager.dataController;
import com.darkweb.helperManager.eventObserver;
import com.darkweb.pluginManager.pluginController;
import com.example.myapplication.R;
import java.util.List;

public class settingController extends AppCompatActivity
{
    /*Private Observer Classes*/

    private homeController mHomeController;
    private settingViewController mSettingViewController;
    private settingModel mSettingModel;

    /*Private Variables*/

    private Spinner mSearch;
    private Spinner mJavascript;
    private Spinner mHistory;
    private Spinner mCookies;
    private Spinner mFontAdjustable;
    private Spinner mNotification;
    private SeekBar mFontSize;
    private TextView mFontSizePercentage;

    /*Initializations*/

    public settingController(){
        mHomeController = activityContextManager.getInstance().getHomeController();
        mSettingModel = new settingModel(new settingModelCallback());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view);

        viewsInitializations();
        modelInitialization();
        listenersInitializations();
        initializeFontSizeListener();
    }

    public void modelInitialization(){
        mSettingModel.setJavaStatus(status.sJavaStatus);
        mSettingModel.setHistoryStatus(status.sHistoryStatus);
        mSettingModel.setSearchStatus(status.sSearchStatus);
        mSettingModel.setAdjustableStatus(status.sFontAdjustable);
        mSettingModel.setFontSize(status.sFontSize);
    }

    public void viewsInitializations()
    {
        mSearch = findViewById(R.id.search_manager);
        mJavascript = findViewById(R.id.javascript_manager);
        mHistory = findViewById(R.id.history_manager);
        mFontSize = findViewById(R.id.font_size);
        mFontAdjustable = findViewById(R.id.font_adjustable);
        mFontSizePercentage = findViewById(R.id.font_size_percentage);
        mCookies = findViewById(R.id.cookies_manager);
        mNotification = findViewById(R.id.notification_manager);

        String currentSearchEngine = dataController.getInstance().getString(keys.SEARCH_ENGINE, strings.DARK_WEB);
        mSettingViewController = new settingViewController(mSearch, mJavascript, mHistory, mFontSize, mFontAdjustable, mFontSizePercentage,this, new settingModelCallback(),this, mCookies,mNotification);
    }

    public void listenersInitializations()
    {
        initializeItemSelectedListener(mSearch);
        initializeItemSelectedListener(mJavascript);
        initializeItemSelectedListener(mNotification);
        initializeItemSelectedListener(mHistory);
        initializeItemSelectedListener(mFontAdjustable);
        initializeItemSelectedListener(mCookies);
        pluginController.getInstance().logEvent(strings.SETTINGS_OPENED);
    }

    /*Event Handlers*/

    @Override
    public void onTrimMemory(int level)
    {
        if(status.sIsAppPaused && (level==80 || level==15))
        {
            dataController.getInstance().setBool(keys.LOW_MEMORY,true);
            finish();
        }
    }

    @Override
    public void onResume()
    {
        status.sIsAppPaused = false;
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }


    @Override
    public void onBackPressed(){
        mSettingModel.onCloseView();
    }

    public void initializeItemSelectedListener(Spinner view){
        view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(parentView.getId()== R.id.search_manager)
                {
                    mSettingModel.setSearchStatus(getEngineURL(position));
                }
                else if(parentView.getId()== R.id.javascript_manager)
                {
                    mSettingModel.setJavaStatus(position==0);
                }
                else if(parentView.getId()== R.id.history_manager)
                {
                    mSettingModel.setHistoryStatus(position==0);
                }
                else if(parentView.getId()== R.id.font_adjustable)
                {
                    mSettingModel.setAdjustableStatus(position==0);
                }
                else if(parentView.getId()== R.id.cookies_manager)
                {
                    mSettingModel.setCookieStatus(position);
                }
                else if(parentView.getId()== R.id.notification_manager)
                {
                    mSettingModel.setmNotificationStatus(position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    public void initializeFontSizeListener(){

        mFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float cur_progress = seekBar.getProgress();
                float progress = (cur_progress);
                mSettingModel.setFontSize(progress);
                mSettingViewController.updatePercentage(mFontSize.getProgress());
                if(cur_progress<1){
                    mFontSize.setProgress(1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void onNavigationBackPressed(View view){
        mSettingModel.onCloseView();
    }

    /*Event Observer*/

    public class settingViewCallback implements eventObserver.eventListener{

        @Override
        public void invokeObserver(List<Object> data, enums.etype e_type)
        {

        }
    }

    public class settingModelCallback implements eventObserver.eventListener{

        @Override
        public void invokeObserver(List<Object> data, enums.etype e_type)
        {
            if(e_type == enums.etype.update_searcn){
                status.sSearchStatus = (String)data.get(0);
                mHomeController.onHomeButton(null);
                dataController.getInstance().setString(keys.SEARCH_ENGINE, mSettingModel.getSearchStatus());
            }
            else if(e_type == enums.etype.update_javascript){
                status.sJavaStatus = (boolean)data.get(0);
                mHomeController.onUpdateJavascript();
                dataController.getInstance().setBool(keys.JAVA_SCRIPT, status.sJavaStatus);
            }
            else if(e_type == enums.etype.update_history){
                status.sHistoryStatus = (boolean)data.get(0);
                dataController.getInstance().setBool(keys.HISTORY_CLEAR, status.sHistoryStatus);
            }
            else if(e_type == enums.etype.update_notification){
                status.sNotificationStatus = (int)data.get(0);
                dataController.getInstance().setInt(keys.NOTIFICATION_STATUS, status.sNotificationStatus);

                if(status.sNotificationStatus==0){
                    pluginController.getInstance().enableTorNotification();
                } else if(status.sNotificationStatus==1){
                    pluginController.getInstance().disableTorNotification();
                }else {
                    pluginController.getInstance().enableTorNotificationNoBandwidth();
                }

            }
            else if(e_type == enums.etype.update_font_adjustable || e_type == enums.etype.update_font_size){
                mHomeController.onLoadFont();
            }
            else if(e_type == enums.etype.update_cookies){
                status.sCookieStatus = (int)data.get(0);
                dataController.getInstance().setInt(keys.COOKIE_ADJUSTABLE, status.sCookieStatus);
                mHomeController.onUpdateCookies();
            }
            else if(e_type == enums.etype.close_view){
                finish();
            }
        }
    }

    /*Helper Methods*/

    public String getEngineURL(int index){

        if (index == 0)
        {
            return constants.BACKEND_GENESIS_URL;
        }
        else if (index == 1)
        {
            return constants.BACKEND_GOOGLE_URL;
        }
        else
        {
            return constants.BACKEND_DUCK_DUCK_GO_URL;
        }
    }

}