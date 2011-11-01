package com.glavsoft.viewer;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Rectangle implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7162999316728239767L;
	public float x;
	public float y;
	public float width;
	public float height;
	public Action action;
	
	public Rectangle(float x, float y, float w, float h, Action a){
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		this.action = a;
	}
	
	public Rectangle() {
		// TODO Auto-generated constructor stub
	}

	public boolean isIn(float pointX, float pointY){
		return ((pointX >= x && pointX <= x + width)
				&&(pointY >= y && pointY <= y + height)); 
	}
	
	public String toString(){
		return "x: "+x+" y: "+y;		
	}
	
	public JSONObject toJSONObject(String project, Map<String,String> extensionMap){
		JSONObject json = new JSONObject();
		try {
			json.put("x", x);
			json.put("y", y);
			json.put("width", width);
			json.put("height", height);
			if(action != null){
				json.put("action", action.toSmallJSON(project, extensionMap));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	public String toJSON(String project, Map<String,String> extensionMap){
		JSONObject json = new JSONObject();
		try {
			json.put("x", x);
			json.put("y", y);
			json.put("width", width);
			json.put("height", height);
			if(action != null){
				json.put("action", action.toSmallJSON(project, extensionMap));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
}
