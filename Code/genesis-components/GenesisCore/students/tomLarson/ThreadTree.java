package tomLarson;


import java.util.*;

import frames.entities.Bundle;
import frames.entities.Thread;

/**
 * A ThreadTree is a way to represent multiple threads in a compact form. Starting from 
 * "thing" at the root, threads branch off when they differ from any children. 
 * @author Thomas
 *
 * @param <N>
 */
@SuppressWarnings("rawtypes")
public class ThreadTree<N extends Node<Type>> {

	private Set<Node<Type>> nodes;
	private Node<Type> head;

	public ThreadTree() {
		nodes = new HashSet<Node<Type>>();
	}

	public static ThreadTree makeThreadTree(Thread t) {
		ThreadTree tree = new ThreadTree();
		tree.addThread(t);
		return tree;

	}
	
	public static ThreadTree makeThreadTree(Bundle b) {
		ThreadTree tree = new ThreadTree();
		for (Thread t : b) {
			tree.addThread(t);
		}
		return tree;
	}

	/*
	 * @param start the index of types to start at
	 */
	private void addRestofThread(Node<Type> n, String[] types, int start) {
		Node<Type> parent = n;
		Node<Type> child;
		for (int i = start; i < types.length - 1; i++) {
			child = new Node<Type>(types[i+1], 0);
			parent.addChild(child);
			child.setParent(parent);
			nodes.add(child);
			parent = child;
		}
	}

	public Node getHead() {
		return head;
	}

	/**
	 * Adds a Thread to this ThreadTree
	 * @param t
	 */
	public void addThread(Thread t) {
		if (isEmpty()) {
			String[] types = getAllTypes(t);
			head = new Node<Type>(types[0],0);
			nodes.add(head);
			addRestofThread(head, types,0);
		}
		String[] types = getAllTypes(t);
		//start comparing thread to what's already there
		String top = types[0].toLowerCase();
		if (!top.equalsIgnoreCase(head.getName())) {
			throw new RuntimeException("There's a Thing that's not a Thing!"); //bad stuff. 
		}
		addThread(types, head, 1 );

	}

	private void addThread(String[] types, Node<Type> current, int index) {
		//The thread being added is a subset of a thread already in memory. Propogate from where the new Thread ends. 
		if (index >= types.length) {
			current.propagateImpact(); 
		}
		else {
			//Check the children 
			for (Node<Type> child : current.getChildren()) {
				if (child.getName().equals(types[index])) {
					addThread(types, child, index+1);
					return;
				}
			}
			addRestofThread(current, types, index-1);
			current.propagateImpact();
		}

	}

	/**
	 * @param t
	 * @return a Type where the name is the place where the thread branches from memory, and the
	 * type is the impact score, normalized by the number of items in the Thread
	 */
	public Type getImpactofThread(Thread t) {
		if (isEmpty()) {
			return new Type("empty", 0);
		}
		String[] types = getAllTypes(t);
		return getImpactofThread(types, head, 1 , 0);

	}


	private Type getImpactofThread(String[] types, Node<Type> current, int index, double sum) {
		if (index >= types.length) {return new Type(current.getName(), sum/types.length);}
		for (Node<Type> child : current.getChildren()) {
			if (child.getName().equals(types[index])) {

				return getImpactofThread(types, child, index+1, sum+child.getWeight());

			}
		}
		return new Type(current.getName(), sum/types.length);
	}
	
	@SuppressWarnings("unused")
	private int threadLength(Thread t) {
		return getAllTypes(t).length;
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	private String[] getAllTypes(Thread t) {
		String stringRep = t.getString();
		return stringRep.split(" ");
	}



	public String toString() {
		String s = "";
		for (Node n : nodes) {
			s += (n.getParent() == null) ? "Node: " + n.getName() + ": " + n.getWeight() + ":" + n.getChildren().toString() + "none" + "\n"  :
				"Node: " + n.getName() + ": " + n.getWeight() + ":" +n.getChildren().toString() + n.getParent().toString() + "\n";
		}
		return s; 
	}


	public static void main (String[] args) {
		Thread t = new Thread();
		t.addType("Thing");
		t.addType("dog");
		t.addType("Mac");
		Thread l = new Thread();
		l.addType("Thing");
		l.addType("dog");
		l.addType("Murphy");
		ThreadTree tree = ThreadTree.makeThreadTree(t);
		tree.addThread(l);
		System.out.println(tree.toString());

	}

}
