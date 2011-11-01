package com.glavsoft.viewer;

import java.util.ArrayList;

public class OzArrayList extends ArrayList<Rectangle>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4349817541156865714L;
	
	public Rectangle isIn(float x, float y){		
		for(Rectangle b : this){
			if(b != null){
				if(b.isIn(x, y)){
					return b;
				}
			}
		}
		return null;
	}
	
}
