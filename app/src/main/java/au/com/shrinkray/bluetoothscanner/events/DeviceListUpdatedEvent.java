package au.com.shrinkray.bluetoothscanner.events;

import java.util.List;

import au.com.shrinkray.bluetoothscanner.Device;

/**
 * Created by neal on 3/03/15.
 */
public class DeviceListUpdatedEvent {

    List<Device> mDevices;

    public DeviceListUpdatedEvent(List<Device> devices) {
        mDevices = devices;
    }

    public List<Device> getDevices() {
        return mDevices;
    }


}
