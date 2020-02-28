package net.i2p.android.router.netdb;

import android.content.Intent;
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
import net.i2p.android.router.util.Util;
import net.i2p.data.DataFormatException;
import net.i2p.data.Hash;

public class NetDbDetailActivity extends I2PActivityBase implements
        NetDbListFragment.OnEntrySelectedListener {
    NetDbDetailFragment mDetailFrag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onepane);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
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
            boolean isRI = getIntent().getBooleanExtra(NetDbDetailFragment.IS_RI, true);
            Hash hash = new Hash();
            try {
                hash.fromBase64(getIntent().getStringExtra(NetDbDetailFragment.ENTRY_HASH));
                mDetailFrag = NetDbDetailFragment.newInstance(isRI, hash);
                getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment, mDetailFrag).commit();
            } catch (DataFormatException e) {
                Util.e(e.toString());
            }
        }
    }

    // NetDbListFragment.OnEntrySelectedListener

    public void onEntrySelected(boolean isRouterInfo, Hash entryHash) {
        // Start the detail activity for the selected item ID.
        Intent detailIntent = new Intent(this, NetDbDetailActivity.class);
        detailIntent.putExtra(NetDbDetailFragment.IS_RI, isRouterInfo);
        detailIntent.putExtra(NetDbDetailFragment.ENTRY_HASH,
                entryHash.toBase64());
        startActivity(detailIntent);
    }
}
