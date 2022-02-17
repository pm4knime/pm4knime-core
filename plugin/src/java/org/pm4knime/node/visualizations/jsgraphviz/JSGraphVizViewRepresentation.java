package org.pm4knime.node.visualizations.jsgraphviz;

import java.util.Random;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.kitfox.svg.SVGDiagram;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JSGraphVizViewRepresentation extends JSONViewContent {

	public final int pseudoIdentifier = (new Random()).nextInt();
	
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
		this.m_dotstr = dotstr;
	}
}