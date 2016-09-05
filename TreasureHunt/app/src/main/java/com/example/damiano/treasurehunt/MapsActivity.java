package com.example.damiano.treasurehunt;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private int MIN_PERIOD = 3000, MIN_DIST = 1; // milliseconds, meters
    private String providerId = LocationManager.GPS_PROVIDER;
    double eventLat, eventLon;
    double myLastLatitude, myLastLongitude;
    long dateStart, dateEnd;
    private Marker myLastPosMarker=null;
    private Circle myLastPosCircle=null;
    private String idEvent;
    private BackgroundTask mMapTask = null;
    private LatLng start=null;
    private LatLng[] indizzi=null;
    private String[] idIndizzi=null;
    private int ultimoIndizioRaggiunto=-1;
    private final float rangeIndizi=30;
    private boolean datiIndizi=false;
    private Marker[] myMarkers=null;
    private Marker startMarker=null;
    private Intent finalResult=null;
    boolean finish =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        eventLat=getIntent().getExtras().getDouble("lat"); //start coordinate
        eventLon=getIntent().getExtras().getDouble("lon");
        idEvent=getIntent().getExtras().getString("idEvent");
        dateStart=Timestamp.valueOf(getIntent().getExtras().getString("date_start")).getTime();
        dateEnd= Timestamp.valueOf(getIntent().getExtras().getString("date_end")).getTime();

        System.out.println("on create idevent "+idEvent);
        mMapTask = new BackgroundTask("{'message_type':'4',\n" + "'idEvent': '"+ idEvent + "',\n }");
        mMapTask.execute((Void) null); //run in backgroun
        System.out.println("on create fine");
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(providerId)) {
            Intent gpsOptionsIntent = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        } else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(providerId, MIN_PERIOD, MIN_DIST, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("on map ready start");
        mMap = googleMap;
        //get element from servlet
        String eventList=null;
        BackgroundTask getEventData = new BackgroundTask("{'message_type':'6',\n}" + "'idEvent': '"+ idEvent + "',\n }");
        getEventData.execute((Void) null); // run in background
        try {
            getEventData.get(10, TimeUnit.SECONDS);
            eventList=getEventData.getResult();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (eventList.length()==0) {
            System.out.println("dati eventi vuoti!");
        }
        String[] startP=eventList.split(",");
        start=new LatLng(Double.parseDouble(startP[0]), Double.parseDouble((startP[1])));
        System.out.println("start: "+start.latitude+","+start.longitude);
        startMarker=mMap.addMarker(new MarkerOptions().position(start).title("start").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17)); // zoom range [2, 21]

        System.out.println("on map ready fine");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (finish && finalResult==null){
            //finish=false;
            //activity finalresut passare idEvent
            finalResult=new Intent(MapsActivity.this, FinalResult.class);
            finalResult.putExtra("idEvent", idEvent);
            startActivity(finalResult);
        }

        System.out.println("on loc change start maps");
        myLastLatitude=location.getLatitude();
        myLastLongitude=location.getLongitude();
        System.out.println("on loc change start maps 0.1");
        if (myLastPosMarker != null)
            myLastPosMarker.remove();
        myLastPosMarker=mMap.addMarker(new MarkerOptions().position(new LatLng(myLastLatitude, myLastLongitude)).title("Current position"));
        // alternativa per usare il cerchietto blu tipico di google maps
        //.icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_point)));
        System.out.println("on loc change start maps 0.2");
        if (myLastPosCircle != null)
            myLastPosCircle.remove();
        myLastPosCircle=mMap.addCircle((new CircleOptions()).center(new LatLng(myLastLatitude, myLastLongitude)).clickable(false).radius(rangeIndizi).strokeColor(Color.GREEN));
        System.out.println("on loc change start maps 0.3");

        if (ultimoIndizioRaggiunto != -1 && ultimoIndizioRaggiunto == indizzi.length) { // se hai finito la partita non controlla più gli indizi
            System.out.println("Partita finita");
            return;
        }
        System.out.println("on loc change start maps 0.4");
        long now=(new Date()).getTime();
        if (now>dateEnd) {
            System.out.println("on loc change time exceeded");
            (new BackgroundTask("{'message_type':'9',\n}" + "'idEvent': '"+ idEvent + "',\n }")).execute((Void) null); // invia messaggio di fallimento
            finish=true;
            ultimoIndizioRaggiunto = indizzi.length;
            return ;
        }
        System.out.println("on loc change start maps 0.5");
        System.out.println("on loc change 1 maps");
        float[] dist=new float[3];
        if (ultimoIndizioRaggiunto==-1)
            Location.distanceBetween(myLastLatitude, myLastLongitude, start.latitude, start.longitude, dist);
        else
            Location.distanceBetween(myLastLatitude, myLastLongitude, indizzi[ultimoIndizioRaggiunto].latitude, indizzi[ultimoIndizioRaggiunto].longitude, dist);

        if (dist[0]<rangeIndizi &&  now >= dateStart) { // ora attuale >= ora inizio evento && siamo a tot metri dal punto iniziale
            System.out.println("on loc change range maps");
            if (datiIndizi==false) {
                System.out.println("on loc change init dati maps");
                //richiedi indizzi
                String eventList=null;
                BackgroundTask getTipsData = new BackgroundTask("{'message_type':'7',\n}" + "'idEvent': '"+ idEvent + "',\n }");
                getTipsData.execute((Void) null); // run in background
                try {
                    getTipsData.get(10, TimeUnit.SECONDS);
                    eventList=getTipsData.getResult();
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                if (eventList.length()==0) {
                    System.out.println("dati eventi vuoti dopo GetTipsData maps");
                }
                // abbiamo solo lo start point
                String[] elements=eventList.split(";");
                String[] coord=null;
                System.out.println("eventlist maps: "+eventList);
                System.out.println("eventlist last maps: "+elements[elements.length-1]);
                int N=Integer.parseInt(elements[elements.length-1]);

                myMarkers= new Marker[N];
                idIndizzi=new String[N];
                indizzi=new LatLng[N];
                for (int i=0;i<N;i++) {
                    coord=elements[i].split(",");
                    indizzi[i]=new LatLng(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]));
                    idIndizzi[i]=coord[2];
                    System.out.println(" maps Indizio "+i+", "+idIndizzi[i]+": "+indizzi[i].latitude+", "+indizzi[i].longitude);
                    myMarkers[i]=mMap.addMarker(new MarkerOptions().position(indizzi[i]).title("Punto "+i).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).visible(false));

                    if (myMarkers[i]==null)
                        System.out.println("MARKER NULLO !!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }

                System.out.println("Dati indizi ottenuti maps");
                datiIndizi=true;
            }


            if (ultimoIndizioRaggiunto == 0) {
                startMarker.setVisible(false);
                myMarkers[0].setVisible(true);
            }
            if (ultimoIndizioRaggiunto > 0) {
                myMarkers[ultimoIndizioRaggiunto-1].setVisible(false);
                myMarkers[ultimoIndizioRaggiunto].setVisible(true);
            }

            //visualizza AR *** passare parametri all'AR di ultimoIndizioRaggiunto
            System.out.println("Chiamo AR: "+datiIndizi+", "+indizzi.length);
            Intent viewCamera=new Intent(MapsActivity.this, CameraViewActivity.class);
            if (ultimoIndizioRaggiunto+1 < indizzi.length) {
                // manda lat, lon del prossimo indizio da raggiungere
                viewCamera.putExtra("next_point_lat", indizzi[ultimoIndizioRaggiunto + 1].latitude);
                viewCamera.putExtra("next_point_lon", indizzi[ultimoIndizioRaggiunto + 1].longitude);
                if(ultimoIndizioRaggiunto==-1) {
                    viewCamera.putExtra("point_lat", start.latitude);
                    viewCamera.putExtra("point_lon", start.longitude);
                }else {
                    viewCamera.putExtra("point_lat", indizzi[ultimoIndizioRaggiunto].latitude);
                    viewCamera.putExtra("point_lon", indizzi[ultimoIndizioRaggiunto].longitude);
                }
                // manda idStep del prossimo indizio da raggiungere
                viewCamera.putExtra("idStep", idIndizzi[ultimoIndizioRaggiunto + 1]);
            }
            else {
                // manda lat, lon del punto in cui si trova il tesoro (stessa direzione del centro città)
                System.out.println("change finish");
                finish=true;
                viewCamera.putExtra("next_point_lat", eventLat);
                viewCamera.putExtra("next_point_lon", eventLon);
                viewCamera.putExtra("point_lat", indizzi[ultimoIndizioRaggiunto ].latitude); //per non avere campo nullo
                viewCamera.putExtra("point_lon", indizzi[ultimoIndizioRaggiunto ].longitude);
                // manda idStep del prossimo indizio da raggiungere
                viewCamera.putExtra("idStep", "-1"); // step fittizio collegato con l'indizio finale e il tesoro
            }
            // manda idEvent da inviare alla sevlet quando verrà trovato il tesoro
            viewCamera.putExtra("idEvent", idEvent);
            startActivity(viewCamera);
            ultimoIndizioRaggiunto++;
        }

        System.out.println("on loc change fine");

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}
