package au.com.shrinkray.bluetoothscanner.events;

import au.com.shrinkray.bluetoothscanner.Device;

/**
 * Created by neal on 3/03/15.
 */
public class DeviceScannedEvent {
    private Device mDevice;

    public DeviceScannedEvent(Device device) {
        mDevice = device;
    }

    public Device getDevice() {
        return mDevice;
    }
}
