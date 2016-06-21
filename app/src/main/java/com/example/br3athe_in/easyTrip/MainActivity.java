package com.example.br3athe_in.easyTrip;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.br3athe_in.easyTrip.Util.DBAssistant;
import com.example.br3athe_in.easyTrip.Util.IntentionExtraKeys;
import com.example.br3athe_in.easyTrip.Util.Travel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IntentionExtraKeys {

	private static final String LOG_TAG = "Custom";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);

		loadViews();
	}

	private void loadViews() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Easy 2 trip, hard 2 debug, mate.", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case INTENTION_CREATE_TRAVEL:
				Toast.makeText(this, "Yay! You have created a travel!", Toast.LENGTH_SHORT).show();
				break;
		}
	}

	public void openCreateTravelMenu(View view) {
		Intent intent = new Intent(this, CitySelector.class);
		startActivityForResult(intent, INTENTION_CREATE_TRAVEL);
	}

	public void lookup(View view) {
		ArrayList<Travel> travels = new DBAssistant(this).readTravels();
		try {
			String firstname = travels.get(0).title;
			Log.d(LOG_TAG, "Wow, here it is: " + firstname);
		} catch (NullPointerException e) {
			Log.d(LOG_TAG, "DB seems to be quite empty. Duplicate.");
		}
	}

	public void followMe(View view) {
		new AlertDialog.Builder(this)
				.setMessage("This stuff nukes DB. Nuke DB?")
				.setPositiveButton(
						"Yeah, teach that motherf*cker a lesson",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								new DBAssistant(MainActivity.this).nukeDB(true);
							}
						}
				)
				.setNegativeButton(
						"Nope, I have changed my mind",
						null
				)
				.show();
	}
}
