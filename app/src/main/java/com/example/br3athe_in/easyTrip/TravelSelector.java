package com.example.br3athe_in.easyTrip;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.br3athe_in.easyTrip.Util.ActionKeys;
import com.example.br3athe_in.easyTrip.Util.DBAssistant;
import com.example.br3athe_in.easyTrip.Util.DBKeys;
import com.example.br3athe_in.easyTrip.Util.IntentionExtraKeys;
import com.example.br3athe_in.easyTrip.Util.Travel;

import java.util.ArrayList;
import java.util.HashMap;

public class TravelSelector extends AppCompatActivity implements DBKeys, ActionKeys, IntentionExtraKeys {
	private static final String LOG_TAG = "Custom";
	private ListView lv;

	private ArrayList<HashMap<String, String>> syncFillContent = new ArrayList<>();
	private ArrayList<Travel> syncTravels = new ArrayList<>();
	private ArrayList<Integer> syncTravelIds = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_travel_selector);

		loadViews();
		prepareActivity();
	}

	private void prepareActivity() {
		initializeFields();
		updateActivityTitle();
		reloadContent(syncFillContent);
	}

	private void loadViews() {
		lv = (ListView) findViewById(R.id.lvAvailableTravels);
	}

	private void initializeFields() {
		syncTravels = new DBAssistant(this).readTravels(syncTravelIds);
		syncFillContent = new DBAssistant(this).extractFillContent(syncTravels);
	}

	private void reloadContent(ArrayList<HashMap<String, String>> to) {
		SimpleAdapter adapter = new SimpleAdapter(this, to,
				android.R.layout.simple_list_item_2,
				new String[]{KEY_NAME, KEY_VAL},
				new int[]{android.R.id.text1, android.R.id.text2}
		);

		lv.setAdapter(adapter);
		registerForContextMenu(lv);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.setHeaderTitle(R.string.tslctr_context_title);
		menu.add(Menu.NONE, ACTION_QUERY_TRAVEL, 1, R.string.tslctr_query_travel);
		menu.add(Menu.NONE, ACTION_REDRAW_ROUTE, 2, R.string.tslctr_redraw_route);
		menu.add(Menu.NONE, ACTION_REMOVE_TRAVEL, 3, R.string.tslctr_remove_travel);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int selectedPosition = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;

		switch (item.getItemId()) {
			case ACTION_QUERY_TRAVEL:
				queryTravel(selectedPosition);
				break;
			case ACTION_REDRAW_ROUTE:
				redrawRoute(syncTravels.get(selectedPosition));
				break;
			case ACTION_REMOVE_TRAVEL:
				removeTravel(selectedPosition);
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case INTENTION_CREATE_TRAVEL:
				prepareActivity();
				break;
		}
	}

	private void queryTravel(int selectedPosition) {
		Log.d(LOG_TAG, "NO WARNINGS BRO THE CODE IS GOD DONUT CLEAR " +
				"btw, selectedPosition = " + selectedPosition);
	}

	private void removeTravel(final int selectedPosition) {
		new AlertDialog.Builder(this)
				.setMessage("Вы действительно хотите стереть путешествие?")
				.setNegativeButton(
						R.string.prompt_negative,
						null
				)
				.setPositiveButton(
						R.string.prompt_positive,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								DBAssistant assistant = new DBAssistant(TravelSelector.this);
								assistant.remove(syncTravelIds.get(selectedPosition));

								syncTravels.remove(selectedPosition);
								syncFillContent.remove(selectedPosition);
								syncTravelIds.remove(selectedPosition);
								updateActivityTitle();
								reloadContent(syncFillContent);
							}
						}
				)
				.show();
	}

	private void redrawRoute(Travel travel) {
		Intent intent = new Intent(this, WorldMap.class);
		intent.putExtra(EXTRA_CITIES_TO_VISIT, travel.citiesToVisit);
		intent.putExtra(EXTRA_ENCODED_POLYLINE, travel.detailedRouteLegs);
		intent.putExtra(EXTRA_SCENARIO, SCENARIO_DRAW_OPTIMAL_ROUTE);

		startActivity(intent);
	}

	private void updateActivityTitle() {
		TextView title = (TextView) findViewById(R.id.tvTitle);
		if (syncFillContent.isEmpty()) {
			title.setText(R.string.tslctr_empty_list_stub);
			title.setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(TravelSelector.this, CitySelector.class);
							startActivityForResult(intent, INTENTION_CREATE_TRAVEL);
							Log.d(LOG_TAG, "CreateTravelMenu has started from TravelSelector");
						}
					}
			);
		} else {
			title.setText(R.string.tslctr_hints);
			title.setOnClickListener(null);
		}
	}

	public void back(View view) {
		finish();
	}
}
