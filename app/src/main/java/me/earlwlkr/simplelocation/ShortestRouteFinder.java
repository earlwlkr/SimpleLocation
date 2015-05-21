package me.earlwlkr.simplelocation;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by earl on 4/29/2015.
 */
public class ShortestRouteFinder {

    private PolylineOptions mPolyLineOptions;
    private LatLng mStartPos;
    private LatLng mDestPos;
    private GoogleMap mMap;

    public ShortestRouteFinder(LatLng start, LatLng dest, GoogleMap map) {
        mPolyLineOptions = null;
        mStartPos = start;
        mDestPos = dest;
        mMap = map;
    }

    public void findShortestRoute() {
        String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);
    }

    private String getMapsApiDirectionsUrl() {
        String waypoints = "origin="
                + mStartPos.latitude + "," + mStartPos.longitude
                + "&destination=" + mDestPos.latitude + ","
                + mDestPos.longitude;

        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + waypoints;
        return url;
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = Utils.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }
    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                routes = Utils.parseDirections(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                mPolyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                mPolyLineOptions.addAll(points);
                mPolyLineOptions.width(5);
                mPolyLineOptions.color(Color.BLUE);
            }

            mMap.addPolyline(mPolyLineOptions);
        }
    }
}
