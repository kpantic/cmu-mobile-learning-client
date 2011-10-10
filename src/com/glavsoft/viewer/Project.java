package com.glavsoft.viewer;

import java.io.Serializable;

public class Project implements Serializable{
	private static final long serialVersionUID = -8621663640029739048L;
	public String name = "default";
	public String[] origin_files = {};
	public String[] files = {};
	
	public String toString(){
		return this.name;
	}
}
