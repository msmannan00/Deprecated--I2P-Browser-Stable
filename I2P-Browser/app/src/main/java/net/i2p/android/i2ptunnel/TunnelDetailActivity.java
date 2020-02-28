package net.i2p.android.i2ptunnel;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import net.i2p.android.I2PActivityBase;
import net.i2p.android.i2ptunnel.preferences.EditTunnelActivity;
import com.example.myapplication.R;

public class TunnelDetailActivity extends I2PActivityBase implements
        TunnelDetailFragment.TunnelDetailListener {
    private boolean transitionReversed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transitionReversed = false;

        if (savedInstanceState == null) {
            int tunnelId = getIntent().getIntExtra(TunnelDetailFragment.TUNNEL_ID, 0);
            TunnelDetailFragment detailFrag = TunnelDetailFragment.newInstance(tunnelId);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, detailFrag).commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
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

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void finish() {
        if (transitionReversed)
            super.finish();
        else {
            transitionReversed = true;
            ActivityCompat.finishAfterTransition(this);
        }
    }

    // TunnelDetailFragment.TunnelDetailListener

    @Override
    public void onEditTunnel(int tunnelId) {
        Intent editIntent = new Intent(this, EditTunnelActivity.class);
        editIntent.putExtra(TunnelDetailFragment.TUNNEL_ID, tunnelId);
        startActivity(editIntent);
    }

    public void onTunnelDeleted(int tunnelId, int numTunnelsLeft) {
        finish();
    }
}
