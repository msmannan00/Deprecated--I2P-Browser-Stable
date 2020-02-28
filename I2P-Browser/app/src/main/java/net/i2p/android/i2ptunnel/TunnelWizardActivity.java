package net.i2p.android.i2ptunnel;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.R;
import net.i2p.android.wizard.model.AbstractWizardModel;
import net.i2p.android.wizard.ui.AbstractWizardActivity;

public class TunnelWizardActivity extends AbstractWizardActivity {
    @Override
    protected AbstractWizardModel onCreateModel() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.blue_dark));
        }
        else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));
        }

        return new TunnelWizardModel(this);
    }

    @Override
    protected DialogFragment onGetFinishWizardDialog() {
        return FinishWizardDialogFragment.newInstance();
    }

    public void onFinishWizard() {
        Intent result = new Intent();
        result.putExtra(TunnelsContainer.TUNNEL_WIZARD_DATA, mWizardModel.save());
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    public static class FinishWizardDialogFragment extends DialogFragment {
        TunnelWizardActivity mListener;

        public static DialogFragment newInstance() {
            return new FinishWizardDialogFragment();
        }

        public void onAttach(Context context) {
            super.onAttach(context);
            // Verify that the host fragment implements the callback interface
            try {
                // Instantiate the TunnelWizardActivity so we can send events to the host
                mListener = (TunnelWizardActivity) context;
            } catch (ClassCastException e) {
                // The fragment doesn't implement the interface, throw exception
                throw new ClassCastException(context.toString()
                        + " must be TunnelWizardActivity");
            }
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.i2ptunnel_wizard_submit_confirm_message)
                    .setPositiveButton(R.string.i2ptunnel_wizard_submit_confirm_button,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    mListener.onFinishWizard();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
