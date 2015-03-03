package au.com.shrinkray.bluetoothscanner;

import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;

import au.com.shrinkray.bluetoothscanner.events.DeviceScannedEvent;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public class DeviceListActivity extends ActionBarActivity {

    @InjectView(R.id.device_list_listview)
    ListView mDeviceListView;

    @InjectView(R.id.device_list_button)
    Button mStartStopButton;

    @OnClick(R.id.device_list_button)
    public void buttonClicked(Button button) {
        Intent intent = new Intent(this,JellyBeanKitKatScanningService.class);
        intent.setAction(LollipopScanningService.ACTION_START_SCANNING);
        intent.putExtra(LollipopScanningService.EXTRA_SCAN_MODE, ScanSettings.SCAN_MODE_LOW_POWER);
        startService(intent);
    }

    DeviceAdapter mDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        ButterKnife.inject(this);
        mDeviceAdapter = new DeviceAdapter(this);

        mDeviceListView.setAdapter(mDeviceAdapter);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(DeviceScannedEvent event) {
        mDeviceAdapter.deviceScanned(event.getDevice());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
