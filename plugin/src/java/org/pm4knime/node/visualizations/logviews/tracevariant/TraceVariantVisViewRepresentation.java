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
	
	String[] data;
	String DATA = "data";
	String delimiter = "#;#";

	@Override
	public void saveToNodeSettings(NodeSettingsWO settings) {
		try {
			settings.addString(DATA, String.join(delimiter, data));
	    } catch (Exception ex) {
	        // do nothing
	    }
	}

	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		try {
			data = settings.getString(DATA).split(delimiter);
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
				.append(data, other.data)
                .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(data)
				 .toHashCode();
	}

	public void setData(String[] size) {
		data = size;
	}
	
	public String[] getData() {
		return data;
	}
}