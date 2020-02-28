package net.i2p.android.help;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import net.i2p.android.router.LicenseActivity;
import com.example.myapplication.R;
import net.i2p.android.router.dialog.TextResourceDialog;
import net.i2p.android.util.LocaleManager;

public class HelpActivity extends AppCompatActivity implements
        HelpListFragment.OnEntrySelectedListener {
    public static final String CATEGORY = "help_category";
    public static final int CAT_MAIN = 0;
    public static final int CAT_CONFIGURE_BROWSER = 1;
    public static final int CAT_ADDRESSBOOK = 2;
    public static final int CAT_I2PTUNNEL = 3;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private int mCategory;

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
                    .add(R.id.main_fragment, new HelpListFragment())
                    .commit();
        }

        mCategory = getIntent().getIntExtra(CATEGORY, -1);
        if (mCategory >= 0) {
            showCategory(mCategory);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        localeManager.onResume(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_help_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mCategory >= 0) {
                    onBackPressed();
                } else {
                    Intent upIntent = NavUtils.getParentActivityIntent(this);
                    if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                        // This activity is NOT part of this app's task, so create a new task
                        // when navigating up, with a synthesized back stack.
                        TaskStackBuilder.create(this)
                                // Add all of this activity's parents to the back stack
                                .addNextIntentWithParentStack(upIntent)
                                        // Navigate up to the closest parent
                                .startActivities();
                    } else {
                        // This activity is part of this app's task, so simply
                        // navigate up to the logical parent activity.
                        NavUtils.navigateUpTo(this, upIntent);
                    }
                }
                return true;
            case R.id.menu_help_licenses:
                Intent lic = new Intent(HelpActivity.this, LicenseActivity.class);
                startActivity(lic);
                return true;
            case R.id.menu_help_release_notes:
                TextResourceDialog dialog = new TextResourceDialog();
                Bundle args = new Bundle();
                args.putString(TextResourceDialog.TEXT_DIALOG_TITLE,
                        getResources().getString(R.string.label_release_notes));
                args.putInt(TextResourceDialog.TEXT_RESOURCE_ID, R.raw.releasenotes);
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "release_notes");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mCategory >= 0)
            mCategory = -1;
    }

    // HelpListFragment.OnEntrySelectedListener

    @Override
    public void onEntrySelected(int entry) {
        if (entry == CAT_CONFIGURE_BROWSER) {
            Intent i = new Intent(this, BrowserConfigActivity.class);
            startActivity(i);
        } else {
            mCategory = entry;
            showCategory(entry);
        }
    }

    private void showCategory(int category) {
        int file;
        switch (category) {
            case CAT_ADDRESSBOOK:
                file = R.raw.help_addressbook;
                break;

            case CAT_I2PTUNNEL:
                file = R.raw.help_i2ptunnel;
                break;

            case CAT_MAIN:
            default:
                file = R.raw.help_main;
                break;
        }
        HelpHtmlFragment f = HelpHtmlFragment.newInstance(file);
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment, f).commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, f)
                    .addToBackStack("help" + category)
                    .commit();
        }
    }
}
