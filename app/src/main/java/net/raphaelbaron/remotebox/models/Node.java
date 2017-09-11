package net.raphaelbaron.remotebox.models;

import java.util.Vector;

public class Node {
	
	//Artist name, album name, track name...
	public String name;
	
	//Url, for example
	public String extra;
	
	public Vector<Node> children;
	
	public Node(String name) { 
		this.name = name;
		this.children = new Vector<Node>();
	}
	
	public void addChild(Node n) {
		this.children.add(n);
	}
	
	public String toString() {
		return name;
	}
} 