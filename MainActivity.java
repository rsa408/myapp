// MainActivity.java
package com.reza.flightmap;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.gestures.gestures;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private TextView flightInfo;
    private LinearLayout infoPanel;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        flightInfo = findViewById(R.id.flightInfo);
        infoPanel = findViewById(R.id.infoPanel);

        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE_STREETS, style -> {
            mapView.getGestures().setZoomEnabled(true);
            fetchFlightData();
        });
    }

    private void fetchFlightData() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                    .url("https://opensky-network.org/api/states/all")
                    .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            try {
                                JSONObject json = new JSONObject(responseData);
                                JSONArray states = json.getJSONArray("states");

                                if (states.length() > 0) {
                                    JSONArray firstFlight = states.getJSONArray(0);
                                    String callsign = firstFlight.getString(1);
                                    double velocity = firstFlight.getDouble(9);
                                    double heading = firstFlight.getDouble(10);
                                    double geoAltitude = firstFlight.getDouble(13);

                                    runOnUiThread(() -> {
                                        String info = "Flight: " + callsign +
                                                "\nSpeed: " + String.format("%.2f", velocity) + " m/s" +
                                                "\nHeading: " + String.format("%.2f", heading) + "°" +
                                                "\nAltitude: " + String.format("%.2f", geoAltitude) + " m";
                                        flightInfo.setText(info);
                                        infoPanel.setVisibility(View.VISIBLE);
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }, 0, 10000); // Refresh every 10 seconds
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
