package com.darkweb.constants;

import com.darkweb.dataManager.dataController;

import net.i2p.android.router.util.Util;
import net.i2p.data.Destination;
import net.i2p.data.Hash;
import net.i2p.data.LeaseSet;
import net.i2p.router.RouterContext;

import java.util.ArrayList;
import java.util.List;

import static org.mozilla.geckoview.ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY;

public class status
{
    /*App Status*/

    public static String current_ABI = "7.0";
    public static boolean paid_status = false;

    /*Settings Status*/

    public static String sSearchStatus = constants.BACKEND_GENESIS_URL;
    public static String sRedirectStatus = strings.EMPTY_STR;
    public static String sLanguage = "en";

    public static boolean sIsAppPaused = false;
    public static boolean sIsWelcomeEnabled = true;
    public static boolean sIsAppStarted = false;
    public static boolean sIsAppRated = false;
    public static boolean sFontAdjustable = true;
    public static boolean sJavaStatus = true;
    public static boolean sHistoryStatus = true;
    public static boolean autoStartProxy = false;
    public static boolean hasAppStarted = false;
    public static boolean sIsTorInitialized = false;
    public static boolean isOpenURL = false;

    public static float sFontSize = 1;

    public static int currentProxyPagePublic = 0;
    public static int sCookieStatus = ACCEPT_FIRST_PARTY;
    public static int sNotificationStatus = 0;

    public static boolean isProxyRunning(){

        RouterContext ctx =  Util.getRouterContext();
        List<Destination> clients = null;

        if(ctx==null){
            return false;
        }else {
            if (ctx.clientManager() != null){
                clients = new ArrayList<Destination>(ctx.clientManager().listClients());
                for (Destination client : clients) {

                    Hash h = client.calculateHash();

                    LeaseSet ls = ctx.netDb().lookupLeaseSetLocally(h);
                    if (ls != null && ctx.tunnelManager().getOutboundClientTunnelCount(h) > 0) {
                        long timeToExpire = ls.getEarliestLeaseDate() - ctx.clock().now();
                        if (timeToExpire < 0) {
                            return false;
                        } else {
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
                return false;
            }
            else {
                return false;
            }
        }
    }


    public static void initStatus()
    {
        status.sJavaStatus = dataController.getInstance().getBool(keys.JAVA_SCRIPT,true);
        status.sHistoryStatus = dataController.getInstance().getBool(keys.HISTORY_CLEAR,true);
        status.sSearchStatus = dataController.getInstance().getString(keys.SEARCH_ENGINE,constants.BACKEND_GENESIS_URL);
        status.sIsWelcomeEnabled = dataController.getInstance().getBool(keys.IS_WELCOME_ENABLED,true);
        status.sIsAppRated = dataController.getInstance().getBool(keys.IS_APP_RATED,false);
        status.sFontSize = dataController.getInstance().getFloat(keys.FONT_SIZE,100);
        status.sFontAdjustable = dataController.getInstance().getBool(keys.FONT_ADJUSTABLE,true);
        status.sCookieStatus = dataController.getInstance().getInt(keys.COOKIE_ADJUSTABLE,ACCEPT_FIRST_PARTY);
        status.sLanguage = dataController.getInstance().getString(keys.LANGUAGE,strings.DEFAULT_LANGUAGE);
        status.sNotificationStatus = dataController.getInstance().getInt(keys.NOTIFICATION_STATUS,0);
    }

}
