package android.support.pager;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.example.myapplication.R;
import net.i2p.android.router.util.Util;

public class CustomViewPager extends ViewPager {
    private boolean mEnabled;
    private int mFixedPage;
    private int mFixedPageString;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEnabled = false;
        mFixedPage = -1;
        mFixedPageString = 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mEnabled && mFixedPage < 0 && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // See Nov. 20, 2013 comment at:
        // https://github.com/JakeWharton/ViewPagerIndicator/pull/257
        // Our ticket #2488

        // prevent NPE if fake dragging and touching ViewPager
        if(isFakeDragging()) return false;

        return mEnabled && mFixedPage < 0 && super.onInterceptTouchEvent(event);
    }

    @Override
    public void setCurrentItem(int item) {
        if ((mEnabled && (mFixedPage < 0 || item == mFixedPage))
                || (!mEnabled && item == 0))
            super.setCurrentItem(item);
        else if (!mEnabled)
            Toast.makeText(getContext(), Util.getRouterContext() == null ?
                    R.string.router_not_running : R.string.router_shutting_down,
                    Toast.LENGTH_SHORT).show();
        else if (mFixedPageString > 0)
            Toast.makeText(getContext(), getContext().getString(mFixedPageString),
                    Toast.LENGTH_SHORT).show();
    }

    public void setPagingEnabled(boolean enabled) {
        mEnabled = enabled;
        updatePagingState();
    }

    public void setFixedPage(int page, int res) {
        mFixedPage = page;
        mFixedPageString = res;
        updatePagingState();
    }

    public void updatePagingState() {
        if (mEnabled) {
            if (mFixedPage >= 0 && getCurrentItem() != mFixedPage)
                setCurrentItem(mFixedPage);

        } else if (getCurrentItem() != 0)
            setCurrentItem(0);
    }

    public static class SavedState extends ViewPager.SavedState {
        boolean enabled;
        int fixedPage;
        int fixedPageString;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(enabled ? 1 : 0);
            out.writeInt(fixedPage);
            out.writeInt(fixedPageString);
        }

        @Override
        public String toString() {
            return "CustomViewPager.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " enabled=" + enabled + " fixedPage=" + fixedPage + "}";
        }


    }

    /*
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.enabled = mEnabled;
        ss.fixedPage = mFixedPage;
        ss.fixedPageString = mFixedPageString;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());

        mEnabled = ss.enabled;
        mFixedPage = ss.fixedPage;
        mFixedPageString = ss.fixedPageString;
    }*/
}
