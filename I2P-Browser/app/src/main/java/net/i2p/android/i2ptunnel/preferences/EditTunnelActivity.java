package net.i2p.android.i2ptunnel.preferences;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import net.i2p.android.i2ptunnel.TunnelDetailActivity;
import net.i2p.android.i2ptunnel.TunnelDetailFragment;
import com.example.myapplication.R;
import net.i2p.android.util.LocaleManager;

public class EditTunnelActivity extends AppCompatActivity {
    private int mTunnelId;

    private final LocaleManager localeManager = new LocaleManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        localeManager.onCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        // Set the action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.blue_dark));
        }
        else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));
        }

        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color_v1));
        toolbar.setBackgroundColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            mTunnelId = getIntent().getIntExtra(TunnelDetailFragment.TUNNEL_ID, 0);
            Fragment editFrag = GeneralTunnelPreferenceFragment.newInstance(mTunnelId);
            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment, editFrag).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        localeManager.onResume(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            Intent intent = new Intent(this, TunnelDetailActivity.class);
            intent.putExtra(TunnelDetailFragment.TUNNEL_ID, mTunnelId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        return true;
    }
}
