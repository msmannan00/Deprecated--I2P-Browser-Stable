package net.i2p.android.i2ptunnel.preferences;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import net.i2p.android.i2ptunnel.util.TunnelLogic;
import net.i2p.android.i2ptunnel.util.TunnelUtil;
import com.example.myapplication.R;

public class AdvancedTunnelPreferenceFragment extends BaseTunnelPreferenceFragment {
    public static AdvancedTunnelPreferenceFragment newInstance(int tunnelId) {
        AdvancedTunnelPreferenceFragment f = new AdvancedTunnelPreferenceFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TUNNEL_ID, tunnelId);
        f.setArguments(args);
        return f;
    }

    @Override
    protected void loadPreferences() {
        String type = TunnelUtil.getController(mGroup, mTunnelId).getType();
        new TunnelPreferences(type).runLogic();
    }

    class TunnelPreferences extends TunnelLogic {
        PreferenceScreen ps;
        PreferenceCategory tunParamCategory;

        public TunnelPreferences(String type) {
            super(type);
        }

        @Override
        protected void general() {
        }

        @Override
        protected void generalClient() {
        }

        @Override
        protected void generalClientStreamr(boolean isStreamr) {
        }

        @Override
        protected void generalClientPort() {
        }

        @Override
        protected void generalClientPortStreamr(boolean isStreamr) {
        }

        @Override
        protected void generalClientProxy(boolean isProxy) {
        }

        @Override
        protected void generalClientProxyHttp(boolean isHttp) {
        }

        @Override
        protected void generalClientStandardOrIrc(boolean isStandardOrIrc) {
        }

        @Override
        protected void generalClientIrc() {
        }

        @Override
        protected void generalServerHttp() {
        }

        @Override
        protected void generalServerHttpBidirOrStreamr(boolean isStreamr) {
        }

        @Override
        protected void generalServerPort() {
        }

        @Override
        protected void generalServerPortStreamr(boolean isStreamr) {
        }

        @Override
        protected void advanced() {
            addPreferencesFromResource(R.xml.tunnel_adv);
            ps = getPreferenceScreen();
            tunParamCategory = (PreferenceCategory) ps.findPreference(
                    getString(R.string.TUNNEL_CAT_TUNNEL_PARAMS));
        }

        @Override
        protected void advancedStreamr(boolean isStreamr) {
            if (isStreamr)
                tunParamCategory.removePreference(tunParamCategory.findPreference(getString(R.string.TUNNEL_OPT_PROFILE)));
        }

        @Override
        protected void advancedServerOrStreamrClient(boolean isServerOrStreamrClient) {
            if (isServerOrStreamrClient)
                tunParamCategory.removePreference(tunParamCategory.findPreference(getString(R.string.TUNNEL_OPT_DELAY_CONNECT)));
        }

        @Override
        protected void advancedServer() {
            addPreferencesFromResource(R.xml.tunnel_adv_server);
        }

        @Override
        protected void advancedServerHttp(boolean isHttp) {
            if (isHttp)
                addPreferencesFromResource(R.xml.tunnel_adv_server_http);
            else {
                PreferenceCategory accessCtlCategory = (PreferenceCategory) ps.findPreference(
                        getString(R.string.TUNNEL_CAT_ACCESS_CONTROL));
                accessCtlCategory.removePreference(accessCtlCategory.findPreference(getString(R.string.TUNNEL_OPT_REJECT_INPROXY)));
            }
        }

        @Override
        protected void advancedIdle() {
            addPreferencesFromResource(R.xml.tunnel_adv_idle);
        }

        @Override
        protected void advancedIdleServerOrStreamrClient(boolean isServerOrStreamrClient) {
            if (isServerOrStreamrClient)
                ps.removePreference(ps.findPreference(getString(R.string.TUNNEL_OPT_DELAY_OPEN)));
        }

        @Override
        protected void advancedClient() {
            PreferenceCategory idleCategory = (PreferenceCategory) ps.findPreference(
                    getString(R.string.TUNNEL_CAT_IDLE)
            );
            addPreferencesFromResource(R.xml.tunnel_adv_idle_client, idleCategory);

            // PERSISTENT_KEY and NEW_KEYS can't be set simultaneously
            final CheckBoxPreference nk = (CheckBoxPreference) findPreference(getString(R.string.TUNNEL_OTP_NEW_KEYS));
            nk.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
                    if ((Boolean) o && prefs.getBoolean(getString(R.string.TUNNEL_OPT_PERSISTENT_KEY),
                            getResources().getBoolean(R.bool.DEFAULT_PERSISTENT_KEY))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.new_keys_on_reopen_conflict_title)
                                .setMessage(R.string.new_keys_on_reopen_conflict_msg)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putBoolean(getString(R.string.TUNNEL_OPT_PERSISTENT_KEY), false);
                                        editor.apply();
                                        nk.setChecked(true);
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                        builder.show();
                        return false;
                    } else
                        return true;
                }
            });
        }

        @Override
        protected void advancedClientHttp() {
            addPreferencesFromResource(R.xml.tunnel_adv_client_http);
        }

        @Override
        protected void advancedClientProxy() {
            addPreferencesFromResource(R.xml.tunnel_adv_client_proxy);
        }

        @Override
        protected void advancedOther() {
            addPreferencesFromResource(R.xml.tunnel_adv_other);
        }
    }
}
