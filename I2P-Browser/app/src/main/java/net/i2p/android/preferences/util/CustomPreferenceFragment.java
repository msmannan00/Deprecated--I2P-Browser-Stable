package net.i2p.android.preferences.util;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Handles custom Preferences.
 */
public abstract class CustomPreferenceFragment extends PreferenceFragmentCompat {
    private static final String DIALOG_FRAGMENT_TAG =
            "android.support.v7.preference.PreferenceFragment.DIALOG";

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // check if dialog is already showing
        if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }

        DialogFragment f = null;
        if (preference instanceof ConnectionLimitPreference) {
            f = ConnectionLimitPreferenceDialog.newInstance(preference.getKey());
        } else if (preference instanceof IntEditTextPreference) {
            f = IntEditTextPreferenceDialog.newInstance(preference.getKey());
        } else if (preference instanceof PortPreference) {
            f = PortPreferenceDialog.newInstance(preference.getKey());
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
        if (f != null) {
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
        }
    }
}
