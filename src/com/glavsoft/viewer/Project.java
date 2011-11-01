package com.glavsoft.viewer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Project implements Serializable{
	private static final long serialVersionUID = -8621663640029739048L;
	public String name = "default";
	public String[] origin_files = {};
	public Map<String,Action> actionMap = new HashMap<String, Action>();	
	public Vector<Action> actions = new Vector<Action>();
	
	
	public String toString(){
		return this.name;
	}
}
