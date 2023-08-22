package org.pm4knime.node.visualizations.jsgraphviz;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.base.node.mine.decisiontree2.image.DecTreeToImageNodeFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JSGraphVizViewRepresentation extends JSONViewContent {

	public final int pseudoIdentifier = (new Random()).nextInt();
	DecTreeToImageNodeFactory f;
	
	private static final String DOT_DATA = "dotstr";
	private String m_dotstr;

	@Override
	public void saveToNodeSettings(NodeSettingsWO settings) {
		try {
			settings.addString(DOT_DATA, m_dotstr);
	    } catch (Exception ex) {
	        // do nothing
	    }   
	}

	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		try {
			m_dotstr = settings.getString(DOT_DATA);
	    } catch (Exception ex) {
	        // do nothing
	    }   
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		
		JSGraphVizViewRepresentation other = (JSGraphVizViewRepresentation)obj;
		return new EqualsBuilder()
				.append(m_dotstr, other.m_dotstr)
                .isEquals();
	}

	@Override
	public int hashCode() {
		 return new HashCodeBuilder().append(m_dotstr)
				 .toHashCode();
	}

	public String getDotstr() {
		return m_dotstr;
	}

	public void setDotstr(final String dotstr) {
////		System.out.println(dotstr);
//		HashMap<String, String> idMap = new HashMap<String, String>();
//        Pattern pattern = Pattern.compile("e[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
//        Matcher matcher = pattern.matcher(dotstr);
//
//        int idCounter = 0;
//        while (matcher.find()) {
////        	System.out.println(matcher.toString());
//            String oldId = matcher.group();
////            System.out.println("OLD ID" + oldId);
//            if (!idMap.containsKey(oldId)) {
//                String newId = "id" + idCounter;
////                System.out.println("New ID" + newId);
//                idMap.put(oldId, newId);
//                idCounter++;
//            }
//        }
//        String finalDotString = dotstr;
//        // Now replace all old ids with new ones
//        for (Map.Entry<String, String> entry : idMap.entrySet()) {
//        	finalDotString = finalDotString.replace(entry.getKey(), entry.getValue());
//        }
//        Pattern edgePattern = Pattern.compile("id=\"id[0-9]+\"");
//        Matcher edgeMatcher = edgePattern.matcher(finalDotString);
//
//        // Replace all matching edge IDs with an empty ID
//        while (edgeMatcher.find()) {
//            String id = edgeMatcher.group();
//            finalDotString = finalDotString.replace(id, "id=\"\"");
//        }
////        System.out.println("NEW DOT");
////        System.out.println(finalDotString);
//        
//        String[] lines = finalDotString.split("\\n");
//
//        // Use a StringBuilder to build the new dot string with sorted edges
//        StringBuilder sortedDot = new StringBuilder();
//
//        // Use a TreeSet to sort the edges by the source node ID
//        TreeSet<String> sortedEdges = new TreeSet<>();
//
//        // Flag to indicate if there are edges
//        boolean hasEdges = false;
//
//        // Iterate over each line
//        for (String line : lines) {
//            // Check if the line represents an edge
//            if (line.contains("->")) {
//                // Get the ID of the source node
//                String sourceNodeId = line.split(" -> ")[0];
//
//                // Store the line in the set, which automatically sorts the edges
//                sortedEdges.add(line);
//                hasEdges = true;
//            } else {
//                // Append non-edge lines directly to the new dot string
//                sortedDot.append(line).append("\n");
//            }
//        }
//        
//     // Remove the last newline character if present
//        if (sortedDot.length() > 0 && sortedDot.charAt(sortedDot.length() - 1) == '\n') {
//            sortedDot.setLength(sortedDot.length() - 2);
//            
//        }
//
//        // Append the sorted edges to the new dot string
//        for (String edge : sortedEdges) {
//            sortedDot.append(edge).append("\n");
//        }
//
//        
//        // Append the closing brace only if there are edges
//        if (hasEdges) {
//            sortedDot.append("}");
//        }
//
//        finalDotString = sortedDot.toString();
//        System.out.println("LAST DOT");
//        System.out.println(finalDotString);

//		this.m_dotstr = finalDotString;
		this.m_dotstr = dotstr;
		
	}
}