package me.earlwlkr.simplelocation;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;



public class MainActivity extends FragmentActivity {

    final int PICK_FIRST_PLACE = 1;
    final int PICK_SECOND_PLACE = 2;

    private LatLng mStartPos;
    private LatLng mDestPos;
    private GoogleMap mMap;
    private LinkedHashMap<String, String> mContactAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200l, 500.0f, new LocationListener() {
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}

                @Override
                public void onLocationChanged(final Location location) {
                    mStartPos = new LatLng(location.getLatitude(), location.getLongitude());
                    TextView text = (TextView) findViewById(R.id.currentPosText);
                    text.setText(mStartPos.toString());
                }
            });
        }
        if (location != null) {
            String addressText = Utils.getAddressFromLocation(getApplicationContext(), location);
            TextView text = (TextView) findViewById(R.id.currentPosText);
            text.setText(addressText);
            mStartPos = new LatLng(location.getLatitude(), location.getLongitude());
        }


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
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Chưa tìm được vị trí được xác định gần đây nhất",
                                    Toast.LENGTH_LONG).show();
                        }
                        break;
                    }
                    case 2: {
                        if (mStartPos != null) {
                            // Get contacts.
                            mContactAddresses = new LinkedHashMap<String, String>();
                            Uri uri = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI;
                            // Declare the fields to get.
                            String[] projection = new String[] {
                                    ContactsContract.Contacts._ID,
                                    ContactsContract.Contacts.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
                            };
                            // Call ContentResolver.
                            Cursor cur = getContentResolver().query(uri, projection, null, null, null);
                            if (cur.getCount() > 0) {
                                while (cur.moveToNext()) {
                                    // If there is an address field.
                                    int colIndex = cur.getColumnIndex(
                                            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
                                    if (colIndex != -1) {
                                        // Put name and address as pair to mContactAddresses.
                                        String contactAddress = cur.getString(colIndex);
                                        String contactName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                        mContactAddresses.put(contactName, contactAddress);
                                    }
                                }
                            }

                            DialogFragment newFragment = new ChooseContactDialog();
                            newFragment.show(getSupportFragmentManager(), "Contacts");
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Chưa tìm được vị trí được xác định gần đây nhất",
                                    Toast.LENGTH_LONG).show();
                        }
                        break;
                    }
                }
            }
        });
    }

    public class ChooseContactDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
            String[] list = new String[mContactAddresses.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : mContactAddresses.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                list[i++] = (key + "\nĐịa chỉ: " + value + "\n");
            }
            builder.setTitle("Chọn contact")
                    .setItems(list, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String addr = (new ArrayList<String>(mContactAddresses.values())).get(which);
                            mDestPos = Utils.getLocationFromAddress(getApplicationContext(), addr);
                            if (mDestPos == null) {
                                Toast.makeText(getApplicationContext(),
                                        "Không thể lấy tọa độ điểm đến",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                drawRoute();
                            }
                        }
                    });
            return builder.create();
        }
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
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
        }

        new ShortestRouteFinder(mStartPos, mDestPos, mMap).findShortestRoute();
        // Center camera to start position.
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
