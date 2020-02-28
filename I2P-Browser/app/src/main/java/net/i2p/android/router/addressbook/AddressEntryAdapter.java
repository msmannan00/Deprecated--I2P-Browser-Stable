package net.i2p.android.router.addressbook;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.R;
import net.i2p.android.util.AlphanumericHeaderAdapter;

import java.util.List;

public class AddressEntryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        AlphanumericHeaderAdapter.SortedAdapter {
    private Context mCtx;
    private AddressbookFragment.OnAddressSelectedListener mListener;
    private List<AddressEntry> mAddresses;

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        public TextView hostName;

        public AddressViewHolder(View itemView) {
            super(itemView);

            hostName = (TextView) itemView.findViewById(R.id.host_name);
        }
    }

    public AddressEntryAdapter(Context context,
                               AddressbookFragment.OnAddressSelectedListener listener) {
        super();
        mCtx = context;
        mListener = listener;
        setHasStableIds(true);
    }

    public void setAddresses(List<AddressEntry> addresses) {
        mAddresses = addresses;
        notifyDataSetChanged();
    }

    public AddressEntry getAddress(int position) {
        if (mAddresses == null || mAddresses.isEmpty() ||
                position < 0 || position >= mAddresses.size())
            return null;

        return mAddresses.get(position);
    }

    @NonNull
    @Override
    public String getSortString(int position) {
        AddressEntry address = getAddress(position);
        if (address == null)
            return "";

        return address.getHostName();
    }

    @Override
    public int getItemViewType(int position) {
        if (mAddresses == null)
            return R.string.router_not_running;
        else if (mAddresses.isEmpty())
            return R.layout.listitem_empty;
        else
            return R.layout.listitem_address;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int vt = viewType;
        if (viewType == R.string.router_not_running)
            vt = R.layout.listitem_empty;

        View v = LayoutInflater.from(parent.getContext())
                .inflate(vt, parent, false);
        switch (viewType) {
            case R.layout.listitem_address:
                return new AddressViewHolder(v);
            default:
                return new SimpleViewHolder(v);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case R.string.router_not_running:
                ((TextView) holder.itemView).setText(
                        mCtx.getString(R.string.router_not_running));
                break;

            case R.layout.listitem_empty:
                ((TextView) holder.itemView).setText(
                        mCtx.getString(R.string.addressbook_is_empty));
                break;

            case R.layout.listitem_address:
                final AddressEntry address = getAddress(position);
                AddressViewHolder avh = (AddressViewHolder) holder;
                avh.hostName.setText(address.getHostName());

                avh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onAddressSelected(address.getHostName());
                    }
                });
                break;

            default:
                break;
        }
    }

    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mAddresses == null || mAddresses.isEmpty())
            return 1;

        return mAddresses.size();
    }

    public long getItemId(int position) {
        AddressEntry address = getAddress(position);
        if (address == null)
            return Integer.MAX_VALUE;

        return address.hashCode();
    }
}
