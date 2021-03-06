package com.example.br3athe_in.easyTrip.Util;

public interface IntentionExtraKeys {
	String EXTRA_CITIES_TO_VISIT = "com.example.br3athe_in.easyTrip.EXTRA_CITIES_TO_VISIT";
	String EXTRA_CITY_TO_FOCUS = "com.example.br3athe_in.easyTrip.EXTRA_CITY_TO_FOCUS";
	String EXTRA_SCENARIO = "com.example.br3athe_in.easyTrip.EXTRA_SCENARIO";
	String EXTRA_ENCODED_POLYLINE = "com.example.br3athe_in.easyTrip.EXTRA_ENCODED_POLYLINE";

	int SCENARIO_PICK_CITIES = 0xD0CF11E0;
	int SCENARIO_DRAW_PLAIN_ROUTE = 0xDeadBeef;
	int SCENARIO_DRAW_OPTIMAL_ROUTE = 0xCafeBabe;

	int INTENTION_CREATE_TRAVEL = 0x00005551;
	int INTENTION_ASK_FOR_CITY = 0x00005552;
}
