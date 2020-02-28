package net.i2p.android.router.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.darkweb.constants.status;
import com.example.myapplication.R;

import net.i2p.android.router.receiver.I2PReceiver;
import net.i2p.android.router.util.Connectivity;
import net.i2p.android.router.util.Notifications;
import net.i2p.android.router.util.Util;
import net.i2p.android.util.LocaleManager;
import net.i2p.data.DataHelper;
import net.i2p.router.Job;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;

import java.lang.ref.WeakReference;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

/**
 * Runs the router
 */
public class RouterService extends Service {

    /**
     * A request to this service for the current router state. Broadcasting
     * this will trigger a state notification.
     */
    public static final String LOCAL_BROADCAST_REQUEST_STATE = "net.i2p.android.LOCAL_BROADCAST_REQUEST_STATE";
    /**
     * A notification of the current state. This is informational; the state
     * has not changed.
     */
    public static final String LOCAL_BROADCAST_STATE_NOTIFICATION = "net.i2p.android.LOCAL_BROADCAST_STATE_NOTIFICATION";
    /**
     * The state has just changed.
     */
    public static final String LOCAL_BROADCAST_STATE_CHANGED = "net.i2p.android.LOCAL_BROADCAST_STATE_CHANGED";
    public static final String LOCAL_BROADCAST_EXTRA_STATE = "net.i2p.android.STATE";
    /**
     * The locale has just changed.
     */
    public static final String LOCAL_BROADCAST_LOCALE_CHANGED = "net.i2p.android.LOCAL_BROADCAST_LOCALE_CHANGED";

    private LocaleManager localeManager = new LocaleManager();

    private RouterContext _context;
    private String _myDir;
    //private String _apkPath;
    private State _state = State.INIT;
    private Thread _starterThread;
    private StatusBar _statusBar;
    private Notifications _notif;
    private I2PReceiver _receiver;
    private IBinder _binder;
    private final Object _stateLock = new Object();
    private Handler _handler;
    private Runnable _updater;
    private static final String SHARED_PREFS = "net.i2p.android.router";
    private static final String LAST_STATE = "service.lastState";
    private static final String EXTRA_RESTART = "restart";
    private static final String MARKER = "**************************************  ";

    /**
     * This is a list of callbacks that have been registered with the
     * service.  Note that this is package scoped (instead of private) so
     * that it can be accessed more efficiently from inner classes.
     */
    final RemoteCallbackList<IRouterStateCallback> mStateCallbacks
            = new RemoteCallbackList<>();

    @Override
    public void onCreate() {
        State lastState = getSavedState();
        setState(State.INIT);
        Util.d(this + " onCreate called"
                + " Saved state is: " + lastState
                + " Current state is: " + _state);

        //(new File(getFilesDir(), "wrapper.log")).delete();
        _myDir = getFilesDir().getAbsolutePath();
        // init other stuff here, delete log, etc.
        Init init = new Init(this);
        init.initialize();
        //_apkPath = init.getAPKPath();
        _statusBar = new StatusBar(this);
        // Remove stale notification icon.
        _statusBar.remove();
        _notif = new Notifications(this);
        _binder = new RouterBinder(this);
        _handler = new Handler();
        _updater = new Updater();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(onStateRequested, new IntentFilter(LOCAL_BROADCAST_REQUEST_STATE));
        lbm.registerReceiver(onLocaleChanged, new IntentFilter(LOCAL_BROADCAST_LOCALE_CHANGED));
        if(lastState == State.RUNNING || lastState == State.ACTIVE) {
            Intent intent = new Intent(this, RouterService.class);
            intent.putExtra(EXTRA_RESTART, true);
            onStartCommand(intent, START_FLAG_REDELIVERY | START_FLAG_RETRY, 67890);
        } else if(lastState == State.MANUAL_QUITTING || lastState == State.GRACEFUL_SHUTDOWN) {
            synchronized(_stateLock) {
                setState(State.MANUAL_QUITTED);
                stopSelf(); // Die.
            }
        }
    }

    private BroadcastReceiver onStateRequested = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Broadcast the current state within this app.
            Intent ni = new Intent(LOCAL_BROADCAST_STATE_NOTIFICATION);
            ni.putExtra(LOCAL_BROADCAST_EXTRA_STATE, (android.os.Parcelable) _state);
            LocalBroadcastManager.getInstance(RouterService.this).sendBroadcast(ni);
        }
    };

    private BroadcastReceiver onLocaleChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            localeManager.updateServiceLocale(RouterService.this);
        }
    };

    /**
     * NOT called by system if it restarts us after a crash
     */

    private final static String NOTIFICATION_CHANNEL_ID = "orbot_channel_1";

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel ()
    {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// The id of the channel.

// The user-visible name of the channel.
        CharSequence name = getString(R.string.app_name);
// The user-visible description of the channel.
        String description = getString(R.string.app_description);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
// Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(false);
        mChannel.enableVibration(false);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private Notification startServiceOreoCondition(){
        if (Build.VERSION.SDK_INT >= 26) {


            String CHANNEL_ID = "TOR_SERVICE";
            String CHANNEL_NAME = "BACKGROUND_SERVICE";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,NotificationManager.IMPORTANCE_NONE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("I2P Status")
                    .setContentText("I2P Network Running")
                    .setCategory(Notification.CATEGORY_SERVICE).setSmallIcon(R.drawable.ic_stat_router_starting).setPriority(PRIORITY_MIN).build();

            return notification;
        }else {
            NotificationCompat.Builder b = new NotificationCompat.Builder(this);

            b.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setTicker("Hearty365")
                    .setContentTitle("Default notification")
                    .setContentText("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                    .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                    .setContentInfo("Info");
            return b.getNotification();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Util.d(this + " onStart called"
                + " Intent is: " + intent
                + " Flags is: " + flags
                + " ID is: " + startId
                + " Current state is: " + _state);
        boolean restart = intent != null && intent.getBooleanExtra(EXTRA_RESTART, false);
        if(restart) {
            Util.d(this + " RESTARTING");
        }
        synchronized(_stateLock) {
            if(_state != State.INIT) //return START_STICKY;
            {
                return START_NOT_STICKY;
            }
            _receiver = new I2PReceiver(this);
            if(Connectivity.isConnected(this)) {
                if(restart) {
                    _statusBar.replace(StatusBar.ICON_STARTING, R.string.notification_status_restarting);
                } else {
                    _statusBar.replace(StatusBar.ICON_STARTING, R.string.notification_status_starting);
                }
                setState(State.STARTING);
                _starterThread = new Thread(new Starter());
                _starterThread.start();
            } else {
                _statusBar.replace(StatusBar.ICON_WAITING_NETWORK, R.string.notification_status_waiting);
                setState(State.WAITING);
                _handler.postDelayed(new Waiter(), 10 * 1000);
            }
        }
        _handler.removeCallbacks(_updater);
        _handler.postDelayed(_updater, 50);
        if(!restart) {
            if(_statusBar!=null && _statusBar.getNote()!=null){
                Log.i("SHIT4","SHIT4");
                startForeground(1337, _statusBar.getNote());
            }else {
                Log.i("SHIT5","SHIT5");
                startForeground(1337, startServiceOreoCondition());
            }
        }

        //return START_STICKY;
        return START_NOT_STICKY;
    }

    /**
     * maybe this goes away when the receiver can bind to us
     */
    private class Waiter implements Runnable {

        public void run() {
            Util.d(MARKER + this + " waiter handler"
                    + " Current state is: " + _state);
            if(_state == State.WAITING) {
                if(Connectivity.isConnected(RouterService.this)) {
                    synchronized(_stateLock) {
                        if(_state != State.WAITING) {
                            return;
                        }
                        _statusBar.replace(StatusBar.ICON_STARTING, R.string.notification_status_starting_after_waiting);
                        setState(State.STARTING);
                        _starterThread = new Thread(new Starter());
                        _starterThread.start();
                    }
                    return;
                }
                _handler.postDelayed(this, 15 * 1000);
            }
        }
    }

    private class Starter implements Runnable {

        public void run() {
            Util.d(MARKER + this + " starter thread"
                    + " Current state is: " + _state);
            //Util.d(MARKER + this + " JBigI speed test started");
            //NativeBigInteger.main(null);
            //Util.d(MARKER + this + " JBigI speed test finished, launching router");

            // Launch the router!
            // TODO Store this somewhere instead of relying on global context?
            Router r = new Router();
            r.setUPnPScannerCallback(new SSDPLocker(RouterService.this));
            r.runRouter();
            synchronized(_stateLock) {
                if(_state != State.STARTING) {
                    return;
                }
                setState(State.RUNNING);
                _statusBar.replace(StatusBar.ICON_RUNNING, R.string.notification_status_running);
                _context = r.getContext();
                if (_context == null) {
                    throw new IllegalStateException("Router has no context?");
                }
                _context.router().setKillVMOnEnd(false);
                Job loadJob = new LoadClientsJob(RouterService.this, _context, _notif);
                _context.jobQueue().addJob(loadJob);
                _context.addShutdownTask(new ShutdownHook());
                _context.addFinalShutdownTask(new FinalShutdownHook());
                _starterThread = null;
            }
            Util.d("Router.main finished");
        }
    }

    private class Updater implements Runnable {

        public void run() {
            RouterContext ctx = _context;
            if(ctx != null && (_state == State.RUNNING || _state == State.ACTIVE || _state == State.GRACEFUL_SHUTDOWN)) {
                Router router = ctx.router();
                if(router.isAlive()) {
                    updateStatus(ctx);
                }
            }
            _handler.postDelayed(this, 15 * 1000);
        }
    }
    private String _currTitle;
    private boolean _hadTunnels;

    private void updateStatus(RouterContext ctx) {
        int active = ctx.commSystem().countActivePeers();
        int known = Math.max(ctx.netDb().getKnownRouters() - 1, 0);
        int inEx = ctx.tunnelManager().getFreeTunnelCount();
        int outEx = ctx.tunnelManager().getOutboundTunnelCount();
        int inCl = ctx.tunnelManager().getInboundClientTunnelCount();
        int outCl = ctx.tunnelManager().getOutboundClientTunnelCount();
        String uptime = DataHelper.formatDuration(ctx.router().getUptime());
        double inBW = ctx.bandwidthLimiter().getReceiveBps();
        double outBW = ctx.bandwidthLimiter().getSendBps();

        String text =
                getResources().getString(R.string.notification_status_text,
                        Util.formatSpeed(inBW), Util.formatSpeed(outBW));

        String bigText =
                getResources().getString(R.string.notification_status_bw,
                        Util.formatSpeed(inBW), Util.formatSpeed(outBW)) + '\n'
                + getResources().getString(R.string.notification_status_peers,
                        active, known) + '\n'
                + getResources().getString(R.string.notification_status_expl,
                        inEx, outEx) + '\n'
                + getResources().getString(R.string.notification_status_client,
                        inCl, outCl);

        boolean haveTunnels = inCl > 0 && outCl > 0;
        if (isGracefulShutdownInProgress()) {
            long ms = ctx.router().getShutdownTimeRemaining();
            if (ms > 1000) {
                _currTitle = getString(R.string.notification_status_graceful, DataHelper.formatDuration(ms));
            } else {
                _currTitle = getString(R.string.notification_status_stopping);
            }
        } else if (haveTunnels != _hadTunnels) {
            if(haveTunnels) {
                _currTitle = getString(R.string.notification_status_client_ready);
                status.sIsTorInitialized = true;
                setState(State.ACTIVE);
                _statusBar.replace(StatusBar.ICON_ACTIVE, _currTitle);
            } else {
                _currTitle = getString(R.string.notification_status_client_down);
                setState(State.RUNNING);
                _statusBar.replace(StatusBar.ICON_RUNNING, _currTitle);
            }
            _hadTunnels = haveTunnels;
        } else if (_currTitle == null || _currTitle.equals(""))
            _currTitle = getString(R.string.notification_status_running);
        _statusBar.update(_currTitle, text, bigText);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Util.d(this + "onBind called"
                + " Current state is: " + _state);
        Util.d("Intent action: " + intent.getAction());
        // Select the interface to return.
        if (RouterBinder.class.getName().equals(intent.getAction())) {
            // Local Activity wanting access to the RouterContext
            Util.d("Returning RouterContext binder");
            return _binder;
        }
        if (IRouterState.class.getName().equals(intent.getAction())) {
            // Someone wants to monitor the router state.
            Util.d("Returning state binder");
            return mStatusBinder;
        }
        Util.d("Unknown binder request, returning null");
        return null;
    }

    /**
     * IRouterState is defined through IDL
     */
    private final IRouterState.Stub mStatusBinder = new IRouterState.Stub() {

        public void registerCallback(IRouterStateCallback cb)
                throws RemoteException {
            if (cb != null) mStateCallbacks.register(cb);
        }

        public void unregisterCallback(IRouterStateCallback cb)
                throws RemoteException {
            if (cb != null) mStateCallbacks.unregister(cb);
        }

        public boolean isStarted() throws RemoteException {
            return _state != State.INIT &&
                    _state != State.STOPPED &&
                    _state != State.MANUAL_STOPPED &&
                    _state != State.MANUAL_QUITTED;
        }

        public State getState() throws RemoteException {
            return _state;
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    // ******** following methods may be accessed from Activities and Receivers ************
    /**
     * @return null if router is not running
     */
    public RouterContext getRouterContext() {
        RouterContext rv = _context;
        if(rv == null) {
            return null;
        }
        if(!rv.router().isAlive()) {
            return null;
        }
        if(_state != State.RUNNING
                && _state != State.ACTIVE
                && _state != State.STOPPING
                && _state != State.MANUAL_STOPPING
                && _state != State.MANUAL_QUITTING
                && _state != State.NETWORK_STOPPING
                && _state != State.GRACEFUL_SHUTDOWN) {
            return null;
        }
        return rv;
    }

    /**
     * debug
     */
    public String getState() {
        return _state.toString();
    }

    public boolean canManualStop() {
        return _state == State.WAITING ||
               _state == State.RUNNING || _state == State.ACTIVE ||
               _state == State.GRACEFUL_SHUTDOWN;
    }

    /**
     * Stop and don't restart the router, but keep the service
     *
     * Apparently unused - see manualQuit()
     */
    public void manualStop() {
        Util.d("manualStop called"
                + " Current state is: " + _state);
        status.sIsTorInitialized = false;
        synchronized(_stateLock) {
            if(!canManualStop()) {
                return;
            }
            if(_state == State.STARTING) {
                _starterThread.interrupt();
            }
            if(_state == State.STARTING || _state == State.RUNNING ||
               _state == State.ACTIVE || _state == State.GRACEFUL_SHUTDOWN) {
                _statusBar.replace(StatusBar.ICON_STOPPING, R.string.notification_status_stopping);
                Thread stopperThread = new Thread(new Stopper(State.MANUAL_STOPPING, State.MANUAL_STOPPED));
                stopperThread.start();
            }
        }
    }

    /**
     * Stop the router and kill the service
     */
    public void manualQuit() {
        Util.d("manualQuit called"
                + " Current state is: " + _state);
        status.sIsTorInitialized = false;
        synchronized(_stateLock) {
            if(!canManualStop()) {
                return;
            }
            if(_state == State.STARTING) {
                _starterThread.interrupt();
            }
            if(_state == State.STARTING || _state == State.RUNNING ||
               _state == State.ACTIVE || _state == State.GRACEFUL_SHUTDOWN) {
                _statusBar.replace(StatusBar.ICON_STOPPING, R.string.notification_status_stopping);
                Thread stopperThread = new Thread(new Stopper(State.MANUAL_QUITTING, State.MANUAL_QUITTED));
                stopperThread.start();
            } else if(_state == State.WAITING) {
                setState(State.MANUAL_QUITTING);
                (new FinalShutdownHook()).run();
            }
        }
    }

    /**
     * Stop and then spin waiting for a network connection, then restart
     */
    public void networkStop() {
        Util.d("networkStop called"
                + " Current state is: " + _state);

        synchronized(_stateLock) {
            if(_state == State.STARTING) {
                _starterThread.interrupt();
            }
            if(_state == State.STARTING || _state == State.RUNNING ||
               _state == State.ACTIVE || _state == State.GRACEFUL_SHUTDOWN) {
                _statusBar.replace(StatusBar.ICON_STOPPING, R.string.notification_status_stopping_after_net);
                // don't change state, let the shutdown hook do it
                Thread stopperThread = new Thread(new Stopper(State.NETWORK_STOPPING, State.NETWORK_STOPPING));
                stopperThread.start();
            }
        }
    }

    public boolean canManualStart() {
        // We can be in INIT if we restarted after crash but previous state was not RUNNING.
        return _state == State.INIT || _state == State.MANUAL_STOPPED || _state == State.STOPPED  || _state == State.MANUAL_QUITTED ;
    }

    public void manualStart() {
        Util.d("restart called"
                + " Current state is: " + _state);
        synchronized(_stateLock) {
            if(!canManualStart()) {
                return;
            }
            _statusBar.replace(StatusBar.ICON_STARTING, R.string.notification_status_starting);
            setState(State.STARTING);
            _starterThread = new Thread(new Starter());
            _starterThread.start();
        }
    }

    /**
     * Graceful Shutdown
     *
     * @since 0.9.19
     */
    public boolean isGracefulShutdownInProgress() {
        if (_state == State.GRACEFUL_SHUTDOWN) {
            RouterContext ctx = _context;
            return ctx != null && ctx.router().gracefulShutdownInProgress();
        }
        return false;
    }

    private String _oldTitle;
    /**
     * Graceful Shutdown
     *
     * @since 0.9.19
     */
    public void gracefulShutdown() {
        Util.d("gracefulShutdown called"
                + " Current state is: " + _state);
        status.sIsTorInitialized = false;
        synchronized(_stateLock) {
            if(!canManualStop()) {
                return;
            }
            if(_state == State.STARTING || _state == State.WAITING) {
                manualQuit();
                return;
            }
            if(_state == State.RUNNING || _state == State.ACTIVE) {
                RouterContext ctx = _context;
                if(ctx != null && ctx.router().isAlive()) {
                    int part = ctx.tunnelManager().getParticipatingCount();
                    if(part <= 0) {
                        manualQuit();
                    } else {
                        ctx.router().shutdownGracefully();
                        _oldTitle = _currTitle;
                        long ms = ctx.router().getShutdownTimeRemaining();
                        if (ms > 1000) {
                            _statusBar.replace(
                                    StatusBar.ICON_STOPPING,
                                    getString(R.string.notification_status_graceful, DataHelper.formatDuration(ms))
                                    );
                        } else {
                            _statusBar.replace(StatusBar.ICON_STOPPING, R.string.notification_status_stopping);
                        }
                        setState(State.GRACEFUL_SHUTDOWN);
                    }
                }
            }
        }
    }

    /**
     * Cancel Graceful Shutdown
     *
     * @since 0.9.19
     */
    public void cancelGracefulShutdown() {
        Util.d("cancelGracefulShutdown called"
                + " Current state is: " + _state);
        synchronized(_stateLock) {
            if(_state != State.GRACEFUL_SHUTDOWN) {
                return;
            }
            RouterContext ctx = _context;
            if(ctx != null && ctx.router().isAlive()) {
                ctx.router().cancelGracefulShutdown();
                _currTitle = _oldTitle;
                if (_hadTunnels) {
                    setState(State.ACTIVE);
                    _statusBar.replace(StatusBar.ICON_ACTIVE, R.string.notification_status_shutdown_cancelled);
                } else {
                    setState(State.RUNNING);
                    _statusBar.replace(StatusBar.ICON_RUNNING, R.string.notification_status_shutdown_cancelled);
                }
            }
        }
    }



    // ******** end methods accessed from Activities and Receivers ************

    private static final int STATE_MSG = 1;

    /**
     * Our Handler used to execute operations on the main thread.
     */
    private final Handler mHandler = new StateHandler(new WeakReference<>(this));
    private static class StateHandler extends Handler {
        WeakReference<RouterService> mReference;

        public StateHandler(WeakReference<RouterService> reference) {
            mReference = reference;
        }

        @Override
        public void handleMessage(Message msg) {
            RouterService parent = mReference.get();
            if (parent == null)
                return;

            switch (msg.what) {
            case STATE_MSG:
                final State state = parent._state;
                // Broadcast to all clients the new state.
                final int N = parent.mStateCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        parent.mStateCallbacks.getBroadcastItem(i).stateChanged(state);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing
                        // the dead object for us.
                    }
                }
                parent.mStateCallbacks.finishBroadcast();
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    /**
     * Turn off the status bar. Unregister the receiver. If we were running,
     * fire up the Stopper thread.
     */
    @Override
    public void onDestroy() {
        Util.d("onDestroy called"
                + " Current state is: " + _state);

        _handler.removeCallbacks(_updater);
        _statusBar.remove();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(onStateRequested);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onLocaleChanged);

        I2PReceiver rcvr = _receiver;
        if(rcvr != null) {
            synchronized(rcvr) {
                try {
                    // throws if not registered
                    unregisterReceiver(rcvr);
                } catch(IllegalArgumentException iae) {
                }
                //rcvr.unbindRouter();
                //_receiver = null;
            }
        }
        synchronized(_stateLock) {
            if(_state == State.STARTING) {
                _starterThread.interrupt();
            }
            if(_state == State.STARTING || _state == State.RUNNING ||
               _state == State.ACTIVE || _state == State.GRACEFUL_SHUTDOWN) {
                // should this be in a thread?
                _statusBar.replace(StatusBar.ICON_SHUTTING_DOWN, R.string.notification_status_shutting_down);
                Thread stopperThread = new Thread(new Stopper(State.STOPPING, State.STOPPED));
                stopperThread.start();
            }
        }
    }

    /**
     * Transition to the next state. If we still have a context, shut down the
     * router. Turn off the status bar. Then transition to the stop state.
     */
    private class Stopper implements Runnable {

        private final State nextState;
        private final State stopState;

        /**
         * call holding statelock
         */
        public Stopper(State next, State stop) {
            nextState = next;
            stopState = stop;
            setState(next);
        }

        public void run() {
            try {
                Util.d(MARKER + this + " stopper thread"
                        + " Current state is: " + _state);
                RouterContext ctx = _context;
                if(ctx != null) {
                    ctx.router().shutdown(Router.EXIT_HARD);
                }
                _statusBar.remove();
                Util.d("********** Router shutdown complete");
                synchronized(_stateLock) {
                    if(_state == nextState) {
                        setState(stopState);
                    }
                }
            } finally {
                stopForeground(true);
                _statusBar.remove();
            }
        }
    }

    /**
     * First (early) hook. Update the status bar. Unregister the receiver.
     */
    private class ShutdownHook implements Runnable {

        public void run() {
            Util.d(this + " shutdown hook"
                    + " Current state is: " + _state);
            _statusBar.replace(StatusBar.ICON_SHUTTING_DOWN, R.string.notification_status_shutting_down);
            I2PReceiver rcvr = _receiver;
            if(rcvr != null) {
                synchronized(rcvr) {
                    try {
                        // throws if not registered
                        unregisterReceiver(rcvr);
                    } catch(IllegalArgumentException iae) {
                    }
                    //rcvr.unbindRouter();
                    //_receiver = null;
                }
            }
            synchronized(_stateLock) {
                // null out to release the memory
                _context = null;
                if(_state == State.STARTING) {
                    _starterThread.interrupt();
                }
                if(_state == State.WAITING || _state == State.STARTING
                        || _state == State.RUNNING || _state == State.ACTIVE
                        || _state == State.GRACEFUL_SHUTDOWN) {
                    setState(State.STOPPING);
                }
            }
        }
    }

    /**
     * Second (late) hook. Turn off the status bar. Null out the context. If we
     * were stopped manually, do nothing. If we were stopped because of no
     * network, start the waiter thread. If it stopped of unknown causes or from
     * manualQuit(), kill the Service.
     */
    private class FinalShutdownHook implements Runnable {

        public void run() {
            try {
                Util.d(this + " final shutdown hook"
                        + " Current state is: " + _state);
                //I2PReceiver rcvr = _receiver;

                synchronized(_stateLock) {
                    // null out to release the memory
                    _context = null;
                    Runtime.getRuntime().gc();
                    if(_state == State.STARTING) {
                        _starterThread.interrupt();
                    }
                    if(_state == State.MANUAL_STOPPING) {
                        setState(State.MANUAL_STOPPED);
                    } else if(_state == State.NETWORK_STOPPING) {
                        // start waiter handler
                        setState(State.WAITING);
                        _handler.postDelayed(new Waiter(), 10 * 1000);
                    } else if(_state == State.STARTING || _state == State.RUNNING
                            || _state == State.ACTIVE || _state == State.STOPPING) {
                        Util.w(this + " died of unknown causes");
                        setState(State.STOPPED);
                        // Unregister all callbacks.
                        mStateCallbacks.kill();
                        stopForeground(true);
                        stopSelf();
                    } else if(_state == State.MANUAL_QUITTING || _state == State.GRACEFUL_SHUTDOWN) {
                        setState(State.MANUAL_QUITTED);
                        // Unregister all callbacks.
                        mStateCallbacks.kill();
                        stopForeground(true);
                        stopSelf();
                    }
                }
            } finally {
                _statusBar.remove();
            }
        }
    }

    private State getSavedState() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, 0);
        String stateString = prefs.getString(LAST_STATE, State.INIT.toString());
        try {
            return State.valueOf(stateString);
        } catch (IllegalArgumentException e) {
            return State.INIT;
        }
    }

    private void setState(State s) {
        _state = s;
        saveState();

        // Broadcast the new state within this app.
        Intent intent = new Intent(LOCAL_BROADCAST_STATE_CHANGED);
        intent.putExtra(LOCAL_BROADCAST_EXTRA_STATE, (android.os.Parcelable) _state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // Notify other apps that the state has changed
        mHandler.sendEmptyMessage(STATE_MSG);
    }

    /**
     * Saves state in background thread
     */
    private void saveState() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(LAST_STATE, _state.toString());
        edit.apply();
    }
}
