package gui;

import frames.PathElementFrame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Vector;

import javax.swing.*;


import connections.*;
import connections.Ports;

/*
 * Created on May 17, 2007 @author phw
 */
public class TransferViewer extends RoleViewer  {
}
//public class TransferViewer extends NegatableJPanel  {
//	String role;
//
//	String agent;
//
//	String name;
//
//	String mover;
//
//	String from;
//
//	String to;
//
//	private Ports ports;
//	
//	JPanel fromPanel = new JPanel();
//	JPanel toPanel = new JPanel();
//	JPanel moverPanel = new JPanel();
//	
//	JLabel fromLabel = new JLabel();
//	JLabel toLabel = new JLabel();
//	JLabel moverLabel = new JLabel("", SwingUtilities.CENTER);
//	
//	boolean initialized = false;
//	
//	public TransferViewer() {
//
//		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//		fromPanel.setBorder(BorderFactory.createTitledBorder("From"));
//		toPanel.setBorder(BorderFactory.createTitledBorder("To"));
//		this.setLayout(new GridLayout(0, 1));
//		this.setOpaque(false);
//		
//	}
//	
//	public void paint (Graphics g) {
//		super.paint(g);
//		paintComponent(g);
//	}
//
//
//	private void initialize() {
//		if (initialized) {return;}
//		initialized = true;
//		fromPanel.setLayout(new BorderLayout());
//		toPanel.setLayout(new BorderLayout());
//		moverPanel.setLayout(new BorderLayout());
//		fromPanel.add(fromLabel, BorderLayout.CENTER);
//		toPanel.add(toLabel, BorderLayout.CENTER);
//		moverPanel.add(moverLabel, BorderLayout.CENTER);
//	    this.add(fromPanel);
//		this.add(moverPanel);
//		this.add(toPanel);
//		fromPanel.setOpaque(false);
//		toPanel.setOpaque(false);
//		moverPanel.setOpaque(false);
//    }
//
//	
//	private void setParameters(String role) {
//		// System.err.println("Trajectory word is " + role);
//		this.role = role;
//		this.repaint();
//	}
//
//	private void clearData() {
//		this.role = null;
//		this.name = null;
//	}
//
//	public Ports getPorts() {
//		if (this.ports == null) {
//			this.ports = new Ports();
//		}
//		return this.ports;
//	}
//
//	public void view (Object signal) {
//		// System.out.println("Transfer viewer received " + signal);
//		agent = from = to = null;
//		if (signal instanceof Relation && ((Relation) signal).isAPrimed("transfer")) {
//			Relation transfer = (Relation) signal;
//			if (transfer.getSubject().entityP()) {
//				agent = transfer.getSubject().getType();
//			}
//			if (transfer.getObject().relationP()) {
//				Relation trajectory = (Relation) (transfer.getObject());
//				if (trajectory.getSubject().entityP()) {
//					mover = trajectory.getSubject().getType();
//				}
//				if (trajectory.getObject().sequenceP()) {
//					Sequence path = ((Sequence) trajectory.getObject());
//					for (Thing element : path.getElements()) {
//						if (element.isA("from") && element.functionP()) {
//							Thing at = ((Derivative) element).getSubject();
//							if (at.functionP() && at.isA("at")) {
//								from = ((Derivative) at).getSubject().getType();
//							}
//						}
//						else if (element.isA("to") && element.functionP()) {
//							Thing at = ((Derivative) element).getSubject();
//							if (at.isA("at")) {
//								to = ((Derivative) at).getSubject().getType();
//							}
//						}
//					}
//				}
//			}
//		}
//		else {
//			System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type " + signal.getClass().toString() + ": "
//			        + signal + " in TransferViewer");
//		}
//		if (agent != null && mover != null && from != null && to != null) {
//			initialize();
//			fromLabel.setText(from);
//			moverLabel.setText(mover);
//			toLabel.setText(to);
//			repaint();
//		}
//		else {
//		}
//		setTruthValue(signal);
//	}
// }
