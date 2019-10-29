package org.pm4knime.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class PetriNetUtil {

	public static Set<Marking> guessFinalMarking(Petrinet net) {
		// TODO Auto-generated method stub
		List<Place> placeList = getEndPlace(net);
		Set<Marking> finalSet = new HashSet<>();
		for(Place p: placeList) {
			Marking finalMarking = new Marking();
			finalMarking.add(p);
			finalSet.add(finalMarking);
		}
		return finalSet;
	}
	
	public static List<Place> getEndPlace(Petrinet net) {
		// firstly to get all places, if one place has no postset edges, then
		// it is the endPlace
		Collection<Place> places = net.getPlaces();
		Place p;
		List<Place> endp = new ArrayList<>();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
		Iterator<Place> pIterator = places.iterator();
		while (pIterator.hasNext()) {
			p = pIterator.next();
			postset = net.getOutEdges(p);
			if (postset.size() < 1) {
				endp.add(p);
			}
		}
		if (endp.isEmpty()) {
			System.out.println("There is no End Place and create end place");
			// and also the Arc to it 
		}
		return endp;
	}
	
	public static Transition findTransition(String transitionName, Collection<Transition> tCol) {
		// TODO given transition name, we can find the transition in net
    	for(Transition t: tCol) {
    		// here due to the space character, we need to strip them at first
    		// and then compare it
    		if(transitionName.trim().equals(t.getLabel().trim()))
    			return t;
    	}
		return null;
	}

	/*
	 * this function collects the transition from log and outputs the names in the sorted order.
	 * we list the visible transitions at first, then silent transitions in order
	 */
	public static List<String> extractTransitionNames(Petrinet net){
		Collection<Transition> transitions = net.getTransitions();
		SortedSet<String> tSet = new TreeSet();
		SortedSet<String> tauSet = new TreeSet();
		for(Transition t: transitions) {
			if(t.isInvisible())
				tauSet.add(t.getLabel());
			else
				tSet.add(t.getLabel());
		}
		
		List<String> nameList = new ArrayList<String>();
		nameList.addAll(tSet);
		nameList.addAll(tauSet);
		return nameList;
	}
	
	// @refer more details : nl.tue.alignment.test.AlignmentTest
	public static TransEvClassMapping constructMapping(XLog log, Petrinet net,  XEventClassifier eventClassifier, XEventClass dummyEvent) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvent);
		// here we need dummy event to map invisible transition. Even if there is no corresponding event classes, we also need to map them
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		for (Transition t : net.getTransitions()) {
			boolean mapped = false;
			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				String id = evClass.getId();

				if (t.getLabel().trim().equals(id.trim())) {
					mapped = true;
					mapping.put(t, evClass);
					break;
				}
			}
			if (!mapped) {
				mapping.put(t, dummyEvent);
			}
		}
		return mapping;
	}
	
	// import the Petri net from a pnml file with the same transition id.
	public static Pnml importPnmlFromStream(InputStream input) throws XmlPullParserException, IOException {
		FullPnmlElementFactory pnmlFactory = new FullPnmlElementFactory();
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(input, null);
		int eventType = xpp.getEventType();
		
		Pnml pnml = new Pnml();
		synchronized (pnmlFactory) {
			pnml.setFactory(pnmlFactory);

			/*
			 * Skip whatever we find until we've found a start tag.
			 */
			while (eventType != XmlPullParser.START_TAG) {
				eventType = xpp.next();
			}
			/*
			 * Check whether start tag corresponds to PNML start tag.
			 */
			if (xpp.getName().equals(Pnml.TAG)) {
				/*
				 * Yes it does. Import the PNML element.
				 */
				pnml.importElement(xpp, pnml);
			} else {
				/*
				 * No it does not. Return null to signal failure.
				 */
				pnml.log(Pnml.TAG, xpp.getLineNumber(), "Expected pnml");
			}
			if (pnml.hasErrors()) {
				return null;
			}
			return pnml;
		}
	}
	
	public static AcceptingPetriNet connectNet(Pnml pnml, PetrinetGraph net) {
		/*
		 * Return the net and the marking.
		 */
		Marking marking = new Marking();
		Set<Marking> finalMarkings = new HashSet<Marking>();
		GraphLayoutConnection layout = new GraphLayoutConnection(net);
		
		pnml.convertToNet(net, marking, finalMarkings, layout);
		
		AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) net, marking, finalMarkings);
		
		return anet;
	}
	
	public static AcceptingPetriNet importFromStream(InputStream input ) throws XmlPullParserException, IOException {
		Pnml pnml = importPnmlFromStream(input);
		
		if (pnml == null) {
			/*
			 * No PNML found in file. Fail.
			 */
			return null;
		}
		
		Petrinet net = PetrinetFactory.newPetrinet(pnml.getLabel());
		return connectNet(pnml, net);
	}
}
