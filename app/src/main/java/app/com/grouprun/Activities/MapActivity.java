package app.com.grouprun.Activities;
import android.Manifest;
import android.annotation.TargetApi;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseUser;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import app.com.grouprun.Fragments.CompletedRunDialogFragment;
import app.com.grouprun.R;
import info.hoang8f.widget.FButton;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        CompletedRunDialogFragment.OnFragmentInteractionListener,
        CompletedRunDialogFragment.CompletedRunDialogListener {


    private GoogleMap mMap;
    LocationManager userLocation;
    Location location;
    private static double latitude = 0.0;
    private static double longitude = 0.0;
    FButton button;
    TextToSpeech textToSpeech;
    MapFragment googleMapFrag;
    Chronometer timeChronometer;
    Chronometer distanceChronometer;
    long time;
    ParseUser currentUser;
    //Google API Client needed for PubNub
    private GoogleApiClient mGoogleApiClient;
    private Pubnub mPubnub;
    private TextView email;
    private TextView name;
    private PolylineOptions mPolylineOptions; // Polyline Options Variable
    private LatLng mLatLng;

    //private TextView milesLabel;

    // PubNub Publish Callback
    Callback publishCallback = new Callback() {
        @Override
        public void successCallback(String channel, Object response) {

            Log.d("PUBNUB-Success", response.toString());
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            Log.e("PUBNUB-Error:", error.toString());
        }
    };

    // PubNub Subscribe Callback
    Callback subscribeCallback = new Callback() {

        @Override
        public void successCallback(String channel, Object message) {
            JSONObject jsonMessage = (JSONObject) message;
            try {
                double mLat = jsonMessage.getDouble("lat");
                double mLng = jsonMessage.getDouble("lng");
                mLatLng = new LatLng(mLat, mLng);
            } catch (JSONException e) {
                Log.e("TAG", e.toString());
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePolyline();
                    updateCamera();
                    updateMarker();
                }
            });
        }
        // Log.d("PUBNUB_TAG", "Message Received: " + message.toString());

    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        android.support.v7.widget.Toolbar  toolbar = (  android.support.v7.widget.Toolbar ) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


//        currentUser = ParseUser.getCurrentUser();
//        name.setText(currentUser.getUsername());
//        email.setText(currentUser.getEmail());

        //Start Google Client
        this.buildGoogleApiClient();
        mGoogleApiClient.connect();


        mPubnub = new Pubnub("pub-c-330ec2e2-f6e7-4558-9010-5247b1f0b098", "sub-c-bad78d26-6f9f-11e5-ac0d-02ee2ddab7fe");
        try {
            mPubnub.subscribe("MainRunning", subscribeCallback);
        } catch (PubnubException e) {
            Log.e("PubNubException", e.toString());
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        uiComponents();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



    }
    /*
        Where all ui components are handled
     */
    public void uiComponents() {

//        name = (TextView) findViewById(R.id.name);
//        email = (TextView) findViewById(R.id.email);
        button = (FButton) findViewById(R.id.startButton);
        timeChronometer = (Chronometer) findViewById(R.id.timeChronometer);
        distanceChronometer = (Chronometer) findViewById(R.id.distanceChronometer);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        button.setOnClickListener(this);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initializeMap();

        mMap.getUiSettings().setScrollGesturesEnabled(false);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            userLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = userLocation.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                LatLng currentLatLng = new LatLng(latitude, longitude);
                //setting initial zoom
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));

                //SOURCE: http://stackoverflow.com/questions/18425141/android-google-maps-api-v2-zoom-to-current-location
                //animate camera to zoom to user location
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(currentLatLng)      // Sets the center of the map to location user
                        .zoom(16)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        } else {
            // Show rationale and request permission.
        }

    }

    private void initializeMap() {
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.BLUE).width(10f);
    }


    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        int flag = 0;
        LatLng prev = new LatLng(0, 0);
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());

        if (flag == 0) {
            prev = current;
        }

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(current, 16);
        mMap.animateCamera(update);
        mMap.addPolyline((new PolylineOptions()).add(prev, current).width(6)
                .color(Color.BLUE)
                .visible(true));


        //PubNub Tracker Location Log
        Log.d("Location Update", "Latitude: " + location.getLatitude() +
                " Longitude: " + location.getLongitude());

         broadcastLocation(location);
    }

    private void broadcastLocation(Location location) {
        JSONObject message = new JSONObject();
        try {
            message.put("lat", location.getLatitude());
            message.put("lng", location.getLongitude());
            message.put("alt", location.getAltitude());
        } catch (JSONException e) {
            System.out.print(e.toString());
        }
        mPubnub.publish("MainRunning", message, publishCallback);


        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        LocationRequest mLocationRequest = createLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);


    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;

    }

    @Override
    public void onConnectionSuspended(int i) {
        System.err.print("Connection to Google API Suspended");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {

        String text = button.getText().toString();
        if (text.equals("Start")) {


            timeChronometer.setBase(SystemClock.elapsedRealtime() + time);
            timeChronometer.start();

            textToSpeech.speak("Run started", TextToSpeech.QUEUE_FLUSH, null, null);
            button.setButtonColor(Color.RED);
            button.setText("Stop");

        } else {
            time = timeChronometer.getBase() - SystemClock.elapsedRealtime();

//            Pass time to completed run dialog
            Bundle bundle = new Bundle();
            String currentTime = timeChronometer.getText().toString();
            String distanceText = distanceChronometer.getText().toString();
            String timeText = String.valueOf(currentTime);
            bundle.putString("timeText", timeText);
            bundle.putString("distanceText", distanceText);
//end
            timeChronometer.stop();
            textToSpeech.speak("Run stopped", TextToSpeech.QUEUE_FLUSH, null, null);


            button.setButtonColor(Color.GREEN);
            button.setText("Start");
            timeChronometer.setBase(SystemClock.elapsedRealtime());



            showNoticeDialog(bundle);

            time = 0;
        }
    }


    private void updatePolyline() {
        mMap.clear();
        mMap.addPolyline(mPolylineOptions.add(mLatLng));
    }

    private void updateMarker() {
        mMap.addMarker(new MarkerOptions().position(mLatLng));
    }

    private void updateCamera() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
    }



    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);

    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
/*
    public void distanceBetween(){
        double startLatitude = 0;
        double startLongitude = 0;
        double endLatitude = 0;
        double endLongitude = 0;


        double R, a, c, d, dLat,dLon;
        R=6371.00;
        dLat = (endLatitude - startLatitude);
        Math.toRadians(dLat);
        dLon = (endLongitude - startLongitude);
        Math.toRadians(dLon);
        a = Math.sin(dLat/2.00) * Math.sin(dLat/2.00) + Math.cos(Math.toRadians(startLatitude)) * Math.cos(Math.toRadians(endLatitude)) * Math.sin(dLon/2) * Math.sin(dLat/2);
        c =2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.00-a));
        d= R * c;
        System.out.println("Miles: "+d);
    }
*/
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            Intent mapViewIntent = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(mapViewIntent);
        } else if (id == R.id.run_history) {
            Intent runViewIntent = new Intent(getApplicationContext(), RunListActivity.class);
            startActivity(runViewIntent);
        }else if (id == R.id.nav_share) {

        } else if (id == R.id.music) {
            Intent musicIntent = new Intent(getApplicationContext(),MusicActivity.class);
            startActivity(musicIntent);
        } else if (id == R.id.nav_send) {

        }else if(id==R.id.logout){
            currentUser.logOut();

            Intent logout = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(logout);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout); 
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://app.com.grouprun/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
       // distanceBetween();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://app.com.grouprun/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }
    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    public void showNoticeDialog(Bundle bundle) {
        // Create an instance of the dialog fragment and show it
        FragmentManager fm = getSupportFragmentManager();


        DialogFragment dialog = new CompletedRunDialogFragment();
        dialog.setArguments(bundle);
        dialog.show(fm,"run_completed_message");



    }
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialogFragment) {
            Toast.makeText(this, "Run saved!" ,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }


}