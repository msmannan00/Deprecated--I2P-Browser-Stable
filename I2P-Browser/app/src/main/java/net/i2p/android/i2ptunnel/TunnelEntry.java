package net.i2p.android.i2ptunnel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;

import net.i2p.android.i2ptunnel.util.SaveTunnelTask;
import net.i2p.android.i2ptunnel.util.TunnelUtil;
import com.example.myapplication.R;
import net.i2p.android.router.util.Util;
import net.i2p.data.Destination;
import net.i2p.data.PrivateKeyFile;
import net.i2p.i2ptunnel.TunnelController;
import net.i2p.i2ptunnel.TunnelControllerGroup;
import net.i2p.i2ptunnel.ui.TunnelConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *  A single tunnel.
 *  Stored by the TunnelEntryAdapter.
 */
public class TunnelEntry {
    public static final int RUNNING = 1;
    public static final int STARTING = 2;
    public static final int NOT_RUNNING = 3;
    public static final int STANDBY = 4;

    private final Context mContext;
    private final TunnelController mController;
    private final int mId;

    /**
     * @param tcg non-null
     * @return the new TunnelEntry, or null if there was an error.
     */
    public static TunnelEntry createNewTunnel(
            Context ctx,
            TunnelControllerGroup tcg,
            TunnelConfig cfg) {
        int tunnelId = tcg.getControllers().size();
        TunnelEntry ret = null;
        List<String> msgs = new ArrayList<>();
        SaveTunnelTask task = new SaveTunnelTask(tcg, -1, cfg);
        try {
            msgs.addAll(task.execute().get());
            TunnelController cur = TunnelUtil.getController(tcg, tunnelId);
            ret = new TunnelEntry(ctx, cur, tunnelId);
        } catch (InterruptedException e) {
            Util.e("Interrupted while saving tunnel config", e);
            msgs.add(ctx.getString(R.string.i2ptunnel_msg_config_save_failed));
        } catch (ExecutionException e) {
            Util.e("Error while saving tunnel config", e);
            msgs.add(ctx.getString(R.string.i2ptunnel_msg_config_save_failed));
        }
        // TODO: Do something else with the other messages.
        Toast.makeText(ctx.getApplicationContext(),
                msgs.get(0), Toast.LENGTH_LONG).show();
        return ret;
    }

    public TunnelEntry(Context context, TunnelController controller, int id) {
        mContext = context;
        mController = controller;
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public TunnelController getController() {
        return mController;
    }

    /* General tunnel data for any type */

    public String getName() {
        if (mController.getName() != null)
            return mController.getName();
        else
            return mContext.getResources()
                    .getString(R.string.i2ptunnel_new_tunnel);
    }

    public String getInternalType() {
        return mController.getType();
    }

    public String getType() {
        return TunnelUtil.getTypeName(mController.getType(), mContext);
    }

    public String getDescription() {
        String rv = mController.getDescription();
        if (rv != null)
            return rv;
        return "";
    }

    public boolean startAutomatically() {
        return mController.getStartOnLoad();
    }

    public int getStatus() {
        if (mController.getIsRunning()) {
            if (isClient() && mController.getIsStandby())
                return STANDBY;
            else
                return RUNNING;
        } else if (mController.getIsStarting()) return STARTING;
        else return NOT_RUNNING;
    }

    public boolean isRunning() {
        switch (getStatus()) {
            case STANDBY:
            case RUNNING:
                return true;
            default:
                return false;
        }
    }

    public boolean isClient() {
        return TunnelUtil.isClient(mController.getType());
    }

    /* Client tunnel data */

    public boolean isSharedClient() {
        return Boolean.parseBoolean(mController.getSharedClient());
    }

    /**
     * Call this to see if it is okay to linkify getClientLink()
     * @return true if getClientLink() can be linkified, false otherwise.
     */
    public boolean isClientLinkValid() {
        return ("ircclient".equals(mController.getType())) &&
                mController.getListenOnInterface() != null &&
                mController.getListenPort() != null;
    }

    /**
     * @return valid host:port only if isClientLinkValid() is true
     */
    public String getClientLink(boolean linkify) {
        String host = getClientInterface();
        String port = getClientPort();
        String link =  host + ":" + port;
        if (linkify) {
            if ("ircclient".equals(mController.getType()))
                link = "irc://" + link;
        }
        return link;
    }

    public String getClientInterface() {
        String rv;
        if ("streamrclient".equals(mController.getType()))
            rv = mController.getTargetHost();
        else
            rv = mController.getListenOnInterface();
        return rv != null ? rv : "";
    }

    public String getClientPort() {
        String rv = mController.getListenPort();
        return rv != null ? rv : "";
    }

    public String getClientDestination() {
        String rv;
        if ("client".equals(getInternalType()) ||
                "ircclient".equals(getInternalType()) ||
                "streamrclient".equals(getInternalType()))
            rv = mController.getTargetDestination();
        else
            rv = mController.getProxyList();
        return rv != null ? rv : "";
    }

    /* Server tunnel data */

    /**
     * Call this to see if it is okay to linkify getServerLink()
     * @return true if getServerLink() can be linkified, false otherwise.
     */
    public boolean isServerLinkValid() {
        return ("httpserver".equals(mController.getType()) ||
                "httpbidirserver".equals(mController.getType())) &&
                mController.getTargetHost() != null &&
                mController.getTargetPort() != null;
    }

    /**
     * @return valid host:port only if isServerLinkValid() is true
     */
    public String getServerLink(boolean linkify) {
        String host;
        if ("streamrserver".equals(getInternalType()))
            host = mController.getListenOnInterface();
        else
            host = mController.getTargetHost();
        String port = mController.getTargetPort();
        if (host == null) host = "";
        if (port == null) port = "";
        if (host.indexOf(':') >= 0)
            host = '[' + host + ']';
        String link =  host + ":" + port;
        if (linkify) {
            if ("httpserver".equals(mController.getType()) ||
                    "httpbidirserver".equals(mController.getType()))
                link = "http://" + link;
        }
        return link;
    }

    public String getDestinationBase64() {
        String rv = mController.getMyDestination();
        if (rv != null)
            return rv;
        // if not running, do this the hard way
        String keyFile = mController.getPrivKeyFile();
        if (keyFile != null && keyFile.trim().length() > 0) {
            PrivateKeyFile pkf = new PrivateKeyFile(keyFile);
            try {
                Destination d = pkf.getDestination();
                if (d != null)
                    return d.toBase64();
            } catch (Exception e) {}
        }
        return "";
    }

    public String getDestHashBase32() {
        String rv = mController.getMyDestHashBase32();
        if (rv != null)
            return rv;
        return "";
    }

    /* Data for some client and server tunnels */

    /* Other output formats */

    public boolean isTunnelLinkValid() {
        if (isClient()) return isClientLinkValid();
        else return isServerLinkValid();
    }

    public String getTunnelLink(boolean linkify) {
        if (isClient()) return getClientLink(linkify);
        else return getServerLink(linkify);
    }

    public Uri getRecommendedAppForTunnel() {
        int resId = 0;
        if ("ircclient".equals(mController.getType()))
            resId = R.string.market_irc;

        if (resId > 0)
            return Uri.parse(mContext.getString(resId));
        else
            return null;
    }

    public String getDetails() {
        String details;
        if (isClient())
            details = getClientDestination();
        else
            details = getDestHashBase32();
        return details;
    }

    public Drawable getStatusIcon() {
        switch (getStatus()) {
        case STANDBY:
            return mContext.getResources()
                    .getDrawable(R.drawable.ic_schedule_black_24dp);
        case STARTING:
            return mContext.getResources()
                    .getDrawable(R.drawable.ic_sync_black_24dp);
        case RUNNING:
        case NOT_RUNNING:
        default:
            return null;
        }
    }

    public Drawable getStatusBackground() {
        switch (getStatus()) {
            case STANDBY:
            case STARTING:
                return mContext.getResources()
                        .getDrawable(R.drawable.tunnel_yellow);
            case RUNNING:
                return mContext.getResources()
                        .getDrawable(R.drawable.tunnel_green);
            case NOT_RUNNING:
            default:
                return mContext.getResources()
                        .getDrawable(R.drawable.tunnel_red);
        }
    }
}
