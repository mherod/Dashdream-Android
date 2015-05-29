package co.herod.dashdream;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by matthewherod on 27/05/15.
 */
public class DashDreamService extends DreamService implements Runnable {

    public static final String TAG = DashDreamService.class.getSimpleName();

    private TextView textView1;

    private TextView textView2;

    private Handler handler = new Handler(Looper.getMainLooper());

    private Thread thread = new Thread(this);

    private SunDataService mSunDataService;

    private boolean serviceActive = true;

    private boolean requestAttachedUpdate = false;

    @Override
    public void onCreate() {
        thread.start();
    }

    @Override
    public void run() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SunDataService.BROADCAST_REFRESHED);

        registerReceiver(mDreamBroadcastReceiver, intentFilter);

        while(serviceActive) {
            if (mSunDataService != null) {
                if (requestAttachedUpdate) {
                    requestAttachedUpdate = false;

                    updateSunData(mSunDataService);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException iex) {
                iex.printStackTrace();
                break;
            }
        }

        unregisterReceiver(mDreamBroadcastReceiver);

        stopSelf();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Exit dream upon user touch
        setInteractive(false);
        // Hide system UI
        setFullscreen(true);
        // Set the dream layout
        setContentView(R.layout.dream);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);

        requestAttachedUpdate = true;
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();

        bindService(
                new Intent(this, SunDataService.class),
                mSunDataServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();

        unbindService(mSunDataServiceConnection);
    }

    @Override
    public void onDestroy() {
        serviceActive = false;
    }

    private void updateSunData(final SunDataProvider sunDataProvider) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                textView1.setText("The sun will rise at " + sunDataProvider.getSunriseTime());
                textView2.setText("The sun will set at " + sunDataProvider.getSunsetTime());
            }
        });
    }

    public final ServiceConnection mSunDataServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            mSunDataService = ((SunDataService.ServiceBinder) binder).getService();

            updateSunData(mSunDataService);
        }

        public void onServiceDisconnected(ComponentName className) {
            mSunDataService = null;
        }
    };

    public final BroadcastReceiver mDreamBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

                if (action != null) {
                    Log.d(TAG, "Received broadcast: " + action);

                    if (action.equals(SunDataService.BROADCAST_REFRESHED)) {
                        updateSunData(mSunDataService);
                    }
            }
        }
    };
}
