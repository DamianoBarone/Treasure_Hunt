package com.example.damiano.treasurehunt;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import java.io.IOException;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;*/

public class CameraViewActivity extends Activity implements
	SurfaceHolder.Callback, OnLocationChangedListener, OnAzimuthChangedListener{

	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;
	private boolean isCameraviewOn = false;
	private AugmentedPOI[] mPoi=null; //, mPoi2, mPoi3;
	private  Toast toast;
	private double mAzimuthReal = 0;
	private double mAzimuthTeoretical = 0;
	private double AZIMUTH_ACCURACY = 5;
	private double mMyLatitude = 0;
	private double mMyLongitude = 0;
	private boolean firstTimeToast=true;
	private MyCurrentAzimuth myCurrentAzimuth;
	private MyCurrentLocation myCurrentLocation;

	TextView descriptionTextView;
	ImageView[] pointerIcon=null; //, pointerIcon2, pointerIcon3;
	String question=null;
    boolean match_finish=false;
    boolean firstTime=false;
	Answer[] answers=null;

	String idProssimoIndizio;
	double nextPointLat, nextPointLon,pointLat,pointLon;
	String idEvent;

	/*public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	public static final String URL  = "http://192.168.1.196:8080/Treasure_server/Servlet";
	OkHttpClient client = new OkHttpClient();

	public String send(String url, String json) {//LOLLO
	    String servletresponse=null;
		System.out.println("funzione send dell'immagine");
		RequestBody body = RequestBody.create(JSON, json);
		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();
		try (Response response = client.newCall(request).execute()) {

			servletresponse=response.body().string();
			System.out.println("immagine grande " +servletresponse.length() );

			return servletresponse;
		} catch (IOException e) {
			System.out.println("eccezionale ");
			e.printStackTrace();
		}
		return "vuoto";

	}*/

	/*Thread thread = new Thread() {//LOLLO
		@Override
		public void run() {
			final String json ="{'message_type':'5',\n}" +
					"'idStep_': '"+ idProssimoIndizio + "',\n }";
			String response_send = send(URL, json);

			if (response_send==null || response_send.length()==0 || response_send.compareTo("vuoto")==0) {
				System.out.println("dati eventi vuoti dopo thread");
			}

			String[] elements=response_send.split(";");
			String[] answerData=null;
			System.out.println("response_send last: "+elements[elements.length-1]);
			int N=Integer.parseInt(elements[elements.length-1]);

			if (N!=3) {
				System.out.println("Errore, numero di risposte errato!");
			}

			question=elements[0];
			System.out.println("Question "+question);

			mPoi=new AugmentedPOI[N];
			pointerIcon=new ImageView[N];

			byte[] encodeByte=null;
			answers=new Answer[N];
			for (int i=0;i<N;i++) {
				answerData=elements[i+1].split(",");
				encodeByte=Base64.decode(answerData[2],Base64.NO_PADDING);
				System.out.println("lunghezza prova: " +encodeByte.length);
				answers[i]=new Answer(answerData[0], (answerData[1].compareTo("1") == 0) ? true : false, BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length));
				System.out.println("Indizio "+i+", "+answers[i].text+", "+answers[i].correct+"("+answerData[1]+"), altezza immagine: " +answers[i].image.getHeight());
			}

			System.out.println("Risposte ottenute");
			firstTime=true;
		}
	};*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		System.out.println("on create start AR");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_view);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		System.out.println("on create start AR 1");
		nextPointLat=getIntent().getExtras().getDouble("next_point_lat");
		nextPointLon=getIntent().getExtras().getDouble("next_point_lon");
		pointLat=getIntent().getExtras().getDouble("point_lat");
		pointLon=getIntent().getExtras().getDouble("point_lon");
		idProssimoIndizio=getIntent().getExtras().getString("idStep");
		idEvent=getIntent().getExtras().getString("idEvent");
		System.out.println("on create start AR 2");
		setupListeners();
		setupLayout();
		//setAugmentedRealityPoint();
		System.out.println("on create start AR 3");
		//thread.start();//LOLLO




		String response_send=null;//new String();
		BackgroundTask getAnswers = new BackgroundTask("{'message_type':'5',\n}" +
				"'idStep_': '"+ idProssimoIndizio + "',\n }");
		getAnswers.execute((Void) null); // run in background
		try {
			getAnswers.get(10, TimeUnit.SECONDS);
			response_send=getAnswers.getResult();
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (response_send==null || response_send.length()==0 || response_send.compareTo("vuoto")==0) {
			System.out.println("dati eventi vuoti dopo thread");
		}

		String[] elements=response_send.split(";");
		String[] answerData=null;
		System.out.println("response_send last: "+elements[elements.length-1]);
		int N=Integer.parseInt(elements[elements.length-1]);

		if (N!=3) {
			System.out.println("Errore, numero di risposte errato!");
		}

		question=elements[0];
		System.out.println("Question "+question);

		mPoi=new AugmentedPOI[N];
		pointerIcon=new ImageView[N];

		byte[] encodeByte=null;
		answers=new Answer[N];
		for (int i=0;i<N;i++) {
			answerData=elements[i+1].split(",");
			encodeByte=Base64.decode(answerData[2],Base64.NO_PADDING);
			System.out.println("lunghezza prova: " +encodeByte.length);
			answers[i]=new Answer(answerData[0], (answerData[1].compareTo("1") == 0) ? true : false, BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length));
			System.out.println("Indizio "+i+", "+answers[i].text+", "+answers[i].correct+"("+answerData[1]+"), altezza immagine: " +answers[i].image.getHeight());
		}

		System.out.println("Risposte ottenute");
		firstTime=true;





		System.out.println("on create fine AR");
	}

	private void setAugmentedRealityPoint() {
		System.out.println("set ar start");
		pointerIcon[0]= (ImageView) findViewById(R.id.icon);
		pointerIcon[0].setImageBitmap(answers[0].image);
		pointerIcon[1]= (ImageView) findViewById(R.id.icon2);
		pointerIcon[1].setImageBitmap(answers[1].image);
		pointerIcon[2]= (ImageView) findViewById(R.id.icon3);
		pointerIcon[2].setImageBitmap(answers[2].image);
		System.out.println("set ar 1");
		mPoi[0] = new AugmentedPOI(
				"Casa",
				"casa che sta a CASUZZE",
				nextPointLat,
				nextPointLon
		);
		mPoi[1] = new AugmentedPOI(
				"Casa 2",
				"indizio sud ovest",
				(2*pointLat-nextPointLat), //44.193803,
				pointLon //9.863363
		);
		mPoi[2] = new AugmentedPOI(
				"Casa 2",
				"indizio nord",
				pointLat, //44.194661,
				(2*pointLon-nextPointLon)//9.863891
		);
		System.out.println("mio lat " + pointLat + " " + pointLon);
		System.out.println("object2 "+(2*pointLat-nextPointLat)+" " +pointLon);
		System.out.println("object3 "+pointLat+" " +(2*pointLon-nextPointLon));

	}

	public double calculateTeoreticalAzimuth(AugmentedPOI Poi) {

		double dX = Poi.getPoiLatitude() - mMyLatitude;   //OGNI TANTO VALE NULL Poi LOLLO
		double dY = Poi.getPoiLongitude() - mMyLongitude;

		double phiAngle;
		double tanPhi;
		double azimuth = 0;

		tanPhi = Math.abs(dY / dX);
		phiAngle = Math.atan(tanPhi);
		phiAngle = Math.toDegrees(phiAngle);

		if (dX > 0 && dY > 0) { // I quater
			return azimuth = phiAngle;
		} else if (dX < 0 && dY > 0) { // II
			return azimuth = 180 - phiAngle;
		} else if (dX < 0 && dY < 0) { // III
			return azimuth = 180 + phiAngle;
		} else if (dX > 0 && dY < 0) { // IV
			return azimuth = 360 - phiAngle;
		}

		return phiAngle;
	}
	
	private List<Double> calculateAzimuthAccuracy(double azimuth) {
		double minAngle = azimuth - AZIMUTH_ACCURACY;
		double maxAngle = azimuth + AZIMUTH_ACCURACY;
		List<Double> minMax = new ArrayList<Double>();

		if (minAngle < 0)
			minAngle += 360;

		if (maxAngle >= 360)
			maxAngle -= 360;

		minMax.clear();
		minMax.add(minAngle);
		minMax.add(maxAngle);

		return minMax;
	}

	private boolean isBetween(double minAngle, double maxAngle, double azimuth) {
		if (minAngle > maxAngle) {
			if (isBetween(0, maxAngle, azimuth) || isBetween(minAngle, 360, azimuth)) // TODO *** ||
				return true;
		} else {
			if (azimuth >= minAngle && azimuth <= maxAngle) // TODO *** >=
				return true;
		}
		return false;
	}

	/*private void updateDescription() {//la scritta (puoi mettere l'indovinello) non c'e' bisogno di refreshare tante volte basta ad ogni indovinello
		System.out.println("update desc start");
		String x;
		if (question!=null)
			x=question;
		else
			x="DOMANDA NON DISPONIBILE ";
		descriptionTextView.setText(x+" --- "+mPoi[0].getPoiName() + " azimuthTeoretical "
				+ mAzimuthTeoretical + " azimuthReal " + mAzimuthReal + " latitude "
				+ mMyLatitude + " longitude " + mMyLongitude);
		System.out.println("update desc fine");
	}*/

	@Override
	public void onLocationChanged(Location location) {
		mMyLatitude = location.getLatitude();
		mMyLongitude = location.getLongitude();
		//mAzimuthTeoretical = calculateTeoreticalAzimuth(mPoi); // inutile ?
		if (firstTimeToast) {
			toast = Toast.makeText(getApplicationContext(), "go back for see the map", Toast.LENGTH_SHORT);
			toast.show();
			firstTimeToast=false;
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					toast.cancel();
				}
			}, 7000);

		}
		//Toast.makeText(this,"go back for the map", Toast.LENGTH_SHORT).show();
		//System.out.println("on loc change 2 AR");
		//updateDescription();
		//System.out.println("on loc change fine AR");
	}

	@Override
	public void onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo) {

		if (firstTime && answers!=null) {
			setAugmentedRealityPoint();
			descriptionTextView.setText(question+"?");
			firstTime = false;
		}
		if (mPoi==null || mPoi[0]==null)
			return ; // useless update image
		mAzimuthReal = azimuthChangedTo;

		double minAngle, maxAngle;

		for (int i=0;i<answers.length;i++) {

			mAzimuthTeoretical = calculateTeoreticalAzimuth(mPoi[i]);
			minAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(0);
			maxAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(1);
			if (isBetween(minAngle, maxAngle, mAzimuthReal)) {
				pointerIcon[i].setVisibility(View.VISIBLE);
				// lancia messaggio fine partita
                //System.out.println("answer: " +answers[i].text );
                if (answers[i].text.compareTo("TREASURE") == 0 && match_finish==false) { // codice che indica il tesoro
					System.out.println("treasure answer");
                    match_finish=true;
					BackgroundTask sendFinishMessage = new BackgroundTask("{'message_type':'8',\n}" + "'idEvent': '"+ idEvent + "',\n }");
					sendFinishMessage.execute((Void) null); // run in background
				}
			} else {
				pointerIcon[i].setVisibility(View.INVISIBLE);
			}
		}

	}

	@Override
	protected void onStop() {
		myCurrentAzimuth.stop();
		myCurrentLocation.stop();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		myCurrentAzimuth.start();
		myCurrentLocation.start();
	}

	private void setupListeners() {
		myCurrentLocation = new MyCurrentLocation(this);
		myCurrentLocation.buildGoogleApiClient(this);
		myCurrentLocation.start();

		myCurrentAzimuth = new MyCurrentAzimuth(this, this);
		myCurrentAzimuth.start();
	}

	private void setupLayout() {
		descriptionTextView = (TextView) findViewById(R.id.cameraTextView);

		getWindow().setFormat(PixelFormat.UNKNOWN);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraview);
		mSurfaceHolder = surfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		if (isCameraviewOn) {
			mCamera.stopPreview();
			isCameraviewOn = false;
		}

		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(mSurfaceHolder);
				mCamera.startPreview();
				isCameraviewOn = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		mCamera.setDisplayOrientation(90);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
		isCameraviewOn = false;
	}

	private class Answer {
		public String text;
		public boolean correct;
		public Bitmap image;
		public Answer(String t, boolean c, Bitmap i) {
			text=t;
			correct=c;
			image=i;
			if (image== null)
				System.out.println("immagine nulla");
		}
	}

}
