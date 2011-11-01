package com.glavsoft.viewer;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Action implements Serializable {

	private static final long serialVersionUID = -667831962619771227L;
	String filename;
	public OzArrayList buttons = new OzArrayList();
	
	public Action(String file) {
		filename = file;
	}

	public Action() {
		// TODO Auto-generated constructor stub
	}

	public String toString(){
		return filename;		
	}
	
	public String toJSON(String project, Map<String,String> extensionMap){
		JSONObject json = new JSONObject();
		try {
			json.put("filename", project+"/"+filename);
			for(Rectangle b : buttons){
				if(b != null){
					json.append("buttons", b.toJSONObject(project, extensionMap));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int point = filename.lastIndexOf(".");
		String ext = filename.substring(point+1,filename.length()); 
		return extensionMap.get(ext)+"###"+json.toString();
	}
	
	public String toSmallJSON(String project, Map<String,String> extensionMap){
		JSONObject json = new JSONObject();
		try {
			json.put("filename", project+"/"+filename);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int point = filename.lastIndexOf(".");
		String ext = filename.substring(point+1,filename.length()); 
		return extensionMap.get(ext)+"###"+json.toString();
	}

}