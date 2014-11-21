package com.elephant.proga.dumbo;


import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elephant.proga.dumbo.interfaces.PredictionHandler;
import com.elephant.proga.dumbo.interfaces.PredictionViewer;
import com.elephant.proga.dumbo.interfaces.SelfStatusHandler;
import com.elephant.proga.dumbo.interfaces.TrafficStatusHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;


public class MainActivity extends FragmentActivity implements SelfStatusHandler, GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerClickListener, TrafficStatusHandler, PredictionHandler {

    private final String ROOTSOURCE = "http://192.168.1.21:8080";
    private final String SELFSOURCE = ROOTSOURCE + "/traffic?item=myState";
    private final String TRAFFICSOURCE = ROOTSOURCE + "/traffic?item=traffic";
    private final String PREDICTIONSOURCE = ROOTSOURCE + "/prediction";
    private final static String RAWPREDICTIONTYPE = "RAW";
    private final static String CUBESPREDICTIONTYPE = "CUBES";
    private final static String USEDPREDICTION = RAWPREDICTIONTYPE;
    private final static String HEATMAPVISUALIZATION = "HEATMAPVIEW";
    private final static String PARTICLESVISUALIZATION = "PARTICLESVIEW";
    private final static String CUBESVISUALIZATION = "CUBEVIEW";
    private final static String PREDICTIONVISUALIZATION = HEATMAPVISUALIZATION;

    private static final long SELFSLEEPINGTIME = 2000;
    private static final long TRAFFICSLEEPINGTIME = 2000;

    private Hashtable<String, Marker> traffic;

    private float autoZoomLevel = 12;
    private float userZoomLevel = -1;
    private float currentZoomLevel = autoZoomLevel;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private TextView mGeneralInfo;
    private LinearLayout mLabel;

    //i need to retain them in order to place a view to an specific position
    //it will be the position of the selected aircraft (more or less)
    private FrameLayout mFrameLayout;
    private FrameLayout.LayoutParams mLayoutParams;

    private LinearLayout mLabelLayout;
    private boolean labelAdded = false;
    private Marker me;

    private HeatmapTileProvider heatmapProvider;
    private TileOverlay heatmapTileOverlay;
    private PredictionViewer predictionViewer;

    //receiver threads
    private Thread selfPositionThread;
    private Thread trafficPositionThread;
    private Thread predictioThread;

    //raw prediction container
    private Hashtable<Integer,ArrayList<Particle>> particles;
    //normal prediction container
    private Object cubes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
        setUpComponents();

        this.traffic = new Hashtable();
        this.heatmapTileOverlay = null;
        this.heatmapProvider = null;

        this.findPredictionViewer();

        mFrameLayout = (FrameLayout) findViewById(R.id.MainFrameLayout);
        mLabel = null;

        this.trafficPositionThread = null;

        //start thread for receiving own position
        receivePosition();
        receiveTraffic();
    }

    private void findPredictionViewer() {

        if (PREDICTIONVISUALIZATION == HEATMAPVISUALIZATION) setPredictionViewer(new PredictionHeatMap());
        if (PREDICTIONVISUALIZATION == PARTICLESVISUALIZATION) setPredictionViewer(new PredictionParticles());


    }

    public void setPredictionViewer(PredictionViewer pv) {
        this.predictionViewer = null;
        this.predictionViewer = pv;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setUpMapIfNeeded();
        receivePosition();
    }


    private void receivePosition() {
        this.selfPositionThread = new Thread(new SelfStatusReceiver(this, this.SELFSOURCE, SELFSLEEPINGTIME));
        this.selfPositionThread.start();
    }

    private void updateSelfUI(JSONObject jSelf) {
        double lat;
        double lon;
        //double h = 0;
        double vx;
        double vy;

        if (jSelf == null) return;

        try {

            lat = jSelf.getDouble("lat");
            lon = jSelf.getDouble("lon");
            vx = jSelf.getDouble("vx");
            vy = jSelf.getDouble("vy");
            //h = jSelf.getDouble("h");


            if(this.me == null) {
                this.me = this.mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.self_30))
                        .flat(true)
                        .title("SELF"));
            }
            else
            {
                animateMarker(this.me,new LatLng(lat,lon),getRotAngle(vx,vy),false,SELFSLEEPINGTIME);
            }

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(lat,lon))      // Sets the center of the map to the new position
                    .zoom(this.currentZoomLevel)                   // Sets the zoom
//                .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(60)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),2000,null);



        } catch (JSONException e) {
            e.printStackTrace();
            //da capire perchè potrebbe generare un'eccezione
            //e decidere se chiudere tutto o lasciar perdere
        }

    }

    @Override
    public void onSelfStatusUpdate(final JSONObject jSelf) {
        //we are still on the parallele thread, we need
        //to call the interface update method in the mainthread

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateSelfUI(jSelf);
            }
        });


    }


    private void receiveTraffic() {
        this.trafficPositionThread = new Thread(new TrafficReceiver(this, this.TRAFFICSOURCE, TRAFFICSLEEPINGTIME));
        this.trafficPositionThread.start();
    }

    @Override
    public void onTrafficUpdate(final JSONObject jTraffic) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                updateTrafficUI(jTraffic);

            }
        });
    }

    private void updateTrafficUI(JSONObject jTraffic) {
        if (jTraffic == null)
        {
            Log.d("TRAFFIC UPDATE",String.format("JSONObject was null"));
        }
        else
        {
            Iterator<String> iter = jTraffic.keys();
            Marker current;
            while (iter.hasNext()) {
                String key = iter.next();
                Log.d("TRAFFIC UPDATE", String.format("flight id:%s",key));
                try {
                    JSONObject status = (JSONObject) jTraffic.get(key);
                    double lat = status.getDouble("lat");
                    double lon = status.getDouble("lon");
                    //double h = status.getDouble("h");

                    double vx = status.getDouble("vx");
                    double vy = status.getDouble("vy");

                    current = this.traffic.get(key);


                    if (current == null) {
                        //Log.d("TRAFFIC UPDATE", String.format("flight id:%s was not in our hashtable",key));
                        current = this.mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lon))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_yellow_30))
                                .flat(true)
                                .title(key));
                        //Log.d("TRAFFIC UPDATE", String.format("Adding %s, currently we have %d elements",key,this.traffic.size()));
                        this.traffic.put(key,current);
                        //Log.d("TRAFFIC UPDATE", String.format("We now have %d elements in the hasthable",this.traffic.size()));

                    }


                    //float angle = getRotAngle(vx,vy);

                    this.animateMarker(current, new LatLng(lat,lon),getRotAngle(vx,vy),false,TRAFFICSLEEPINGTIME);

                } catch (JSONException e) {
                    // Something went wrong!
                    Log.e("JSONERROR", "SOMETHING WRONG WITH JSON");
                }
            }
        }
    }



    private float getRotAngle(double vx, double vy) {

        if (vx == 0.0 && vy == 0.0)
            return 0.0f;

        float rotAngle = (float) Math.acos(vy/(Math.sqrt(Math.pow(vx,2)+Math.pow(vy,2)))) * (float) (vx/Math.abs(vx));
        return rotAngle * (180/(float)Math.PI);

    }

    public void animateMarker(final Marker marker, final LatLng toPosition, final float rotAngle,
                              final boolean hideMarker, final long duration) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = this.mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final float startAngle = marker.getRotation();
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                float intermediate_angle = t * rotAngle + (1 -t) * startAngle;

                marker.setPosition(new LatLng(lat, lng));
                marker.setRotation(intermediate_angle);


                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setOnCameraChangeListener(this);
            mMap.setOnMarkerClickListener(this);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(10, 10))
                .title("Hello motherfucker"));

    }

    private void setUpComponents() {

        mGeneralInfo = (TextView) findViewById(R.id.GeneralInformationText);
        mGeneralInfo.setText("Hello MotherFucker");

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        this.currentZoomLevel = cameraPosition.zoom;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //askPrediction(marker);
        showLabel(marker);

        //we only have one marker for the moment
        //i return true to avoid map centering automatically on the marker
        return true;
    }

    private void showLabel(Marker marker) {
        Projection proj = mMap.getProjection();
        LatLng markerPos = marker.getPosition();
        Point screenPoint = proj.toScreenLocation(markerPos);
        int x = screenPoint.x;
        int y = screenPoint.y;
        mGeneralInfo.setText(String.format("x:%d - y:%d", x,y));


        if (mLabel == null) {
            LayoutInflater inflater = getLayoutInflater();
            mLabel = (LinearLayout) inflater.inflate(R.layout.label_layout, null);

            //int width = aircraftID.getWidth();
            //int height = aircraftID.getHeight();
            mLayoutParams = new FrameLayout.LayoutParams(100,100);
            //mLayoutParams.leftMargin = x;
            //mLayoutParams.topMargin = y;
            mFrameLayout.addView(mLabel,mLayoutParams);
        }



        mLabel.setTranslationX(x);
        mLabel.setTranslationY(y);
        TextView aircraftID = (TextView) mLabel.findViewById(R.id.AircraftID);
        aircraftID.setText(String.format("x:%d - y:%d", x,y));



    }

    private void askPrediction(Marker marker) {
        marker.showInfoWindow();
        Log.d("MARKER",String.format("MARKER TOUCHED, HELLO I M %s",marker.getTitle()));
        PredictionReceiver pr = new PredictionReceiver(this,this.PREDICTIONSOURCE);
        pr.setPredictionParams(marker.getTitle(), 300, 1, USEDPREDICTION==RAWPREDICTIONTYPE);
        this.predictioThread = new Thread(pr);
        this.predictioThread.start();
    }

    public void onPredictionReceived(Object prediction) {

        Log.d("PREDICTION", "Hey, prediction received maybe");

        if (USEDPREDICTION==RAWPREDICTIONTYPE) {
            this.particles = (Hashtable<Integer, ArrayList<Particle>>) prediction;
            this.onRawPredictionReceived(particles);
        }
        else
        {
            Log.d("PREDICTION", "Hey, prediction with cubes received maybe");
        }

    }


    public void onRawPredictionReceived(Hashtable<Integer, ArrayList<Particle>> particles) {
        //delete from map all of the other circles
        this.predictionViewer.removePrediction(mMap);
        this.predictionViewer.drawPrediction(particles,mMap);

    }



}
