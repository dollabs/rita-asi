package memory2.lattice;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import frames.entities.Thread;
import utils.Mark;
public class LatticeUI extends JFrame implements KeyListener {

    class LatticePanel extends JPanel implements MouseListener{
        private TypeLattice lattice;
        private Concept<String> concept;
        private JTextArea textfield;
        private float node_width;
        private int node_height;

        private Set<String> positives = new HashSet<String>();
        private Set<String> negatives = new HashSet<String>();

        public LatticePanel(TypeLattice lattice, JTextArea textfield) {
            setLattice(lattice);
            addMouseListener(this);
            this.textfield = textfield;
        }

        public void setLattice(TypeLattice lattice) {
            this.lattice = lattice;
            concept = new FasterLLConcept<String>(lattice);
            positives.clear();
            negatives.clear();
        }

        private float spacing = 1.5f;
        private Map<String, Point2D> map;
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D)g;
            Font font = g2d.getFont().deriveFont(Font.BOLD).deriveFont(20.0f);
            g2d.setFont(font);

            g2d.setColor(Color.white);
            int height = g2d.getClipBounds().height;
            int width = g2d.getClipBounds().width;
            g2d.fillRect(0, 0, width, height);

            List<Set<String>> sorted = lattice.topologicalSort();

            int layers = sorted.size()*2-1;
            node_height = height / layers;

            int columns = 0;
            for (Set<String> c : sorted) {
                columns = Math.max(columns, c.size());
            }

            node_width = width / (spacing * columns);

            map = new HashMap<String, Point2D>();
            int y = 0;

            for (Set<String> c : sorted) {
                float x = 0;
                for (String type : c) {
                    map.put(type, new Point2D.Float(x,y));

                    if (positives.size() + negatives.size() > 0) {
                        g2d.setColor(concept.contains(type) ? Color.green : Color.red);
                        if (positives.contains(type) || negatives.contains(type)) {
                            g2d.setColor(g2d.getColor().brighter());
                        } else {
                            g2d.setColor(g2d.getColor().darker().darker());
                        }
                        g2d.fillOval((int)x, y, (int)node_width, node_height);
                        if (positives.contains(type) || negatives.contains(type)) {
                            g2d.setColor(Color.DARK_GRAY);
                        } else {
                            g2d.setColor(Color.LIGHT_GRAY);
                        }

                        Rectangle2D box = g2d.getFontMetrics().getStringBounds(type, g2d);
                        g2d.drawString(type, (int)(x+(node_width-box.getWidth())/2), y+node_height/2);
                    } else {
                        g2d.setColor(Color.black);
                        g2d.drawOval((int)x, y, (int)node_width, node_height);

                        Rectangle2D box = g2d.getFontMetrics().getStringBounds(type, g2d);
                        g2d.drawString(type, (int)(x+(node_width-box.getWidth())/2), y+node_height/2);
                    }


                    x += spacing*node_width;
                }
                y += 2*node_height;
            }

            for(String type : map.keySet()) {
            	for (String parent : lattice.getParents(type)) {
            		 Point2D p1 = map.get(parent);
                     Point2D p2 = map.get(type);
                     g2d.setColor(Color.gray);
                     g2d.drawLine((int)(p1.getX()+node_width/2), (int)(p1.getY()+node_height), (int)(p2.getX()+node_width/2), (int)p2.getY());
                     g2d.drawOval((int)(p2.getX()+node_width/2-2), (int)(p2.getY()-2), 4, 4);
            	}
            }
        }

        public void mouseClicked(MouseEvent e) {
            Point xy =e.getPoint();

            String closest = "None";
            double dist = Double.MAX_VALUE;
            for(String type : map.keySet()) {
                Point2D corner = map.get(type);
                Point2D center = new Point2D.Double(corner.getX()+node_width/2, corner.getY()+node_height/2);
                if (xy.distanceSq(center) < dist) {
                    dist = xy.distanceSq(center);
                    closest = type;
                }
            }

            if (e.getButton() == 1) {
            	positives.add(closest);
            	concept.learnPositive(closest);
            } else {
            	negatives.add(closest);
            	concept.learnNegative(closest);
            }

            repaint();
            textfield.setText(concept.toString());
        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }

        public void mousePressed(MouseEvent e) {

        }

        public void mouseReleased(MouseEvent e) {

        }
    }
    JTextArea text;
    JTextArea analysis;
    LatticePanel lp;
    public LatticeUI()  {

        text = new JTextArea("entity animate animal mammal cat\n"+
                "mammal dog\n"+
                "animal fish\n"+
                "animate plant\n"+
                "entity inanimate rug\n"+
        "inanimate car\n");

        analysis = new JTextArea();
        analysis.setEditable(false);
        analysis.setFont(analysis.getFont().deriveFont(20.0f));
        analysis.setWrapStyleWord(true);

        JPanel right = new JPanel(new BorderLayout());
        right.add(text, BorderLayout.CENTER);
        right.add(analysis, BorderLayout.SOUTH);

        List<Thread> threads = new ArrayList<Thread>();
        for (String string : text.getText().split("\n")) {
          Mark.say("~~~~~~~~~~ UI",string);
            threads.add(Thread.parse(string));
        }
        TypeLattice lattice = new TypeLattice(threads);
        lp = new LatticePanel(lattice, analysis);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lp, right);
        split.setResizeWeight(0.8);
        split.setContinuousLayout(true);

        getContentPane().setLayout(new GridLayout());
        getContentPane().add(split);

        text.addKeyListener(this);

        setSize(800,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {

        } catch (IllegalAccessException e) {

        } catch (InstantiationException e) {

        } catch (ClassNotFoundException e) {

        }

        LatticeUI ui = new LatticeUI();
        ui.setVisible(true);
    }


    public void keyPressed(KeyEvent e) {

    }


    public void keyReleased(KeyEvent e) {

    }


    public void keyTyped(KeyEvent e) {
        if(KeyEvent.getKeyText(e.getKeyChar()).equalsIgnoreCase("Enter")) {
            List<Thread> threads = new ArrayList<Thread>();
            for (String string : text.getText().split("\n")) {
                threads.add(Thread.parse(string));
            }
            lp.setLattice(new TypeLattice(threads));
            lp.repaint();
        }
    }
}
