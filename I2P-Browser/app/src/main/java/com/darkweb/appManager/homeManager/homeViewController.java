package com.darkweb.appManager.homeManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.darkweb.constants.constants;
import com.darkweb.constants.enums;
import com.darkweb.constants.messages;
import com.darkweb.constants.status;
import com.darkweb.constants.strings;
import com.darkweb.widget.AnimatedProgressBar;
import com.darkweb.appManager.historyManager.historyRowModel;
import com.darkweb.helperManager.animatedColor;
import com.darkweb.helperManager.autoCompleteAdapter;
import com.darkweb.helperManager.eventObserver;
import com.darkweb.helperManager.helperMethod;
import com.example.myapplication.R;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import org.mozilla.geckoview.GeckoView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Callable;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT;
import static org.mozilla.geckoview.GeckoSessionSettings.USER_AGENT_MODE_DESKTOP;

class homeViewController
{
    /*ViewControllers*/
    private AppCompatActivity mContext;
    private eventObserver.eventListener mEvent;

    /*ViewControllers*/
    private FrameLayout mWebviewContainer;
    private AnimatedProgressBar mProgressBar;
    private AutoCompleteTextView mSearchbar;
    private ConstraintLayout mSplashScreen;
    private ImageView mLoading;
    private TextView mLoadingText;
    private AdView mBannerAds = null;
    private Handler mUpdateUIHandler = null;
    private ImageView mEngineLogo;
    private ImageButton mGatewaySplash;
    private LinearLayout mTopBar;
    private GeckoView mGeckoView;
    private ImageView mBackSplash;
    private Button mConnectButton;
    private Button mNewTab;
    private PopupWindow popupWindow = null;
    private boolean disableSplash = false;

    private Callable<String> mLogs = null;
    private boolean isLandscape = false;

    void initialization(eventObserver.eventListener event, AppCompatActivity context, Button mNewTab, FrameLayout webviewContainer, TextView loadingText, AnimatedProgressBar progressBar, AutoCompleteTextView searchbar, ConstraintLayout splashScreen, ImageView loading, AdView banner_ads, ArrayList<historyRowModel> suggestions, ImageView engineLogo, ImageButton gateway_splash, LinearLayout top_bar, GeckoView gecko_view, ImageView backsplash, Button connect_button){
        this.mContext = context;
        this.mProgressBar = progressBar;
        this.mSearchbar = searchbar;
        this.mSplashScreen = splashScreen;
        this.mLoading = loading;
        this.mLoadingText = loadingText;
        this.mWebviewContainer = webviewContainer;
        this.mBannerAds = banner_ads;
        this.mEvent = event;
        this.mEngineLogo = engineLogo;
        this.mGatewaySplash = gateway_splash;
        this.mTopBar = top_bar;
        this.mGeckoView = gecko_view;
        this.mBackSplash = backsplash;
        this.mConnectButton = connect_button;
        //this.mSwitchEngineBack = switch_engine_back;
        this.mNewTab = mNewTab;
        this.popupWindow = null;

        initSplashScreen();
        initializeSuggestionView(suggestions);
        initLock();
        createUpdateUiHandler();
        initTopBar();
    }

    private void initTopBar(){
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            Window window = mContext.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(mContext.getResources().getColor(R.color.dark_purple));
        }
        else {
            mContext.getWindow().setStatusBarColor(ContextCompat.getColor(mContext,R.color.dark_purple));
        }
    }

    @SuppressLint("SetTextI18n")
    void initTab(int count){
        mNewTab.setText(count+ strings.EMPTY_STR);

        YoYo.with(Techniques.FlipInX)
                .duration(450)
                .repeat(0)
                .playOn(mNewTab);
    }

    private void initPostUI(boolean isSplash){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = mContext.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if(isSplash){
                window.setStatusBarColor(mContext.getResources().getColor(R.color.landing_ease_blue));
            }
            else{
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    window.setStatusBarColor(mContext.getResources().getColor(R.color.blue_dark));
                }
                else {
                    initStatusBarColor();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initStatusBarColor() {
        animatedColor oneToTwo = new animatedColor(ContextCompat.getColor(mContext, R.color.dark_purple), ContextCompat.getColor(mContext, R.color.dark_purple));
        animatedColor twoToThree = new animatedColor(ContextCompat.getColor(mContext, R.color.dark_purple), ContextCompat.getColor(mContext, R.color.ease_blue_light));
        animatedColor ThreeToFour = new animatedColor(ContextCompat.getColor(mContext, R.color.ease_blue_light), ContextCompat.getColor(mContext, R.color.white));

        ValueAnimator animator = ObjectAnimator.ofFloat(0f, 1f).setDuration(0);
        animator.setStartDelay(800);
        animator.addUpdateListener(animation ->
        {
            float v = (float) animation.getAnimatedValue();
            mContext.getWindow().setStatusBarColor(oneToTwo.with(v));
            mContext.getWindow().setStatusBarColor(oneToTwo.with(v));
            mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        });
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                final ValueAnimator animator2 = ObjectAnimator.ofFloat(0f, 1f).setDuration(217);
                animator2.addUpdateListener(animation1 ->
                {
                    float v = (float) animation1.getAnimatedValue();
                    mContext.getWindow().setStatusBarColor(twoToThree.with(v));
                });
                animator2.start();

                animator2.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        final ValueAnimator animator3 = ObjectAnimator.ofFloat(0f, 1f).setDuration(0);
                        animator3.addUpdateListener(animation1 ->
                        {
                            float v = (float) animation1.getAnimatedValue();
                            mContext.getWindow().setStatusBarColor(ThreeToFour.with(v));

                        });
                        animator3.start();
                    }
                });
                animator2.start();

            }
        });
        animator.start();
    }

    private void initLock(){
        Drawable img = mContext.getResources().getDrawable( R.drawable.icon_lock);
        mSearchbar.measure(0, 0);
        img.setBounds( -10, (int)(mSearchbar.getMeasuredHeight()*0.00), (int)(mSearchbar.getMeasuredHeight()*1.10)-10, (int)(mSearchbar.getMeasuredHeight()*0.69) );
        mSearchbar.setCompoundDrawables( img, null, null, null );
    }

    void initializeSuggestionView(ArrayList<historyRowModel> suggestions){

        if(mSearchbar.isFocused()){
            return;
        }


        autoCompleteAdapter suggestionAdapter = new autoCompleteAdapter(mContext, R.layout.hint_view, (ArrayList<historyRowModel>) suggestions.clone());

        int width = Math.round(helperMethod.screenWidth());
        mSearchbar.setThreshold(2);
        mSearchbar.setAdapter(suggestionAdapter);
        mSearchbar.setDropDownVerticalOffset(helperMethod.pxFromDp(8));
        mSearchbar.setDropDownWidth(width);
        mSearchbar.setDropDownHeight(WRAP_CONTENT);

        Drawable drawable;
        Resources res = mContext.getResources();
        try {
            drawable = Drawable.createFromXml(res, res.getXml(R.xml.sc_rounded_corner_suggestion));
            mSearchbar.setDropDownBackgroundDrawable(drawable);
            mSearchbar.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            mSearchbar.setClipToOutline(true);
        } catch (Exception ignored) {
        }
        mSearchbar.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

    private void initSplashLoading(){

        mLoadingText.setAlpha(0);
        mLoadingText.setVisibility(View.VISIBLE);
        mLoadingText.animate().setDuration(300).alpha(1);

        mLoading.setAlpha(0f);
        mLoading.setVisibility(View.VISIBLE);
        mLoading.animate().setDuration(300).alpha(1).withEndAction((() -> mEvent.invokeObserver(Collections.singletonList(false), enums.etype.init_proxy)));

        mLoading.setLayoutParams(helperMethod.getCenterScreenPoint(mLoading.getLayoutParams()));

        mConnectButton.setVisibility(View.GONE);
        mGatewaySplash.setVisibility(View.GONE);

    }

    void initHomePage(){
        mConnectButton.setClickable(false);
        mGatewaySplash.setClickable(false);

        mConnectButton.animate().setDuration(300).alpha(0f).withEndAction((this::initSplashLoading));

        mGatewaySplash.animate().setDuration(300).alpha(0f);
    }

    private void initSplashScreen(){
        mSearchbar.setEnabled(false);
        helperMethod.hideKeyboard(mContext);
        initPostUI(true);

        mBackSplash.getLayoutParams().height = helperMethod.getScreenHeight(mContext) - helperMethod.getStatusBarHeight(mContext)*2;
        mSearchbar.setEnabled(false);

        View root = mSearchbar.getRootView();
        root.setBackgroundColor(ContextCompat.getColor(mContext, R.color.dark_purple));
        mGeckoView.setBackgroundResource( R.color.dark_purple);

    }

    void initProxyLoading(Callable<String> logs){
        this.mLogs = logs;

        if(mSplashScreen.getAlpha()==1){
            new Thread(){
                public void run(){
                    startPostTask();
                }
            }.start();
        }
    }

    /*-------------------------------------------------------PAGE UI Methods-------------------------------------------------------*/

    void onPageFinished(){
        mSearchbar.setEnabled(true);
        mProgressBar.bringToFront();
        mSplashScreen.bringToFront();
        if(mSplashScreen.getVisibility()!=View.GONE){
            mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        }
        splashScreenDisable();
    }

    private void postSplashDisable(){
        mSplashScreen.setVisibility(View.GONE);
        mEvent.invokeObserver(Collections.singletonList(false), enums.etype.on_load_ads);
        mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
    }

    void splashScreenDisable(){
        mTopBar.setAlpha(1);
        if(!disableSplash)
        {
            disableSplash = true;
            final Handler handler = new Handler();
            handler.postDelayed(() ->
            {
                mSplashScreen.animate().setDuration(300).setStartDelay(800).alpha(0).withEndAction((this::postSplashDisable));
                initPostUI(false);
            }, 2000);

            new Thread(){
                public void run(){
                    int counter=0;
                    String dots=".";
                    while (mSplashScreen.getVisibility()!=View.GONE){
                        try
                        {
                            sleep(700);
                            mLoadingText.setText(mContext.getString(R.string.loading_text).concat(dots));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        if(dots.equals("...")){
                            dots="";
                        }else {
                            dots = dots.concat(".");
                        }
                        counter+=1;
                    }
                }
            }.start();
        }
    }

    /*-------------------------------------------------------Helper Methods-------------------------------------------------------*/

    void onOpenMenu(View view,boolean canGoForward,boolean canGoBack,boolean isLoading,int userAgent){

        if(popupWindow!=null){
            popupWindow.dismiss();
        }

        LayoutInflater layoutInflater
                = (LayoutInflater) mContext
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_menu, null);


        int height = helperMethod.getScreenHeight(mContext)*90 /100;

        popupWindow = new PopupWindow(
                popupView,
                ActionMenuView.LayoutParams.WRAP_CONTENT,
                ActionMenuView.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        View parent = view.getRootView();
        popupWindow.setAnimationStyle(R.style.popup_window_animation);
        if(isLandscape){
            helperMethod.hideKeyboard(mContext);
            popupWindow.setHeight(height);
        }
        popupWindow.showAtLocation(parent, Gravity.TOP|Gravity.END,0,0);

        ImageButton back = popupView.findViewById(R.id.menu22);
        ImageButton forward = popupView.findViewById(R.id.menu23);
        ImageButton close = popupView.findViewById(R.id.menu20);
        CheckBox desktop = popupView.findViewById(R.id.menu27);
        desktop.setChecked(userAgent==USER_AGENT_MODE_DESKTOP);

        if(!canGoForward){
           forward.setColorFilter(Color.argb(255, 191, 191, 191));
           forward.setEnabled(false);
        }
        if(!canGoBack){
           back.setEnabled(false);
           back.setColorFilter(Color.argb(255, 191, 191, 191));
        }
        if(!isLoading){
           close.setEnabled(false);
           close.setColorFilter(Color.argb(255, 191, 191, 191));
        }

    }

    void downloadNotification(String message, enums.etype e_type){

        if(popupWindow!=null){
            popupWindow.dismiss();
        }

        LayoutInflater layoutInflater
                = (LayoutInflater) mContext
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        final View popupView = layoutInflater.inflate(R.layout.notification_menu, null);
        popupWindow = new PopupWindow(
                popupView,
                ActionMenuView.LayoutParams.MATCH_PARENT,
                ActionMenuView.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        View parent = mGeckoView.getRootView();
        popupWindow.setAnimationStyle(R.style.popup_window_animation);


        if(message.length()>30){
            message = message.substring(message.length()-20);
        }

        TextView notification_message = popupView.findViewById(R.id.notification_message);
        notification_message.setText(message);

        Button btn = popupView.findViewById(R.id.notification_event);
        btn.setOnClickListener(v ->
        {
            mEvent.invokeObserver(Collections.singletonList(status.sSearchStatus), e_type);
            popupWindow.dismiss();
        });

        popupWindow.showAtLocation(parent, Gravity.BOTTOM|Gravity.START,0,-10);
    }

    void closeMenu(){
        if(popupWindow!=null){
            popupWindow.dismiss();
        }
    }

    public void setOrientation(boolean status){
        isLandscape = status;
    }

    void onSetBannerAdMargin(boolean status,boolean isAdLoaded){
        /*if(isAdLoaded){
            if(status && !isLandscape){
                mBannerAds.setVisibility(View.VISIBLE);
                mBannerAds.setAlpha(1f);

                final Handler handler = new Handler();
                handler.postDelayed(() ->
                {
                    mWebviewContainer.clearAnimation();
                    mWebviewContainer.setPadding(0,AdSize.SMART_BANNER.getHeightInPixels(mContext)+1,0,0);
                    mProgressBar.bringToFront();
                }, 250);
            }else{
                mWebviewContainer.setPadding(0,0,0,0);
                mBannerAds.setVisibility(View.GONE);
            }
        }*/
    }

    private Handler searchBarUpdateHandler = new Handler();
    private String handlerLocalUrl = "";
    void onUpdateSearchBar(String url,boolean showProtocol){
        int delay = 50;
        handlerLocalUrl = url;

        if(searchBarUpdateHandler.hasMessages(100)){
            return;
        }

        searchBarUpdateHandler.sendEmptyMessage(100);
        searchBarUpdateHandler.postDelayed(() ->
        {
            searchBarUpdateHandler.removeMessages(100);
            triggerUpdateSearchBar(handlerLocalUrl,showProtocol);

        }, delay);
    }

    private void triggerUpdateSearchBar(String url, boolean showProtocol){
        if (mSearchbar == null || url==null)
        {
            return;
        }

        if(!showProtocol){
            url=url.replace("https://","");
            url=url.replace("http://","");
        }

        url = url.replace("boogle.store","genesis.onion");
        boolean isTextSelected = false;

        if(mSearchbar.isSelected()){
            isTextSelected = true;
        }


        if(url.length()<=300){
            url = removeEndingSlash(url);
            mSearchbar.setText(helperMethod.urlDesigner(url));
            mSearchbar.selectAll();

            if(isTextSelected){
                mSearchbar.selectAll();
            }

            mSearchbar.setSelection(0);
        }else {
            url = removeEndingSlash(url);
            mSearchbar.setText(url);
        }

        if(mSearchbar.isFocused()){
            mSearchbar.setSelection(mSearchbar.getText().length());
            mSearchbar.selectAll();
        }
    }

    private String removeEndingSlash(String url){
        return helperMethod.removeLastSlash(url);
    }

    void onNewTab(boolean keyboard,boolean isKeyboardOpen){
        onUpdateSearchBar(strings.BLANK_PAGE,false);
        if(keyboard){

            if(!isKeyboardOpen){
                final Handler handler = new Handler();
                handler.postDelayed(() ->
                {
                    InputMethodManager imm = (InputMethodManager)   mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    Objects.requireNonNull(imm).toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_SHOWN, 0);
                }, 250);
            }

            mSearchbar.requestFocus();
            mSearchbar.selectAll();
        }
    }
    void onUpdateLogs(){
        mLoadingText.setText(mContext.getString(R.string.loading_text));
    }
    void progressBarReset(){
        mProgressBar.setProgress(5);
        mProgressBar.setVisibility(View.INVISIBLE);
    }
    void onProgressBarUpdate(int value){
        if(mSplashScreen.getAlpha()>0){
            mProgressBar.setProgress(value*100);
        }
        if(value==100){
            mProgressBar.setAlpha(1f);
            mProgressBar.animate().setStartDelay(200).alpha(0);
            setProgressAnimate(mProgressBar,value);
        }
        else if(mSplashScreen.getAlpha()==0) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.animate().setStartDelay(50).alpha(1);
        }
        setProgressAnimate(mProgressBar,value);
    }

    private ObjectAnimator animation = null;
    private void setProgressAnimate(ProgressBar pb, int progressTo)
    {
        int progress = 0;
        if((progressTo)<mProgressBar.getProgress()){
            progress = 5;
        }

        if(animation!=null){
            animation.removeAllListeners();
            animation.end();
            animation.cancel();
        }

        animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo * 100);
        animation.setDuration(progress);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    void onClearSelections(boolean hideKeyboard){
        mSearchbar.clearFocus();
        if(hideKeyboard){
            helperMethod.hideKeyboard(mContext);
        }
    }

    private int defaultFlag = 0;
    void onFullScreenUpdate(boolean status){
        int value = !status ? 1 : 0;

        mTopBar.setClickable(!status);
        mTopBar.setAlpha(value);

        if(status){
            mWebviewContainer.setPadding(0,0,0,0);
            defaultFlag = mContext.getWindow().getDecorView().getSystemUiVisibility();
            final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            mContext.getWindow().getDecorView().setSystemUiVisibility(flags);
            mProgressBar.setVisibility(View.GONE);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mWebviewContainer.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            mWebviewContainer.setLayoutParams(params);
            mBannerAds.setVisibility(View.GONE);
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }else {
                mContext.getWindow().setStatusBarColor(mContext.getResources().getColor(R.color.blue_dark));
            }

            mContext.getWindow().getDecorView().setSystemUiVisibility(defaultFlag);
            mProgressBar.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mWebviewContainer.getLayoutParams();
            params.setMargins(0, helperMethod.pxFromDp(55), 0, 0);
            mWebviewContainer.setLayoutParams(params);
            mBannerAds.setVisibility(View.GONE);
            mEvent.invokeObserver(Collections.singletonList(!isLandscape), enums.etype.on_init_ads);
        }
    }

    void onReDraw(){
        if(mWebviewContainer.getPaddingBottom()==0){
            mWebviewContainer.setPadding(0,0,0,1);
        }
        else {
            mWebviewContainer.setPadding(0,0,0,0);
        }
    }

    void onSessionChanged(){
    }

    void onUpdateLogo(){

        switch (status.sSearchStatus)
        {
            case constants.BACKEND_GOOGLE_URL:
                mEngineLogo.setImageResource(R.drawable.genesis_logo);
                break;
            case constants.BACKEND_GENESIS_URL:
                mEngineLogo.setImageResource(R.drawable.duck_logo);
                break;
            case constants.BACKEND_DUCK_DUCK_GO_URL:
                mEngineLogo.setImageResource(R.drawable.google_logo);
                break;
        }
    }

    /*-------------------------------------------------------POST UI TASK HANDLER-------------------------------------------------------*/

    private void startPostTask() {
        Message message = new Message();
        message.what = messages.ON_URL_LOAD;
        mUpdateUIHandler.sendMessage(message);
    }

    @SuppressLint("HandlerLeak")
    private void createUpdateUiHandler(){
        mUpdateUIHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if(msg.what == messages.ON_URL_LOAD)
                {
                    if(status.sRedirectStatus.equals(strings.EMPTY_STR)){
                        mEvent.invokeObserver(Collections.singletonList(status.sSearchStatus), enums.etype.on_url_load);
                    }else {
                        mEvent.invokeObserver(Collections.singletonList(status.sRedirectStatus), enums.etype.on_url_load);
                    }
                }
                if(msg.what == messages.UPDATE_LOADING_TEXT)
                {
                    if(mLogs !=null)
                    {
                        try
                        {
                            mLogs.call();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }
}