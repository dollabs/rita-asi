package kevinWhite;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JPanel;

import utils.Mark;

@SuppressWarnings("serial")
public class Arrow extends JPanel {
    private Polygon arrow;
    private boolean isDesirable = true;
    private Point center;
    
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        int width = this.getWidth();
        int height = this.getHeight();
        initArrow(width,height);
        Graphics2D reverse = (Graphics2D) g;
        AffineTransform original = reverse.getTransform();
        Mark.say("Displayable:",this.isVisible(),"Desirable:",isDesirable);
        if (this.isVisible()){
            if(!isDesirable){
                reverse.rotate(Math.toRadians(180), center.x, center.y);
                reverse.drawPolygon(arrow);
                reverse.fillPolygon(arrow);
            }
            else{
                g.drawPolygon(arrow);
                g.fillPolygon(arrow);
            }
        }
        reverse.setTransform(original);
    }
    
    /**
     * Draws an arrow of a fixed size
     * @param isDesired, a boolean that determines the direction in which the arrow points. It points
     * right if desired and left otherwise. By default it points to the right.
     */
    public Arrow(boolean isDesired) {
        this.setPreferredSize(new Dimension(200,200));
        this.setBackground(Color.WHITE);
        isDesirable = isDesired;
            center = new Point(40,40);
            ///System.out.println("The center must leave at least 40 pixels on all sides. Painting arrow around (40,40).");
            arrow = new Polygon(new int[] {0, 60, 60, 80, 60, 60, 0}, new int[]{30, 30, 0, 40, 80, 50, 50},7);
    }
    
    /**
     * Draws an arrow of a fixed size that by default points to the right. Direction can be changed with the changeDirection method.
     */
    public Arrow(){
        this.setBackground(Color.WHITE);
        center = new Point(40,40);
        //System.out.println("The center must leave at least 40 pixels on all sides. Painting arrow around (40,40).");
        arrow = new Polygon(new int[] {0, 60, 60, 80, 60, 60, 0}, new int[]{30, 30, 0, 40, 80, 50, 50},7);
    }
    
    /**
     * initializes the arrow to be based on the size of the panel it is in
     * @param width, the width of the panel
     * @param height, the height of the panel
     */
    private void initArrow(int width, int height){
        arrow = new Polygon(new int[] {width/8, width * 5/8, width * 5/8, width * 7/8, width*5/8, width *5/8, width/8}, 
                new int[] {height * 3/8, height * 3/8, height * 1/4, height * 1/2, height * 3/4, height * 5/8, height * 5/8}, 7);
        center = new Point(width/2,height/2);
    }
    
    /**
     * Changes the direction of the arrow, where true corresponds to right and fals corresponds to left
     * @param desire, the boolean that determines arrow direction
     */
    protected void changeDirection(boolean desire){
        isDesirable = desire;
        repaint();
    }
    
    protected Polygon getArrow(){
        return arrow;
    }
    
    protected Point getCenter(){
        return center;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        JFrame desirable = new JFrame("Arrow");
        desirable.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Arrow arrow = new Arrow(true);
        arrow.setVisible(true);
        desirable.add(arrow);
        desirable.pack();
        desirable.setVisible(true);
        
        JFrame undesirable = new JFrame("Reverse Arrow");
        undesirable.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Arrow revArrow = new Arrow(false);
        revArrow.setVisible(true);
        undesirable.add(revArrow);
        undesirable.pack();
        undesirable.setVisible(true);
        
        JFrame invalid = new JFrame("Invalid Arrow");
        invalid.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Arrow badCenter = new Arrow(false);
        invalid.add(badCenter);
        invalid.pack();
        invalid.setVisible(true);
    }

}
