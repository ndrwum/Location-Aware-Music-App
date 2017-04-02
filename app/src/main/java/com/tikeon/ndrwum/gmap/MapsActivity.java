package com.tikeon.ndrwum.gmap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, MediaPlayer.OnPreparedListener {

    private static MediaPlayer mPlayer;
    protected GoogleApiClient mGoogleApiClient;
    String result;
    Location owl, ppl, ssl;
    Circle mCircle;
    TextView text;
    Double lat, lng;
    private GoogleMap mMap;
    private LatLng oldWell, polkPlace, sitterson, center;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
        text = (TextView) findViewById(R.id.textView);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        owl = new Location("owl");
        owl.setLatitude(35.912073);
        owl.setLongitude(-79.051230);
        oldWell = new LatLng(owl.getLatitude(), owl.getLongitude());
        ppl = new Location("ppl");
        ppl.setLatitude(35.910762);
        ppl.setLongitude(-79.050555);
        polkPlace = new LatLng(ppl.getLatitude(), ppl.getLongitude());
        ssl = new Location("ssl");
        ssl.setLatitude(35.909925);
        ssl.setLongitude(-79.053235);
        sitterson = new LatLng(ssl.getLatitude(), ssl.getLongitude());
        center = new LatLng(35.910486, -79.051930);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.addMarker(new MarkerOptions().position(oldWell).title("Old Well"));
        mMap.addMarker(new MarkerOptions().position(polkPlace).title("Polk Place"));
        mMap.addMarker(new MarkerOptions().position(sitterson).title("Sitterson Hall"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 17));
    }


    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        Geocoder g = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addressList = g.getFromLocation(lat, lng, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }
                sb.append(address.getLocality()).append("\n");
                sb.append(address.getPostalCode()).append("\n");
                result = sb.toString();
                text.post(new Runnable() {
                    public void run() {
                        text.setText(result + " Lat: " + lat + " Long: " + lng);
                        text.invalidate();
                    }
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        float distance2ow = location.distanceTo(owl);
        float distance2pp = location.distanceTo(ppl);
        float distance2st = location.distanceTo(ssl);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (distance2ow < 51) {
            if (mCircle != null) {
                mCircle.remove();
            }
            mCircle = mMap.addCircle(new CircleOptions().center(oldWell).radius(50).strokeColor(Color.RED));
            playMusic("ow");
        } else if (distance2pp < 51) {
            if (mCircle != null) {
                mCircle.remove();
            }
            mCircle = mMap.addCircle(new CircleOptions().center(polkPlace).radius(50).strokeColor(Color.RED));
            playMusic("pp");
        } else if (distance2st < 51) {
            if (mCircle != null) {
                mCircle.remove();
            }

            mCircle = mMap.addCircle(new CircleOptions().center(sitterson).radius(50).strokeColor(Color.RED));
            playMusic("st");
        } else {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
            if (mCircle != null) {
                mCircle.remove();
            }
        }
    }

    public void playMusic(String loc) {
        String ows = "https://www.partnersinrhyme.com/pir/libs/media/Arabian_Salsa_1.wav";
        String pps = "https://www.partnersinrhyme.com/pir/libs/media/1234_Rock_it.wav";
        String sss = "https://www.partnersinrhyme.com/pir/libs/media/Analog_Boys_2.wav";
        if (mPlayer == null || !mPlayer.isPlaying()) {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (loc == "ow") {
                try {
                    mPlayer.setDataSource(ows);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (loc == "pp") {
                try {
                    mPlayer.setDataSource(pps);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (loc == "st") {
                try {
                    mPlayer.setDataSource(sss);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(this);
        }

    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mPlayer != null) {
            mPlayer.stop();
        }
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayer != null) {
            mPlayer.start();
        }
        if (mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.pause();
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(2000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

}