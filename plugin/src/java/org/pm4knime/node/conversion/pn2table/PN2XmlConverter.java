package org.pm4knime.node.conversion.pn2table;

import org.knime.core.node.ExecutionContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.Collection;
import java.util.HashMap;

import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.base.Pnml.PnmlType;
import org.processmining.plugins.pnml.base.PnmlElementFactory;

public class PN2XmlConverter {
	
	static String convert(AcceptingPetriNet anet, BufferedDataContainer buf, ExecutionContext exec) throws CanceledExecutionException {
		
		GraphLayoutConnection  layout = new GraphLayoutConnection(anet.getNet());
		HashMap<PetrinetGraph, Marking> markedNets = new HashMap<PetrinetGraph, Marking>();
		HashMap<PetrinetGraph, Collection<Marking>> finalMarkedNets = new HashMap<PetrinetGraph, Collection<Marking>>();
		markedNets.put(anet.getNet(), anet.getInitialMarking());
		finalMarkedNets.put(anet.getNet(), anet.getFinalMarkings());
		
		PnmlElementFactory factory = new FullPnmlElementFactory();
		Pnml pnml = new Pnml();
		synchronized (factory) {
			pnml.setFactory(factory);
			pnml.setType(PnmlType.PNML);
			pnml = new Pnml().convertFromNet(anet.getNet(), anet.getInitialMarking(), anet.getFinalMarkings(), layout);
			
		}
		
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + pnml.exportElement(pnml);
		
		return text;
		
	}

}
