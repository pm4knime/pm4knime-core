package org.pm4knime.node.visualizations.jsgraphviz.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.processmining.extendedhybridminer.models.causalgraph.ExtendedCausalGraph;
import org.processmining.extendedhybridminer.models.causalgraph.HybridDirectedGraphEdge;
import org.processmining.extendedhybridminer.models.causalgraph.HybridDirectedGraphNode;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotNode;

public class GraphvizCausalGraph {
	public static Dot convert(ExtendedCausalGraph cg) {
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);
		convert(dot, cg);
		return dot;
	}


	private static class LocalDotTransition extends DotNode {
		//transition
		public LocalDotTransition(String label) {
			super(label, null);
			setOption("shape", "box");
		}

		//tau transition
		public LocalDotTransition() {
			super("", null);
			setOption("style", "filled");
			setOption("fillcolor", "#EEEEEE");
			setOption("width", "0.15");
			setOption("shape", "box");
		}
	}

	private static void convert(Dot dot, ExtendedCausalGraph cg) {
		HashMap<HybridDirectedGraphNode, DotNode> mapCG2Dot = new HashMap<HybridDirectedGraphNode, DotNode>();

		

		//add transitions
		List<HybridDirectedGraphNode> sortedNodes = new ArrayList<>(cg.getNodes());
		Collections.sort(sortedNodes, Comparator.comparing(HybridDirectedGraphNode::getLabel));
		for (HybridDirectedGraphNode t : sortedNodes) {
			DotNode transition;
			transition = new LocalDotTransition(t.getLabel());
			dot.addNode(transition);
			mapCG2Dot.put(t, transition);
		}

		//add arcs
		for (HybridDirectedGraphEdge e : cg.getSureGraphEdges()) {
			Color color = cg.getSureColor();
			add(dot, e, color, mapCG2Dot);	
		}
		for (HybridDirectedGraphEdge e : cg.getUncertainGraphEdges()) {
			Color color = cg.getUnsureColor();
			add(dot, e, color, mapCG2Dot);	
		}
		for (HybridDirectedGraphEdge e : cg.getLongDepGraphEdges()) {
			Color color = cg.getLongDepColor();
			add(dot, e, color, mapCG2Dot);	
		}
	}

	private static void add(Dot dot, HybridDirectedGraphEdge e, Color color, HashMap<HybridDirectedGraphNode, DotNode> mapCG2Dot) {
		if (mapCG2Dot.get(e.getSource()) != null && mapCG2Dot.get(e.getTarget()) != null) {
			HashMap<String, String> options = new HashMap<String, String>();
		    options.put("color", String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
			dot.addEdge(mapCG2Dot.get(e.getSource()), mapCG2Dot.get(e.getTarget()), "", options);
		}
		
	}

}
