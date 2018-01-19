package coding.virtusa.virtusacodingchallenge;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * There are three ways of getting the user Location
 *
 * 1)Using GPS provider. -this is direct satlite location, but its hard to get it especially when the user inside the building
 *
 * 2)Using Network- this may not give you accurate location,since its network based. but it give you approxiamte location
 *
 * 3)Using Play services- from time to time google play services, save user's location , we can access this information
 *
 * due to time contraint im only implemeting GPS. But a good app uses all three of these
 *
 * @TODO why is it everything is in one class
 */
public class MainActivity extends AppCompatActivity implements LocationListener{
    private static final String TAG = "Mainactivty";

    private LocationManager mLocationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    private void showData(ISSResponse response) {
        ListView lListView = (ListView)findViewById(R.id.list_view);
        ListAdapter mAdapter = new ListAdapter(getLayoutInflater(),response);
        lListView.setAdapter(mAdapter);


    }

    @SuppressWarnings({"ResourceType"})
    private void getLocation() {
        if(checkLocationPermission()) {

            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //@TODO add network provider along with GPS
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGPSEnabled) {

                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    Double latitude = location.getLatitude();
                    Double longitude = location.getLongitude();
                    getPassData(latitude, longitude);
                } else {
                    //we dont know last location so lets wait for the location
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            60 * 1000,
                            10, this);
                }
            } else {
                //gps is not enabled
                launchSetiings();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocation();

    }

    private void getPassData(Double latitude,Double longitude) {

        //@TODO use retrofit
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://api.open-notify.org/iss-pass.json?lat="+latitude+"&lon="+longitude;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //@TODO dont parse on UI thread
                        Gson gson = new Gson();
                        ISSResponse lParsedResponse = gson.fromJson(response,ISSResponse.class);
                        showData(lParsedResponse);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,error.getLocalizedMessage());
            }
        });
        queue.add(stringRequest);
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Message")
                        .setMessage("Application wants to use your location")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        getLocation();

                    }

                } else {

                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale( MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION );
                    if (! showRationale) {
                        // user seleted son't ask again
                        showMissingPermissionsDialog();

                    }

                }
                return;
            }

        }
    }

    /**
     * user disabled the app permissions
     */
    public void showMissingPermissionsDialog(){
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Enable Permissions");

        // Setting Dialog Message
        alertDialog.setMessage("Application Needs location permissions");

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                launchAppPermissions();
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void launchAppPermissions() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    public void launchSetiings(){
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);

        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        getPassData(location.getLatitude(),location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
