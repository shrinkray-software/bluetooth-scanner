package au.com.shrinkray.bluetoothscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.com.shrinkray.bluetoothscanner.utils.BidirectionalMap;

/**
 * Created by neal on 3/03/15.
 */
public class DeviceAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;

    private List<Device> mDevices;
    private Set<Device> mDeviceSet;
    private BidirectionalMap<Device,View> mViewsByDevice;

    public DeviceAdapter(Context context) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDevices = new ArrayList<Device>();
        mDeviceSet = new HashSet<Device>();
        mViewsByDevice = new BidirectionalMap<Device,View>();
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        Device device = mDevices.get(position);

        if ( view == null ) {
            view = mLayoutInflater.inflate(R.layout.item_device,parent,false);
            mViewsByDevice.removeByKey(device);
            mViewsByDevice.put(device,view);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.nameTextView = (TextView)view.findViewById(R.id.device_textview_name);
            viewHolder.addressTextView = (TextView)view.findViewById(R.id.device_textview_address);
            viewHolder.rssiTextView = (TextView)view.findViewById(R.id.device_textview_rssi);
            viewHolder.lastScannedTextView = (TextView)view.findViewById(R.id.device_textview_last_scanned);
            viewHolder.lastIntervalTextView = (TextView)view.findViewById(R.id.device_textview_last_interval);
            view.setTag(viewHolder);
        } else {
            // We're reusing the view.  We need to find and remove it from the list.
            mViewsByDevice.removeByValue(view);
            mViewsByDevice.put(device,view);
        }

        updateViewWithDevice(view,device);

        return view;
    }

    class ViewHolder {
        TextView nameTextView;
        TextView addressTextView;
        TextView rssiTextView;
        TextView lastIntervalTextView;
        TextView lastScannedTextView;
    }

    // Normally you'd create a ThreadLocal for a non thread safe object (see JavaDoc), but this one is used
    // only on the Main thread.
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss.SSS");

    private void updateViewWithDevice(View view,Device device) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.nameTextView.setText(device.getName());
        viewHolder.addressTextView.setText(device.getAddress());
        viewHolder.rssiTextView.setText(device.getRssi()+"dB");
        viewHolder.lastIntervalTextView.setText(
                device.getLastInterval() > 0 ? device.getLastInterval() + "ms" : null );
        viewHolder.lastScannedTextView.setText(dateFormat.format(new Date(device.getLastSeen())));
    }

    public void deviceScanned(Device device) {
        if (mDeviceSet.contains(device)) {
            // Check to see if we have a view to update.
            if ( mViewsByDevice.containsKey(device)){
                updateViewWithDevice(mViewsByDevice.get(device), device);
            }
        } else {
            // This is a new device, just add it to the list and fire the change notification.
            mDevices.add(device);
            mDeviceSet.add(device);
            notifyDataSetChanged();
        }

    }

    /**
     * Clear the list of devices and notify the listeners.
     */
    public void clear() {
        mDevices.clear();
        mDeviceSet.clear();
        mViewsByDevice.clear();
        notifyDataSetInvalidated();
    }
}
