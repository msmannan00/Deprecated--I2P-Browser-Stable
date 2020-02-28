package net.i2p.android.help;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.myapplication.R;
import net.i2p.android.util.LocaleManager;

import java.lang.reflect.Field;

public class BrowserConfigActivity extends AppCompatActivity implements
        BrowserAdapter.OnBrowserSelectedListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private final LocaleManager localeManager = new LocaleManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        localeManager.onCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.blue_dark));
        }
        else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));
        }

        // Set the action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color_v1));
        toolbar.setBackgroundColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.detail_fragment) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment, new BrowserListFragment())
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        localeManager.onResume(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // BrowserAdapter.OnBrowserSelected

    @Override
    public void onBrowserSelected(Browser browser) {
        int file;
        if (browser.isKnown) {
            if (browser.isSupported) {
                // Check for embedded browser
                if (browser.packageName.startsWith("net.i2p.android"))
                    file = R.raw.help_embedded_browser;
                else {
                    // Load the configuration guide for this browser
                    try {
                        String name = "help_" + browser.packageName.replace('.', '_');
                        Class res = R.raw.class;
                        Field field = res.getField(name);
                        file = field.getInt(null);
                    } catch (Exception e) {
                        file = R.raw.help_unknown_browser;
                    }
                }
            } else
                file = R.raw.help_unsupported_browser;
        } else
            file = R.raw.help_unknown_browser;
        HelpHtmlFragment configFrag = HelpHtmlFragment.newInstance(file);
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment, configFrag)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, configFrag)
                    .addToBackStack("config" + browser.packageName)
                    .commit();
        }
    }
}
