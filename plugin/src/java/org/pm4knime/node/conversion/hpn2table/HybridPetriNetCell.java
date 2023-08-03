package org.pm4knime.node.conversion.hpn2table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.node.CanceledExecutionException;
import org.pm4knime.util.HybridPetriNetUtil;
import org.processmining.extendedhybridminer.models.hybridpetrinet.ExtendedHybridPetrinet;
import org.processmining.extendedhybridminer.models.hybridpetrinet.LongDepEdge;
import org.processmining.extendedhybridminer.models.hybridpetrinet.SureEdge;
import org.processmining.extendedhybridminer.models.hybridpetrinet.UncertainEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

@SuppressWarnings("serial")
public class HybridPetriNetCell extends DataCell {
	
	
    String pnString;
    List<String> places;
    List<String> transitions;
	String iMarking;
	List<String> fMarking;
//	int placeColor;
//	int sureColor;
//	int unsureColor;
//	int longDepcolor;
	List<String> long_dep_edges;
	List<String> sure_edges;
	List<String> unsure_edges;
	List<String> place_edges;
    public static final DataType TYPE = DataType.getType(HybridPetriNetCell.class);

    public static final class HybridPetriNetSerializer implements DataCellSerializer<HybridPetriNetCell> {
        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final HybridPetriNetCell cell, final DataCellDataOutput output) throws IOException {
            try {
                output.writeUTF(cell.pnString);
                
//                output.writeInt(cell.placeColor);
//                output.writeInt(cell.sureColor);
//                output.writeInt(cell.unsureColor);
//                output.writeInt(cell.longDepcolor);
                
                output.writeInt(cell.places.size());
                for (int i = 0; i < cell.places.size(); i++) {
                	output.writeUTF(cell.places.get(i));
                }
                output.writeInt(cell.transitions.size());
                for (int i = 0; i < cell.transitions.size(); i++) {
                	output.writeUTF(cell.transitions.get(i));
                }
                output.writeInt(cell.place_edges.size());
                for (int i = 0; i < cell.place_edges.size(); i++) {
                	output.writeUTF(cell.place_edges.get(i));
                }
                output.writeInt(cell.sure_edges.size());
                for (int i = 0; i < cell.sure_edges.size(); i++) {
                	output.writeUTF(cell.sure_edges.get(i));
                }
                output.writeInt(cell.unsure_edges.size());
                for (int i = 0; i < cell.unsure_edges.size(); i++) {
                	output.writeUTF(cell.unsure_edges.get(i));
                }
                output.writeInt(cell.long_dep_edges.size());
                for (int i = 0; i < cell.long_dep_edges.size(); i++) {
                	output.writeUTF(cell.long_dep_edges.get(i));
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
        public HybridPetriNetCell deserialize(final DataCellDataInput input) throws IOException {
            String pnString = input.readUTF();
            
//            int c1 = input.readInt();
//            int c2 = input.readInt();
//            int c3 = input.readInt();
//            int c4 = input.readInt();
            
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
    		int num_edges_places = input.readInt();
    		List<String> edges_places = new ArrayList<String>();
    		for (int i = 0; i < num_edges_places; i++) {
    			edges_places.add(input.readUTF());
    		}
    		int num_edges_sure = input.readInt();
    		List<String> edges_sure = new ArrayList<String>();
    		for (int i = 0; i < num_edges_sure; i++) {
    			edges_sure.add(input.readUTF());
    		}
    		int num_edges_unsure = input.readInt();
    		List<String> edges_unsure = new ArrayList<String>();
    		for (int i = 0; i < num_edges_unsure; i++) {
    			edges_unsure.add(input.readUTF());
    		}
    		int num_edges_longDep = input.readInt();
    		List<String> edges_longDep = new ArrayList<String>();
    		for (int i = 0; i < num_edges_longDep; i++) {
    			edges_longDep.add(input.readUTF());
    		}
    		String iM = input.readUTF();
     		int num_fM = input.readInt();
             List<String> fM = new ArrayList<String>();
     		for (int i = 0; i < num_fM; i++) {
     			fM.add(input.readUTF());
     		}
            return new HybridPetriNetCell(pnString, places, transitions, edges_places, edges_sure, edges_unsure, edges_longDep, iM, fM);
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


    public static DataCellSerializer<HybridPetriNetCell> getCellSerializer() {
        return new HybridPetriNetSerializer();
    }

    
    public HybridPetriNetCell(ExtendedHybridPetrinet net) {
    	try {
    		pnString = HybridPetriNetUtil.convertHybridPetriNetToString(net);
//    		placeColor = net.getSurePlaceColor().getRGB();
//    		sureColor = net.getSureColor().getRGB();
//    		unsureColor = net.getUnsureColor().getRGB();
//    		longDepcolor = net.getLDColor().getRGB();
    		places = new ArrayList<String>();
    		Iterator<Place> it = net.getPlaces().iterator();
    		for (int i = 0; i < net.getPlaces().size(); i++) {
    			places.add(it.next().getLabel());
    		}
    		transitions = new ArrayList<String>();
    		Iterator<Transition> it_2 = net.getTransitions().iterator();
    		for (int i = 0; i < net.getTransitions().size(); i++) {
    			transitions.add(it_2.next().getLabel());
    		}
    		place_edges = new ArrayList<String>();
    		sure_edges = new ArrayList<String>();
    		unsure_edges = new ArrayList<String>();
    		long_dep_edges = new ArrayList<String>();
    		Iterator<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> it_edges = net.getEdges().iterator();
    		for (int i = 0; i < net.getEdges().size(); i++) {
    			PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e = it_edges.next();
    			if (e instanceof SureEdge) {
    				sure_edges.add(e.getSource().getLabel() + " --> " + e.getTarget().getLabel());
    			}
    			else if (e instanceof UncertainEdge) {
    				unsure_edges.add(e.getSource().getLabel() + " --> " + e.getTarget().getLabel());
    			}
    			else if (e instanceof LongDepEdge) {
    				long_dep_edges.add(e.getSource().getLabel() + " --> " + e.getTarget().getLabel());
    			}
    			else if (e.getSource() instanceof Place || e.getTarget() instanceof Place) {
    				place_edges.add(e.getSource().getLabel() + " --> " + e.getTarget().getLabel());
    			}
    				
    		}
    		iMarking = net.initialMarking.toString();
    		fMarking = new ArrayList<String>();
    		Iterator<Marking> it_fm = net.finalMarkings.iterator();
    		for (int i = 0; i < net.finalMarkings.size(); i++) {
    			fMarking.add(it_fm.next().toString());
    		}
    		Collections.sort(places);
    		Collections.sort(transitions);
    		Collections.sort(place_edges);
    		Collections.sort(sure_edges);
    		Collections.sort(unsure_edges);
    		Collections.sort(long_dep_edges);
    		Collections.sort(fMarking);
    		
		} catch (CanceledExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HybridPetriNetCell(String pnString2, List<String> placeList, List<String> transitionList, List<String> place_edges, List<String> sure_edges, List<String> unsure_edges, List<String> long_dep_edges, String iM, List<String> fM) {
		this.pnString = pnString2;
//		this.placeColor = placeColor;
//		this.sureColor = sureColor;
//		this.unsureColor = unsureColor;
//		this.longDepcolor = longDepcolor;
		this.places = placeList;
		this.transitions = transitionList;
		this.place_edges = place_edges;
		this.sure_edges = sure_edges;
		this.unsure_edges = unsure_edges;
		this.long_dep_edges = long_dep_edges;
		this.iMarking = iM;
		this.fMarking = fM;
		Collections.sort(this.places);
		Collections.sort(this.transitions);
		Collections.sort(place_edges);
		Collections.sort(sure_edges);
		Collections.sort(unsure_edges);
		Collections.sort(long_dep_edges);
		Collections.sort(this.fMarking);
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
    	if (otherValue instanceof HybridPetriNetCell) {
    		HybridPetriNetCell cell = (HybridPetriNetCell) otherValue;
    		return places.equals(cell.places) && transitions.equals(cell.transitions) && place_edges.equals(cell.place_edges) && sure_edges.equals(cell.sure_edges) && unsure_edges.equals(cell.unsure_edges) && long_dep_edges.equals(cell.long_dep_edges) && iMarking.equals(cell.iMarking) && fMarking.equals(cell.fMarking);
    	} else {
    		return false;
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
    	return places.hashCode() + transitions.hashCode() + place_edges.hashCode() + sure_edges.hashCode() + unsure_edges.hashCode() + long_dep_edges.hashCode() + iMarking.hashCode() + fMarking.hashCode();
    }

	public String getStringValue() {
		// TODO Auto-generated method stub
		return this.pnString;
	}
	

    
}
