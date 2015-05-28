package co.herod.dashdream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.herod.dashdream.sunrisesunset.Results;
import co.herod.dashdream.sunrisesunset.SunApiResponse;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;

public class SunDataService extends Service implements Runnable {

    public static final String TAG = SunDataService.class.getSimpleName();
    public static final String ME = SunDataService.class.getName();

    public static final String ACTION_REFRESH = ME + ".action.SUN_DATA_REFRESH";

    public static final String BROADCAST_REFRESHED = ME + ".SUN_DATA_REFRESHED";

    public static void refreshSunData(Context context) {
        context.startService(new Intent(context, SunDataService.class)
                .setAction(ACTION_REFRESH));
    }

    private final IBinder binder = new ServiceBinder();

    private final Thread thread = new Thread(this);

    private SunRestClient sunRestClient = new SunRestClient();

    private boolean serviceActive = true;

    private boolean requestSunDataRefresh = false;

    public SunDataService() {
    }

    @Override
    public void onCreate() {
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, toString() + "-onStartCommand");

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                Log.d(TAG, "Received action: " + action);

                if (action.equals(ACTION_REFRESH)) {
                    requestSunDataRefresh = true;
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void run() {
        while (serviceActive && !thread.isInterrupted()) {
            if (requestSunDataRefresh) {
                requestSunDataRefresh = false;

                // refresh

                Results sunResults = sunRestClient.getSunApiService()
                        .getSunData("36.7201600", "-4.4203400", "today")
                        .getResults();

                String sunrise = sunResults.getSunrise();
                String sunset = sunResults.getSunset();

                Log.d(TAG, "GOT DATAAA! " + sunrise + " " + sunset);

                sendBroadcast(new Intent(BROADCAST_REFRESHED));
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException iex) {
                iex.printStackTrace();
                break;
            }
        }
        serviceActive = false;
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, toString() + "-onDestroy");

        serviceActive = false;
    }

    /**
     * Called when a new client is binding to the service
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Called when all clients have unbound from the service
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    public class ServiceBinder extends Binder {
        public SunDataService getService() {
            return SunDataService.this;
        }
    }

    public interface SunApiService {
        @GET("/json")
        public SunApiResponse getSunData(@Query("lat") String lat, @Query("lng") String lng, @Query("date") String date);
    }

    public class SunRestClient
    {

        private SunApiService sunApiService;

        public SunRestClient()
        {
            Gson gson = new GsonBuilder()
                    .create();

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setEndpoint("http://api.sunrise-sunset.org")
                    .setConverter(new GsonConverter(gson))
                    .build();

            sunApiService = restAdapter.create(SunApiService.class);
        }

        public SunApiService getSunApiService()
        {
            return sunApiService;
        }
    }

}
