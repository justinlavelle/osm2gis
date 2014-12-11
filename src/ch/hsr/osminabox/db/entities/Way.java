package ch.hsr.osminabox.db.entities;

import java.util.LinkedList;

/**
 * Way Entity
 * 
 * @author m2huber
 * 
 */
public class Way extends OSMEntity {
	
	/** A Way consists of sorted! Nodes ***/
	public LinkedList<Node> nodes = new LinkedList<Node>();
	
	public Way(){
	}

	public Way(Way way) {
		super(way);
		this.nodes = way.nodes;
	}
}
