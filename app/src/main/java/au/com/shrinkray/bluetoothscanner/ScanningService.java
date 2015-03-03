package au.com.shrinkray.bluetoothscanner;

import android.app.Service;

/**
 * Created by neal on 3/03/15.
 */
public abstract class ScanningService extends Service {

    public static final String ACTION_START_SCANNING = "start_scanning";
    public static final String ACTION_STOP_SCANNING = "stop_scanning";
    public static final String ACTION_CLEAR_DEVICE_LIST = "clear_device_list";

    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_ADDRESS = "address";
    public static final String EXTRA_SERVICE_UUID = "service_uuid";
    public static final String EXTRA_LIMIT = "limit";
    public static final String EXTRA_UPDATE_SCAN_RECORDS = "update_scan_records";
    public static final String EXTRA_SCAN_MODE = "scan_mode";

    public enum ScanModeSettings {
        MODE_LOW_POWER,
        MODE_BALANCED,
        MODE_LOW_LATENCY
    }


}
