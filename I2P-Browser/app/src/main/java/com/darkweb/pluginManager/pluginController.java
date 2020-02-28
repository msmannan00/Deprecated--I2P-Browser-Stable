package com.darkweb.pluginManager;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.darkweb.appManager.activityContextManager;
import com.darkweb.appManager.homeManager.homeController;
import com.darkweb.constants.constants;
import com.darkweb.constants.enums;
import com.darkweb.constants.keys;
import com.darkweb.constants.status;
import com.darkweb.constants.strings;
import com.darkweb.dataManager.dataController;
import com.darkweb.helperManager.eventObserver;
import com.darkweb.helperManager.helperMethod;

import net.i2p.android.I2PActivity;
import net.i2p.android.util.LocaleManager;

import java.util.List;
import java.util.Locale;

public class pluginController
{
    /*Plugin Instance*/

    private adManager mAdManager;
    private analyticManager mAnalyticManager;
    private fabricManager mFabricManager;
    private firebaseManager mFirebaseManager;
    private langManager mLangManager;
    private messageManager mMessageManager;
    private activityContextManager mContextManager;
    private boolean mIsInitialized = false;

    /*Private Variables*/

    private static pluginController ourInstance = new pluginController();
    private homeController mHomeController;

    /*Initializations*/

    public static pluginController getInstance()
    {
        return ourInstance;
    }

    public void initialize(){
        instanceObjectInitialization();
        mIsInitialized = true;
    }

    public void  preInitialize(homeController context){
        mLangManager = new langManager(context,new langCallback());
        mLangManager.setDefaultLanguage(new Locale(status.sLanguage));

        mFabricManager = new fabricManager(context,new fabricCallback());
    }

    private void instanceObjectInitialization()
    {
        mHomeController = activityContextManager.getInstance().getHomeController();
        mContextManager = activityContextManager.getInstance();

        mAdManager = new adManager(getAppContext(),new admobCallback(), mHomeController.getBannerAd());
        mAnalyticManager = new analyticManager(getAppContext(),new analyticCallback());
        mFirebaseManager = new firebaseManager(getAppContext(),new firebaseCallback());
        mMessageManager = new messageManager(new messageCallback());
    }

    /*Helper Methods*/

    private AppCompatActivity getAppContext()
    {
        return mHomeController;
    }

    public boolean isInitialized(){
        return mIsInitialized;
    }

    /*---------------------------------------------- EXTERNAL REQUEST LISTENER-------------------------------------------------------*/

    /*Message Manager*/
    public void MessageManagerHandler(AppCompatActivity app_context, List<String> data, enums.etype type){
        mMessageManager.createMessage(app_context,data,type);
    }
    public void MessageManagerHandlerI2P(Context app_context, enums.etype type){
        mMessageManager.createMessage(app_context,null,type);
    }
    public void onResetMessage(){
        if(mMessageManager!=null){
            mMessageManager.onFinish();
        }
    }

    /*Lang Manager*/
    public void setLanguage(AppCompatActivity context){
        mLangManager.setDefaultLanguage(new Locale(status.sLanguage));
    }
    public void onCreate(Activity activity) {
        mLangManager.onCreate(activity);
    }
    public void onResume(Activity activity) {
        mLangManager.onResume(activity);
    }

    /*Firebase Manager*/
    public void logEvent(String value){
        if(mFirebaseManager!=null){
            mFirebaseManager.logEvent(value);
        }
    }

    /*Ad Manager*/
    public void initializeBannerAds(){
        mAdManager.loadAds();
    }
    public boolean isAdvertLoaded(){
       return mAdManager.isAdvertLoaded();
    }

    /*Onion Proxy Manager*/
    public void initializeOrbot(){
        I2PManager.getInstance().startOrbot(getAppContext());
    }
    public void setProxy(String url){
        I2PManager.getInstance().setProxy(url);
    }
    public void enableTorNotification(){
        I2PManager.getInstance().enableTorNotification();
    }
    public void disableTorNotification(){
        I2PManager.getInstance().disableTorNotification();
    }
    public void enableTorNotificationNoBandwidth(){
        I2PManager.getInstance().enableTorNotificationNoBandwidth();
    }

    /*------------------------------------------------ CALLBACK LISTENERS------------------------------------------------------------*/

    /*Ad Manager*/
    private class admobCallback implements eventObserver.eventListener{
        @Override
        public void invokeObserver(List<Object> data, enums.etype event_type)
        {
            mHomeController.onSetBannerAdMargin();
        }
    }

    /*Analytics Manager*/
    private class analyticCallback implements eventObserver.eventListener{
        @Override
        public void invokeObserver(List<Object> data, enums.etype event_type)
        {
            mAnalyticManager.logUser();
        }
    }

    /*Fabric Manager*/
    private class fabricCallback implements eventObserver.eventListener{
        @Override
        public void invokeObserver(List<Object> data, enums.etype event_type)
        {
        }
    }

    /*Firebase Manager*/
    private class firebaseCallback implements eventObserver.eventListener{
        @Override
        public void invokeObserver(List<Object> data, enums.etype event_type)
        {
        }
    }

    /*Lang Manager*/
    private class langCallback implements eventObserver.eventListener{
        @Override
        public void invokeObserver(List<Object> data, enums.etype event_type)
        {
        }
    }

    /*Message Manager*/
    private class messageCallback implements eventObserver.eventListener{
        @Override
        public void invokeObserver(List<Object> data, enums.etype event_type)
        {
            if(event_type.equals(enums.etype.language_change))
            {
                status.sLanguage = data.get(0).toString();
                dataController.getInstance().setString(keys.LANGUAGE,data.get(0).toString());

                mLangManager.setDefaultLanguage(new Locale(data.get(0).toString()));
                mHomeController.onRestartApplication();
            }
            else if(event_type.equals(enums.etype.cancel_welcome)){
                dataController.getInstance().setBool(keys.IS_WELCOME_ENABLED,false);
            }
            else if(event_type.equals(enums.etype.clear_history)){
                dataController.getInstance().clearHistory();
                mContextManager.getHistoryController().onclearData();
                mHomeController.onClearSession();
                dataController.getInstance().clearTabs();
                mHomeController.initTab();
            }
            else if(event_type.equals(enums.etype.clear_bookmark)){
                dataController.getInstance().clearBookmark();
                mContextManager.getBookmarkController().onclearData();
            }
            else if(event_type.equals(enums.etype.bookmark)){
                String [] dataParser = data.get(0).toString().split("split");
                if(dataParser.length>1){
                    logEvent(strings.URL_BOOKMARKED);
                    dataController.getInstance().addBookmark(dataParser[0],dataParser[1]);
                }else {
                    dataController.getInstance().addBookmark(dataParser[0],"");
                }
            }
            else if(event_type.equals(enums.etype.app_rated)){
                dataController.getInstance().setBool(keys.IS_APP_RATED,true);
            }
            else if(event_type.equals(enums.etype.download_file)){
                mHomeController.onDownloadFile();
            }
            else if(event_type.equals(enums.etype.download_file_manual)){
                mHomeController.onManualDownload(data.get(0).toString());
            }
            else if(event_type.equals(enums.etype.open_link_new_tab)){
                mHomeController.onOpenLinkNewTab(data.get(0).toString());
            }
            else if(event_type.equals(enums.etype.open_link_current_tab)){
                mHomeController.onLoadURL(data.get(0).toString());
            }
            else if(event_type.equals(enums.etype.copy_link)){
                helperMethod.copyURL(data.get(0).toString(),mContextManager.getHomeController());
            }
            else if(event_type.equals(enums.etype.clear_tab)){
                dataController.getInstance().clearTabs();
                mHomeController.initTab();
                activityContextManager.getInstance().getTabController().finish();
            }
        }
    }
}
