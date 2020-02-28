package net.i2p.android.help;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.example.myapplication.R;

import org.sufficientlysecure.htmltextview.HtmlTextView;

public class HelpHtmlFragment extends Fragment {
    public static final String ARG_HTML_FILE = "htmlFile";

    static HelpHtmlFragment newInstance(int htmlFile) {
        HelpHtmlFragment f = new HelpHtmlFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_HTML_FILE, htmlFile);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView scroller = new ScrollView(getActivity());
        HtmlTextView text = new HtmlTextView(getActivity());
        scroller.addView(text);
        int padH = getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
        int padV = getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
        text.setPadding(padH, padV, padH, padV);
        text.setHtml(getArguments().getInt(ARG_HTML_FILE));
        return scroller;
    }
}
