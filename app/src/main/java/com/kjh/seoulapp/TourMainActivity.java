package com.kjh.seoulapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kjh.seoulapp.data.CulturalData;
import com.kjh.seoulapp.data.ProblemData;

import static com.kjh.seoulapp.data.SharedData.CULTURAL_REF;
import static com.kjh.seoulapp.data.SharedData.POPUP_TYPE;
import static com.kjh.seoulapp.data.SharedData.probList;
import static com.kjh.seoulapp.data.SharedData.regionIndex;

public class TourMainActivity extends GoogleApiClientActivity
		implements NavigationView.OnNavigationItemSelectedListener
{
	final String TAG = "TourMainActivity";
	FirebaseAuth auth;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tour_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		auth = FirebaseAuth.getInstance();
		regionIndex = -1;
		progressBar = (ProgressBar) findViewById(R.id.progressBarMain);
		hideProgressDialog();

		FirebaseUser user = auth.getCurrentUser();
		if (user != null)
		{
			View nav_header = navigationView.getHeaderView(0);
			ImageView user_photo = nav_header.findViewById(R.id.user_photo);
			TextView user_name = nav_header.findViewById(R.id.user_name);
			TextView user_email = nav_header.findViewById(R.id.user_email);

			String strName = user.getDisplayName();
			String strEmail = user.getEmail();

//            if (user_photo != null) {
//                Glide
//                        .with(nav_header.getContext())
//                        .load(currentUser.getPhotoUrl()) // the uri you got from Firebase
//                        .override(200,200)
//                        .into(user_photo); // Your imageView variable
//            }
			if (user_name  != null && strName  != null) user_name.setText(strName);
			if (user_email != null && strEmail != null) user_email.setText(strEmail);
		}
	} // onCreate()

	@Override
	public void onStart()
	{
		android.util.Log.d(TAG,"TOTAL MEMORY : "+(Runtime.getRuntime().totalMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d(TAG,"MAX MEMORY : "+(Runtime.getRuntime().maxMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d(TAG,"FREE MEMORY : "+(Runtime.getRuntime().freeMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d(TAG,"ALLOCATION MEMORY : "+((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)) + "MB");

		super.onStart();
	}

	@Override // button event: open drawer
	public void onBackPressed()
	{
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START))
		{
			drawer.closeDrawer(GravityCompat.START);
		} else
		{
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_search, menu);

		return initSearch(menu);
	}

	public static boolean initSearch(Menu menu)
	{
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String s)
			{
				return false;
			}

			@Override
			public boolean onQueryTextChange(String s)
			{
				System.out.println(s);
				return false;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_search)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item)
	{
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_sign_out) signOut();
		else if (id == R.id.nav_app_info) popupActivity(POPUP_TYPE.APP_INFO);
		else if (id == R.id.nav_contact) popupActivity(POPUP_TYPE.CONTACT);
		else if (id == R.id.nav_donate) popupActivity(POPUP_TYPE.DONATE);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);

		return true;
	} // onNavigationItemSelected()

	public void onClick(View v)
	{
		int id = v.getId();

		ImageView map_full = (ImageView) findViewById(R.id.map_full);
		ImageView map_1 = (ImageView) findViewById(R.id.map_1);
		ImageView map_2 = (ImageView) findViewById(R.id.map_2);
		Button map_mid_button = (Button) findViewById(R.id.map_mid_button);
		Button map_east_button = (Button) findViewById(R.id.map_east_button);
		ImageButton icon_indepen = (ImageButton) findViewById(R.id.icon_indepen);
		switch (id)
		{
			case R.id.btn_next:
				regionIndex = 1;
				showProgressDialog();
				startRegionActivity();
				break;
			case R.id.btn_test:
				break;
			case R.id.icon_indepen:
				regionIndex = 3;
				showProgressDialog();
				startRegionActivity();
				break;
			case R.id.map_mid_button:
				map_full.setVisibility(View.GONE);
				map_mid_button.setVisibility(View.GONE);
				map_east_button.setVisibility(View.GONE);
				map_1.setVisibility(View.VISIBLE);
				icon_indepen.setVisibility(View.VISIBLE);
				break;
			case R.id.map_east_button:
				map_full.setVisibility(View.GONE);
				map_mid_button.setVisibility(View.GONE);
				map_east_button.setVisibility(View.GONE);
				map_2.setVisibility(View.VISIBLE);
				break;
			case R.id.cam_test:
				Intent testIntent = new Intent(TourMainActivity.this, ARActivity.class);
				startActivity(testIntent);
				break;
		}
	} // onClick()

	private void signOut()
	{
		// Firebase sign out
		auth.signOut();

		// Google sign out
		Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>()
		{
			@Override
			public void onResult(@NonNull Status status)
			{
				Intent intent = new Intent(TourMainActivity.this, SocialLoginActivity.class);
				startActivity(intent);
				finish();
			}
		});
	} // signOut()

	void popupActivity(POPUP_TYPE type)
	{
		Intent intent = new Intent(TourMainActivity.this, PopupActivity.class);
		intent.putExtra("POPUP_TYPE", type);
		startActivity(intent);
	}

	void startRegionActivity()
	{
		if (regionIndex == -1)
			Log.d(TAG, "regionIndex is not initialized");

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference(CULTURAL_REF).child(""+regionIndex);
		Log.v(TAG, ref.toString());
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				CulturalData cultural = dataSnapshot.getValue(CulturalData.class);
				Log.d(TAG, "Value is: " + cultural);

				probList.clear();
				probList.add(new ProblemData(cultural.pro1, cultural.ans1));
				probList.add(new ProblemData(cultural.pro2, cultural.ans2));
				probList.add(new ProblemData(cultural.pro3, cultural.ans3));
				Log.d(TAG, "probList: " + probList);

				TourRegionActivity.cultural = cultural;
				hideProgressDialog();

				Intent intent = new Intent(TourMainActivity.this, TourRegionActivity.class);
				startActivity(intent);
			}

			@Override
			public void onCancelled(DatabaseError e) {
				Log.w(TAG, "Failed to read value.", e.toException());
				// TODO: 네트워크가 불안정하여 퀴즈진행이 불가능합니다.
			}
		});
	} // startRegionActivity()
} // class
