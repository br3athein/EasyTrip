package com.example.br3athe_in.easyTrip.Util;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.Language;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

// TODO: 18.06.2016 should I really explain this one?
public class Travel {
	ArrayList<City> citiesToVisit;

	Travel(ArrayList<City> toFill) {
		citiesToVisit.addAll(toFill);
	}

	public void optimize(String gMapsKey) {
		//Abc.optimize();
		City first = citiesToVisit.get(0);
		City last = citiesToVisit.get(citiesToVisit.size() - 1);

		LatLng f = new LatLng(first.getPosition().latitude, first.getPosition().longitude);
		LatLng l = new LatLng(last.getPosition().latitude, last.getPosition().longitude);


		//GoogleDirection
		//		.withServerKey(gMapsKey)
		//		.from(f)
		//		.to(l)
		//		.transportMode(TransportMode.DRIVING)
		//		.language(Language.RUSSIAN)
		//		.unit(Unit.METRIC)
		//		.execute(new DirectionCallback() {
		//			@Override
		//			public void onDirectionSuccess(Direction direction, String rawBody) {

		//			}

		//			@Override
		//			public void onDirectionFailure(Throwable t) {

		//			}
		//		});
	}
	//public static String excutePost(String targetURL, String urlParameters) {
	//	HttpURLConnection connection = null;
	//	try {
	//		//Create connection
	//		URL url = new URL(targetURL);
	//		connection = (HttpURLConnection)url.openConnection();
	//		connection.setRequestMethod("POST");
	//		connection.setRequestProperty("Content-Type",
	//				"application/x-www-form-urlencoded");
	//
	//		connection.setRequestProperty("Content-Length",
	//				Integer.toString(urlParameters.getBytes().length));
	//		connection.setRequestProperty("Content-Language", "en-US");
	//
	//		connection.setUseCaches(false);
	//		connection.setDoOutput(true);
	//
	//		//Send request
	//		DataOutputStream wr = new DataOutputStream (
	//				connection.getOutputStream());
	//		wr.writeBytes(urlParameters);
	//		wr.close();
	//
	//		//Get Response
	//		InputStream is = connection.getInputStream();
	//		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	//		StringBuilder response = new StringBuilder();
	//		String line;
	//		while((line = rd.readLine()) != null) {
	//			response.append(line);
	//			response.append('\r');
	//		}
	//		rd.close();
	//		return response.toString();
	//	} catch (Exception e) {
	//		e.printStackTrace();
	//		return null;
	//	} finally {
	//		if(connection != null) {
	//			connection.disconnect();
	//		}
	//	}
	//}
}
