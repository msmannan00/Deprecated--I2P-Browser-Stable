package net.i2p.android.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.example.myapplication.R;
import net.i2p.android.router.SettingsActivity;
import net.i2p.android.router.service.StatSummarizer;
import net.i2p.android.router.util.Util;
import net.i2p.router.RouterContext;
import net.i2p.stat.FrequencyStat;
import net.i2p.stat.Rate;
import net.i2p.stat.RateStat;
import net.i2p.stat.StatManager;

import java.util.Map;
import java.util.SortedSet;

public class GraphsPreferenceFragment extends I2PreferenceFragment {
    public static final String GRAPH_PREFERENCES_SEEN = "graphPreferencesSeen";

    @Override
    public void onCreatePreferences(Bundle paramBundle, String s) {
        addPreferencesFromResource(R.xml.settings_graphs);
        setupGraphSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.label_graphs);
    }

    private void setupGraphSettings() {
        PreferenceScreen ps = getPreferenceScreen();
        RouterContext ctx = Util.getRouterContext();
        if (ctx == null) {
            PreferenceCategory noRouter = new PreferenceCategory(getActivity());
            noRouter.setTitle(R.string.router_not_running);
            ps.addPreference(noRouter);
            ps.setIconSpaceReserved(false);
            noRouter.setIconSpaceReserved(false);
        } else if (StatSummarizer.instance() == null) {
            PreferenceCategory noStats = new PreferenceCategory(getActivity());
            noStats.setIconSpaceReserved(false);
            noStats.setTitle(R.string.stats_not_ready);
            ps.setIconSpaceReserved(false);
            ps.addPreference(noStats);
        } else {
            StatManager mgr = ctx.statManager();
            Map<String, SortedSet<String>> all = mgr.getStatsByGroup();


            for (String group : all.keySet()) {
                SortedSet<String> stats = all.get(group);
                if (stats.size() == 0) continue;
                PreferenceCategory groupPrefs = new PreferenceCategory(getActivity());
                groupPrefs.setKey("stat.groups." + group);
                groupPrefs.setTitle(group);
                ps.addPreference(groupPrefs);
                ps.setIconSpaceReserved(false);
                for (String stat : stats) {
                    String key;
                    String description;
                    boolean canBeGraphed = false;
                    boolean currentIsGraphed = false;
                    RateStat rs = mgr.getRate(stat);
                    if (rs != null) {
                        description = rs.getDescription();
                        long period = rs.getPeriods()[0]; // should be the minimum
                        key = stat + "." + period;
                        if (period <= 10*60*1000) {
                            Rate r = rs.getRate(period);
                            canBeGraphed = r != null;
                            if (canBeGraphed) {
                                currentIsGraphed = r.getSummaryListener() != null;
                            }
                        }
                        ps.setIconSpaceReserved(false);
                    } else {

                        FrequencyStat fs = mgr.getFrequency(stat);
                        if (fs != null) {
                            key = stat;
                            description = fs.getDescription();
                            // FrequencyStats cannot be graphed, but can be logged.
                            // XXX: Should log settings be here as well, or in a
                            // separate settings menu?
                        } else {
                            Util.e("Stat does not exist?!  [" + stat + "]");
                            continue;
                        }
                    }
                    CheckBoxPreference statPref = new CheckBoxPreference(getActivity());
                    statPref.setKey("stat.summaries." + key);
                    statPref.setTitle(stat);
                    statPref.setSummary(description);
                    statPref.setEnabled(canBeGraphed);
                    statPref.setChecked(currentIsGraphed);
                    statPref.setIconSpaceReserved(false);
                    groupPrefs.addPreference(statPref);
                    groupPrefs.setIconSpaceReserved(false);
                }
            }

            // The user has now seen the current (possibly default) configuration
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (!prefs.getBoolean(GRAPH_PREFERENCES_SEEN, false))
                prefs.edit()
                        .putBoolean(GRAPH_PREFERENCES_SEEN, true)
                        .apply();
        }
    }
}
