package com.darkweb.pluginManager;

import android.content.Context;
import com.darkweb.constants.constants;
import com.darkweb.constants.keys;
import com.darkweb.constants.status;
import net.i2p.android.router.service.StatusBar;
import org.mozilla.gecko.PrefsHelper;

class I2PManager
{

    /*Private Variables*/

    private Context mAppContext;

    /*Initialization*/
    private static I2PManager sOurInstance = new I2PManager();
    public static I2PManager getInstance()
    {
        return sOurInstance;
    }

    public void initialize(){
    }

    void startOrbot(Context context){
        this.mAppContext = context;
        initializeProxy();
    }

    void enableTorNotification(){
        StatusBar.toogleNotification(0);
    }
    void disableTorNotification(){
        StatusBar.toogleNotification(1);
    }
    void enableTorNotificationNoBandwidth(){
        StatusBar.toogleNotification(2);
    }

    /*------------------------------------------------------- POST TASK HANDLER -------------------------------------------------------*/

    void setProxy(String url){
        /*if(url.contains("boogle.store")){
            PrefsHelper.setPref(keys.PROXY_TYPE, 0);
            PrefsHelper.setPref(keys.PROXY_SOCKS,null);
            PrefsHelper.setPref(keys.PROXY_SOCKS_PORT, null);
            PrefsHelper.setPref(keys.PROXY_SOCKS_VERSION,null);
            PrefsHelper.setPref(keys.PROXY_SOCKS_REMOTE_DNS,null);
        }
        else {
            PrefsHelper.setPref(keys.PROXY_TYPE, 1);
            PrefsHelper.setPref(keys.PROXY_SOCKS,constants.PROXY_SOCKS);
            PrefsHelper.setPref(keys.PROXY_SOCKS_PORT, 9050);
            PrefsHelper.setPref(keys.PROXY_SOCKS_VERSION,constants.PROXY_SOCKS_VERSION);
            PrefsHelper.setPref(keys.PROXY_SOCKS_REMOTE_DNS,constants.PROXY_SOCKS_REMOTE_DNS);
        }*/
    }

    private void initializeProxy()
    {
        PrefsHelper.setPref(keys.PROXY_CACHE,constants.PROXY_CACHE);
        PrefsHelper.setPref(keys.PROXY_MEMORY,constants.PROXY_MEMORY);
        PrefsHelper.setPref(keys.PROXY_DO_NOT_TRACK_HEADER_ENABLED,constants.PROXY_DO_NOT_TRACK_HEADER_ENABLED);
        PrefsHelper.setPref(keys.PROXY_DO_NOT_TRACK_HEADER_VALUE,constants.PROXY_DO_NOT_TRACK_HEADER_VALUE);

        PrefsHelper.setPref("browser.cache.disk.enable",true);
        PrefsHelper.setPref("browser.cache.memory.enable",true);
        PrefsHelper.setPref("browser.cache.disk.capacity",1000);

        setPrivacyPrefs();
    }

    private void setPrivacyPrefs ()
    {
        PrefsHelper.setPref("browser.cache.disk.enable",false);
        PrefsHelper.setPref("browser.cache.memory.enable",true);
        PrefsHelper.setPref("browser.cache.disk.capacity",0);
        PrefsHelper.setPref("privacy.resistFingerprinting",true);
        PrefsHelper.setPref("privacy.clearOnShutdown.cache",status.sHistoryStatus);
        PrefsHelper.setPref("privacy.clearOnShutdown.downloads",status.sHistoryStatus);
        PrefsHelper.setPref("privacy.clearOnShutdown.formdata",status.sHistoryStatus);
        PrefsHelper.setPref("privacy.clearOnShutdown.history",status.sHistoryStatus);
        PrefsHelper.setPref("privacy.clearOnShutdown.offlineApps",status.sHistoryStatus);
        PrefsHelper.setPref("privacy.clearOnShutdown.passwords",status.sHistoryStatus);
        PrefsHelper.setPref("privacy.clearOnShutdown.sessions",status.sHistoryStatus);
        PrefsHelper.setPref("privacy.clearOnShutdown.siteSettings",status.sHistoryStatus);
        PrefsHelper.setPref("privacy.donottrackheader.enabled",false);
        PrefsHelper.setPref("privacy.donottrackheader.value",1);
        PrefsHelper.setPref("network.http.sendRefererHeader", 0);
        PrefsHelper.setPref("security.OCSP.require", true);
        PrefsHelper.setPref("security.checkloaduri",true);
        PrefsHelper.setPref("security.mixed_content.block_display_content", true);
        PrefsHelper.setPref("media.peerconnection.enabled",false); //webrtc disabled
    }


}
