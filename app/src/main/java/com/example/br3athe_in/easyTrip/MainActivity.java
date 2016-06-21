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
import android.widget.Toast;

import com.example.br3athe_in.easyTrip.Util.DBAssistant;
import com.example.br3athe_in.easyTrip.Util.IntentionExtraKeys;

public class MainActivity extends AppCompatActivity implements IntentionExtraKeys {

	private static final String LOG_TAG = "Custom";

	// Итого: слишком много лишней логики реализации не в тех Activity.
	// Рефакторить, рефакторить и ещё раз рефакторить. ©

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
		assert fab != null;
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
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case INTENTION_CREATE_TRAVEL:
				if (resultCode == RESULT_OK) {
					Toast.makeText(this, R.string.main_create_travel_success, Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}

	public void openCreateTravelMenu(View view) {
		Intent intent = new Intent(this, CitySelector.class);
		startActivityForResult(intent, INTENTION_CREATE_TRAVEL);
		Log.d(LOG_TAG, "CreateTravelMenu has started");
	}

	public void lookup(View view) {
		Intent intent = new Intent(this, TravelSelector.class);
		startActivity(intent);
		Log.d(LOG_TAG, "TravelSelector has started");
	}

	public void followMe(View view) {
		final DBAssistant dbKiller = new DBAssistant(this);
		new AlertDialog.Builder(this)
				.setMessage("This stuff nukes DB.\n" +
						"Nuke DB?")
				.setPositiveButton(
						"Yeah, teach that goddamn punk a lesson",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dbKiller.nukeDB(true);
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
