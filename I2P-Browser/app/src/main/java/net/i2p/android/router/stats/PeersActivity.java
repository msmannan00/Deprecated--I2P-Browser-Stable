package net.i2p.android.router.stats;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import net.i2p.android.I2PActivityBase;
import com.example.myapplication.R;
import net.i2p.android.router.service.RouterService;

public class PeersActivity extends I2PActivityBase {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onepane);

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

        // Start with the base view
        if (savedInstanceState == null) {
            PeersFragment f = new PeersFragment();
            f.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment, f).commit();
        }
    }

    /**
     *  Not bound by the time onResume() is called, so we have to do it here.
     *  If it is bound we update twice.
     */
    @Override
    protected void onRouterBind(RouterService svc) {
        PeersFragment f = (PeersFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        f.update();
    }

    @Override
    public void onBackPressed() {
        PeersFragment f = (PeersFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        if (!f.onBackPressed())
            super.onBackPressed();
    }
}
