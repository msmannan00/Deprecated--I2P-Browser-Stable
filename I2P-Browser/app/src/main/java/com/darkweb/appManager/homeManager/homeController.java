package com.darkweb.appManager.homeManager;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.darkweb.appManager.languageManager.languageController;
import com.darkweb.pluginManager.pluginController;
import com.darkweb.widget.AnimatedProgressBar;
import com.darkweb.appManager.activityContextManager;
import com.darkweb.appManager.bookmarkManager.bookmarkController;
import com.darkweb.appManager.databaseManager.databaseController;
import com.darkweb.appManager.historyManager.historyController;
import com.darkweb.appManager.settingManager.settingController;
import com.darkweb.appManager.tabManager.tabController;
import com.darkweb.appManager.tabManager.tabRowModel;
import com.darkweb.constants.constants;
import com.darkweb.constants.enums;
import com.darkweb.constants.keys;
import com.darkweb.constants.status;
import com.darkweb.constants.strings;
import com.darkweb.dataManager.dataController;
import com.darkweb.helperManager.KeyboardUtils;
import com.darkweb.helperManager.eventObserver;
import com.darkweb.helperManager.helperMethod;
import com.example.myapplication.R;
import com.google.android.gms.ads.AdView;
import net.i2p.android.I2PActivity;
import net.i2p.android.router.util.Connectivity;
import org.mozilla.geckoview.GeckoView;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import static com.darkweb.constants.constants.BACKEND_GENESIS_URL_TOKEN;

@SuppressWarnings({"FieldCanBeLocal", "BooleanMethodIsAlwaysInverted"})
public class homeController extends AppCompatActivity implements ComponentCallbacks2
{
    /*Model Declaration*/
    private homeViewController mHomeViewController;
    private homeModel mHomeModel;
    private geckoClients mGeckoClient = null;

    /*View Webviews*/
    private GeckoView mGeckoView = null;
    private FrameLayout mWebViewContainer;

    /*View Objects*/
    private AnimatedProgressBar mProgressBar;
    private ConstraintLayout mSplashScreen;
    private AutoCompleteTextView mSearchbar;
    private ImageView mLoadingIcon;
    private TextView mLoadingText;
    private AdView mBannerAds = null;
    private ImageView mEngineLogo;
    private ImageButton mGatewaySplash;
    private LinearLayout mTopBar;
    private ImageView mBackSplash;
    private Button mConnectButton;
    private Button mNewTab;

    /*Redirection Objects*/
    private boolean mPageClosed = false;
    private boolean isKeyboardOpened = false;
    private boolean isSuggestionChanged = false;

    /*-------------------------------------------------------INITIALIZATION-------------------------------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        pluginController.getInstance().preInitialize(this);
        databaseController.getInstance().initialize(this);
        dataController.getInstance().initialize(this);
        status.initStatus();
        pluginController.getInstance().onCreate(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_view);
        activityContextManager.getInstance().setHomeController(this);
        dataController.getInstance().initializeListData();
        initializeAppModel();
        initializeAppModel();
        initializeConnections();
        pluginController.getInstance().initialize();
        initializeGeckoView(savedInstanceState == null);
        initializeLocalEventHandlers();
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        Uri data = intent.getData();
        if(data!=null){
            status.sRedirectStatus = data.toString();
            if(status.sIsAppStarted){
                onLoadURL(status.sRedirectStatus);
            }
        }
    }

    public void initializeAppModel()
    {
        mHomeViewController = new homeViewController();
        mHomeModel = new homeModel();
    }

    public void initializeConnections()
    {
        mGeckoView = findViewById(R.id.webLoader);
        mProgressBar = findViewById(R.id.progressBar);
        mSplashScreen = findViewById(R.id.splashScreen);
        mSearchbar = findViewById(R.id.search);
        mLoadingIcon = findViewById(R.id.imageView_loading_back);
        mLoadingText = findViewById(R.id.loadingText);
        mWebViewContainer = findViewById(R.id.webviewContainer);
        mBannerAds = findViewById(R.id.adView);
        mEngineLogo = findViewById(R.id.switchEngine);
        mGatewaySplash = findViewById(R.id.gateway_splash);
        mTopBar = findViewById(R.id.topbar);
        mBackSplash = findViewById(R.id.backsplash);
        mConnectButton = findViewById(R.id.Connect);
        mNewTab = findViewById(R.id.newButtonInvoke);
        mGeckoView.setSaveEnabled(false);
        mGeckoView.setSaveFromParentEnabled(false);
        mGeckoClient = new geckoClients();

        mHomeViewController.initialization(new homeViewCallback(),this,mNewTab, mWebViewContainer, mLoadingText, mProgressBar, mSearchbar, mSplashScreen, mLoadingIcon, mBannerAds,dataController.getInstance().getSuggestions(), mEngineLogo, mGatewaySplash, mTopBar, mGeckoView, mBackSplash, mConnectButton);
    }

    public void initializeGeckoView(boolean isForced){
        mGeckoClient.initialize(mGeckoView, new geckoViewCallback(), this,isForced);
        onSaveCurrentTab(mGeckoClient.getSession(),false);
        mHomeViewController.initTab(dataController.getInstance().getTotalTabs());
    }

    public void initTab(){
        onNewTab(false);
        mHomeViewController.initTab(dataController.getInstance().getTotalTabs());
    }

    /*-------------------------------------------------------Helper Methods-------------------------------------------------------*/

    public void onLoadFont(){
        mGeckoClient.onUpdateFont();
        mHomeViewController.onReDraw();
    }

    public void onLoadProxy(View view){
        if(pluginController.getInstance().isInitialized() && !mPageClosed){
            pluginController.getInstance().logEvent(strings.GATEWAY_OPENED);
            helperMethod.openActivity(I2PActivity.class, constants.LIST_HISTORY, homeController.this,true);
        }
    }

    public void onUpdateJavascript(){
        mGeckoView.clearFocus();
        mGeckoClient.updateJavascript();
    }

    public void onUpdateCookies(){
        mGeckoView.clearFocus();
        mGeckoClient.updateCookies();
    }

    public void onLoadURL(String url,boolean wasInvoked){
        hasApplicationStartCheck();
        mHomeViewController.onClearSelections(true);
        mGeckoClient.loadURL(url.replace("genesis.onion","boogle.store"));
        mHomeViewController.onUpdateSearchBar(url,false);
        mHomeViewController.splashScreenDisable();
    }

    public void onLoadURL(String url){
        if(status.isProxyRunning() || url.contains(BACKEND_GENESIS_URL_TOKEN) || url.equals("about:blank")){
            hasApplicationStartCheck();
            mHomeViewController.onClearSelections(true);
            mGeckoClient.loadURL(url.replace("genesis.onion","boogle.store"));
            mHomeViewController.onUpdateSearchBar(url,false);
        }else {
            onProxyError(url);
        }
    }

    void onProxyError(String url){
        mSearchbar.clearFocus();
        final Handler handler = new Handler();
        helperMethod.hideKeyboard(this);
        handler.postDelayed(() ->
        {
            mGeckoView.clearFocus();
            pluginController.getInstance().MessageManagerHandler(activityContextManager.getInstance().getHomeController(), Collections.singletonList(Uri.parse(url).toString()), enums.etype.on_proxy_loading);
        }, 200);
    }

    public void hasApplicationStartCheck(){
        if(!status.hasAppStarted){
            status.hasAppStarted = true;
            mHomeViewController.initHomePage();
        }
    }

    public void onLoadTab(geckoSession mTempSession,boolean isSessionClosed){
        if(!isSessionClosed){
            dataController.getInstance().moveTabToTop(mTempSession);
        }

        mGeckoView.releaseSession();
        mGeckoClient.initSession(mTempSession);
        mGeckoView.setSession(mTempSession);

        mHomeViewController.onClearSelections(false);
        mHomeViewController.onUpdateSearchBar(mTempSession.getCurrentURL(),false);
        if(mTempSession.getProgress()>0 && mTempSession.getProgress()<100){
            mHomeViewController.onProgressBarUpdate(mTempSession.getProgress());
        }else {
            mHomeViewController.progressBarReset();
        }
    }

    public void onSetBannerAdMargin(){
        mHomeViewController.onSetBannerAdMargin(true,pluginController.getInstance().isAdvertLoaded());
    }

    public void onClearSession(){
        mGeckoClient.onClearSession();
    }

    public void onDownloadFile(){
        mGeckoClient.downloadFile();
    }

    public void onManualDownload(String url){

        mGeckoClient.manual_download(url,this);
    }

    public AdView getBannerAd()
    {
        return mBannerAds;
    }


    /*-------------------------------------------------------USER EVENTS-------------------------------------------------------*/

    private BroadcastReceiver downloadStatus = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            DownloadManager dMgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            Cursor c= Objects.requireNonNull(dMgr).query(new DownloadManager.Query().setFilterById(id));

            if(c.moveToFirst()){
                String url = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                onNotificationInvoked(URLUtil.guessFileName(url, null, null), enums.etype.download_folder);
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private void initializeLocalEventHandlers() {

        registerReceiver(downloadStatus,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        mSearchbar.setOnEditorActionListener((v, actionId, event) ->
        {
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE)
            {
                helperMethod.hideKeyboard(homeController.this);
                mGeckoClient.setLoading(true);
                final Handler handler = new Handler();
                handler.postDelayed(() ->
                {
                    pluginController.getInstance().logEvent(strings.SEARCH_INVOKED);
                    onSearchBarInvoked(v);
                    mGeckoView.clearFocus();
                }, 500);
            }
            return true;
        });

        mGatewaySplash.setOnTouchListener((v, event) ->
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                mGatewaySplash.setElevation(9);
            else if (event.getAction() == MotionEvent.ACTION_UP)
                mGatewaySplash.setElevation(2);
                return false;
        });

        mGeckoView.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus)
            {
                helperMethod.hideKeyboard(homeController.this);
                status.sIsAppStarted = true;
                pluginController.getInstance().onResetMessage();
                mHomeViewController.onUpdateSearchBar(mGeckoClient.getSession().getCurrentURL(),false);
            }
        });

        mSearchbar.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,int before, int count) {
            }
        });

        mSearchbar.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus)
            {
                if(!mGeckoClient.isLoading()){
                    mHomeViewController.onUpdateSearchBar(mGeckoClient.getSession().getCurrentURL(),false);
                }
                if(isSuggestionChanged){
                    isSuggestionChanged = false;
                    mHomeViewController.initializeSuggestionView(dataController.getInstance().getSuggestions());
                }
            }else {
                mHomeViewController.onUpdateSearchBar(mGeckoClient.getSession().getCurrentURL(),true);
            }
        });
        pluginController.getInstance().logEvent(strings.APP_STARTED);

        KeyboardUtils.addKeyboardToggleListener(this, isVisible -> isKeyboardOpened = isVisible);
    }

    void onSearchBarInvoked(View view){
        String url = ((EditText)view).getText().toString();
        String validated_url = mHomeModel.urlComplete(url);
        if(validated_url!=null){
            url = validated_url;
        }
        mHomeViewController.onUpdateSearchBar(url,false);
        onLoadURL(url);
    }

    public void onSuggestionInvoked(View view){
        String val = ((TextView)view.findViewById(R.id.hintCompletionUrl)).getText().toString();
        mSearchbar.clearFocus();
        mHomeViewController.onUpdateSearchBar(val,false);
        onLoadURL(val);
    }

    public void onSwitchSearch(View view){

        Intent theIntent = new Intent(this,I2PActivity.class);
        theIntent.putExtra("urllist", true);
        startActivity(theIntent);

    }

    public void onHomeButton(View view){
        pluginController.getInstance().logEvent(strings.HOME_INVOKED);
        onLoadURL(mHomeModel.getSearchEngine());
        mHomeViewController.onUpdateLogo();
    }

    public void onNewTab(boolean isKeyboardOpenedTemp){
        initializeGeckoView(true);
        onLoadURL("about:blank");
        mHomeViewController.progressBarReset();
        mHomeViewController.onNewTab(true,isKeyboardOpenedTemp);
        mHomeViewController.onSessionChanged();
    }

    public void onOpenTabView(View view){
        helperMethod.openActivity(tabController.class, constants.LIST_HISTORY, homeController.this,true);
    }

    public void onNotificationInvoked(String message,enums.etype e_type){
        mHomeViewController.downloadNotification(message,e_type);
    }

    public void onOpenDownloadFolder(View view){
        helperMethod.openDownloadFolder(homeController.this);
    }

    public void onOpenMenuItem(View view){
        pluginController.getInstance().logEvent(strings.MENU_INVOKED);
        status.sIsAppStarted = true;
        pluginController.getInstance().onResetMessage();
        pluginController.getInstance().setLanguage(this);

        mHomeViewController.onOpenMenu(view,mGeckoClient.canGoBack(),mGeckoClient.canGoForward(),!(mProgressBar.getAlpha()<=0 || mProgressBar.getVisibility() ==View.INVISIBLE),mGeckoClient.getUserAgent());
    }

    public void onStartApplication(View view){
        if(Connectivity.isConnected(this)){
            status.autoStartProxy = true;
            hasApplicationStartCheck();
        }else {
            pluginController.getInstance().MessageManagerHandler(homeController.this,null,enums.etype.internet_error);
        }
    }

    public void onRestartApplication(){
        finish();
        helperMethod.openActivity(homeController.class, constants.LIST_HISTORY, this,true);
        status.hasAppStarted = false;
    }

    /*------------------------------------------------------- Local Overrides -------------------------------------------------------*/

    @Override
    public void onPause(){
        super.onPause();
        if(mHomeViewController!=null){
            mHomeViewController.closeMenu();
            helperMethod.hideKeyboard(this);
        }

        mGeckoClient.onExitFullScreen();
        pluginController.getInstance().onResetMessage();
    }

    @Override
    public void onResume()
    {
        pluginController.getInstance().onResume(this);
        activityContextManager.getInstance().setCurrentActivity(this);
        if (mGeckoClient!=null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mGeckoClient.getUriPermission()!=null) {
            this.revokeUriPermission(mGeckoClient.getUriPermission(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==1){
            mGeckoClient.onUploadRequest(resultCode,data);
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed(){
        pluginController.getInstance().logEvent(strings.ON_BACK);
        mSearchbar.clearFocus();
        if(mGeckoClient.getFullScreenStatus()){
            mGeckoClient.onBackPressed(true);
            mHomeViewController.onClearSelections(true);
        }
        else {
            mGeckoClient.onExitFullScreen();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(downloadStatus);
        }catch (Exception ignored){}
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        pluginController.getInstance().onResetMessage();
        mHomeViewController.closeMenu();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mHomeViewController.setOrientation(true);
            if(mGeckoClient.getFullScreenStatus())
            {
                mHomeViewController.onSetBannerAdMargin(false, true);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            mHomeViewController.setOrientation(false);
            if(mGeckoClient.getFullScreenStatus())
            {
                mHomeViewController.onSetBannerAdMargin(true,pluginController.getInstance().isAdvertLoaded());
            }
        }
    }

    /*-------------------------------------------------------External Callback Methods-------------------------------------------------------*/

    public void onSuggestionUpdate(){
        if(!mSearchbar.isFocused()){
            mHomeViewController.initializeSuggestionView(dataController.getInstance().getSuggestions());
        }else {
            isSuggestionChanged = true;
        }
    }

    public void onInvokeProxyLoading(){
        Callable<String> callable = () -> strings.EMPTY_STR;
        mHomeViewController.initProxyLoading(callable);
    }
    public void initProxy(boolean wasInvoked){
        pluginController.getInstance().initializeOrbot();
        if(!wasInvoked){
            helperMethod.openActivity(I2PActivity.class, constants.LIST_HISTORY, homeController.this,false);
            onInvokeProxyLoading();
        }
    }

    public void onOpenLinkNewTab(String url){
        initializeGeckoView(true);
        mHomeViewController.progressBarReset();
        mHomeViewController.onNewTab(false,isKeyboardOpened);
        mHomeViewController.onUpdateSearchBar(url,false);
        mGeckoClient.loadURL(url);
    }

    public void onSaveCurrentTab(geckoSession session,boolean isHardCopy){
        dataController.getInstance().addTab(session,isHardCopy);
    }

    public boolean onCloseCurrentTab(geckoSession session){
        dataController.getInstance().closeTab(session);
        tabRowModel model = dataController.getInstance().getCurrentTab();

        session.stop();
        session.close();
        initTabCount();

        if(model!=null){
            onLoadTab(model.getSession(),true);
            return true;
        }
        else {
            return false;
        }
    }

    public void initTabCount(){
        mHomeViewController.initTab(dataController.getInstance().getTotalTabs());
    }

    public void releaseSession(){
        mGeckoView.releaseSession();
    }

    public void loadExistingTab()
    {
        tabRowModel model = dataController.getInstance().getCurrentTab();
        if (model != null)
        {
            onLoadTab(model.getSession(), true);
        }
    }

    /*-------------------------------------------------------CALLBACKS-------------------------------------------------------*/

    public void onMenuItemInvoked(View view){
        int menuId = view.getId();
        if (menuId == R.id.menu11) {
            onNewTab(isKeyboardOpened);
        }
        else if (menuId == R.id.menu10) {
            if(!onCloseCurrentTab(mGeckoClient.getSession())){
                onNewTab(isKeyboardOpened);
            }
        }
        else {
            helperMethod.hideKeyboard(this);
            mSearchbar.clearFocus();
            if (menuId == R.id.menu9) {
                helperMethod.openActivity(tabController.class, constants.LIST_HISTORY, homeController.this,true);
            }
            else if (menuId == R.id.menu8) {
                onOpenDownloadFolder(null);
            }
            else if (menuId == R.id.menu7) {
                helperMethod.openActivity(historyController.class, constants.LIST_HISTORY, homeController.this,true);
            }
            else if (menuId == R.id.menu6)
            {
                helperMethod.openActivity(settingController.class,constants.LIST_HISTORY, homeController.this,true);
            }
            else if (menuId == R.id.menu5)
            {
                pluginController.getInstance().MessageManagerHandler(homeController.this, Collections.singletonList(mGeckoClient.getSession().getCurrentURL()),enums.etype.bookmark);
            }
            else if (menuId == R.id.menu4)
            {
                helperMethod.openActivity(bookmarkController.class,constants.LIST_BOOKMARK, homeController.this,true);
            }
            else if (menuId == R.id.menu3)
            {
                pluginController.getInstance().MessageManagerHandler(homeController.this,null,enums.etype.report_url);
            }
            else if (menuId == R.id.menu2)
            {
                dataController.getInstance().setBool(keys.IS_APP_RATED,true);
                status.sIsAppRated = true;
                pluginController.getInstance().MessageManagerHandler(activityContextManager.getInstance().getHomeController(), Collections.singletonList(strings.EMPTY_STR), enums.etype.rate_app);
            }
            else if (menuId == R.id.menu1)
            {
                helperMethod.shareApp(homeController.this);
            }
            if (menuId == R.id.menu20) {
                mGeckoClient.onStop();
            }
            if (menuId == R.id.menu21) {
                mGeckoClient.onReload();
            }
            if (menuId == R.id.menu22) {
                mGeckoClient.onForwardPressed();
            }
            if (menuId == R.id.menu23) {
                mGeckoClient.onBackPressed(false);
            }
            if (menuId == R.id.menu24) {
                onHomeButton(view);
            }
            if (menuId == R.id.menu26 || menuId == R.id.menu27 || menuId == R.id.menu28) {
                mGeckoClient.toogleUserAgent();
                mGeckoClient.onReload();
            }
            if (menuId == R.id.menu29) {
                status.isOpenURL=true;
                helperMethod.openActivity(I2PActivity.class, constants.LIST_HISTORY, homeController.this,true);
            }
            if (menuId == R.id.menu12) {
                onLoadProxy(null);
            }
            if(menuId == R.id.menu25){
                helperMethod.openActivity(languageController.class, constants.LIST_HISTORY, homeController.this,true);
                //pluginController.getInstance().MessageManagerHandler(homeController.this,null,enums.etype.change_language);
            }
        }
        mHomeViewController.closeMenu();
    }

    public class homeViewCallback implements eventObserver.eventListener{

        @Override
        public void invokeObserver(List<Object> data, enums.etype e_type)
        {
           if(e_type.equals(enums.etype.download_folder))
           {
               onOpenDownloadFolder(null);
           }
           if(e_type.equals(enums.etype.on_init_ads))
           {
               mHomeViewController.onSetBannerAdMargin((boolean)data.get(0),pluginController.getInstance().isAdvertLoaded());
           }
           else if(e_type.equals(enums.etype.on_url_load)){
               mHomeViewController.onUpdateLogs();
               onLoadURL(data.get(0).toString());
           }
           else if(e_type.equals(enums.etype.init_proxy)){
               initProxy((boolean)data.get(0));
           }
        }
    }

    String dataToStr(Object data,String defaultVal){
        if(data==null){
            return defaultVal;
        }
        else {
            return data.toString();
        }
    }

    String dataToStr(Object data){
        if(data==null){
            return strings.EMPTY_STR;
        }
        else {
            return data.toString();
        }
    }


    public class geckoViewCallback implements eventObserver.eventListener{

        @Override
        public void invokeObserver(List<Object> data, enums.etype e_type)
        {
            if(e_type.equals(enums.etype.progress_update)){
                mHomeViewController.onProgressBarUpdate((int)data.get(0));
            }
            else if(e_type.equals(enums.etype.on_url_load)){
                mHomeViewController.onUpdateSearchBar(dataToStr(data.get(0),mGeckoClient.getSession().getCurrentURL()),false);
        }
            else if(e_type.equals(enums.etype.back_list_empty)){
                if(dataController.getInstance().getTotalTabs()>1){
                    if(!onCloseCurrentTab(mGeckoClient.getSession())){
                        onNewTab(false);
                    }
                }else {
                    helperMethod.onMinimizeApp(homeController.this);
                }
            }
            else if(e_type.equals(enums.etype.start_proxy)){
                pluginController.getInstance().setProxy(dataToStr(data.get(0)));
            }
            else if(e_type.equals(enums.etype.on_request_completed)){
                dataController.getInstance().addHistory(data.get(0).toString());
            }
            else if(e_type.equals(enums.etype.on_update_suggestion)){
                dataController.getInstance().addSuggesion(data.get(0).toString(),data.get(2).toString());
            }
            else if(e_type.equals(enums.etype.on_page_loaded)){
                pluginController.getInstance().logEvent(strings.PAGE_OPENED_SUCCESS);
                dataController.getInstance().setBool(keys.IS_BOOTSTRAPPED,true);
                mHomeViewController.onPageFinished();
                if(status.sIsWelcomeEnabled && !status.sIsAppStarted){
                    final Handler handler = new Handler();
                    Runnable runnable = () ->
                    {
                        status.sIsAppStarted = true;
                    };
                    handler.postDelayed(runnable, 1300);
                }else {
                    final Handler handler = new Handler();
                    Runnable runnable = () ->
                            pluginController.getInstance().initializeBannerAds();
                    handler.postDelayed(runnable, 2000);
                }
            }
            else if(e_type.equals(enums.etype.rate_application)){
                if(!status.sIsAppRated){
                    dataController.getInstance().setBool(keys.IS_APP_RATED,true);
                    status.sIsAppRated = true;
                    pluginController.getInstance().MessageManagerHandler(activityContextManager.getInstance().getHomeController(), Collections.singletonList(strings.EMPTY_STR), enums.etype.rate_app);
                }
            }
            else if(e_type.equals(enums.etype.on_load_error)){
                pluginController.getInstance().setLanguage(homeController.this);
                mHomeViewController.onPageFinished();
                mHomeViewController.onUpdateSearchBar(dataToStr(data.get(0),mGeckoClient.getSession().getCurrentURL()),false);
            }
            else if(e_type.equals(enums.etype.search_update)){
                mHomeViewController.onUpdateSearchBar(dataToStr(data.get(0),mGeckoClient.getSession().getCurrentURL()),false);
            }
            else if(e_type.equals(enums.etype.download_file_popup)){
                pluginController.getInstance().MessageManagerHandler(homeController.this,Collections.singletonList(dataToStr(data.get(0))),enums.etype.download_file);
            }
            else if(e_type.equals(enums.etype.on_full_screen)){
                boolean status = (Boolean)data.get(0);
                mHomeViewController.onFullScreenUpdate(status);
            }
            else if(e_type.equals(enums.etype.on_long_press_with_link)){
                 pluginController.getInstance().MessageManagerHandler(homeController.this, Arrays.asList(dataToStr(data.get(0)),dataToStr(data.get(2)),dataToStr(data.get(3))),enums.etype.on_long_press_with_link);
            }
            else if(e_type.equals(enums.etype.on_long_press)){
                pluginController.getInstance().MessageManagerHandler(homeController.this,Arrays.asList(dataToStr(data.get(0)),dataToStr(data.get(2))),enums.etype.download_file_long_press);
            }
            else if(e_type.equals(enums.etype.on_long_press_url)){
                pluginController.getInstance().MessageManagerHandler(homeController.this,Arrays.asList(dataToStr(data.get(0)),dataToStr(data.get(2))),enums.etype.on_long_press_url);
            }
            else if(e_type.equals(enums.etype.open_new_tab)){
                onOpenLinkNewTab(dataToStr(data.get(0)));
            }
            else if(e_type.equals(enums.etype.on_close_sesson)){
                if(!onCloseCurrentTab(mGeckoClient.getSession())){
                    onNewTab(false);
                }
            }
            else if(e_type.equals(enums.etype.on_playstore_load)){
                helperMethod.openPlayStore(dataToStr(data.get(0)).split("__")[1], homeController.this);
            }
            else if(e_type.equals(enums.etype.on_update_suggestion_url)){
                dataController.getInstance().updateSuggestionURL(dataToStr(data.get(0)),dataToStr(data.get(2)));
            }
        }
    }
}

