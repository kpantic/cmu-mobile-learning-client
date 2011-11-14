package com.glavsoft.viewer;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Action implements Serializable {

	private static final long serialVersionUID = -667831962619771227L;
	String name;	
	
	public Action(String file) {
		name = file;
	}

	public Action() {
		// TODO Auto-generated constructor stub
	}

	public String toString(){
		return name;		
	}
	
	public String toJSON(String phase){
		JSONObject json = new JSONObject();
		try {
			json.put("action_name", name);
			json.put("phase_name", phase);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();		
	}
}