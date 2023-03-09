package matthewFay.viewers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import utils.Mark;
import matthewFay.Utilities.HashMatrix;

public class MatrixGridViewer extends JPanel implements MouseListener {
	
	private HashMatrix<String, String, Float> matrix;
	private boolean lower_matrix_only = false;
	private boolean readyToDraw = false;
	
	private boolean zeroToOneScaling = false;
	
	private Map<String, Float> rmin_values;
	private Map<String, Float> rmax_values;
	
	private Map<String, Float> cmin_values;
	private Map<String, Float> cmax_values;
	
	public MatrixGridViewer(HashMatrix<String, String, Float> matrix, boolean zeroToOneScaling) {
		this.zeroToOneScaling = zeroToOneScaling;
		setMatrix(matrix);
		
		this.addMouseListener(this);	
	}
	
	public MatrixGridViewer(HashMatrix<String, String, Float> matrix) {
		setMatrix(matrix);
		
		this.addMouseListener(this);
	}
	
	public void setMatrix(HashMatrix<String, String, Float> matrix) {
		readyToDraw = false;
		this.matrix = new HashMatrix<>(matrix);

		this.rmin_values = new LinkedHashMap<>();
		this.rmax_values = new LinkedHashMap<>();
		
		this.cmin_values = new LinkedHashMap<>();
		this.cmax_values = new LinkedHashMap<>();
		
		reweighMatrix();
		readyToDraw = true;
		this.invalidate();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paintComponent(g);
		this.setBackground(Color.WHITE);
		paintMatrix(g);
	}
	
	private void reweighMatrix() {
		if(matrix == null)
			return;
		
		//Need a consistent ordering for rows, cols
		Set<String> key_set = new LinkedHashSet<>(matrix.keySetRows());
		key_set.addAll(matrix.keySetCols());
		ArrayList<String> key_list = new ArrayList<String>(key_set);
		
		//Find min, max, range value in matrix (for coloring)
		float min = Float.MAX_VALUE;
		float max2 = Float.MIN_VALUE;

		for(String row : matrix.keySetRows()) {
			for(String col : matrix.keySetCols()) {
				if(col.equals(row))
					continue;
				if(matrix.contains(row, col)) {
					float val = matrix.get(row, col);
					min = Math.min(val,min);
					max2 = Math.max(val,max2);
				}
			}
		}
		
		if(min > 0) min = 0;
		float range = max2 - min;
		
		Mark.say("(min max, range):("+min+","+max2+","+range+")");
		
		//Find Min-Max on a row/col basis
		for(String row : matrix.keySetRows()) {
			float rmin = Float.MAX_VALUE;
			float rmax = Float.MIN_VALUE;
			for(String col : matrix.keySetCols()) {
				if(col.equals(row))
					continue;
				if(matrix.contains(row, col)) {
					float value = matrix.get(row, col);
					rmin = Math.min(value, rmin);
					rmax = Math.max(value, rmax);
				}
			}
			
			if(rmin > 0) rmin = 0;
			rmin_values.put(row, rmin);
			rmax_values.put(row, rmax);
			Mark.say(row+".row->(min, max):("+rmin+","+rmax+")");
		}
		
		for(String col : matrix.keySetCols()) {
			float cmin = Float.MAX_VALUE;
			float cmax = Float.MIN_VALUE;
			for(String row : matrix.keySetRows()) {
				if(col.equals(row))
					continue;
				if(matrix.contains(row, col)) {
					float value = matrix.get(row, col);
					cmin = Math.min(value, cmin);
					cmax = Math.max(value, cmax);
				}
			}
			
			if(cmin > 0) cmin = 0;
			cmin_values.put(col, cmin);
			cmax_values.put(col, cmax);
			Mark.say(col+".col->(min, max):("+cmin+","+cmax+")");
		}
		
		boolean old_coloring = true;
		
		
		//now adjust all the values
		if(old_coloring) {
			for(String row : matrix.keySetRows()) {
				for(String col : matrix.keySetCols()) {
					if(row.equals(col)) {
						matrix.put(row, col, 2.0f);
					} else {
						if(matrix.contains(row, col)) {
							//For ZeroToOne, trust values are in that range...
							if(!zeroToOneScaling) {
								float old_value = matrix.get(row, col);
								float val = (old_value-min)/range;
								matrix.put(row, col, val);
							}
						}
					}
				}
			}
		} else {
			for(String row : matrix.keySetRows()) {
				float rrange = rmax_values.get(row) - rmin_values.get(row);
				
				for(String col : matrix.keySetCols()) {
					float crange = cmax_values.get(col) - cmin_values.get(col);
					
					if(row.equals(col)) {
						matrix.put(row, col, 2.0f);
					} else {
						if(matrix.contains(row, col)) {
							//For ZeroToOne, trust values are in that range...
							if(!zeroToOneScaling) {
								float old_value = matrix.get(row, col);
								float rval = (old_value-rmin_values.get(row))/rrange;
								float cval = (old_value-cmin_values.get(col))/crange;
								float val = Math.max(rval, cval);
								if(val > 1.0f) {
									Mark.say("weird");
								}
								matrix.put(row, col, val);
							}
						}
					}
				}
			}
		}
	}
	
	private int width;
	private int height;
	private int name_width;
	private int name_height;
	private double scale_x;
	private double scale_y;
	private ArrayList<String> key_list;
	
	private void paintMatrix(Graphics g) {
		if(matrix == null)
			return;
		if(!readyToDraw)
			return;
		
		width = getWidth();
		height = getHeight();
		
		Font font = new Font("Dialog", Font.PLAIN, 12);
		
		Graphics2D canvas = (Graphics2D) g;
		
		//Need a consistent ordering for rows, cols
		Set<String> key_set = new LinkedHashSet<>(matrix.keySetRows());
		key_set.addAll(matrix.keySetCols());
		key_list = new ArrayList<String>(key_set);
		
		//Find name length/height
		name_width = 0;
		name_height = 0;
		int x = 0;
		int y = 0;
		for(String key : key_list) {
			Rectangle2D bounds = font.getStringBounds(x+": "+key, canvas.getFontRenderContext());
			name_width = (int) Math.max(name_width, bounds.getWidth());
			name_height = (int) Math.max(name_height, bounds.getHeight());
			x++;
		}
		
		//Do math to set the scale
		scale_x = (double)width / (double)(name_width+key_list.size()*20);
		scale_y = (double)height / (double)(name_height+name_height*key_list.size());

		canvas.scale(scale_x, scale_y);
		//Draw Matrix
		x=0;
		y=0;
		//Top Row
		canvas.setColor(Color.BLACK);
		canvas.setFont(font);
		canvas.drawRect(0, 0, name_width, name_height);
		for(String key : key_list) {
			
			canvas.drawString(" "+x, name_width+x*20, name_height);
			canvas.drawRect(name_width+x*20, 0, 20, name_height);
			x++;
		}
		x=0;
		//Rest of the Matrix
		for(String row : key_list) {
			canvas.setColor(Color.BLACK);
			canvas.setFont(font);
			canvas.drawString(x+": "+row, 0, (x+2)*name_height);
			canvas.drawRect(0, (x+1)*name_height, name_width, name_height);
			for(String col : key_list) {
				if(matrix.contains(row, col)) {
					float val = matrix.get(row, col);
					Color c;
					if(val > 1.0f)
						c = Color.blue;
					else
						c = new Color(0f, val, 0f);
					if(x>y && lower_matrix_only)
						c = Color.black;
					canvas.setColor(c);
					canvas.fillRect(name_width+x*20, (y+1)*name_height, 20, name_height);
					canvas.setColor(Color.BLACK);
					canvas.drawRect(name_width+x*20, (y+1)*name_height, 20, name_height);
					y++;
				}
			}
			y=0;
			x++;
		}
		
	}
	
	public static HashMatrix<String, String, Float> getDemoData() {
		HashMatrix<String, String, Float> matrix = new HashMatrix<>();
		matrix.put("A", "A", 100f);
		matrix.put("B", "A", 50f);
		matrix.put("C", "A", 25f);
		matrix.put("A", "B", 50f);
		matrix.put("B", "B", 100f);
		matrix.put("C", "B", 50f);
		matrix.put("A", "C", 25f);
		matrix.put("B", "C", 50f);
		matrix.put("C", "C", 100f);
		return matrix;
	}
	
	public static JFrame createPopoutMatrixViewer(HashMatrix<String, String, Float> matrix) {
		MatrixGridViewer viewer = new MatrixGridViewer(matrix);
		
		JFrame frame = new JFrame();
		
		frame.getContentPane().setBackground(Color.WHITE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(viewer);
		
		frame.setBounds(0, 0, 1024, 768);
		
		frame.setTitle(MatrixGridViewer.class.toString());
		
		frame.setVisible(true);
		
		return frame;
	}
	
	// For testing Purposes
	public static void main(String[] args) {
		HashMatrix<String, String, Float> matrix = getDemoData();
		
		JFrame frame = createPopoutMatrixViewer(matrix);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getButton() == MouseEvent.BUTTON1) {
			float x = arg0.getPoint().x;
			float y = arg0.getPoint().y;
			
			float count = key_list.size();
			
//			Mark.say("(x,y) : ("+x+","+y+")");
//			Mark.say("(count) : ("+count+")");
//			Mark.say("(height,width) : ("+height+","+width+")");
//			Mark.say("(scale_x,scale_y) : ("+scale_x+","+scale_y+")");
//			Mark.say("(name_width,name_height) : ("+name_width+","+name_height+")");
			
			//With no scaling
			int clicked_x = (int)( ((x/scale_x)-name_width)/(float)((width/scale_x)-name_width)*count );
			int clicked_y = (int)( ((y/scale_y)-name_height)/(float)((height/scale_y)-name_height)*count);
//			Mark.say("(clicked_x,clicked_y) : ("+clicked_x+","+clicked_y+")");
			
			String x_name = key_list.get(clicked_x);
			String y_name = key_list.get(clicked_y);
			
			if(matrix.contains(x_name, y_name)) {
				Mark.say("(x_name,y_name,score) : ("+x_name+","+y_name+","+matrix.get(x_name, y_name)+")");
			} else {
				Mark.say("(x_name,y_name) : ("+x_name+","+y_name+")");
			}
			throwMatrixClickEvent(new MatrixClickEvent(this, x_name, y_name));
		}
	}

	List<MatrixClickListener> listeners = new ArrayList<MatrixClickListener>();
	public void addMatrixClickListener(MatrixClickListener listener) {
		listeners.add(listener);
	}
	public void throwMatrixClickEvent(MatrixClickEvent e) {
		for(MatrixClickListener listener : listeners) listener.handleMatrixClickEvent(e);
	}
	
	public class MatrixClickEvent extends java.util.EventObject {

		public String x_name;
		public String y_name;
		
		public MatrixClickEvent(Object source, String x_name, String y_name) {
			super(source);
			this.x_name = x_name;
			this.y_name = y_name;
		}
		
	}
	
	public interface MatrixClickListener {
		public void handleMatrixClickEvent(MatrixClickEvent e);
	}
	
}
