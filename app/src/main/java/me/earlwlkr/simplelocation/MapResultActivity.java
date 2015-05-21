package me.earlwlkr.simplelocation;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapResultActivity extends FragmentActivity {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_result);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        Bundle bundle = getIntent().getExtras();
        double startLat = bundle.getDouble("startLat");
        double startLong = bundle.getDouble("startLong");
        double destLat = bundle.getDouble("destLat");
        double destLong = bundle.getDouble("destLong");

        LatLng startPos = new LatLng(startLat, startLong);
        LatLng destPos = new LatLng(destLat, destLong);

        new ShortestRouteFinder(startPos, destPos, mMap).findShortestRoute();
        // Center camera to start position.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, 17));
        addMarkers(startPos, destPos);
    }

    private void addMarkers(LatLng start, LatLng dest) {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(start)
                    .title("Điểm khởi hành"));
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .position(dest)
                    .title("Điểm đến"));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
