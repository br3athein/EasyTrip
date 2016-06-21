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
import com.example.br3athe_in.easyTrip.Util.IntentionExtraKeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class CitySelector extends AppCompatActivity implements IntentionExtraKeys {
	private static final String LOG_TAG = "Custom";
	private static final String LIST_ITEM_TITLE = "TITLE";
	private static final String LIST_ITEM_DETAILS = "COORDINATES";
	private static final int ACTION_SHOW_ON_MAP = 1;
	private static final int ACTION_REQUEST_INFO = 2;
	private static final int ACTION_SHOW_NOTES = 3;
	private static final int ACTION_REMOVE_CITY = 4;

	private ListView lv;

	private HashMap<String, String> incomingCity = new HashMap<>();

	/** <code>{@link City}</code> list, main information source */
	private ArrayList<City> ctCitiesToVisit = new ArrayList<>();
	/** Another city list, just to fill the <code>{@link ListView}</code> */
	private ArrayList<HashMap<String, String>> lvFillContent =
			new ArrayList<HashMap<String, String>>() {
				@Override
				public void clear() {
					super.clear();

					incomingCity = new HashMap<>();
					incomingCity.put(LIST_ITEM_TITLE, getResources().getString(R.string.cs_add_city_top));
					incomingCity.put(LIST_ITEM_DETAILS, getResources().getString(R.string.cs_add_city_bot));

					lvFillContent.add(incomingCity);
				}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_city_selector);
		loadViews();
	}

	private void loadViews() {
		lv = (ListView) findViewById(R.id.lvCitiesSelected);

		lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
				if(pos == lv.getCount() - 1) {
					jumpToMap(ctCitiesToVisit.size() - 1);
				}
				getInfo(v);
			}
		});

		lvFillContent.clear();

		lv.setAdapter(
				new SimpleAdapter(this, lvFillContent,
						android.R.layout.simple_list_item_2,
						new String[] {LIST_ITEM_TITLE, LIST_ITEM_DETAILS},
						new int[] {android.R.id.text1, android.R.id.text2}
				)
		);

		registerForContextMenu(lv);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;

		// no-op on last item selection
		if (acmi.position == lv.getCount() - 1) {
			return;
		}

		menu.setHeaderTitle(
				((HashMap<String, String>) lv.getItemAtPosition(acmi.position)).get(LIST_ITEM_TITLE)
		);

		menu.add(Menu.NONE, ACTION_SHOW_ON_MAP, 1, R.string.slctr_cntxt_show_map);
		menu.add(Menu.NONE, ACTION_REQUEST_INFO, 2, R.string.slctr_cntxt_request_info);
		menu.add(Menu.NONE, ACTION_SHOW_NOTES, 3, R.string.slctr_cntxt_edit_notes);
		menu.add(Menu.NONE, ACTION_REMOVE_CITY, 4, R.string.slctr_cntxt_remove_city);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int selectedPosition = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;

		switch (item.getItemId()) {
			case ACTION_SHOW_ON_MAP:
				jumpToMap(selectedPosition);
				break;
			case ACTION_REQUEST_INFO:
				// noop? Awaiting implementation.
				break;
			case ACTION_SHOW_NOTES:
				ctCitiesToVisit.get(selectedPosition).showNotes(this);
				break;
			case ACTION_REMOVE_CITY:
				ctCitiesToVisit.remove(selectedPosition);
				lvFillContent.remove(selectedPosition);
				reloadAdapter(lvFillContent);
				break;
		}
		return super.onContextItemSelected(item);
	}

	/** Opens <code>{@link WorldMap}</code> with an actual content
	 *  and sets camera position to specified city.
	 *  @param focusToId id of the city that map should focus to.
	 */
	private void jumpToMap(int focusToId) {
		Intent intent = new Intent(this, WorldMap.class);
		intent.putExtra(EXTRA_CITIES_TO_VISIT, ctCitiesToVisit);
		intent.putExtra(EXTRA_CITY_TO_FOCUS, focusToId);
		intent.putExtra(EXTRA_SCENARIO, SCENARIO_PICK_CITIES);
		startActivityForResult(intent, INTENTION_ASK_FOR_CITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case INTENTION_ASK_FOR_CITY:
				if(resultCode == RESULT_CANCELED) { return; }

				ctCitiesToVisit = (ArrayList<City>) data.getExtras().get(EXTRA_CITIES_TO_VISIT);
				reloadContent(ctCitiesToVisit);
				break;
			case INTENTION_CREATE_TRAVEL:
				finish();
				break;
		}
	}

	private void reloadContent(ArrayList<City> newContent) {
		lvFillContent.clear();

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

			incomingCity.put(LIST_ITEM_TITLE, nullableCityName + ", " + nullableCountryName);
			incomingCity.put(LIST_ITEM_DETAILS,
					String.format(Locale.US, getString(R.string.slctr_cntxt_coords_template),
							m.getPosition().latitude, m.getPosition().longitude));
			lvFillContent.add(lvFillContent.size() - 1, incomingCity);
		}
		reloadAdapter(lvFillContent);
	}

	/** Sets <code>lv's</code> content to current <code>lvFillContent's</code>.
	 * @param to Specifies the incoming content to set to. Just in case.
	 *           May be useful after ABC optimizing.
	 */
	private void reloadAdapter(ArrayList<HashMap<String, String>> to) {
		SimpleAdapter newAdapter = new SimpleAdapter(this, to,
				android.R.layout.simple_list_item_2,
				new String[] {LIST_ITEM_TITLE, LIST_ITEM_DETAILS},
				new int[] {android.R.id.text1, android.R.id.text2}
		);
		newAdapter.getItem(newAdapter.getCount() - 1);
		lv.setAdapter(newAdapter);
	}

	public void createTravel(View view) {
		Intent intent = new Intent(this, CreateTravelActivity.class);
		intent.putExtra(EXTRA_CITIES_TO_VISIT, ctCitiesToVisit);
		startActivityForResult(intent, INTENTION_CREATE_TRAVEL);
	}

	/* no-op */
	private void getInfo(View view) {
	}
}
