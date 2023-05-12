package org.pm4knime.node.visualizations.logviews.tracevariant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;
import org.pm4knime.util.defaultnode.TraceVariant;
import org.pm4knime.util.defaultnode.TraceVariantRepresentation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TraceVariantVisViewRepresentation extends JSONViewContent {
	
	String[] data;
	String DATA = "data";
	String delimiter = "#;#";
	
	TraceVariantRepresentation variants;
	String VARIANTS = "variants";

	@Override
	public void saveToNodeSettings(NodeSettingsWO settings) {
		try {
			settings.addString(DATA, String.join(delimiter, data));
			saveVariantsToNodeSettings(settings);
	    } catch (Exception ex) {
	        // do nothing
	    }
	}

	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		try {
//			System.out.println("LOADING");
			data = settings.getString(DATA).split(delimiter);
//			System.out.println("DATA LOADED:");
//			System.out.println(data.toString());
			loadVariantsFromNodeSettings(settings);
	    } catch (Exception ex) {
	        System.out.println(ex.toString());
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
                .isEquals() && new EqualsBuilder()
				.append(variants, other.variants)
                .isEquals();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder b = new HashCodeBuilder();
		b.append(data);
		b.append(variants);
		return b.toHashCode();
	}

	public void setData(String[] size) {
		data = size;
	}
	
	public String[] getData() {
		return data;
	}

	public void setVariants(TraceVariantRepresentation varinats) {
		variants = varinats;
	}
	
	public TraceVariantRepresentation getVariants() {
		return variants;
	}
	
	public void saveVariantsToNodeSettings(NodeSettingsWO settings) {
		settings.addInt("numberOfTraces", variants.numberOfTraces);
		
		for (int i = 0; i < variants.getActivities().size(); i++){
			settings.addString("SetOfActivities", String.join(delimiter, variants.getActivities()));
		}
		
		settings.addInt("numberOfVariants", variants.variants.size());
        for (int i = 0; i < variants.variants.size(); i++){
			settings.addString("Trace"+i, String.join(delimiter, variants.variants.get(i).getActivities()));
			settings.addInt("Freq"+i, variants.variants.get(i).getFrequency());
		}	
		
	}


	public void loadVariantsFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		ArrayList<TraceVariant> tracevariants = new ArrayList<TraceVariant>();
		int numberOfTraces = settings.getInt("numberOfTraces");
		
		HashSet<String> activities = new HashSet<String>(Arrays.asList(settings.getString("SetOfActivities").split(delimiter)));
//		System.out.println("Activities LOADED:");
//		System.out.println(activities.toString());
		int numberOfVariants = settings.getInt("numberOfVariants");
		for (int i = 0; i < numberOfVariants; i++){
//			System.out.println(settings.getString("Trace"+i));
			ArrayList<String> trace = new ArrayList<>(Arrays.asList(settings.getString("Trace" + i).split(delimiter)));
//            System.out.println(trace.toString());
			TraceVariant variant = new TraceVariant(trace, settings.getInt("Freq"+i));
			tracevariants.add(variant);
		}	
//		System.out.println("Activity Sequences LOADED:");
//		System.out.println(tracevariants.toString());
		
		this.variants = new TraceVariantRepresentation(numberOfTraces, activities, tracevariants);
//		System.out.println("TVR LOADED:");
//		System.out.println(this.variants.toString());
		
	}
}