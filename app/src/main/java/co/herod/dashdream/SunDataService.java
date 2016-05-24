package co.herod.dashdream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.herod.dashdream.rest.Results;
import co.herod.dashdream.rest.SunApiResponse;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;

public class SunDataService extends Service implements Runnable, LocationListener, SunDataProvider {

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

    private final SunRestClient sunRestClient = new SunRestClient();

    private LocationManager locationManager;

    private String locationProvider;

    private Location currentLocation = null;

    private boolean serviceActive = true;

    private boolean requestSunDataRefresh = true;

    private String sunriseTime = null;
    private String sunsetTime = null;

    public SunDataService() {
    }

    @Override
    public void onCreate() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        selectBestLocationProvider();

        currentLocation = locationManager.getLastKnownLocation(locationProvider);
        locationManager.requestLocationUpdates(locationProvider, 2000, 10, this);

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
            if (requestSunDataRefresh && currentLocation != null) {
                requestSunDataRefresh = false;

                // refresh

                String lat = String.valueOf(currentLocation.getLatitude());
                String lng = String.valueOf(currentLocation.getLongitude());

                Results sunResults = sunRestClient.getSunApiService()
                        .getSunData(lat, lng, "today")
                        .getResults();

                sunriseTime = sunResults.getSunrise();
                sunsetTime = sunResults.getSunset();

                Log.d(TAG, "Updated sunrise-sunset");

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
    public void onLocationChanged(Location location) {
        currentLocation = location;

        requestSunDataRefresh = true;

        Log.d(TAG, "New location: " + location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //
    }

    @Override
    public void onProviderEnabled(String provider) {
        selectBestLocationProvider();
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(locationProvider)) {
            selectBestLocationProvider();
        }
    }

    private void selectBestLocationProvider() {
        locationProvider = locationManager.getBestProvider(new Criteria(), true);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, toString() + "-onDestroy");

        serviceActive = false;

        locationManager.removeUpdates(this);
    }

    @Override
    public String getSunriseTime() {
        return sunriseTime;
    }

    @Override
    public String getSunsetTime() {
        return sunsetTime;
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
