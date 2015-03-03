package au.com.shrinkray.bluetoothscanner;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import au.com.shrinkray.bluetoothscanner.events.DeviceListClearedEvent;
import au.com.shrinkray.bluetoothscanner.events.DeviceListUpdatedEvent;
import au.com.shrinkray.bluetoothscanner.events.DeviceScannedEvent;
import au.com.shrinkray.bluetoothscanner.events.ScanningStartedEvent;
import au.com.shrinkray.bluetoothscanner.events.ScanningStoppedEvent;
import de.greenrobot.event.EventBus;

/**
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LollipopScanningService extends ScanningService {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private ScanCallback mScanCallback;

    private Map<String, Device> mDevicesByAddress;

    @Override
    public void onCreate() {
        super.onCreate();

        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ) {
            return;
        }

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mDevicesByAddress = new HashMap<>();
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

            if ( mScanCallback == null ) {

                UUID serviceUuid = null;
                String name = null;
                String address = null;
                long limit = 0;
                boolean updateScanRecords = false;
                int scanMode = ScanSettings.SCAN_MODE_BALANCED;

                if (extras != null) {
                    name = extras.getString(EXTRA_NAME,null);
                    address = extras.getString(EXTRA_ADDRESS,null);
                    serviceUuid = (UUID) extras.getSerializable(EXTRA_SERVICE_UUID);
                    limit = extras.getLong(EXTRA_LIMIT,0);
                    updateScanRecords = extras.getBoolean(EXTRA_UPDATE_SCAN_RECORDS, false);
                    scanMode = extras.getInt(EXTRA_SCAN_MODE,ScanSettings.SCAN_MODE_BALANCED);
                }

                ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder()
                        .setDeviceName(name)
                        .setDeviceAddress(address);

                if ( serviceUuid != null ) {
                    scanFilterBuilder.setServiceUuid(ParcelUuid.fromString(serviceUuid.toString()));
                }

                List<ScanFilter> scanFilters = new ArrayList<>();
                scanFilters.add(scanFilterBuilder.build());

                ScanSettings scanSettings = new ScanSettings.Builder()
                        .setScanMode(scanMode)
                        .build();

                mScanCallback = new ScanCallbackImplementation(limit,updateScanRecords);
                mBluetoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);

                EventBus.getDefault().post(new ScanningStartedEvent());
            }

        } else if (ACTION_STOP_SCANNING.equals(action)) {

            if (mScanCallback != null) {
                mBluetoothLeScanner.stopScan(mScanCallback);
                mScanCallback = null;

                EventBus.getDefault().post(new ScanningStoppedEvent());
            }

        } else if (ACTION_CLEAR_DEVICE_LIST.equals(action)) {

            mDevicesByAddress.clear();

            EventBus.getDefault().post(new DeviceListClearedEvent());

        }

        return START_STICKY;
    }

    /**
     *
     */
    private class ScanCallbackImplementation extends android.bluetooth.le.ScanCallback {

        private long mLimit;
        private boolean mUpdateScanRecords;

        private ScanCallbackImplementation(long limit, boolean updateScanRecords) {
            mLimit = limit;
            mUpdateScanRecords = updateScanRecords;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            long scanTime = System.currentTimeMillis();

            Device device = mDevicesByAddress.get(result.getDevice().getAddress());
            BluetoothDevice bluetoothDevice = result.getDevice();

            // If we haven't seen this device before, create an entry for it.
            if ( device == null ) {
                device = new Device(bluetoothDevice,result.getRssi(),result.getScanRecord().getBytes(),scanTime);
                mDevicesByAddress.put(device.getAddress(),device);
            } else {
                device.setLastSeen(scanTime);
                device.setRssi(device.getRssi());
                if (mUpdateScanRecords) {
                    device.setName(bluetoothDevice.getName());
                    device.setScanRecord(result.getScanRecord().getBytes());
                }
            }

            // Limit the number of times we fire an update event for any device to every mLimit ms.
            if ( mLimit > 0 ) {
                // Update the last fired time or return otherwise.
                if ( ( scanTime - device.getLastFired() ) > mLimit ) {
                    device.setLastFired(scanTime);
                    EventBus.getDefault().post(new DeviceScannedEvent(device));
                }
            } else {
                device.setLastFired(scanTime);
                EventBus.getDefault().post(new DeviceScannedEvent(device));
            }

            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
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
        LollipopScanningService getService() {
            return LollipopScanningService.this;
        }
    }


}
