package app.com.grouprun.Activities;
import android.Manifest;
import android.annotation.TargetApi;
import android.os.Environment;
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
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.TextView;

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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
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
    private static double latitudeOnStart = 0.0;
    private static double prevLatitude = 0.0;
    private static double longitudeOnStart = 0.0;
    private static double prevLongitude = 0.0;
    double distanceTraveled = 0.0;
    FButton button;
    TextToSpeech textToSpeech;
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
    Location prevLocation;
    //private TextView milesLabel;
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GroupRun";

    //Calculating Location Between
    Location location1;
    Location location2;

    //Java Socket Varibles
    TextView info, infoip, msg;
    String message = "";
    ServerSocket serverSocket;

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
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        File dir = new File(path);
        dir.mkdirs();

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

        // Java Socket Layout

        //info = (TextView) findViewById(R.id.info);
        //infoip = (TextView) findViewById(R.id.infoip);
        //msg = (TextView) findViewById(R.id.msg);


        infoip.setText(getIpAddress());

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();


    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                latitudeOnStart = location.getLatitude();
                longitudeOnStart = location.getLongitude();
                LatLng currentLatLng = new LatLng(latitudeOnStart, longitudeOnStart);
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
        prevLocation = new Location("prev_location");
        prevLocation.setLatitude(prevLatitude);
        prevLocation.setLongitude(prevLongitude);
        LatLng prev = new LatLng(prevLatitude, prevLatitude);

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
        this.location = location;

        JSONObject message = new JSONObject();
        try {
            message.put("lat", location.getLatitude());
            message.put("lng", location.getLongitude());
            message.put("alt", location.getAltitude());
        } catch (JSONException e) {
            System.out.print(e.toString());
        }
        mPubnub.publish("MainRunning", message, publishCallback);

        System.out.println("Latitude: " + location.getLatitude());
        System.out.println("Longitude: " + location.getLongitude());


        distanceTraveled += location.distanceTo(prevLocation) * 0.00062137119;
        String distance = String.format("%.2f", distanceTraveled);


        System.out.println("Distance between locations: " + distance + " miles");

        prevLatitude = location.getLatitude();
        prevLongitude = location.getLongitude();

        distanceChronometer.setText(distance);
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
        ParseGeoPoint currentLocation;
        String text = button.getText().toString();
        if (text.equals("Start")) {


            timeChronometer.setBase(SystemClock.elapsedRealtime() + time);
            timeChronometer.start();

            location1 = new Location("Start");
            // location1.setLatitude(latitude);
            //  location1.setLongitude(longitude);


            textToSpeech.speak("Run started", TextToSpeech.QUEUE_FLUSH, null, null);
            currentUser.put("isRunning", true);
            currentLocation = new ParseGeoPoint(latitudeOnStart, longitudeOnStart);
            currentUser.put("current_location", currentLocation);
            currentUser.saveInBackground();
            prevLatitude = currentLocation.getLatitude();
            prevLongitude = currentLocation.getLongitude();


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

            //Create a local file
//end
            timeChronometer.stop();
            textToSpeech.speak("Run stopped", TextToSpeech.QUEUE_FLUSH, null, null);
            currentLocation = new ParseGeoPoint(latitudeOnStart, longitudeOnStart);
            currentUser.put("isRunning", false);
            currentUser.put("current_location", currentLocation);
            currentUser.saveInBackground();
            button.setButtonColor(Color.GREEN);
            button.setText("Start");
            timeChronometer.setBase(SystemClock.elapsedRealtime());

            distanceChronometer.stop();
            distanceChronometer.setText("0.00");
            distanceTraveled = 0;
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
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.music) {
            Intent musicIntent = new Intent(getApplicationContext(), MusicActivity.class);
            startActivity(musicIntent);
        } else if (id == R.id.nav_send) {

        } else if (id == R.id.logout) {
            currentUser.logOutInBackground();

            Intent logout = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(logout);
        } else if (id == R.id.join) {
            Intent join = new Intent(getApplicationContext(), ClientActivity.class);
            startActivity(join);
        }
        else if (id == R.id.currentgroup){
            Intent join = new Intent(getApplicationContext(), ClientActivity.class);
            startActivity(join);

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = ParseUser.getCurrentUser();
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
        dialog.show(fm, "run_completed_message");


    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 8080;
        int count = 0;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                MapActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        info.setText("Port: "
                                + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    Socket socket = serverSocket.accept();
                    count++;
                    message += "#" + count + " from " + socket.getInetAddress()
                            + ":" + socket.getPort() + "\n";

                    MapActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            msg.setText(message);
                        }
                    });

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                            socket, count);
                    socketServerReplyThread.run();

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            String msgReply = "Hello from Android, you are #" + cnt;

            try {
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                printStream.close();

                message += "replayed: " + msgReply + "\n";

                MapActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        msg.setText(message);
                    }
                });

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }

            MapActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    msg.setText(message);
                }
            });
        }

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }
}


