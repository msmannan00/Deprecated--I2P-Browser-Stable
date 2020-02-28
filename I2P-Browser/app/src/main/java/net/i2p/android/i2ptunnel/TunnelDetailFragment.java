package net.i2p.android.i2ptunnel;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.i2p.I2PAppContext;
import net.i2p.android.i2ptunnel.util.TunnelUtil;
import com.example.myapplication.R;
import net.i2p.android.router.util.Util;
import net.i2p.android.util.FragmentUtils;
import net.i2p.app.ClientAppState;
import net.i2p.i2ptunnel.TunnelController;
import net.i2p.i2ptunnel.TunnelControllerGroup;

import java.util.List;

public class TunnelDetailFragment extends Fragment {
    public static final String TUNNEL_ID = "tunnel_id";

    TunnelDetailListener mCallback;
    private TunnelControllerGroup mGroup;
    private TunnelEntry mTunnel;
    private Toolbar mToolbar;
    private ImageView mStatus;

    public static TunnelDetailFragment newInstance(int tunnelId) {
        TunnelDetailFragment f = new TunnelDetailFragment();
        Bundle args = new Bundle();
        args.putInt(TUNNEL_ID, tunnelId);
        f.setArguments(args);
        return f;
    }

    // Container Activity must implement this interface
    public interface TunnelDetailListener {
        void onEditTunnel(int tunnelId);
        void onTunnelDeleted(int tunnelId, int numTunnelsLeft);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        mCallback = FragmentUtils.getParent(this, TunnelDetailListener.class);
        if (mCallback == null)
            throw new ClassCastException("Parent must implement TunnelDetailListener");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String error;
        List<TunnelController> controllers;
        try {
            mGroup = TunnelControllerGroup.getInstance();
            error = mGroup == null ? getResources().getString(R.string.i2ptunnel_not_initialized) : null;
            controllers = mGroup == null ? null : mGroup.getControllers();
        } catch (IllegalArgumentException iae) {
            mGroup = null;
            controllers = null;
            error = iae.toString();
        }

        if (mGroup == null) {
            Toast.makeText(getActivity().getApplicationContext(),
                    error, Toast.LENGTH_LONG).show();
            getActivity().finish();
        } else if (getArguments().containsKey(TUNNEL_ID)) {
            int tunnelId = getArguments().getInt(TUNNEL_ID);
            try {
                TunnelController controller = controllers.get(tunnelId);
                mTunnel = new TunnelEntry(getActivity(), controller, tunnelId);
            } catch (IndexOutOfBoundsException e) {
                // Tunnel doesn't exist
                Util.e("Could not load tunnel details", e);
                Toast.makeText(getActivity().getApplicationContext(),
                        R.string.i2ptunnel_no_tunnel_details, Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_i2ptunnel_detail, container, false);

        mToolbar = (Toolbar) v.findViewById(R.id.detail_toolbar);
        mToolbar.inflateMenu(R.menu.fragment_i2ptunnel_detail_actions);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onToolbarItemSelected(menuItem);
            }
        });
        updateToolbar();

        if (mTunnel != null) {
            mStatus = (ImageView) v.findViewById(R.id.tunnel_status);
            updateStatus();
            ViewCompat.setTransitionName(mStatus, "status" + mTunnel.getId());

            TextView name = (TextView) v.findViewById(R.id.tunnel_name);
            name.setText(mTunnel.getName());

            TextView type = (TextView) v.findViewById(R.id.tunnel_type);
            type.setText(mTunnel.getType());

            TextView description = (TextView) v.findViewById(R.id.tunnel_description);
            description.setText(mTunnel.getDescription());

            if (!mTunnel.getDetails().isEmpty()) {
                v.findViewById(R.id.tunnel_details_container).setVisibility(View.VISIBLE);
                TextView details = (TextView) v.findViewById(R.id.tunnel_details);
                View copyDetails = v.findViewById(R.id.tunnel_details_copy);
                details.setText(mTunnel.getDetails());
                if (!mTunnel.isClient()) {
                    copyDetails.setVisibility(View.VISIBLE);
                    copyDetails.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                                copyToClipbardLegacy();
                            else
                                copyToClipboardHoneycomb();

                            Toast.makeText(getActivity(), R.string.address_copied_to_clipboard, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            View accessIfacePortItem = v.findViewById(R.id.tunnel_access_interface_port_item);
            TextView accessIfacePort = (TextView) v.findViewById(R.id.tunnel_access_interface_port);
            View accessIfaceOpen = v.findViewById(R.id.tunnel_access_open);
            View targetIfacePortItem = v.findViewById(R.id.tunnel_target_interface_port_item);
            TextView targetIfacePort = (TextView) v.findViewById(R.id.tunnel_target_interface_port);
            View targetIfaceOpen = v.findViewById(R.id.tunnel_target_open);
            switch (mTunnel.getInternalType()) {
                case "httpbidirserver":
                    accessIfacePort.setText(mTunnel.getClientLink(false));
                    setupOpen(accessIfaceOpen, true);
                    v.findViewById(R.id.icon_link_access).setVisibility(View.GONE);
                    targetIfacePort.setText(mTunnel.getServerLink(false));
                    setupOpen(targetIfaceOpen, false);
                    break;
                case "streamrserver":
                    accessIfacePort.setText(mTunnel.getServerLink(false));
                    setupOpen(accessIfaceOpen, true);
                    targetIfacePortItem.setVisibility(View.GONE);
                    break;
                case "streamrclient":
                    accessIfacePortItem.setVisibility(View.GONE);
                    targetIfacePort.setText(mTunnel.getClientLink(false));
                    setupOpen(targetIfaceOpen, false);
                    break;
                default:
                    if (mTunnel.isClient()) {
                        accessIfacePort.setText(mTunnel.getClientLink(false));
                        setupOpen(accessIfaceOpen, true);
                        targetIfacePortItem.setVisibility(View.GONE);
                    } else {
                        accessIfacePortItem.setVisibility(View.GONE);
                        targetIfacePort.setText(mTunnel.getServerLink(false));
                        setupOpen(targetIfaceOpen, false);
                    }
            }

            CheckBox autoStart = (CheckBox) v.findViewById(R.id.tunnel_autostart);
            autoStart.setChecked(mTunnel.startAutomatically());
        }

        return v;
    }

    private void setupOpen(View open, final boolean client) {
        if (mTunnel.isRunning() &&
                (client ? mTunnel.isClientLinkValid() : mTunnel.isServerLinkValid())) {
            open.setVisibility(View.VISIBLE);
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(client ? mTunnel.getClientLink(true) : mTunnel.getServerLink(true)));
                    try {
                        startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.install_recommended_app)
                                .setMessage(R.string.app_needed_for_this_tunnel_type)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Uri uri = mTunnel.getRecommendedAppForTunnel();
                                        if (uri != null) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            try {
                                                startActivity(intent);
                                            } catch (ActivityNotFoundException e) {
                                                Toast.makeText(getContext(),
                                                        R.string.no_market_app,
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                        builder.show();
                    }
                }
            });
        } else
            open.setVisibility(View.GONE);
    }

    private void updateToolbar() {
        Menu menu = mToolbar.getMenu();
        MenuItem start = menu.findItem(R.id.action_start_tunnel);
        MenuItem stop = menu.findItem(R.id.action_stop_tunnel);

        if (mTunnel != null && mGroup != null &&
                (mGroup.getState() == ClientAppState.STARTING ||
                        mGroup.getState() == ClientAppState.RUNNING)) {
            boolean isStopped = mTunnel.getStatus() == TunnelEntry.NOT_RUNNING;

            start.setVisible(isStopped);
            start.setEnabled(isStopped);

            stop.setVisible(!isStopped);
            stop.setEnabled(!isStopped);
        } else {
            start.setVisible(false);
            start.setEnabled(false);

            stop.setVisible(false);
            stop.setEnabled(false);
        }
    }

    private void updateStatus() {
        mStatus.setImageDrawable(mTunnel.getStatusIcon());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            mStatus.setBackgroundDrawable(mTunnel.getStatusBackground());
        else
            mStatus.setBackground(mTunnel.getStatusBackground());
    }

    private boolean onToolbarItemSelected(MenuItem item) {
        if (mTunnel == null)
            return false;

        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_start_tunnel:
            mTunnel.getController().startTunnelBackground();
            Toast.makeText(getActivity().getApplicationContext(),
                    getResources().getString(R.string.i2ptunnel_msg_tunnel_starting)
                    + ' ' + mTunnel.getName(), Toast.LENGTH_LONG).show();
            // Reload the toolbar to change the start/stop action
            updateToolbar();
            // Update the status icon
            updateStatus();
            return true;
        case R.id.action_stop_tunnel:
            mTunnel.getController().stopTunnel();
            Toast.makeText(getActivity().getApplicationContext(),
                    getResources().getString(R.string.i2ptunnel_msg_tunnel_stopping)
                    + ' ' + mTunnel.getName(), Toast.LENGTH_LONG).show();
            // Reload the toolbar to change the start/stop action
            updateToolbar();
            // Update the status icon
            updateStatus();
            return true;
        case R.id.action_edit_tunnel:
            mCallback.onEditTunnel(mTunnel.getId());
            return true;
        case R.id.action_delete_tunnel:
            DialogFragment dg = DeleteTunnelDialogFragment.newInstance();
            dg.show(getChildFragmentManager(), "delete_tunnel_dialog");
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void onDeleteTunnel() {
        List<String> msgs = TunnelUtil.deleteTunnel(
                I2PAppContext.getGlobalContext(),
                mGroup, mTunnel.getId(), null);
        Toast.makeText(getActivity().getApplicationContext(),
                msgs.get(0), Toast.LENGTH_LONG).show();
        mCallback.onTunnelDeleted(mTunnel.getId(),
                mGroup.getControllers().size());
    }

    private void copyToClipbardLegacy() {
        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(mTunnel.getDetails());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void copyToClipboardHoneycomb() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(
                mTunnel.getName(), mTunnel.getDetails());
        clipboard.setPrimaryClip(clip);
    }

    public static class DeleteTunnelDialogFragment extends DialogFragment {
        TunnelDetailFragment mListener;

        public static DialogFragment newInstance() {
            return new DeleteTunnelDialogFragment();
        }

        private void onAttachToParentFragment(Fragment fragment) {
            // Verify that the host fragment implements the callback interface
            try {
                // Instantiate the TunnelDetailFragment so we can send events to the host
                mListener = (TunnelDetailFragment) fragment;
            } catch (ClassCastException e) {
                // The fragment doesn't implement the interface, throw exception
                throw new ClassCastException(fragment.toString()
                        + " must be TunnelDetailFragment");
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            onAttachToParentFragment(getParentFragment());
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.i2ptunnel_delete_confirm_message)
                    .setPositiveButton(R.string.i2ptunnel_delete_confirm_button,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    mListener.onDeleteTunnel();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
