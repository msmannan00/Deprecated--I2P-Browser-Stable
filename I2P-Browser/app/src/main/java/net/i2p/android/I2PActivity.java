package net.i2p.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.support.pager.CustomViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.darkweb.constants.enums;
import com.darkweb.constants.status;
import com.darkweb.helperManager.helperMethod;
import com.example.myapplication.R;

import net.i2p.android.help.HelpActivity;
import net.i2p.android.i2ptunnel.TunnelsContainer;
import net.i2p.android.router.ConsoleContainer;
import net.i2p.android.router.MainFragment;
import com.example.myapplication.R;
import net.i2p.android.router.SettingsActivity;
import net.i2p.android.router.addressbook.AddressbookContainer;
import net.i2p.android.router.service.RouterService;
import net.i2p.android.router.service.State;
import net.i2p.android.router.util.Connectivity;
import net.i2p.android.router.util.Util;
import net.i2p.android.util.MemoryFragmentPagerAdapter;
import net.i2p.android.widget.SlidingTabLayout;

import java.io.File;

import static android.graphics.Color.WHITE;

/**
 * The main activity of the app. Contains a ViewPager that holds the three main
 * views:
 * <ul>
 * <li>The console</li>
 * <li>The addressbook</li>
 * <li>The tunnel manager</li>
 * </ul>
 */
public class I2PActivity extends I2PActivityBase implements
        MainFragment.RouterControlListener {
    CustomViewPager mViewPager;
    ViewPagerAdapter mViewPagerAdapter;
    SlidingTabLayout mSlidingTabLayout;

    private boolean mAutoStartFromIntent = false;
    private boolean _keep = true;
    private boolean _startPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra("init") && getIntent().getBooleanExtra("init", false))
        {
            onStartRouterClicked();
            finish();
            return;
        }

        setContentView(R.layout.activity_viewpager);

        if(status.autoStartProxy){
            setContentView(R.layout.invalid_setup_view);
            //if (mViewPager.getCurrentItem() != 0)
            //    mViewPager.setCurrentItem(status.currentProxyPagePublic, false);
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            mAutoStartFromIntent = true;
            onStartRouterClicked();
            LinearLayout layout = findViewById(R.id.backactivity);
            setTheme(R.style.Theme_Transparent);
            layout.setAlpha(0);
            initSplashScreen();
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        }

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color_v1));
        toolbar.setBackgroundColor(getResources().getColor(R.color.dark_purple));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawable drawable = toolbar.getNavigationIcon();
        toolbar.setNavigationOnClickListener(v -> finish());

        drawable.setColorFilter(WHITE, PorterDuff.Mode.SRC_ATOP);
        toolbar.getOverflowIcon().setColorFilter(Color.WHITE , PorterDuff.Mode.SRC_ATOP);

        mViewPager = findViewById(R.id.pager);
        mViewPagerAdapter = new ViewPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        toolbar.setTitleTextColor(WHITE);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        // Center the tabs in the layout
        mSlidingTabLayout.setDistributeEvenly(true);

        // Customize tab color
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.accent);
            }
        });
        // Give the SlidingTabLayout the ViewPager
        mSlidingTabLayout.setViewPager(mViewPager);

        _keep = true;

        if (getIntent().hasExtra("urllist") && getIntent().getBooleanExtra("urllist", false))
        {
        }


        final Handler handler = new Handler();
        Runnable runnable = () -> {
            if(status.isOpenURL){
                mViewPager.setCurrentItem(3,false);
            }else if(!status.isOpenURL){
                mViewPager.setCurrentItem(status.currentProxyPagePublic,false);
            }
        };
        handler.postDelayed(runnable, 0);

    }

    private void initSplashScreen(){
        ImageView mBackSplash = findViewById(R.id.backsplash);
        ImageView mLoading = findViewById(R.id.imageView_loading_back);
        mBackSplash.getLayoutParams().height = helperMethod.getScreenHeight(this) - helperMethod.getStatusBarHeight(this)*2;
        View root = mBackSplash.getRootView();
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_purple));

        //mLoading.setAnimation(helperMethod.getRotationAnimation());
        mLoading.setLayoutParams(helperMethod.getCenterScreenPoint(mLoading.getLayoutParams()));
        TextView mLoadingText = findViewById(R.id.loadingText);

        new Thread(){
            public void run(){
                int counter=0;
                String dots=".";
                while (!I2PActivity.this.isDestroyed()){
                    try
                    {
                        sleep(700);
                        if(canStart() || Connectivity.isConnected(I2PActivity.this)){
                            mLoadingText.setText("Starting | Genesis Search"+dots);
                        }else {
                            mLoadingText.setText("Internet Connection Error"+dots);
                        }

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if(dots.equals("...")){
                        dots="";
                    }else {
                        dots+=".";
                    }
                    counter+=1;
                }
            }
        }.start();
    }

    public static class ViewPagerAdapter extends MemoryFragmentPagerAdapter {
        private static final int NUM_ITEMS = 3;

        private Context mContext;

        public ViewPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            mContext = context;
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ConsoleContainer();
                case 1:
                    return new TunnelsContainer();
                case 2:
                    return new AddressbookContainer();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return mContext.getString(R.string.label_console);
                case 1:
                    return mContext.getString(R.string.label_tunnels);
                case 2:
                    return mContext.getString(R.string.label_addresses);
                default:
                    return null;
            }
        }
    }

        @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Util.d("Initializing...");
        InitActivities init = new InitActivities(this);
        init.debugStuff();
        init.initialize();
        super.onPostCreate(savedInstanceState);
        handleIntents();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntents();
    }

    private void handleIntents() {
        if (getIntent() == null)
            return;

        Intent intent = getIntent();
        String action = intent.getAction();

        if (action == null)
            return;

        switch (action) {
            case "net.i2p.android.router.START_I2P":
                if (mViewPager.getCurrentItem() != 0)
                    if(!status.isOpenURL){
                        mViewPager.setCurrentItem(status.currentProxyPagePublic, false);
                    }
                autoStart();
                break;

            case Intent.ACTION_PICK:
                mViewPager.setFixedPage(2, R.string.select_an_address);
                break;
        }
    }

    private void autoStart() {
        if (canStart()) {
            if (Connectivity.isConnected(this)) {
                mAutoStartFromIntent = true;
                onStartRouterClicked();
            } else {
                // Not connected to a network
                // TODO: Notify user
            }
        } else {
            // TODO: Notify user
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(RouterService.LOCAL_BROADCAST_STATE_NOTIFICATION);
        filter.addAction(RouterService.LOCAL_BROADCAST_STATE_CHANGED);
        lbm.registerReceiver(onStateChange, filter);
    }

    private BroadcastReceiver onStateChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            State state = intent.getParcelableExtra(RouterService.LOCAL_BROADCAST_EXTRA_STATE);

            if (_startPressed && Util.getRouterContext() != null)
                _startPressed = false;

            // Update menus, FAMs etc.
            supportInvalidateOptionsMenu();

            // Update main paging state
            mViewPager.setPagingEnabled(!(Util.isStopping(state) || Util.isStopped(state)));

            // If I2P was started by another app and is running, return to that app
            if (state == State.RUNNING && mAutoStartFromIntent) {
                I2PActivity.this.setResult(RESULT_OK);
                finish();
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();

        if(status.autoStartProxy){
            status.autoStartProxy = false;
            overridePendingTransition(0, 0);
        }

        if(mViewPager!=null){
            if(!status.isOpenURL){
                status.currentProxyPagePublic = mViewPager.getCurrentItem();
            }
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        status.isOpenURL = false;

    }

    @Override
    public void onResume() {
        super.onResume();

        // Handle edge cases after shutting down router
        mViewPager.updatePagingState();

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(RouterService.LOCAL_BROADCAST_REQUEST_STATE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_base_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_help:
                Intent hi = new Intent(this, HelpActivity.class);
                switch (mViewPager.getCurrentItem()) {
                    case 1:
                        hi.putExtra(HelpActivity.CATEGORY, HelpActivity.CAT_I2PTUNNEL);
                        break;
                    case 2:
                        hi.putExtra(HelpActivity.CATEGORY, HelpActivity.CAT_ADDRESSBOOK);
                        break;
                }
                startActivity(hi);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onBackPressed() {
        if(status.autoStartProxy){
            moveTaskToBack(true);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(onStateChange);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!_keep) {
            Thread t = new Thread(new KillMe());
            t.start();
        }
    }

    private class KillMe implements Runnable {

        public void run() {
            Util.d("*********************************************************");
            Util.d("KillMe started!");
            Util.d("*********************************************************");
            try {
                Thread.sleep(500); // is 500ms long enough?
            } catch (InterruptedException ex) {
            }
            System.exit(0);
        }
    }

    private boolean canStart() {
        RouterService svc = _routerService;
        return (svc == null) || (!_isBound) || svc.canManualStart();
    }

    private boolean canStop() {
        RouterService svc = _routerService;
        return svc != null && _isBound && svc.canManualStop();
    }

    // MainFragment.RouterControlListener

    public boolean shouldShowOnOff() {
        return (canStart() && Connectivity.isConnected(this)) || (canStop() && !isGracefulShutdownInProgress());
    }

    public boolean shouldBeOn() {
        String action = getIntent().getAction();
        return (canStop()) ||
                (action != null && action.equals("net.i2p.android.router.START_I2P"));
    }

    public void onStartRouterClicked() {
        _startPressed = true;
        RouterService svc = _routerService;
        if (svc != null && _isBound) {
            setPref(PREF_AUTO_START, true);
            svc.manualStart();
        } else {
            (new File(Util.getFileDir(this), "wrapper.log")).delete();
            startRouter();
        }
    }

    public boolean onStopRouterClicked() {
        RouterService svc = _routerService;
        if (svc != null && _isBound) {
            setPref(PREF_AUTO_START, false);
            svc.manualQuit();
            return true;
        }
        return false;
    }

    /** @since 0.9.19 */
    public boolean isGracefulShutdownInProgress() {
        RouterService svc = _routerService;
        return svc != null && svc.isGracefulShutdownInProgress();
    }

    /** @since 0.9.19 */
    public boolean onGracefulShutdownClicked() {
        RouterService svc = _routerService;
        if(svc != null && _isBound) {
            setPref(PREF_AUTO_START, false);
            svc.gracefulShutdown();
            return true;
        }
        return false;
    }

    /** @since 0.9.19 */
    public boolean onCancelGracefulShutdownClicked() {
        RouterService svc = _routerService;
        if(svc != null && _isBound) {
            setPref(PREF_AUTO_START, false);
            svc.cancelGracefulShutdown();
            return true;
        }
        return false;
    }
}
