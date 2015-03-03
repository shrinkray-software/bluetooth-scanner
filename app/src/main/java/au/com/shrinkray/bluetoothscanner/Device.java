package au.com.shrinkray.bluetoothscanner;

import android.bluetooth.BluetoothDevice;

import java.util.Set;
import java.util.UUID;

/**
 * Created by neal on 3/03/15.
 */
public class Device {

    private String mAddress;
    private String mName;
    private byte[] mScanRecord;
    private int mRssi;
    private long mLastSeen;
    private long mLastFired;
    private long mLastInterval;

    private Set<UUID> mAdvertisedServices;

    public Device(String address) {
        mAddress = address;
    }

    public Device(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord,long currentTime) {
        mAddress = bluetoothDevice.getAddress();
        mName = bluetoothDevice.getName();
        mRssi = rssi;
        mScanRecord = scanRecord;
        mLastSeen = currentTime;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public byte[] getScanRecord() {
        return mScanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        // Clear the advertised services if we have a new scan record.
        this.mScanRecord = scanRecord;
        this.mAdvertisedServices = null;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    public long getLastSeen() {
        return mLastSeen;
    }

    public void setLastSeen(long lastSeen) {

        // Update the interval (the time between packets).
        if ( lastSeen > 0 ) {
            mLastInterval = mLastSeen - lastSeen;
        }

        mLastSeen = lastSeen;
    }

    public long getLastFired() {
        return mLastFired;
    }

    public long getLastInterval() {
        return mLastInterval;
    }

    public void setLastFired(long lastFired) {
        mLastFired = lastFired;
    }

    /**
     * Get the list of advertised services.
     *
     * @return
     */
    public Set<UUID> getAdvertisedServices() {

        if ( mAdvertisedServices == null ) {
            mAdvertisedServices = BluetoothUtils.parseUUIDsFromScanRecord(mScanRecord);
        }

        return mAdvertisedServices;

    }

}
