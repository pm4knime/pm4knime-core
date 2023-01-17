package org.pm4knime.node.visualizations.logviews.tracevariant;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TraceVariantVisViewRepresentation extends JSONViewContent {

	private String numrows;
	private static final String NUM_ROWS = "numrows";

	@Override
	public void saveToNodeSettings(NodeSettingsWO settings) {
		try {
			settings.addString(NUM_ROWS, numrows);
	    } catch (Exception ex) {
	        // do nothing
	    }
	}

	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		try {
			numrows = settings.getString(NUM_ROWS);
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
		TraceVariantVisViewRepresentation other = (TraceVariantVisViewRepresentation)obj;
		return new EqualsBuilder()
				.append(numrows, other.numrows)
                .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(numrows)
				 .toHashCode();
	}

	public void setNumberRows(String size) {
		numrows = size;
	}
	
	public String getNumberRows() {
		return numrows;
	}
}