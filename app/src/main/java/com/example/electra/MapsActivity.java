package com.example.electra;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    MarkerOptions markerOptions;
    private GoogleMap mMap;
    private TelephonyManager tm;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    SignalStrengthListener signalStrengthListener;
    List<CellInfo> cellInfoList;
    int cellSig, cellID, cellMcc, cellMnc, cellPci, cellTac = 0, cellRsrq, cellSnr;
    String networkOperator, cellNetworkOperator;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        signalStrengthListener = new SignalStrengthListener();

        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(signalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        networkOperator = tm.getNetworkOperator();
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cellInfoList = tm.getAllCellInfo();
        } catch (Exception e) {
            Log.d("SignalStrength", "+++++++++++++++++++++++++++++++++++++++++ null array: " + e);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        try {
            if (signalStrengthListener != null) {
                tm.listen(signalStrengthListener, SignalStrengthListener.LISTEN_NONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        super.onDestroy();

        try {
            if (signalStrengthListener != null) {
                tm.listen(signalStrengthListener, SignalStrengthListener.LISTEN_NONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onLocationChanged(Location location) {

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("RSRP: " + cellSig + "\nSimoperator: " + tm.getSimOperator() + "\nNetworkname: " + cellNetworkOperator + "\nNetworkoperator: " + tm.getNetworkOperator() + "\nRSRQ: " + cellRsrq + "\nSNR: " + cellSnr);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        InfoAdapter adapter = new InfoAdapter(MapsActivity.this);
        mMap.setInfoWindowAdapter(adapter);
        mCurrLocationMarker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    public class SignalStrengthListener extends PhoneStateListener {
        @SuppressLint("NewApi")
        @Override
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {
            try {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cellInfoList = tm.getAllCellInfo();
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoLte) {

                        cellSig = ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();

                        cellRsrq = ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrq();

                        cellSnr = ((CellInfoLte) cellInfo).getCellSignalStrength().getRssnr();

                        cellNetworkOperator = tm.getNetworkOperatorName();

                        cellMcc = ((CellInfoLte) cellInfo).getCellIdentity().getMcc();

                        cellMnc = ((CellInfoLte) cellInfo).getCellIdentity().getMnc();
                    }
                }
            } catch (Exception e) {
                Log.d("SignalStrength", "+++++++++++++++++++++++++++++++ null array: " + e);
            }

            Log.e("SignalStrength", cellSnr+ "");

            super.onSignalStrengthsChanged(signalStrength);
        }
    }
}

