package me.earlwlkr.simplelocation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    final int PICK_FIRST_PLACE = 1;
    final int PICK_SECOND_PLACE = 2;

    private LatLng mStartPos;
    private LatLng mDestPos;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        mStartPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        TextView text = (TextView) findViewById(R.id.currentPosText);
        text.setText(mStartPos.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleApiClient();

        final ListView listview = (ListView) findViewById(R.id.mainMenu);
        String[] values = new String[] { "Tìm đường đi ngắn nhất giữa 2 vị trí", "Tìm đường đi từ vị trí hiện tại" +
                " tới vị trí tùy chọn", "Tìm đường đi từ vị trí hiện tại tới các vị trí trong danh bạ" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                switch (position) {
                    case 0: {
                        openPlacePicker(PICK_FIRST_PLACE);
                        break;
                    }
                    case 1: {
                        if (mStartPos != null) {
                            openPlacePicker(PICK_SECOND_PLACE);
                        }
                        break;
                    }
                    case 2: {
                        if (mStartPos.getClass() == LatLng.class) {
                            // ...
                        }
                        break;
                    }
                }
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void openPlacePicker(int req) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        Context context = getApplicationContext();
        try {
            // This will call onActivityResult with requestCode specified when finished.
            startActivityForResult(builder.build(context), req);
            if (req == PICK_FIRST_PLACE) {
                Toast.makeText(this, "Hãy chọn điểm khởi hành", Toast.LENGTH_LONG).show();
            } else if (req == PICK_SECOND_PLACE) {
                Toast.makeText(this, "Hãy chọn điểm đến", Toast.LENGTH_LONG).show();
            }
        } catch (GooglePlayServicesRepairableException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FIRST_PLACE) {
            if (resultCode == RESULT_OK) {
                mStartPos = PlacePicker.getPlace(data, this).getLatLng();
                openPlacePicker(PICK_SECOND_PLACE);
            }
        } else if (requestCode == PICK_SECOND_PLACE) {
            if (resultCode == RESULT_OK) {
                mDestPos = PlacePicker.getPlace(data, this).getLatLng();
                drawRoute();
            }
        }
    }

    private void drawRoute() {
        setContentView(R.layout.layout_map);
        mMap = ((SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map)).getMap();

        new ShortestRouteFinder(mStartPos, mDestPos, mMap).findShortestRoute();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mStartPos, 17));
        addMarkers();
    }

    private void addMarkers() {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(mStartPos)
                    .title("Điểm khởi hành"));
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .position(mDestPos)
                    .title("Điểm đến"));
        }
    }


}
