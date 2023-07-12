package org.pm4knime.node.conversion.pn2table;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

//import org.knime.base.data.xml.Element;
//import org.knime.base.data.xml.Node;
//import org.knime.base.data.xml.PetriNetCell;
//import org.knime.base.data.xml.PetriNetCellFactory;
//import org.knime.base.data.xml.SvgImageContent;
//import org.knime.base.data.xml.PetriNetValue;
//import org.knime.base.data.xml.PetriNetCell.SvgSerializer;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.image.png.PNGImageCell;
import org.knime.core.node.CanceledExecutionException;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

@SuppressWarnings("serial")
public class PetriNetCell extends DataCell {
	
	
    String pnString;
    List<String> places;
    List<String> transitions;
    List<String> edges;
	String iMarking;
	List<String> fMarking;
    public static final DataType TYPE = DataType.getType(PetriNetCell.class);

    public static final class PetriNetSerializer implements DataCellSerializer<PetriNetCell> {
        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final PetriNetCell cell, final DataCellDataOutput output) throws IOException {
            try {
                output.writeUTF(cell.pnString);
                output.writeInt(cell.places.size());
                for (int i = 0; i < cell.places.size(); i++) {
                	output.writeUTF(cell.places.get(i));
                }
                output.writeInt(cell.transitions.size());
                for (int i = 0; i < cell.transitions.size(); i++) {
                	output.writeUTF(cell.transitions.get(i));
                }
                output.writeInt(cell.edges.size());
                for (int i = 0; i < cell.edges.size(); i++) {
                	output.writeUTF(cell.edges.get(i));
                }
                output.writeUTF(cell.iMarking);
                output.writeInt(cell.fMarking.size());
                for (int i = 0; i < cell.fMarking.size(); i++) {
                	output.writeUTF(cell.fMarking.get(i));
                }
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IOException("Could not serialize Petri Net", ex);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public PetriNetCell deserialize(final DataCellDataInput input) throws IOException {
            String pnString = input.readUTF();
            int num_places = input.readInt();
            List<String> places = new ArrayList<String>();
    		for (int i = 0; i < num_places; i++) {
    			places.add(input.readUTF());
    		}
    		int num_transitions = input.readInt();
    		List<String> transitions = new ArrayList<String>();
    		for (int i = 0; i < num_transitions; i++) {
    			transitions.add(input.readUTF());
    		}
    		int num_edges = input.readInt();
    		List<String> edges = new ArrayList<String>();
    		for (int i = 0; i < num_edges; i++) {
    			edges.add(input.readUTF());
    		}
    		String iM = input.readUTF();
     		int num_fM = input.readInt();
             List<String> fM = new ArrayList<String>();
     		for (int i = 0; i < num_fM; i++) {
     			fM.add(input.readUTF());
     		}
            return new PetriNetCell(pnString, places, transitions, edges, iM, fM);
        }
    }
    
//    PNGImageCell ic;
	

    private static final Collection<String> SVG_TEXT_CONTENT_NOT_IGNORED_TAGS =
            Arrays.asList("text", "tspan", "textPath");

//    static final XmlDomComparerCustomizer SVG_XML_CUSTOMIZER = new XmlDomComparerCustomizer(
//        ChildrenCompareStrategy.ORDERED) {
//
//        @Override
//        public boolean include(final Node node) {
//            switch (node.getNodeType()) {
//                case Node.TEXT_NODE:
//                    //ignore all text nodes, except the ones in the defined set
//                    return SVG_TEXT_CONTENT_NOT_IGNORED_TAGS.contains(node.getParentNode().getLocalName());
//                case Node.ELEMENT_NODE:
//                    //ignore metadata elements
//                    Element element = (Element)node;
//                    return !"metadata".equals(element.getLocalName());
//                case Node.COMMENT_NODE:
//                    //ignore comments
//                    return false;
//                default:
//                    return true;
//            }
//        }
//    };

//    private SoftReference<String> m_xmlString;

//    private final ReentrantLock m_lock = new ReentrantLock();

//    private final SvgImageContent m_content;


    public static DataCellSerializer<PetriNetCell> getCellSerializer() {
        return new PetriNetSerializer();
    }

    public PetriNetCell(AcceptingPetriNet anet) {
    	try {
    		pnString = PN2XmlConverter.convert(anet);
    		places = new ArrayList<String>();
    		Iterator<Place> it = anet.getNet().getPlaces().iterator();
    		for (int i = 0; i < anet.getNet().getPlaces().size(); i++) {
    			places.add(it.next().getLabel());
    		}
    		transitions = new ArrayList<String>();
    		Iterator<Transition> it_2 = anet.getNet().getTransitions().iterator();
    		for (int i = 0; i < anet.getNet().getTransitions().size(); i++) {
    			transitions.add(it_2.next().getLabel());
    		}
    		edges = new ArrayList<String>();
    		Iterator<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> it_edges = anet.getNet().getEdges().iterator();
    		for (int i = 0; i < anet.getNet().getEdges().size(); i++) {
    			PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e = it_edges.next();
    			edges.add(e.getSource().getLabel() + " --> " + e.getTarget().getLabel());
    		}
    		iMarking = anet.getInitialMarking().toString();
    		fMarking = new ArrayList<String>();
    		Iterator<Marking> it_fm = anet.getFinalMarkings().iterator();
    		for (int i = 0; i < anet.getFinalMarkings().size(); i++) {
    			fMarking.add(it_fm.next().toString());
    		}
    		Collections.sort(places);
    		Collections.sort(transitions);
    		Collections.sort(edges);
    		Collections.sort(fMarking);
    		
		} catch (CanceledExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PetriNetCell(String pnString2, List<String> placeList, List<String> transitionList, List<String> edgeList, String iM, List<String> fM) {
		pnString = pnString2;
		places = placeList;
		transitions = transitionList;
		edges = edgeList;
		iMarking = iM;
		fMarking = fM;
		Collections.sort(places);
		Collections.sort(transitions);
		Collections.sort(edges);
		Collections.sort(fMarking);
	}

	public String toString() {
//		StringBuilder sb = new StringBuilder();
//	    sb.append("Petri Net: \n");
//	    sb.append("Places: ").append(places.size()).append(" elements \n");
//        for (int i = 0; i < places.size(); i++) {
//            sb.append("  ").append(places.get(i)).append(" \n");
//        }
//        sb.append("Initial Marking:").append(" \n");
//        sb.append("  ").append(iMarking).append(" \n");
//        sb.append("Final Markings:").append(" \n");
//        for (int i = 0; i < fMarking.size(); i++) {
//            sb.append("  ").append(fMarking.get(i)).append(" \n");
//        }
//        sb.append("Transitions: ").append(transitions.size()).append(" elements \n");
//        for (int i = 0; i < transitions.size(); i++) {
//            sb.append("  ").append(transitions.get(i)).append(" \n");
//        }
//        sb.append("Arcs: ").append(edges.size()).append(" elements \n");
//        for (int i = 0; i < edges.size(); i++) {
//            sb.append("  ").append(edges.get(i)).append(" \n");
//        }          
//        return sb.toString();
		return pnString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalsDataCell(final DataCell cell) {
    	return this.equalContent(cell);  
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalContent(final DataValue otherValue) {
    	if (otherValue instanceof PetriNetCell) {
    		PetriNetCell cell = (PetriNetCell) otherValue;
    		return places.equals(cell.places) && transitions.equals(cell.transitions) && edges.equals(cell.edges) && iMarking.equals(cell.iMarking) && fMarking.equals(cell.fMarking);
    	} else {
    		return false;
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
    	return places.hashCode() + transitions.hashCode() + edges.hashCode() + iMarking.hashCode() + fMarking.hashCode();
    }

	public String getStringValue() {
		// TODO Auto-generated method stub
		return this.pnString;
	}
    
}
