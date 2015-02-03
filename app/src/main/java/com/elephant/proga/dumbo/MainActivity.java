package com.elephant.proga.dumbo;


import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.app.FragmentTransaction;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Property;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elephant.proga.dumbo.helpers.LatLngInterpolator;
import com.elephant.proga.dumbo.interfaces.ConflictHandler;
import com.elephant.proga.dumbo.interfaces.Label;
import com.elephant.proga.dumbo.interfaces.LabelUser;
import com.elephant.proga.dumbo.interfaces.PredictionHandler;
import com.elephant.proga.dumbo.interfaces.PredictionViewer;
import com.elephant.proga.dumbo.interfaces.SelfStatusHandler;
import com.elephant.proga.dumbo.interfaces.TrafficStatusHandler;
import com.elephant.proga.dumbo.receivers.ConflictMonitorReceiver;
import com.elephant.proga.dumbo.receivers.PredictionReceiver;
import com.elephant.proga.dumbo.receivers.SelfStatusReceiver;
import com.elephant.proga.dumbo.receivers.TrafficReceiver;
import com.elephant.proga.dumbo.viewers.ConnectedParticles;
import com.elephant.proga.dumbo.viewers.HeatMap;
import com.elephant.proga.dumbo.viewers.Particles;
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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;


public class MainActivity extends FragmentActivity implements SelfStatusHandler, GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener, TrafficStatusHandler, PredictionHandler, LabelUser, GoogleMap.CancelableCallback,
        GoogleMap.OnMapClickListener, ConflictHandler {

    private final String ROOTSOURCE = "http://192.168.2.33:8080";
    private final String SELFSOURCE = ROOTSOURCE + "/traffic?item=myState";
    private final String TRAFFICSOURCE = ROOTSOURCE + "/traffic?item=traffic";
    private final String PREDICTIONSOURCE = ROOTSOURCE + "/prediction";
    private final String MONITORSOURCE = ROOTSOURCE + "/prediction";

    private final static String RAWPREDICTIONTYPE = "RAW";
    private final static String CUBESPREDICTIONTYPE = "CUBES";
    private final static String USEDPREDICTION = RAWPREDICTIONTYPE;
    private final static String HEATMAPVISUALIZATION = "HEATMAPVIEW";
    private final static String PARTICLESVISUALIZATION = "PARTICLESVIEW";
    private final static String CONNECTEDPARTICLESVISUALIZATION = "CONNECTEDPARTICLESVIEW";
    private final static String CUBESVISUALIZATION = "CUBEVIEW";
    private final static String PREDICTIONVISUALIZATION = CONNECTEDPARTICLESVISUALIZATION;

    private final static String SELFFLIGHTID = "SELF";

    private static final long SELFSLEEPINGTIME = 1000;
    private static final long MAPANIMATIONTIME = 1500;

    private static final long TRAFFICSLEEPINGTIME = 2000;
    private static final long MONITORSLEEPINGTIME = 2000;
    private static final double METERS_TO_FEET = 3.2808399;


    private Hashtable<String, Marker> traffic;
    private HashSet conflicts;

    private static final float DEFAULT_TILT = 60;
    private float autoZoomLevel = 12;
    private float userZoomLevel = -1;
    private float currentZoomLevel = autoZoomLevel;
    private Marker lastTargetRequested = null;

    private boolean monitorActive = false;


    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private TextView mGeneralInfo;
    private FrameLayout mLabel;
    private FragmentLabel mFragmentLabel = null;
    private boolean labelVisible = false;
    private TextView mFlight_id;
    private TextView mAltitude;



    //i need to retain them in order to place a view to an specific position
    //it will be the position of the selected aircraft (more or less)
    private FrameLayout mFrameLayout;
    private FrameLayout.LayoutParams mLayoutParams;

    private LinearLayout mLabelLayout;
    private boolean labelAdded = false;
    private Marker me;
    private double meAltitude = -1;

    private HeatmapTileProvider heatmapProvider;
    private TileOverlay heatmapTileOverlay;
    private PredictionViewer predictionViewer;


    //receiver threads
    private Thread selfPositionThread;
    private Thread trafficPositionThread;
    private Thread predictionThread;
    private PredictionReceiver predictionReceiver;

    private Thread monitorThread;
    private ConflictMonitorReceiver conflictMonitorReceiver;

    //raw prediction container
    //private Hashtable<Integer,ArrayList<Particle>> particles;
    private Hashtable<String,Prediction> predictions;
    //normal prediction container
    private Object cubes;
    private boolean cameraChangeListenerIsSet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraChangeListenerIsSet = false;
        setUpMapIfNeeded();
        setUpComponents();
        try {
            createLabel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.traffic = new Hashtable();
        this.heatmapTileOverlay = null;
        this.heatmapProvider = null;

        this.findPredictionViewer();

        mFrameLayout = (FrameLayout) findViewById(R.id.mainFrameLayout);

        mLabel = null;

        this.trafficPositionThread = null;

        //start thread for receiving own position
        receivePosition();
        receiveTraffic();
    }

    private void createLabel() throws Exception {
        mFragmentLabel = new FragmentLabel();
        if (!(mFragmentLabel instanceof Label))
            throw new Exception("The label MUST implement the Label Interface");

        mFragmentLabel.setLabelUser(this);
        Log.d("TOUCH","FRAGMENT CREATED");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.mainFrameLayout, mFragmentLabel).commit();
    }

    private void findPredictionViewer() {

        if (PREDICTIONVISUALIZATION == HEATMAPVISUALIZATION) setPredictionViewer(new HeatMap());
        if (PREDICTIONVISUALIZATION == PARTICLESVISUALIZATION) setPredictionViewer(new Particles());
        if (PREDICTIONVISUALIZATION == CONNECTEDPARTICLESVISUALIZATION) setPredictionViewer(new ConnectedParticles(this.getApplicationContext()));

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


    private void animateMap(GoogleMap map, LatLng newLocation, float bearing) {


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(newLocation)      // Sets the center of the map to the new position
                .zoom(this.currentZoomLevel)                   // Sets the zoom
                .bearing(bearing)                // Sets the orientation of the camera to east
                .tilt(60)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder

        this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),(int) SELFSLEEPINGTIME, this);
    }



    private void updateSelfUI(JSONObject jSelf) {
        double lat;
        double lon;
        double h = -1;
        double vx;
        double vy;
        float bearing = 0;

        if (jSelf == null) return;

        try {

            lat = jSelf.getDouble("lat");
            lon = jSelf.getDouble("lon");
            vx = jSelf.getDouble("vx");
            vy = jSelf.getDouble("vy");
            bearing = getRotAngle(vx,vy);
            this.meAltitude = jSelf.getDouble("h");


            if(this.me == null) {
                this.me = this.mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.self_30))
                        .flat(true)
                        .title(SELFFLIGHTID));
            }
            else
            {
                LatLng currentPosition = new LatLng(lat,lon);
                LatLngInterpolator linearInterpolator = new LatLngInterpolator.Linear();
                animateMarkerToICS(this.me,currentPosition,linearInterpolator,bearing,SELFSLEEPINGTIME);
            }

            this.animateMap(mMap, new LatLng(lat,lon), bearing);

            if(this.cameraChangeListenerIsSet == false) {
                mMap.setOnCameraChangeListener(this);
                this.cameraChangeListenerIsSet = true;
            }



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
                //Log.d("TRAFFIC", "Updating traffic");
                updateTrafficUI(jTraffic);
                updateLabel();

            }
        });
    }

    private void updateLabel() {
        //let's remember the dirty stuff: altitude is inside the snippet property of the marker :P
        if (traffic != null) {
            String selectedFlight = mFragmentLabel.getFlightID();
            if (selectedFlight != null && selectedFlight != "SELF") {
                float altitude = Float.valueOf(traffic.get(selectedFlight).getSnippet());
                mFragmentLabel.setAltitude(altitude);
            }

        }

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
                //Log.d("TRAFFIC UPDATE", String.format("flight id:%s",key));
                try {
                    JSONObject status = (JSONObject) jTraffic.get(key);
                    double lat = status.getDouble("lat");
                    double lon = status.getDouble("lon");
                    double h = status.getDouble("h");
                    h = h * METERS_TO_FEET;

                    double vx = status.getDouble("vx");
                    double vy = status.getDouble("vy");
                    float bearing = getRotAngle(vx,vy);

                    //look for pointed element in the already present traffic
                    current = this.traffic.get(key);
                    //if element was not in traffic, then we create marker and retain reference in traffic structure
                    //for later access
                    if (current == null) {
                        //we put the altitude in the snippet field, it's dirty and i know it
                        //but i can't extend Marker class and composition is too much for just one field
                        //to add
                        current = this.mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lon))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_yellow_30))
                                .rotation(bearing)
                                .snippet(String.valueOf(h))
                                .flat(true)
                                .title(key));
                        this.traffic.put(key,current);
                    }
                    else
                    {
                        current.setSnippet(String.valueOf(h));
                        setTrafficColor(current,h);
                    }
                    //finally we animate it ()
                    LatLngInterpolator linearInterpolator = new LatLngInterpolator.Linear();
                    animateMarkerToICS(current, new LatLng(lat,lon),linearInterpolator,bearing,TRAFFICSLEEPINGTIME);
                    if (this.predictionViewer != null && this.mMap != null && this.lastTargetRequested != null && this.me != null)
                            this.predictionViewer.updatePrediction(this.mMap,this.lastTargetRequested.getPosition(),this.me.getPosition(),this.meAltitude);


                } catch (JSONException e) {
                    // Something went wrong!
                    Log.e("JSONERROR", "SOMETHING WRONG WITH JSON");
                }
            }
        }
    }

    private void setTrafficColor(Marker flight, double h) {
        double delta = Math.abs(h - this.meAltitude);

        boolean inConclict = false;
        if (this.conflicts != null)
            inConclict = this.conflicts.contains(flight.getTitle());
        if (inConclict)
            flight.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_red_30));
        else {
            if (delta >= 450) {
                flight.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_green_30));
            } else
                flight.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_yellow_30));
        }

    }



    private float getRotAngle(double vx, double vy) {

        if (vx == 0.0 && vy == 0.0)
            return 0.0f;

        float rotAngle = (float) Math.acos(vy/(Math.sqrt(Math.pow(vx,2)+Math.pow(vy,2)))) * (float) (vx/Math.abs(vx));
        return rotAngle * (180/(float)Math.PI);

    }


    static void animateMarkerToICS(Marker marker, LatLng finalPosition, final LatLngInterpolator latLngInterpolator, float rotation, long duration ) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };

        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        PropertyValuesHolder pvhRot = PropertyValuesHolder.ofFloat("rotation",rotation);
        PropertyValuesHolder pvhLatLng = PropertyValuesHolder.ofObject(property,typeEvaluator,finalPosition);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(marker,pvhLatLng,pvhRot);


        //ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(duration);
        animator.start();
    }

    @Override
    public void onConflictDetected(HashSet flightIDs) {
        if(this.conflicts == null) {
            this.conflicts = new HashSet();
        }
        else
            this.conflicts.clear();
        Log.d("MONITOR_ME","CONFLICT RECEIVED");
        Iterator i = flightIDs.iterator();
        String c;
        while(i.hasNext()) {
            c = (String) i.next();
            this.conflicts.add(c);

            Log.d("MONITOR_ME",String.format("CONFLICT WITH:%s", c));
        }

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
        if (id == R.id.action_clear) {
            if (this.predictionViewer != null)
            {
                this.predictionViewer.removePrediction(mMap);
                this.predictions = null;
            }
        }
        if (id == R.id.action_monitor_me) {
            if (monitorActive) {
                Log.d("MONITOR_ME", "monitor clicked - DEACTIVATING");
                //stop monitor thread

                this.monitorThread.interrupt();
                this.monitorThread = null;
                this.conflictMonitorReceiver = null;
                monitorActive = false;


            }
            else
            {
                Log.d("MONITOR_ME", "monitor clicked - ACTIVATING");
                if (this.conflictMonitorReceiver == null) {
                    this.conflictMonitorReceiver = new ConflictMonitorReceiver(this, MONITORSOURCE, MONITORSLEEPINGTIME);
                    this.monitorThread = new Thread(this.conflictMonitorReceiver);

                }

                this.monitorThread.start();
                monitorActive = true;
            }

        }


        return super.onOptionsItemSelected(item);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        this.currentZoomLevel = this.autoZoomLevel;
        //mMap.setOnCameraChangeListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);





    }

    private void setUpComponents() {

        mGeneralInfo = (TextView) findViewById(R.id.GeneralInformationText);
        mGeneralInfo.setText(":)");

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        this.currentZoomLevel = cameraPosition.zoom;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //askPrediction(marker);

        Log.d("MARKER", String.format("%s : %s",marker.getTitle(),SELFFLIGHTID));
        if (marker.getTitle().equals(SELFFLIGHTID))
            showSelfLabel();
        else
            showLabel(marker);

        //we only have one marker for the moment
        //i return true to avoid map centering automatically on the marker
        return true;
    }

    private void showSelfLabel() {

    }

    private void showLabel(Marker marker) {
        Projection proj = mMap.getProjection();
        LatLng markerPos = marker.getPosition();
        Point screenPoint = proj.toScreenLocation(markerPos);
        int x = screenPoint.x;
        int y = screenPoint.y;
        mGeneralInfo.setText(String.format("x:%d - y:%d", x,y));

        if (predictionReceiver==null)
            predictionReceiver = new PredictionReceiver(this,this.PREDICTIONSOURCE);



        mFragmentLabel.setPosition(x,y);

        //mFragmentLabel.getView().setTranslationX(x);
        //mFragmentLabel.getView().setTranslationY(y);
        mFragmentLabel.setFlightID(marker.getTitle());
        mFragmentLabel.showLabel();


    }

    private void askPrediction(Marker marker) {
        this.predictions = null;
        this.lastTargetRequested = marker;
        marker.showInfoWindow();
        Log.d("MARKER",String.format("MARKER TOUCHED, HELLO I M %s",marker.getTitle()));
        PredictionReceiver pr = new PredictionReceiver(this,this.PREDICTIONSOURCE);
        pr.setPredictionParams(marker.getTitle(), 300, 1, USEDPREDICTION==RAWPREDICTIONTYPE);
        this.predictionThread = new Thread(pr);
        this.predictionThread.start();
    }

    @Override
    public void onPredictionReceived(Object prediction) {

        Log.d("PREDICTION", "Hey, prediction received maybe");

        if (USEDPREDICTION==RAWPREDICTIONTYPE) {

            //this.particles = (Hashtable<Integer, ArrayList<Particle>>) prediction;
            this.predictions = (Hashtable<String,Prediction>) prediction;
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRawPredictionReceived(predictions, lastTargetRequested);
                }
            });

        }
        else
        {
            Log.d("PREDICTION", "Hey, prediction with cubes received maybe");
        }
        //this.predictions = null;
        //this.particles = null;

    }

    @Override
    public void onPredictionFailed(int motivation) {
        switch(motivation) {
            case PredictionReceiver.PREDICTION_FAILED_JSON_TO_OBJECT: { break; }
            case PredictionReceiver.PREDICTION_FAILED_SERVER_RETURNED_NULL: { break; }
            case PredictionReceiver.PREDICTION_FAILED_STRING_TO_JSON: { break; }
        }

    }




    public void onRawPredictionReceived(Hashtable<String, Prediction> predictions, Marker selectedFlight) {
        //delete from map all of the other circles
        this.predictionViewer.removePrediction(mMap);
        this.predictionViewer.drawPrediction(predictions,mMap,selectedFlight.getPosition(),this.me.getPosition(),this.meAltitude);
        this.predictions = null;

    }


    @Override
    public void predictionSelected(int prediction, String flightid) {
        this.lastTargetRequested = this.traffic.get(flightid);
        //Log.d("MARKER",String.format("MARKER TOUCHED, HELLO I M %s",marker.getTitle()));
        PredictionReceiver pr = new PredictionReceiver(this,this.PREDICTIONSOURCE);
        //public boolean setPredictionParams(String flight, int dt, int nsteps, boolean rawPrediction) {
        int prediction_secs = prediction*60;
        //this value represents the size of each intermediate predition in seconds
        //if 30 seconds it's set then we have 1 prediciton every 30 seconds until we reach the prediction_Secs limit
        int prediction_slot_size = 30;
        int steps = prediction_secs / prediction_slot_size;
        pr.setPredictionParams(flightid, prediction*60, steps, USEDPREDICTION==RAWPREDICTIONTYPE);
        this.predictionThread = new Thread(pr);
        this.predictionThread.start();
        //return true;
    }


    //CANCELLABLECALLBACK INTERFACE METHODS
    @Override
    public void onFinish() {
        //Log.d("SELF","ANIMATION FINISHED");

    }

    @Override
    public void onCancel() {
        //Log.d("SELF","ANIMATION CANCELED");

    }

    @Override
    public void onMapClick(LatLng latLng) {
        this.mFragmentLabel.hideLabel();
    }
}
