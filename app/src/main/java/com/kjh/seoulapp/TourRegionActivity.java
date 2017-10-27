package com.kjh.seoulapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kjh.seoulapp.data.CulturalData;
import com.kjh.seoulapp.data.ProblemData;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

public class TourRegionActivity extends AuthActivity
		implements View.OnClickListener
{
    private static final String TAG = "TourRegionActivity";
    static final int GPS_PERMISSION_REQUEST = 1235;
	static final int INFO_TAB = 1;
	static final int ROAD_TAB = 2;
	static final int QUIZ_START_TAB = 3;


	// static PlaceholderFragment class에서 access하기 위하여 static declaration
	static Button quizStart;
	static ViewFlipper flipper;
	static ToggleButton toggleFlipping;
	static TextView infotextview = null;

	DatabaseReference ref;
	String inputData;


	static String infoData;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		/* auto-generated code */
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tour_region);


		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

       /*for(int num1=0 ; num1 < 2 ; num1++){
            ImageView img = new ImageView(this);
            img.setImageResource(R.drawable.t4+num1);
            flipper.addView(img);
        }

        Animation showIn= AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);

        flipper.setInAnimation(showIn);

        flipper.setOutAnimation(this, android.R.anim.slide_out_right);
*/

        /*



        toggleFlipping.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    flipper.setFlipInterval(2000);
                    flipper.startFlipping();
                }else{
                    flipper.stopFlipping();
                }
            }
        });
*/

		/* check gps permissions */
		if (ContextCompat.checkSelfPermission(TourRegionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
			ActivityCompat.requestPermissions(TourRegionActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, GPS_PERMISSION_REQUEST);
		if (ContextCompat.checkSelfPermission(TourRegionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
			ActivityCompat.requestPermissions(TourRegionActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION_REQUEST);

		/* init members */
		inputData = TourMainActivity.regionFlag;
		ref = database.getReference("cultural").child(inputData);
		infoData = "infoContent";
	}

    @Override
    public void onStart()
    {
        super.onStart();
        loadCulturalData();
    } // onStart()

    void loadCulturalData()
    {
        Log.v(TAG, ref.toString());
		ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                CulturalData cultural = dataSnapshot.getValue(CulturalData.class);

				infoData = cultural.content;

				while(infotextview == null);
				infotextview.setText(cultural.content);

				QuizProblemActivity.probList.clear();
				QuizProblemActivity.probList.add(new ProblemData(cultural.pro1, cultural.ans1));
				QuizProblemActivity.probList.add(new ProblemData(cultural.pro2, cultural.ans2));
				QuizProblemActivity.probList.add(new ProblemData(cultural.pro3, cultural.ans3));
                Log.d(TAG, "Value is: " + cultural);
				Log.d(TAG, "probList: " + QuizProblemActivity.probList);
                // TODO
            }

            @Override
            public void onCancelled(DatabaseError e) {
                Log.w(TAG, "Failed to read value.", e.toException());
                // TODO: 네트워크가 불안정하여 퀴즈진행이 불가능합니다.
            }
        });

    }

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		switch (requestCode)
		{
			case GPS_PERMISSION_REQUEST:
			{
				if (grantResults.length > 0)
				{
					for (int i = 0; i < grantResults.length; i++)
					{
						if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
						{
							// granted
						}
						else
						{
							// permission denied
						}
					}
				}
				break;
			}
		} // switch()
	} // onRequestPermissionsResult()

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if (id == R.id.action_search)
			return true;

		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.quiz_start:
                Intent intent = new Intent(TourRegionActivity.this, QuizProblemActivity.class);
				// TODO:
                startActivity(intent);
                break;
            case R.id.flipper_pre:
                flipper.showPrevious();
                break;
            case R.id.flipper_next:
                flipper.showNext();
                break;
        }
    }

	public static class PlaceholderFragment extends Fragment implements MapView.OpenAPIKeyAuthenticationResultListener
	{
		private static final String ARG_SECTION_NUMBER = "section_number";

		public static PlaceholderFragment newInstance(int sectionNumber)
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			Activity activity = getActivity();
			int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
			View rootView = null;
			switch (sectionNumber)
			{
				case INFO_TAB:
					rootView = inflater.inflate(R.layout.fragment_region_info, container, false);
					flipper = rootView.findViewById(R.id.ViewFlipperID);
					toggleFlipping = rootView.findViewById(R.id.toggle_auto);
					infotextview = (TextView) rootView.findViewById(R.id.infotext);

					break;
				case ROAD_TAB:
					//////////////////////////////////////////////////////////////////////
					//					Road Tab with Daum Map API begin				//
					//////////////////////////////////////////////////////////////////////
					rootView = inflater.inflate(R.layout.fragment_region_road, container, false);

					MapView mapView = new MapView(activity);
					mapView.setOpenAPIKeyAuthenticationResultListener(this);

					Location region = new Location("region");
					region.setLatitude(37.581812);
					region.setLongitude(126.992723);
					Location myLoc = new Location("myLoc");
					myLoc.setLatitude(37.586577);
					myLoc.setLongitude(126.989258);
					Log.d(TAG, "distance: " + myLoc.distanceTo(region)); // 611.0321

					MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(region.getLatitude(), region.getLongitude());
					mapView.setMapCenterPointAndZoomLevel(mapPoint, 2, true);
					MapPOIItem marker = new MapPOIItem();
					marker.setItemName("Default Marker");
					marker.setTag(0);
					marker.setMapPoint(mapPoint);
					marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
					marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

					mapView.addPOIItem(marker);

					ViewGroup mapViewContainer = rootView.findViewById(R.id.map_view);
					mapViewContainer.addView(mapView);
					break;
					//////////////////////////////////////////////////////////////////////
					//					Road Tab with Daum Map API end					//
					//////////////////////////////////////////////////////////////////////

				case QUIZ_START_TAB:
					rootView = inflater.inflate(R.layout.fragment_region_quiz_start, container, false);
					quizStart = rootView.findViewById(R.id.quiz_start);
					int size = QuizProblemActivity.probList.size();
					if(size == 0)
						quizStart.setEnabled(false);
					else
						quizStart.setEnabled(true);

					final TextView logView = rootView.findViewById(R.id.my_stamp_desc);
					logView.setText("GPS 가 잡혀야 좌표가 구해짐");

					// Acquire a reference to the system Location Manager
					LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

					// GPS 프로바이더 사용가능여부
					boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
					// 네트워크 프로바이더 사용가능여부
					boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

					Log.d(TAG, "isGPSEnabled=" + isGPSEnabled);
					Log.d(TAG, "isNetworkEnabled=" + isNetworkEnabled);

					LocationListener locationListener = new LocationListener()
					{
						public void onLocationChanged(Location location)
						{
							double lat = location.getLatitude();
							double lng = location.getLongitude();

							logView.setText("latitude: " + lat + ", longitude: " + lng);
						}
						public void onStatusChanged(String provider, int status, Bundle extras) { logView.setText("onStatusChanged"); }
						public void onProviderEnabled(String provider) { logView.setText("onProviderEnabled"); }
						public void onProviderDisabled(String provider) { logView.setText("onProviderDisabled"); }
					};

					if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
					{
						// Register the listener with the Location Manager to receive location updates
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
						locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

						// 수동으로 위치 구하기
						String locationProvider = LocationManager.GPS_PROVIDER;
						Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
						if (lastKnownLocation != null)
						{
							double lng = lastKnownLocation.getLatitude();
							double lat = lastKnownLocation.getLatitude();
							Log.d(TAG, "longtitude=" + lng + ", latitude=" + lat);
						}
					}
					break;
			}
			return rootView;
		} // onCreateView()

		@Override
		public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s) { Log.d(TAG, "Daum Map API Auth: " + s); }
	}

	public class SectionsPagerAdapter extends FragmentStatePagerAdapter
	{
		public SectionsPagerAdapter(FragmentManager fm) { super(fm); }

		// getItem() -> newInstance() -> onCreateView()
		@Override
		public Fragment getItem(int position)
		{
			Log.d(TAG, "getItem() " + (position + 1));
			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() { return 3; }

		@Override
		public CharSequence getPageTitle(int position)
		{
			switch (position + 1)
			{
				case INFO_TAB: return "유적지 설명";
				case ROAD_TAB: return "가는 길";
				case QUIZ_START_TAB: return "문제풀이 & 스탬프 얻기";
				default: return null;
			}
		}
	}
}
