package com.example.br3athe_in.easyTrip;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.br3athe_in.easyTrip.Util.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class CitySelector extends AppCompatActivity implements IntentionExtraKeys {

	private static final String LOG_TAG = "Custom";
	private static final int ASK_FOR_CITY = 1337;
	private static final String FIELD_1 = "TITLE";
	private static final String FIELD_2 = "COORDINATES";
	private static final int ACTION_SHOW_MAP = 1;
	private static final int ACTION_REQUEST_INFO = 2;
	private static final int ACTION_EDIT_NOTES = 3;
	private static final int ACTION_REMOVE_CITY = 4;

	private ListView lv;

	private ArrayList<HashMap<String, String>> citiesSelected =
			new ArrayList<HashMap<String, String>>() {
				@Override
				public void clear() {
					super.clear();

					incomingCity = new HashMap<>();
					incomingCity.put(FIELD_1, getResources().getString(R.string.cs_last_list_item_1));
					incomingCity.put(FIELD_2, getResources().getString(R.string.cs_last_list_item_2));

					citiesSelected.add(incomingCity);
				}
	};

	private HashMap<String, String> incomingCity = new HashMap<>();

	private ArrayList<City> citiesToVisit = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city_selector);
		loadViews();
		lv.setChoiceMode(ListView.CHOICE_MODE_NONE);

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
				if(pos == lv.getCount() - 1) {
					askForAddingCity();
				}
				// TODO: 29.05.2016 change to context menu, then implement getInfo(city)
				// TODO: 17.06.2016 wut? :D :D :D
				getInfo(v);
			}
		});

		citiesSelected.clear();

		SimpleAdapter adapter = new SimpleAdapter(this, citiesSelected,
				android.R.layout.simple_list_item_2,
				new String[] {FIELD_1, FIELD_2},
				new int[] {android.R.id.text1, android.R.id.text2}
		);

		lv.setAdapter(adapter);
		registerForContextMenu(lv);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// TODO: 17.06.2016 should have different behavior on a last item: no-op.
		// if(true) does not handle this, obviously.
		super.onCreateContextMenu(menu, v, menuInfo);
		// TODO! Change to extracted city's name.
		menu.setHeaderTitle("Мадрид");

		menu.add(Menu.NONE, ACTION_SHOW_MAP, 1, R.string.slctr_cntxt_show_map);
		menu.add(Menu.NONE, ACTION_REQUEST_INFO, 2, R.string.slctr_cntxt_request_info);
		menu.add(Menu.NONE, ACTION_EDIT_NOTES, 3, R.string.slctr_cntxt_edit_notes);
		menu.add(Menu.NONE, ACTION_REMOVE_CITY, 4, R.string.slctr_cntxt_remove_city);
		// }
		// ...
		// ah, sorry, I forgot there is no more dumb if(true) there.
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO: put some actions, bruh!
		AdapterView.AdapterContextMenuInfo menuInfo =
				(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
			case ACTION_SHOW_MAP:
				break;
			case ACTION_REQUEST_INFO:
				break;
			case ACTION_EDIT_NOTES:
				break;
			case ACTION_REMOVE_CITY:
				citiesSelected.remove(menuInfo.position);
				citiesToVisit.remove(menuInfo.position);
				reloadAdapter(citiesSelected);
				break;
		}
		return super.onContextItemSelected(item);
	}

	private void loadViews() {
		lv = (ListView) findViewById(R.id.lwCitiesSelected);
	}

	private void askForAddingCity() {
		Intent intent = new Intent(this, CityPicker.class);
		intent.putExtra(EXTRA_CITIES_TO_VISIT, citiesToVisit);
		startActivityForResult(intent, ASK_FOR_CITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ASK_FOR_CITY:
				if(resultCode == RESULT_CANCELED) { return; }

				citiesToVisit = (ArrayList<City>) data.getExtras().get(EXTRA_CITIES_TO_VISIT);
				reloadContent(citiesToVisit);
				break;
		}
	}

	private void reloadContent(ArrayList<City> newContent) {
		citiesSelected.clear();
		// what, we're coding in Whitespace now?



		for(City m : newContent) {
			incomingCity = new HashMap<>();
			String nullableCityName = m.getCityName();
			if (nullableCityName == null) {
				nullableCityName = getString(R.string.placeholder_city_unknown);
			}
			String nullableCountryName = m.getCountryName();
			if (nullableCountryName == null) {
				nullableCityName = getString(R.string.placeholder_country_unknown);
			}

			incomingCity.put(FIELD_1, nullableCityName + ", " + nullableCountryName);
			incomingCity.put(FIELD_2,
					String.format(Locale.US, getString(R.string.slctr_cntxt_coords_template),
							m.getPosition().latitude, m.getPosition().longitude));
			citiesSelected.add(citiesSelected.size() - 1, incomingCity);
		}
		reloadAdapter(citiesSelected);
	}

	/** Sets <code>lv's</code> content to current <code>citiesSelected's</code>.
	 * @param to Specifies the incoming content to set to. Just in case.
	 *           May be useful after ABC optimizing.
	 */
	private void reloadAdapter(ArrayList<HashMap<String, String>> to) {
		SimpleAdapter newAdapter = new SimpleAdapter(this, to,
				android.R.layout.simple_list_item_2,
				new String[] {FIELD_1, FIELD_2},
				new int[] {android.R.id.text1, android.R.id.text2}
		);
		newAdapter.getItem(newAdapter.getCount() - 1);
		lv.setAdapter(newAdapter);
	}

	public void getCont(View view) {
		Toast.makeText(
				this, "This should optimize your input. But it's not time yet!", Toast.LENGTH_LONG)
				.show();
	}

	private void getInfo(View view) {
	}
}
