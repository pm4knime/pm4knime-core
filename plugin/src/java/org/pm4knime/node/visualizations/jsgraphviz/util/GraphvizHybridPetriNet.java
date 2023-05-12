package org.pm4knime.node.visualizations.jsgraphviz.util;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import org.processmining.extendedhybridminer.models.hybridpetrinet.ExtendedHybridPetrinet;
import org.processmining.extendedhybridminer.models.hybridpetrinet.LongDepEdge;
import org.processmining.extendedhybridminer.models.hybridpetrinet.SureEdge;
import org.processmining.extendedhybridminer.models.hybridpetrinet.UncertainEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotNode;

public class GraphvizHybridPetriNet {

	public static Dot convert(ExtendedHybridPetrinet petrinet) {
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);
		convert(dot, petrinet);
		return dot;
	}

	private static class LocalDotPlace extends DotNode {
		public LocalDotPlace(Color color) {
			super("", null);
			setOption("shape", "circle");
			setOption("color", String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
		}
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

	private static void convert(Dot dot, ExtendedHybridPetrinet petrinet) {
		HashMap<PetrinetNode, DotNode> mapPetrinet2Dot = new HashMap<PetrinetNode, DotNode>();

		//add places
		for (Place p : petrinet.getPlaces()) {
			DotNode place;

			//find final marking
			boolean inFinalMarking = false;
			Marking initialMarking = petrinet.initialMarking;
			Collection<Marking> finalMarkings = petrinet.finalMarkings;
//			Marking fMarking = (Marking) finalMarkings.toArray()[0];
//			if (fMarking.size() == 0) {
//				fMarking.add(petrinet.getPlace("end"));
//			}
			//Marking finalMarking = new Marking();
			
			if (finalMarkings != null) {
				for (Marking finalMarking : finalMarkings) {
					inFinalMarking |= finalMarking.contains(p);
				}
			}

			place = new LocalDotPlace(petrinet.getSurePlaceColor());
			if (initialMarking != null && initialMarking.contains(p) && finalMarkings != null && inFinalMarking) {
				place.setOption("style", "filled");
				place.setOption("fillcolor", "#80ff00");
				place.setOption("peripheries", "2");
			} else if (initialMarking != null && initialMarking.contains(p)) {
				place.setOption("style", "filled");
				place.setOption("fillcolor", "#80ff00");
			} else if (finalMarkings != null && inFinalMarking) {
				place.setOption("peripheries", "2");
			}
			dot.addNode(place);
			mapPetrinet2Dot.put(p, place);
		}

		//add transitions
		for (Transition t : petrinet.getTransitions()) {
			DotNode transition;
			if (t.isInvisible()) {
				transition = new LocalDotTransition();
			} else {
				transition = new LocalDotTransition(t.getLabel());
			}
			dot.addNode(transition);
			mapPetrinet2Dot.put(t, transition);
		}

		//add arcs
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : petrinet.getEdges()) {
			if (mapPetrinet2Dot.get(e.getSource()) != null && mapPetrinet2Dot.get(e.getTarget()) != null) {
				HashMap<String, String> options = new HashMap<String, String>();
				Color color = petrinet.getSurePlaceColor();
				if(e instanceof SureEdge) {
					color = petrinet.getSureColor();
				} else if(e instanceof UncertainEdge) {
					color = petrinet.getUnsureColor();
				} else if(e instanceof LongDepEdge) {
					color = petrinet.getLDColor();
				}
			    options.put("color", String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
				dot.addEdge(mapPetrinet2Dot.get(e.getSource()), mapPetrinet2Dot.get(e.getTarget()), "", options);
			}
		}
	}
}

