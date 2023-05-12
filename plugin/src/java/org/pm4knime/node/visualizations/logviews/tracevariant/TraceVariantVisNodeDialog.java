package org.pm4knime.node.visualizations.logviews.tracevariant;


import java.util.Arrays;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnFilter;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerModel;


public class TraceVariantVisNodeDialog extends DefaultNodeSettingsPane {
	
	public static final String[] DEFAULT_TRACE_CLASSES = new String [] {"#Trace Attribute#concept:name", "case:concept:name", "case"};
	public static final String[] DEFAULT_EVENT_CLASSES = new String [] {"#Event Attribute#concept:name", "concept:name", "activity"};
	
	protected TraceVariantVisNodeModel node;
	protected DialogComponentColumnNameSelection event_classifierComp ;
	protected DialogComponentColumnNameSelection trace_classifierComp;	
	protected DialogComponentBoolean m_generateImageCheckBox;
	protected SettingsModelString t_classifier =  new SettingsModelString(DefaultTableMinerModel.KEY_TRACE_CLASSIFIER, null);
	protected SettingsModelString e_classifier =  new SettingsModelString(DefaultTableMinerModel.KEY_EVENT_CLASSIFIER, null);
	protected SettingsModelBoolean create_image = new SettingsModelBoolean("Create image at outport", false);
	
	public TraceVariantVisNodeDialog(TraceVariantVisNodeModel n) {
		
		node = n;
		
//		String[] classifierSet = new String[] {};
		final ColumnFilter filter = new ColumnFilter() {
            @Override
            public boolean includeColumn(final DataColumnSpec colSpec) {
//                return VariableAndDataCellUtil.isTypeCompatible(colSpec.getType());
            	  return true;
            }

            @Override
            public String allFilteredMsg() {
                return "No columns compatible";
//                    + Arrays.stream(VariableAndDataCellUtil.getSupportedVariableTypes())
//                        .map(t -> t.getIdentifier().toLowerCase()).collect(Collectors.joining(", "))
//                    + ".";
            }
        };
		trace_classifierComp = new DialogComponentColumnNameSelection(t_classifier,
				"Trace Classifier", 0, filter);
	    addDialogComponent(trace_classifierComp);
		
		event_classifierComp = new DialogComponentColumnNameSelection(e_classifier,
				"Event Classifier", 0, filter);
	    addDialogComponent(event_classifierComp);
	    m_generateImageCheckBox = new DialogComponentBoolean(create_image, "Create image at outport");
	    addDialogComponent(m_generateImageCheckBox);
	}

	
	
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
 
    	DataTableSpec logSpec = (DataTableSpec) specs[0];
		String[] currentClasses = logSpec.getColumnNames();
		if (currentClasses.length == 0) {
			throw new NotConfigurableException("Please make sure the connected table is in excution state");
		} else {
			try {
				this.node.loadValidatedSettingsFrom(settings);
	        } catch (InvalidSettingsException e) {
	        	System.out.println("Exception:" + e.toString());
	        }
			String tClassifier = getDefaultTraceClassifier(currentClasses, this.node.t_classifier);
			String eClassifier = getDefaultEventClassifier(currentClasses, this.node.e_classifier);
			t_classifier.setStringValue(tClassifier);
			e_classifier.setStringValue(eClassifier);
			this.node.setTraceClassifier(tClassifier);
			this.node.setEventClassifier(eClassifier);
			this.create_image.setBooleanValue(this.node.generate_image);
		}	
    }

    
	private String getDefaultTraceClassifier(String[] currentClasses, String oldValue) {		
		String res = currentClasses[0];
		for (String s: currentClasses) {
			if (s.equals(oldValue)) {
				return oldValue;
			}
			if (Arrays.asList(DEFAULT_TRACE_CLASSES).contains(s)) {
				res = s;
			}
		}
		return res;
	}
    
	
    private String getDefaultEventClassifier(String[] currentClasses, String oldValue) {		
    	String res;
    	if (currentClasses.length > 1) {
			res = currentClasses[1];
		} else {
			res = currentClasses[0];
		}
    	for (String s: currentClasses) {
    		if (s.equals(oldValue)) {
				return oldValue;
			}
			if (Arrays.asList(DEFAULT_EVENT_CLASSES).contains(s)) {
				res = s;
			}
		}   	
    	return res;
	}


	@Override
	 public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
	            throws InvalidSettingsException {
	    this.node.t_classifier = this.t_classifier.getStringValue();
		this.node.e_classifier = this.e_classifier.getStringValue();
		this.node.generate_image = this.create_image.getBooleanValue();
		this.node.saveSettingsTo(settings);	    
	}
}
