package net.i2p.android.util;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.darkweb.constants.status;
import com.example.myapplication.R;

import java.util.Locale;

public class LocaleManager {
    private Locale currentLocale;

    public void onCreate(Activity activity) {
        currentLocale = getSelectedLocale(activity);
        setContextLocale(activity, currentLocale);
    }

    public void onResume(Activity activity) {
        // If the activity has the incorrect locale, restart it
        if (!currentLocale.equals(getSelectedLocale(activity))) {
            Intent intent = activity.getIntent();
            activity.finish();
            activity.overridePendingTransition(0, 0);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
        }
    }

    public void updateServiceLocale(Service service) {
        currentLocale = getSelectedLocale(service);
        setContextLocale(service, currentLocale);
    }

    private static Locale getSelectedLocale(Context context) {
        String defaultLanguage = context.getString(R.string.DEFAULT_LANGUAGE);
        String selectedLanguage = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getResources().getString(R.string.PREF_LANGUAGE),
                defaultLanguage
        );
        String language[] = TextUtils.split(selectedLanguage, "_");

        if (language[0].equals(defaultLanguage))
            return Locale.getDefault();
        else if (language.length == 2)
            return new Locale(language[0], language[1]);
        else
            return new Locale(language[0]);
    }

    private static void setContextLocale(Context context, Locale selectedLocale) {
        Configuration configuration = context.getResources().getConfiguration();
            configuration.locale = new Locale(status.sLanguage);
            context.getResources().updateConfiguration(
                    configuration,
                    context.getResources().getDisplayMetrics()
            );
    }
}
