package com.glavsoft.viewer;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class Phase {
	ArrayList<Action> actions = new ArrayList<Action>();
	public String name;
	
	public Phase(String name){
		this.name = name;
	}
	
	public String toJSON(){
		JSONObject json = new JSONObject();
		try {
			json.put("phase_name", name);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();		
	}
}
