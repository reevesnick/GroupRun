package app.com.grouprun;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Chronometer;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

import info.hoang8f.widget.FButton;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GMapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    private OnFragmentInteractionListener mListener;
    LocationManager userLocation;
    Location location;
    private static double latitude =0.0;
    private static double longitude = 0.0;
    private GoogleMap mMap;
    FButton button;
    Chronometer chronometer;
    long time;
    MapFragment googleMapFrag;
    TextToSpeech textToSpeech;
    public GMapFragment() {
        // Required empty public constructor
    }


    public static GMapFragment newInstance() {
        GMapFragment fragment = new GMapFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_gmap, container, false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map(googleMap);
            }
        });
        // Inflate the layout for this fragment
        button = (FButton) view.findViewById(R.id.startButton);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OnStartButtonClick();
            }
        });

        chronometer = (Chronometer)view.findViewById(R.id.chronometer);
        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status!=TextToSpeech.ERROR){
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void OnStartButtonClick() {

        String text = button.getText().toString();
        if (text.equals("Start")) {


            chronometer.setBase(SystemClock.elapsedRealtime() + time);
            chronometer.start();
            textToSpeech.speak("Run started", TextToSpeech.QUEUE_FLUSH, null, null);
            button.setButtonColor(Color.RED);
            button.setText("Stop");

        } else {
            time = chronometer.getBase()- SystemClock.elapsedRealtime();
            chronometer.stop();
            textToSpeech.speak("Run stopped", TextToSpeech.QUEUE_FLUSH, null, null);
            button.setButtonColor(Color.GREEN);
            button.setText("Start");
            chronometer.setBase(SystemClock.elapsedRealtime());
            time =0;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void map(GoogleMap googleMap) {
        System.out.println("LONGITUDE: "+ longitude);
        System.out.println("LATITUDE: " + latitude);
        this.mMap =googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            userLocation = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
            location = userLocation.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(location!=null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                LatLng currentLatLng = new LatLng(latitude, longitude);
                //setting initial zoom
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));

                System.out.println("LONGITUDE: "+ longitude);
                System.out.println("LATITUDE: " + latitude);
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
            System.out.println("CAN'T FIND ANYTHING");
        }

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
