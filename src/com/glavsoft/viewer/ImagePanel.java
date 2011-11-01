package com.glavsoft.viewer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImagePanel extends JPanel implements MouseListener, MouseMotionListener{

    private BufferedImage image;
    public ViewerFrame caller;
    private int imagewidth;
    private int imageheight;
    public Action a;
    public Rectangle pressed;
    private boolean newbutton = false;
    private float scalex;
    private float scaley;
    private float diffx;
    private float diffy;

    public ImagePanel(String file, Action a) {
       try {                
    	   image = ImageIO.read(new File(file));
    	   addMouseListener(this);
    	   addMouseMotionListener(this);
       } catch (IOException ex) {
    	   // handle exception...
       }
    }
    
    public ImagePanel(){
    	addMouseListener(this);
 	   	addMouseMotionListener(this);
    }

    public void setImage(String file){
    	try {
    		image = ImageIO.read(new File(file));
    		imagewidth = image.getWidth();
    		imageheight = image.getHeight();
    		if(imagewidth > this.getWidth() || imageheight > this.getHeight()){
    			if(imageheight >= imagewidth){
    				imagewidth = (image.getWidth() * this.getHeight())/image.getHeight();
    				imageheight = this.getHeight();
    			}else{
    				imageheight = (this.getWidth() * image.getHeight())/image.getWidth();
    				imagewidth = this.getWidth();
    			}
    		}
    		scalex = 1;
    		scaley = 1;
			if(imagewidth < image.getWidth()){
	    		scalex = (float)image.getWidth()/imagewidth;
			}
			if(imageheight < image.getHeight()){
				scaley = (float)image.getHeight()/imageheight;	
			}    		
    		this.repaint();
        } catch (IOException ex) {
        	// handle exception...
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
    	Graphics2D g2 = (Graphics2D)g;
    	super.paintComponent(g);
    	if(image != null){    		
    		g.drawImage(image, 0, 0, imagewidth, imageheight, null);
    	}
    	float buttonx;
    	float buttony;
    	float buttonwidth;
    	float buttonheight;
    	if(a != null){
	    	for(Rectangle r : a.buttons){
	    		if(r != null){
		    		buttonx = r.x;
		    		buttony = r.y;
		    		buttonwidth = r.width;
		    		buttonheight = r.height;
		    		if(imageheight != image.getHeight() || imagewidth != image.getWidth()){
		    			 if(imageheight < image.getHeight()){
		    				 buttony = buttony/scaley;
		    				 buttonheight = buttonheight/scaley;
		    			 }
		    			 if(imagewidth < image.getWidth()){
		    				 buttonx = buttonx/scalex;
		    				 buttonwidth = buttonwidth/scalex;
		    			 }
		    		}
		    		g2.draw(new Rectangle2D.Float(buttonx, buttony, buttonwidth, buttonheight));
		    		if(r.equals(pressed)){
		    			g2.draw(new Rectangle2D.Float(buttonx - 1, buttony - 1 , 2, 2));
		    			g2.draw(new Rectangle2D.Float(buttonx - 1, buttony + buttonheight - 1 , 2, 2));
		    			g2.draw(new Rectangle2D.Float(buttonx + buttonwidth - 1, buttony - 1 , 2, 2));
		    			g2.draw(new Rectangle2D.Float(buttonx + buttonwidth - 1, buttony + buttonheight - 1 , 2, 2));
		    		}
	    		}
	    	}
    	}
    }

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {		
		float x = arg0.getX();
		float y = arg0.getY();
		if(x >= 0 && x <= imagewidth && y >= 0 && y <= imageheight){
			if(a != null && a.buttons != null){
				pressed = a.buttons.isIn(x*scalex, y*scaley);
				if(pressed == null){					
					pressed = new Rectangle(x*scalex,y*scaley,scalex,scaley, null);
					a.buttons.add(pressed);
					newbutton = true;
				}else{					
					newbutton = false;					
					diffx = x*scalex - pressed.x;
					diffy = y*scaley - pressed.y;
					this.caller.comboBox_2.setSelectedItem(pressed.action);
				}				
			}
		}	
		this.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		float x = arg0.getX();
		float y = arg0.getY();
		if(pressed != null){
			if(x >= 0 && x <= imagewidth && y >= 0 && y <= imageheight){
				if(newbutton){
					pressed.width = Math.abs(pressed.x/scalex - x)*scalex;
					pressed.height = Math.abs(pressed.y/scaley - y)*scaley;
					a.buttons.remove(pressed);
					a.buttons.add(pressed);
				}else{
					pressed.x = x*scalex - diffx;
					pressed.y = y*scaley - diffy;
					a.buttons.remove(pressed);
					a.buttons.add(pressed);
				}
			}
		}
		this.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}