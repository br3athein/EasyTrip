package com.example.br3athe_in.easyTrip.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Travel implements Serializable {
	public static int totalTravels = 0;

	ArrayList<City> citiesToVisit;
	public String title;
	public int length;

	public Travel() {
		this("", new ArrayList<City>(), 0);
	}

	public Travel(Travel other) {
		this.citiesToVisit = new ArrayList<>();
		this.citiesToVisit.addAll(other.citiesToVisit);
		this.title = other.title;
		this.length = other.length;
	}

	public Travel(String travelTitle, ArrayList<City> toFill, int travelLength) {
		totalTravels++;
		citiesToVisit = new ArrayList<>();
		citiesToVisit.addAll(toFill);
		title = new String(travelTitle);
		length = travelLength;
	}

	public static Travel deserialize(byte[] serializedTravel) {
		ByteArrayInputStream bis = new ByteArrayInputStream(serializedTravel);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bis.close();
			} catch (IOException ignored) {
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ignored) {
			}
		}
		try {
			return (Travel) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] serialize() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(this);

		} catch (IOException e) {
			// ignore
		} finally {
			try {
				if(out != null) {
					out.close();
				}
			} catch (IOException e) {
				// gtfo, bloody IOException!
			}
			return bos.toByteArray();
		}
	}

	public void optimize(String gMapsKey) {
		//Abc.optimize();
	}
}
