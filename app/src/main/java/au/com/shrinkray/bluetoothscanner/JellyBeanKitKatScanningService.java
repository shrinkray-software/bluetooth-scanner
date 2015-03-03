package au.com.shrinkray.bluetoothscanner;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import au.com.shrinkray.bluetoothscanner.events.DeviceListUpdatedEvent;
import au.com.shrinkray.bluetoothscanner.events.DeviceScannedEvent;
import au.com.shrinkray.bluetoothscanner.events.ScanningStartedEvent;
import au.com.shrinkray.bluetoothscanner.events.ScanningStoppedEvent;
import de.greenrobot.event.EventBus;

/**
 */
public class JellyBeanKitKatScanningService extends ScanningService {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothAdapter.LeScanCallback mCallback;

    private Map<String, Device> mDevicesByAddress;

    @Override
    public void onCreate() {
        super.onCreate();

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mDevicesByAddress = new HashMap<String,Device>();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if ( intent == null ) {
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (ACTION_START_SCANNING.equals(action)) {


            if (mCallback == null) {
                UUID serviceUuid = null;
                String name = null;
                String address = null;
                long limit = 0;
                boolean updateScanRecords = false;

                if (extras != null) {
                    name = extras.getString(EXTRA_NAME,null);
                    address = extras.getString(EXTRA_ADDRESS,null);
                    serviceUuid = (UUID) extras.getSerializable(EXTRA_SERVICE_UUID);
                    limit = extras.getLong(EXTRA_LIMIT,0);
                    updateScanRecords = extras.getBoolean(EXTRA_UPDATE_SCAN_RECORDS, false);
                }

                mCallback = new LeScanCallbackImplementation(name,address,serviceUuid,limit,updateScanRecords);
                mBluetoothAdapter.startLeScan(mCallback);

                // Do not use this version of the call.  It only works for 50% of UUIDs because of a byte conversion bug in the
                // implementation of the scanning filter.
                // mBluetoothAdapter.startLeScan("",)

                EventBus.getDefault().post(new ScanningStartedEvent());
            }

        } else if (ACTION_STOP_SCANNING.equals(action)) {

            if (mCallback != null) {
                mBluetoothAdapter.stopLeScan(mCallback);
                mCallback = null;
                EventBus.getDefault().post(new ScanningStoppedEvent());
            }

        } else if (ACTION_CLEAR_DEVICE_LIST.equals(action)) {

            mDevicesByAddress.clear();

            EventBus.getDefault().post(new DeviceListUpdatedEvent(new ArrayList<>(mDevicesByAddress.values())));

        }

        return START_STICKY;
    }

    /**
     *
     */
    private class LeScanCallbackImplementation implements BluetoothAdapter.LeScanCallback {

        private UUID mUuid;
        private String mAddress;
        private String mName;
        private long mLimit;
        private boolean mUpdateScanRecords;

        public LeScanCallbackImplementation(String name, String address, UUID uuid, long limit, boolean updateScanRecords) {
            mName = name;
            mAddress = address;
            mUuid = uuid;
            mLimit = limit;
            mUpdateScanRecords = updateScanRecords;
        }

        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {

            long currentTime = System.currentTimeMillis();

            // Filter by name if we need to.
            if (mName != null) {
                if (! bluetoothDevice.getName().equals(bluetoothDevice)) {
                    return;
                }
            }

            // Filter by address if we need to.
            if (mAddress != null) {
                if (! bluetoothDevice.getAddress().equals(mAddress)) {
                    return;
                }
            }

            Device device = mDevicesByAddress.get(bluetoothDevice.getAddress());

            // If we haven't seen this device before, create an entry for it.
            if ( device == null ) {
                device = new Device(bluetoothDevice,rssi,scanRecord,currentTime);
                mDevicesByAddress.put(device.getAddress(),device);
            } else {
                device.setLastSeen(currentTime);
                device.setRssi(rssi);
                if (mUpdateScanRecords) {
                    device.setName(device.getName());
                    device.setScanRecord(scanRecord);
                }
            }

            // Filter by UUID if we need to.  We do the getAdvertised call
            if (mUuid != null && ! device.getAdvertisedServices().contains(mUuid)) {
                return;
            }


            // Limit the number of times we fire an update event for any device to every mLimit ms.
            if ( mLimit > 0 ) {
                // Update the last fired time or return otherwise.
                if ( ( currentTime - device.getLastFired() ) > mLimit ) {
                    device.setLastFired(currentTime);
                    EventBus.getDefault().post(new DeviceScannedEvent(device));
                }
            }else {
                device.setLastFired(currentTime);
                EventBus.getDefault().post(new DeviceScannedEvent(device));
            }


        }
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        JellyBeanKitKatScanningService getService() {
            return JellyBeanKitKatScanningService.this;
        }
    }


}
