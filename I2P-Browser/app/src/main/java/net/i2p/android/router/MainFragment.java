package net.i2p.android.router;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.darkweb.constants.status;
import com.example.myapplication.R;

import net.i2p.android.I2PActivity;
import net.i2p.android.I2PActivityBase;
import net.i2p.android.help.BrowserConfigActivity;
import net.i2p.android.router.dialog.FirstStartDialog;
import net.i2p.android.router.service.RouterService;
import net.i2p.android.router.service.State;
import net.i2p.android.router.util.Connectivity;
import net.i2p.android.router.util.LongToggleButton;
import net.i2p.android.router.util.Util;
import net.i2p.data.DataHelper;
import net.i2p.data.Destination;
import net.i2p.data.Hash;
import net.i2p.data.LeaseSet;
import net.i2p.router.RouterContext;
import net.i2p.router.TunnelPoolSettings;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainFragment extends I2PFragmentBase {

    private Handler _handler;
    private Runnable _updater;
    private Runnable _oneShotUpdate;
    private String _savedStatus;

    private ImageView mConsoleLights;
    private LongToggleButton mOnOffButton;
    private LinearLayout vGracefulButtons;
    private ScrollView mScrollView;
    private View vStatusContainer;
    private ImageView vNetStatusLevel;
    private TextView vNetStatusText;
    private View vNonNetStatus;
    private TextView vUptime;
    private TextView vActive;
    private TextView vKnown;
    private TableLayout vTunnels;
    private LinearLayout vAdvStatus;
    private TextView vAdvStatusText;

    private static final String PREF_CONFIGURE_BROWSER = "app.dialog.configureBrowser";
    private static final String PREF_CONFIGURE_BATTERY = "app.dialog.configureBattery";
    private static final String PREF_FIRST_START = "app.router.firstStart";
    private static final String PREF_SHOW_STATS = "i2pandroid.main.showStats";
    protected static final String PROP_NEW_INSTALL = "i2p.newInstall";
    protected static final String PROP_NEW_VERSION = "i2p.newVersion";
    public static RouterControlListener mCallback;

    // Container Activity must implement this interface
    public interface RouterControlListener {
        boolean shouldShowOnOff();

        boolean shouldBeOn();

        void onStartRouterClicked();

        boolean onStopRouterClicked();

        /**
         * @since 0.9.19
         */
        boolean isGracefulShutdownInProgress();

        /**
         * @since 0.9.19
         */
        boolean onGracefulShutdownClicked();

        /**
         * @since 0.9.19
         */
        boolean onCancelGracefulShutdownClicked();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (I2PActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RouterControlListener");
        }

    }

    /**
     * Called when the fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Init stuff here so settings work.
        if (savedInstanceState != null) {
            lastRouterState = savedInstanceState.getParcelable("lastState");
            String saved = savedInstanceState.getString("status");
            if (saved != null) {
                _savedStatus = saved;
            }
        }

        _handler = new Handler();
        _updater = new Updater();
        _oneShotUpdate = new OneShotUpdate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        mConsoleLights = (ImageView) v.findViewById(R.id.console_lights);
        mOnOffButton = (LongToggleButton) v.findViewById(R.id.router_onoff_button);
        vGracefulButtons = (LinearLayout) v.findViewById(R.id.router_graceful_buttons);
        mScrollView = (ScrollView) v.findViewById(R.id.main_scrollview);
        vStatusContainer = v.findViewById(R.id.status_container);
        vNetStatusLevel = (ImageView) v.findViewById(R.id.console_net_status_level);
        vNetStatusText = (TextView) v.findViewById(R.id.console_net_status_text);
        vNonNetStatus = v.findViewById(R.id.console_non_net_status_container);
        vUptime = (TextView) v.findViewById(R.id.console_uptime);
        vActive = (TextView) v.findViewById(R.id.console_active);
        vKnown = (TextView) v.findViewById(R.id.console_known);
        vTunnels = (TableLayout) v.findViewById(R.id.main_tunnels);
        vAdvStatus = (LinearLayout) v.findViewById(R.id.console_advanced_status);
        vAdvStatusText = (TextView) v.findViewById(R.id.console_advanced_status_text);

        updateState(lastRouterState);

        mOnOffButton.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View view) {
                boolean on = ((ToggleButton) view).isChecked();
                if (on) {
                    mCallback.onStartRouterClicked();
                    //updateOneShot();
                    checkFirstStart();
                } else if (mCallback.onGracefulShutdownClicked()){
                    //updateOneShot();
                }

                return true;
            }
        });

        Button gb = (Button) v.findViewById(R.id.button_shutdown_now);
        gb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mCallback.isGracefulShutdownInProgress())
                    if (mCallback.onStopRouterClicked())
                        updateOneShot();
                return true;
            }
        });
        gb = (Button) v.findViewById(R.id.button_cancel_graceful);
        gb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mCallback.isGracefulShutdownInProgress())
                    if (mCallback.onCancelGracefulShutdownClicked())
                        updateOneShot();
                return true;
            }
        });

        updateOneShot();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        _handler.removeCallbacks(_updater);
        _handler.removeCallbacks(_oneShotUpdate);
        if (_savedStatus != null) {
            TextView tv = (TextView) getActivity().findViewById(R.id.console_advanced_status_text);
            tv.setText(_savedStatus);
        }
        checkDialog();
        _handler.postDelayed(_updater, 100);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());

        IntentFilter filter = new IntentFilter();
        filter.addAction(RouterService.LOCAL_BROADCAST_STATE_NOTIFICATION);
        filter.addAction(RouterService.LOCAL_BROADCAST_STATE_CHANGED);
        lbm.registerReceiver(onStateChange, filter);

        lbm.sendBroadcast(new Intent(RouterService.LOCAL_BROADCAST_REQUEST_STATE));
    }

    private State lastRouterState;
    private BroadcastReceiver onStateChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            State state = intent.getParcelableExtra(RouterService.LOCAL_BROADCAST_EXTRA_STATE);
            if (lastRouterState == null || lastRouterState != state) {
                updateState(state);
                // If we have stopped, clear the status info immediately
                if (Util.isStopped(state)) {
                    updateOneShot();
                }
                lastRouterState = state;
            }
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        _handler.removeCallbacks(_updater);
        _handler.removeCallbacks(_oneShotUpdate);

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onStateChange);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateOneShot();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (lastRouterState != null)
            outState.putParcelable("lastState", lastRouterState);
        if (_savedStatus != null)
            outState.putString("status", _savedStatus);
        super.onSaveInstanceState(outState);
    }

    private void updateOneShot() {
        _handler.postDelayed(_oneShotUpdate, 10);
    }

    private class OneShotUpdate implements Runnable {

        public void run() {
            updateVisibility();
            try {
                updateStatus();
            } catch (NullPointerException npe) {
                // RouterContext wasn't quite ready
                Util.w("Status was updated before RouterContext was ready", npe);
            }
        }
    }

    private class Updater implements Runnable {


        private int counter;
        private final int delay = 1000;
        private final int toloop = delay / 500;
        public void run() {
            updateVisibility();
            if (counter++ % toloop == 0) {
                try {
                    updateStatus();
                } catch (NullPointerException npe) {
                    // RouterContext wasn't quite ready
                    Util.w("Status was updated before RouterContext was ready", npe);
                }
            }
            //_handler.postDelayed(this, 2500);
            _handler.postDelayed(this, delay);
        }
    }

    private void updateVisibility() {
        boolean showOnOff = mCallback.shouldShowOnOff();
        //mOnOffButton.setVisibility(showOnOff ? View.VI    SIBLE : View.GONE);

        boolean isOn = mCallback.shouldBeOn();
        if(showOnOff){
            mOnOffButton.setChecked(isOn);
        }

        boolean isGraceful = mCallback.isGracefulShutdownInProgress();
        vGracefulButtons.setVisibility(isGraceful ? View.VISIBLE : View.GONE);
        if (isOn && isGraceful) {
            RouterContext ctx = getRouterContext();
            if (ctx != null) {
                TextView tv = (TextView) vGracefulButtons.findViewById(R.id.router_graceful_status);
                long ms = ctx.router().getShutdownTimeRemaining();
                if (ms > 1000) {
                    tv.setText(getActivity().getResources().getString(R.string.button_router_graceful,
                            DataHelper.formatDuration(ms)));
                } else {
                    tv.setText(getActivity().getString(R.string.notification_status_stopping));
                }
            }
        }
    }

    /**
     *  Changes the logo based on the state.
     */
    private void updateState(State newState) {
        if (newState == State.INIT ||
                newState == State.STOPPED ||
                newState == State.MANUAL_STOPPED ||
                newState == State.MANUAL_QUITTED ||
                newState == State.NETWORK_STOPPED) {
            mConsoleLights.setImageResource(R.drawable.routerlogo_0);
        } else if (newState == State.STARTING ||
                newState == State.STOPPING ||
                newState == State.MANUAL_STOPPING ||
                newState == State.MANUAL_QUITTING ||
                newState == State.NETWORK_STOPPING) {
            mConsoleLights.setImageResource(R.drawable.routerlogo_1);
        } else if (newState == State.RUNNING ||
                   newState == State.GRACEFUL_SHUTDOWN) {
            mConsoleLights.setImageResource(R.drawable.routerlogo_2);
        } else if (newState == State.ACTIVE) {
            mConsoleLights.setImageResource(R.drawable.routerlogo_3);
        } else if (newState == State.WAITING) {
            mConsoleLights.setImageResource(R.drawable.routerlogo_4);
        } // Ignore unknown states.
    }

    private void updateStatus() {
        RouterContext ctx = getRouterContext();

        if (!Connectivity.isConnected(getActivity())) {
            // Manually set state, RouterService won't be running
            updateState(State.WAITING);
            vNetStatusText.setText(R.string.no_internet);
            vStatusContainer.setVisibility(View.VISIBLE);
            vNonNetStatus.setVisibility(View.GONE);
        } else if (lastRouterState != null &&
                !Util.isStopping(lastRouterState) &&
                !Util.isStopped(lastRouterState) &&
                ctx != null) {
            Util.NetStatus netStatus = Util.getNetStatus(getActivity(), ctx);
            switch (netStatus.level) {
                case ERROR:
                    vNetStatusLevel.setImageDrawable(getResources().getDrawable(R.drawable.ic_error_red_24dp));
                    vNetStatusLevel.setVisibility(View.VISIBLE);
                    break;
                case WARN:
                    vNetStatusLevel.setImageDrawable(getResources().getDrawable(R.drawable.ic_warning_amber_24dp));
                    vNetStatusLevel.setVisibility(View.VISIBLE);
                    break;
                case INFO:
                default:
                    vNetStatusLevel.setVisibility(View.GONE);
            }
            vNetStatusText.setText(getString(R.string.settings_label_network) + ": " + netStatus.status);

            String uptime = DataHelper.formatDuration(ctx.router().getUptime());
            int active = ctx.commSystem().countActivePeers();
            int known = Math.max(ctx.netDb().getKnownRouters() - 1, 0);
            vUptime.setText(uptime);
            vActive.setText(Integer.toString(active));
            vKnown.setText(Integer.toString(known));

            // Load running tunnels
            loadDestinations(ctx);

            if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(PREF_SHOW_STATS, false)) {
                int inEx = ctx.tunnelManager().getFreeTunnelCount();
                int outEx = ctx.tunnelManager().getOutboundTunnelCount();
                int inCl = ctx.tunnelManager().getInboundClientTunnelCount();
                int outCl = ctx.tunnelManager().getOutboundClientTunnelCount();
                int part = ctx.tunnelManager().getParticipatingCount();
                double dLag = ctx.statManager().getRate("jobQueue.jobLag").getRate(60000).getAverageValue();
                String jobLag = DataHelper.formatDuration((long) dLag);
                String msgDelay = DataHelper.formatDuration(ctx.throttle().getMessageDelay());

                String tunnelStatus = ctx.throttle().getTunnelStatus();
                //ctx.commSystem().getReachabilityStatus();

                String status =
                        "Exploratory Tunnels in/out: " + inEx + " / " + outEx
                                + "\nClient Tunnels in/out: " + inCl + " / " + outCl;


                // Need to see if we have the participation option set to on.
                // I thought there was a router method for that? I guess not! WHY NOT?
                // It would be easier if we had a number to test status.
                String participate = "\nParticipation: " + tunnelStatus + " (" + part + ")";

                String details =
                        "\nMemory: " + DataHelper.formatSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                                + "B / " + DataHelper.formatSize(Runtime.getRuntime().maxMemory()) + 'B'
                                + "\nJob Lag: " + jobLag
                                + "\nMsg Delay: " + msgDelay;

                _savedStatus = status + participate + details;
                vAdvStatusText.setText(_savedStatus);
                vAdvStatus.setVisibility(View.VISIBLE);
            } else {
                vAdvStatus.setVisibility(View.GONE);
            }
            vStatusContainer.setVisibility(View.VISIBLE);
            vNonNetStatus.setVisibility(View.VISIBLE);

            // Usage stats in bottom toolbar

            double inBw = ctx.bandwidthLimiter().getReceiveBps();
            double outBw = ctx.bandwidthLimiter().getSendBps();
            double inData = ctx.bandwidthLimiter().getTotalAllocatedInboundBytes();
            double outData = ctx.bandwidthLimiter().getTotalAllocatedOutboundBytes();

            ((TextView) getActivity().findViewById(R.id.console_download_stats)).setText(
                    Util.formatSpeed(inBw) + "B / " + Util.formatSize(inData) + "B");
            ((TextView) getActivity().findViewById(R.id.console_upload_stats)).setText(
                    Util.formatSpeed(outBw) + "B / " + Util.formatSize(outData) + "B");

            getActivity().findViewById(R.id.console_usage_stats).setVisibility(View.VISIBLE);
        } else {
            // network but no router context
            vStatusContainer.setVisibility(View.GONE);
            getActivity().findViewById(R.id.console_usage_stats).setVisibility(View.INVISIBLE);
            updateState(State.STOPPED);
            /**
             * **
             * RouterService svc = _routerService; String status = "connected? "
             * + Util.isConnected(this) + "\nMemory: " +
             * DataHelper.formatSize(Runtime.getRuntime().totalMemory() -
             * Runtime.getRuntime().freeMemory()) + "B / " +
             * DataHelper.formatSize(Runtime.getRuntime().maxMemory()) + 'B' +
             * "\nhave ctx? " + (ctx != null) + "\nhave svc? " + (svc != null) +
             * "\nis bound? " + _isBound + "\nsvc state: " + (svc == null ?
             * "null" : svc.getState()) + "\ncan start? " + (svc == null ?
             * "null" : svc.canManualStart()) + "\ncan stop? " + (svc == null ?
             * "null" : svc.canManualStop()); tv.setText(status);
             * tv.setVisibility(View.VISIBLE);
             ***
             */
        }
    }

    /**
     * Based on net.i2p.router.web.SummaryHelper.getDestinations()
     *
     * @param ctx The RouterContext
     */
    private void loadDestinations(RouterContext ctx) {
        vTunnels.removeAllViews();

        List<Destination> clients = null;
        if (ctx.clientManager() != null)
            clients = new ArrayList<Destination>(ctx.clientManager().listClients());

        if (clients != null && !clients.isEmpty()) {
            Collections.sort(clients, new AlphaComparator(ctx));
            for (Destination client : clients) {
                String name = getName(ctx, client);
                Hash h = client.calculateHash();
                TableRow dest = new TableRow(getActivity());
                dest.setPadding(16, 4, 0, 4);

                // Client or server
                TextView type = new TextView(getActivity());
                type.setTextColor(getResources().getColor(android.R.color.primary_text_light));
                type.setTypeface(Typeface.DEFAULT_BOLD);
                type.setGravity(Gravity.CENTER);
                if (ctx.clientManager().shouldPublishLeaseSet(h))
                    type.setText(R.string.char_server_tunnel);
                else
                    type.setText(R.string.char_client_tunnel);
                dest.addView(type);

                // Name
                TextView destName = new TextView(getActivity());
                destName.setPadding(16, 0, 0, 0);
                destName.setGravity(Gravity.CENTER_VERTICAL);
                destName.setTextColor(Color.WHITE);
                destName.setText(name);
                dest.addView(destName);

                // Status
                LeaseSet ls = ctx.netDb().lookupLeaseSetLocally(h);
                if (ls != null && ctx.tunnelManager().getOutboundClientTunnelCount(h) > 0) {
                    long timeToExpire = ls.getEarliestLeaseDate() - ctx.clock().now();
                    if (timeToExpire < 0) {
                        // red or yellow light
                        type.setBackgroundResource(R.drawable.tunnel_yellow);
                    } else {
                        // green light
                        type.setBackgroundResource(R.drawable.tunnel_green);

                    }
                } else {
                    // yellow light
                    type.setBackgroundResource(R.drawable.tunnel_yellow);
                }

                vTunnels.addView(dest);
            }
        } else {
            TableRow empty = new TableRow(getActivity());
            TextView emptyText = new TextView(getActivity());
            emptyText.setTextColor(Color.WHITE);
            emptyText.setText(R.string.no_tunnels_running);
            empty.addView(emptyText);
            vTunnels.addView(empty);
        }
    }

    private static final String SHARED_CLIENTS = "shared clients";

    /**
     * compare translated nicknames - put "shared clients" first in the sort
     */
    private class AlphaComparator implements Comparator<Destination> {
        private final String xsc;
        private final RouterContext _ctx;

        public AlphaComparator(RouterContext ctx) {
            _ctx = ctx;
            xsc = _t(ctx, SHARED_CLIENTS);
        }

        public int compare(Destination lhs, Destination rhs) {
            String lname = getName(_ctx, lhs);
            String rname = getName(_ctx, rhs);
            if (lname.equals(xsc))
                return -1;
            if (rname.equals(xsc))
                return 1;
            return Collator.getInstance().compare(lname, rname);
        }
    }

    /**
     * translate here so collation works above
     */
    private String getName(RouterContext ctx, Destination d) {
        TunnelPoolSettings in = ctx.tunnelManager().getInboundSettings(d.calculateHash());
        String name = (in != null ? in.getDestinationNickname() : null);
        if (name == null) {
            TunnelPoolSettings out = ctx.tunnelManager().getOutboundSettings(d.calculateHash());
            name = (out != null ? out.getDestinationNickname() : null);
        }

        if (name == null)
            name = d.calculateHash().toBase64().substring(0, 6);
        else
            name = _t(ctx, name);

        return name;
    }

    private String _t(RouterContext ctx, String s) {
        if (SHARED_CLIENTS.equals(s))
            return getString(R.string.shared_clients);
        else
            return s;
    }

    private void checkDialog() {
        if(status.hasAppStarted){
            return;
        }
        final I2PActivityBase ab = (I2PActivityBase) getActivity();
        String language = PreferenceManager.getDefaultSharedPreferences(ab).getString(
                getString(R.string.PREF_LANGUAGE), null
        );
        if (ab.getPref(PREF_CONFIGURE_BROWSER, true)) {
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            b.setTitle(R.string.configure_browser_title)
                    .setMessage(R.string.configure_browser_for_i2p)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                            ab.setPref(PREF_CONFIGURE_BROWSER, false);
                            Intent hi = new Intent(getActivity(), BrowserConfigActivity.class);
                            startActivity(hi);
                            checkDialog();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                            ab.setPref(PREF_CONFIGURE_BROWSER, false);
                            checkDialog();
                        }
                    })
                    .show();
        } else if (ab.getPref(PREF_CONFIGURE_BATTERY, true)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // only for Marshmallow and newer versions
                final Intent intent = new Intent();
                final Context mContext = ab.getApplicationContext();
                String packageName = mContext.getPackageName();
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                    b.setTitle(R.string.configure_no_doze_title)
                            .setMessage(R.string.configure_no_doze)
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    String packageName = mContext.getPackageName();
                                    dialog.dismiss();
                                    ab.setPref(PREF_CONFIGURE_BATTERY, true);
                                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                    intent.setData(Uri.parse("package:" + packageName));
                                    try {
                                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent);
                                    } catch (ActivityNotFoundException activityNotFound) {
                                        ab.setPref(PREF_CONFIGURE_BATTERY, true);
                                    }
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.cancel();
                                    ab.setPref(PREF_CONFIGURE_BATTERY, false);
                                }
                            })
                            .show();
                }
            } else {
                ab.setPref(PREF_CONFIGURE_BATTERY, false);
            }
        }
        /*VersionDialog dialog = new VersionDialog();
        String oldVersion = ((I2PActivityBase) getActivity()).getPref(PREF_INSTALLED_VERSION, "??");
        if(oldVersion.equals("??")) {
            // TODO Don't show this dialog until it is reworked
            Bundle args = new Bundle();
            args.putInt(VersionDialog.DIALOG_TYPE, VersionDialog.DIALOG_NEW_INSTALL);
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), "newinstall");
        } else {
            // TODO Don't show dialog on new version until we have something new to tell them
            String currentVersion = Util.getOurVersion(getActivity());
            if(!oldVersion.equals(currentVersion)) {
                Bundle args = new Bundle();
                args.putInt(VersionDialog.DIALOG_TYPE, VersionDialog.DIALOG_NEW_VERSION);
                dialog.setArguments(args);
                dialog.show(getActivity().getSupportFragmentManager(), "newversion");
            }
        }*/
    }

    private void checkFirstStart() {
        if(status.hasAppStarted){
            return;
        }

        I2PActivityBase ab = (I2PActivityBase) getActivity();
        boolean firstStart = ab.getPref(PREF_FIRST_START, true);
        if (firstStart && !ab.isFinishing()) {
            FirstStartDialog dialog = new FirstStartDialog();
            dialog.show(ab.getSupportFragmentManager(), "firststart");
            ab.setPref(PREF_FIRST_START, false);
        }
    }
}
