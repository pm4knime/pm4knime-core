package org.pm4knime.util;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.extendedhybridminer.models.hybridpetrinet.ExtendedHybridPetrinet;
import org.processmining.extendedhybridminer.models.pnml.HybridPnml;
import org.processmining.extendedhybridminer.models.pnml.PnmlHybridArc;
import org.processmining.extendedhybridminer.models.pnml.PnmlHybridNet;
import org.processmining.extendedhybridminer.models.pnml.PnmlHybridPage;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.ExpandableSubNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.base.Pnml.PnmlType;
import org.processmining.plugins.pnml.base.PnmlElementFactory;
import org.processmining.plugins.pnml.elements.PnmlName;
import org.processmining.plugins.pnml.elements.PnmlNet;
import org.processmining.plugins.pnml.elements.PnmlNode;
import org.processmining.plugins.pnml.elements.PnmlPage;
import org.processmining.plugins.pnml.elements.PnmlPlace;
import org.processmining.plugins.pnml.elements.PnmlReferencePlace;
import org.processmining.plugins.pnml.elements.PnmlReferenceTransition;
import org.processmining.plugins.pnml.elements.PnmlTransition;
import org.processmining.plugins.pnml.elements.extensions.opennet.PnmlFinalMarkings;
import org.processmining.plugins.pnml.elements.graphics.PnmlNodeGraphics;
import org.processmining.plugins.pnml.toolspecific.PnmlToolSpecific;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class HybridPetriNetUtil {
	
	public static void importHybridPetrinetFromStream(InputStream input, ExtendedHybridPetrinet net) throws Exception {
		FullPnmlElementFactory pnmlFactory = new FullPnmlElementFactory();
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(input, null);
		int eventType = xpp.getEventType();
		Pnml pnml = new Pnml();
		List<PnmlNode> nodeList = new ArrayList<PnmlNode>();
		List<PnmlHybridArc> arcList = new ArrayList<PnmlHybridArc>();
		synchronized (pnmlFactory) {
			pnml.setFactory(pnmlFactory);
			while (eventType != XmlPullParser.START_TAG) {
				eventType = xpp.next();
			}
			if (xpp.getName().equals(Pnml.TAG)) {
				ArrayList<String> stack = new ArrayList<String>();
				stack.add(Pnml.TAG);
				while (!stack.isEmpty()) {
					try {
						eventType = xpp.next();
						if (eventType == XmlPullParser.START_TAG) {
							String name = xpp.getName();
							if (name == null) {
						    	continue;
						    } 
							stack.add(name);
							if (stack.size() == 2) {
								if (name.equals(PnmlName.TAG) || name.equals(PnmlPage.TAG) 
										|| name.equals(PnmlNet.TAG) || name.equals(PnmlNodeGraphics.TAG) 
										|| name.equals(PnmlToolSpecific.TAG) || name.equals(PnmlReferenceTransition.TAG) 
										|| name.equals(PnmlReferencePlace.TAG)) {
									stack.remove(stack.size() - 1);
								} else if (name.equals(PnmlPlace.TAG)) {
									stack.remove(stack.size() - 1);
									PnmlPlace place = pnmlFactory.createPnmlPlace();
									place.importElement(xpp, pnml);
									nodeList.add(place);
								} else if (name.equals(PnmlTransition.TAG)) {
									stack.remove(stack.size() - 1);
									PnmlTransition transition = pnmlFactory.createPnmlTransition();
									transition.importElement(xpp, pnml);
									nodeList.add(transition);
								} else if (name.equals(PnmlHybridArc.TAG)) {
									stack.remove(stack.size() - 1);
									PnmlHybridArc arc = new PnmlHybridArc();
									arc.importElement(xpp, pnml);
									arcList.add(arc);
								}
							}
						} else if ((eventType == XmlPullParser.END_TAG)) {
							if (xpp.getName().equals(stack.get(stack.size() - 1))) {
								stack.remove(stack.size() - 1);
							}
						}
					} catch (Exception ex) {
					}
				}
			}
		}
		net.setColors();
		GraphLayoutConnection layout = new GraphLayoutConnection(net);
		synchronized (factory) {
			Map<String, Place> placeMap = new HashMap<String, Place>();
			Map<String, Transition> transitionMap = new HashMap<String, Transition>();
			Map<String, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edgeMap = new HashMap<String, PetrinetEdge<?, ?>>();
			Double displacement = new Point2D.Double(0.0, 0.0);
			for (PnmlNode node : nodeList) {
				if (node instanceof PnmlPlace) {
					((PnmlPlace) node).convertToNet(net, null, net.initialMarking, placeMap, displacement , layout);
				} else if (node instanceof PnmlTransition) {
					((PnmlTransition) node).convertToNet(net, null, transitionMap, displacement, layout);
				} 
			}
			for (PnmlHybridArc arc : arcList) {
				arc.convertToNet(net, null, placeMap, transitionMap, edgeMap, displacement, layout);
			}
			pnml.setLayout(net, layout);			
		}
		if (net.finalMarkings.isEmpty()) {
			net.finalMarkings.add(new Marking());
		}
	}
	
	public static void exportHybridPetrinetToFile(ObjectOutputStream objOut, ExtendedHybridPetrinet net) throws IOException {
		GraphLayoutConnection layout = new GraphLayoutConnection(net);
		
		PnmlElementFactory factory = new FullPnmlElementFactory();
		HybridPnml pnml = new HybridPnml();
		pnml.setFactory(factory);
		synchronized (factory) {
			convertNetToPnml(net, pnml, layout, factory);
			pnml.setType(PnmlType.PNML);
		}
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + pnml.exportElement(pnml);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(objOut));
		bw.write(text);
		bw.close();
	}

	public static void convertNetToPnml(ExtendedHybridPetrinet net, HybridPnml pnml, GraphLayoutConnection layout,
			PnmlElementFactory factory) {
		
		Map<String, AbstractGraphElement> idMap = new HashMap<String, AbstractGraphElement>();
		Marking iMarking = net.setInitialMarking();
		Collection<Marking> fMarkings = net.setFinalMarkings();
		synchronized (factory) {
			Map<Pair<AbstractGraphElement, ExpandableSubNet>, String> map = new HashMap<Pair<AbstractGraphElement, ExpandableSubNet>, String>();
		    ArrayList<PnmlHybridNet> netList = new ArrayList<PnmlHybridNet>();
			PnmlHybridNet pnmlNet = new PnmlHybridNet();
			ArrayList<PnmlPage> pageList = new ArrayList<PnmlPage>();
		    pageList.add(createPnmlPage(net, iMarking, map, layout, factory));
			
			for (Pair<AbstractGraphElement, ExpandableSubNet> object : map.keySet()) {
				idMap.put(map.get(object), object.getFirst());
			}
			PnmlFinalMarkings pnmlFinalMarkings = factory.createPnmlFinalMarkings();
			pnmlFinalMarkings.convertFromOpenNet(net.getPlaces(), fMarkings, map);
			pnmlNet.setPageList(pageList);
			netList.add(pnmlNet);
			pnml.setNetList(netList);
			Map<AbstractGraphElement, String> map2 = new HashMap<AbstractGraphElement, String>();
			for (Pair<AbstractGraphElement, ExpandableSubNet> pair : map.keySet()) {
				map2.put(pair.getFirst(), map.get(pair));
			}
		}
	}

	public static PnmlPage createPnmlPage(ExtendedHybridPetrinet net, Marking iMarking, Map<Pair<AbstractGraphElement, ExpandableSubNet>, String> map,
			GraphLayoutConnection layout, PnmlElementFactory factory) {
		
		PnmlHybridPage page = new PnmlHybridPage();
		Map<ExpandableSubNet, PnmlPage> pageMap = new HashMap<ExpandableSubNet, PnmlPage>();
		pageMap.put(null, page);
		((PnmlNode) page).convertFromNet(null, null, map, layout);
		ArrayList<PnmlNode> nodeList = new ArrayList<PnmlNode>();
		ArrayList<PnmlHybridArc> arcList = new ArrayList<PnmlHybridArc>();
		for (Place place : net.getPlaces()) {
			nodeList.add(factory.createPnmlPlace().convertFromNet(iMarking,
					place.getParent(), place, map, layout));
		}
		for (Transition transition : net.getTransitions()) {
			nodeList.add(factory.createPnmlTransition().convertFromNet(net,
					transition.getParent(), transition, map, layout));
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getEdges()) {
			if (edge instanceof Arc) {
				PnmlHybridArc arc = new PnmlHybridArc();
				arc.convertFromNet(null, edge, page, map, layout);
				arcList.add(arc);
			}
		}
		page.setLists(nodeList, arcList);
		return page;
	}
	
}
