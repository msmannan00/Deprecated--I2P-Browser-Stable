package net.i2p.android.router.netdb;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.i2p.android.router.I2PFragmentBase;
import com.example.myapplication.R;
import net.i2p.android.router.util.Util;
import net.i2p.util.ObjectCounter;

import java.util.List;

public class NetDbSummaryPagerFragment extends I2PFragmentBase implements
        LoaderManager.LoaderCallbacks<List<ObjectCounter<String>>> {
    private NetDbPagerAdapter mNetDbPagerAdapter;
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.parentfragment_viewpager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up NetDbPagerAdapter containing the categories
        mNetDbPagerAdapter = new NetDbPagerAdapter(getChildFragmentManager());

        // Set up ViewPager for swiping between categories
        mViewPager = (ViewPager) getActivity().findViewById(R.id.pager);
        mViewPager.setAdapter(mNetDbPagerAdapter);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        mViewPager.setCurrentItem(position);
                    }
                });
    }

    @Override
    public void onRouterConnectionReady() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onRouterConnectionNotReady() {
        Util.d("Router not running or not bound to NetDbSummaryPagerFragment");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_netdb_list_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_refresh:
            if (getRouterContext() != null) {
                Util.d("Refresh called, restarting Loader");
                mNetDbPagerAdapter.setData(null);
                mViewPager.invalidate();
                getLoaderManager().restartLoader(0, null, this);
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public class NetDbPagerAdapter extends FragmentStatePagerAdapter {
        private List<ObjectCounter<String>> mData;

        public NetDbPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setData(List<ObjectCounter<String>> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int i) {
            if (mData == null)
                return null;

            return NetDbSummaryTableFragment.newInstance(i, mData.get(i));
        }

        @Override
        public int getCount() {
            if (mData == null)
                return 0;
            else
                return 2;
        }

        @Override
        public CharSequence getPageTitle(int i) {
            switch (i) {
            case 1:
            //    return getString(R.string.countries);
            //case 2:
                return getString(R.string.settings_label_transports);
            default:
                return getString(R.string.versions);
            } 
        }
    }

    // LoaderManager.LoaderCallbacks<List<ObjectCounter<String>>>

    public Loader<List<ObjectCounter<String>>> onCreateLoader(int id, Bundle args) {
        return new NetDbStatsLoader(getActivity(), getRouterContext());
    }

    public void onLoadFinished(Loader<List<ObjectCounter<String>>> loader,
            List<ObjectCounter<String>> data) {
        mNetDbPagerAdapter.setData(data);
    }

    public void onLoaderReset(Loader<List<ObjectCounter<String>>> loader) {
        mNetDbPagerAdapter.setData(null);
    }
}
