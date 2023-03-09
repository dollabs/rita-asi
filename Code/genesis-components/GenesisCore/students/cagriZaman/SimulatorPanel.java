package cagriZaman;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import cagriZaman.RobotInterface;
import utils.Mark;

    
 public class SimulatorPanel extends JPanel{

        /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		private BufferedImage image;
        public boolean isAlive=false;
        public SimulatorPanel() {       
        	  Timer t = new Timer(20, new ActionListener() {
  				
  				@Override
  				public void actionPerformed(ActionEvent e) {
  					repaint();
  					
  				}
  			});
        	  
        	  JButton b=new JButton("Connect to Simulator");  
        	  b.setBounds(0,0,95,30);  
        	  
        	  b.addActionListener(new ActionListener(){  
        	  public void actionPerformed(ActionEvent e){  
        	            isAlive=!isAlive;
        	            if(isAlive){
        	            	t.start();
        	            	b.setText("Disconnect");
        	            	}
        	            else{       	         	
        	            	t.stop();
                            RobotInterface.disconnect();
                            b.setText("Connect to Simulator");

        	            }
        	        }  
        	    });  
        	   
        	  
        	   JButton b2 = new JButton("Reset");
        	   b2.setBounds(100,0,95,30);
        	   b2.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					RobotInterface.restartSimulation();
				}
			});
        	   
        	   this.add(b);this.add(b2);
        	   SimulatorPanel me = this;
        	   //Gracefully disconnect from Simulator when Genesis is closed.
        	   this.addHierarchyListener(new HierarchyListener() {
                   @Override
                   public void hierarchyChanged(HierarchyEvent e) {
                       if((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == HierarchyEvent.SHOWING_CHANGED){
                           SwingUtilities.getWindowAncestor(me).addWindowListener(new WindowAdapter(){
                               public void windowClosing(WindowEvent e){
                                   RobotInterface.disconnect();
                                   Mark.say("Fancy Simulator Disconnected...");
                               }
                           });
                       }
                   }
               });

        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(isAlive){
            	image = RobotInterface.getImage();
            	g.drawImage(image, 0, 0, this); //            
            }
            }
        

    }

