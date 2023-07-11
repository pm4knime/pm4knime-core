package org.pm4knime.node.conversion.table2pn;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import java.io.File;
import java.io.IOException;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.js.core.node.AbstractSVGWizardNodeModel;
import org.pm4knime.node.conversion.pn2table.PetriNetCell;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.AbstractDotPanelPortObject;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.util.PetriNetUtil;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.plugins.graphviz.dot.Dot;


class Table2PetriNetConverterNodeModel extends AbstractSVGWizardNodeModel<JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {
	
//	private SettingsModelString m_pnColSettingsModel =
//			Table2PetriNetConverterNodeDialog.getPetriNetColumnSettingsModel();

	protected PortObject pnPO;
	protected BufferedDataTable inTable;
    public Table2PetriNetConverterNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE},
                new PortType[]{PetriNetPortObject.TYPE},
                "Petri Net JS View");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpec inSpec = (DataTableSpec)inSpecs[0];

        String column = null;
        int columnIndex = inSpec.findColumnIndex(column);
        if (columnIndex < 0) {
            columnIndex = findPetriNetColumnIndex(inSpec);
            if (columnIndex >= 0) {
//                setWarningMessage("Found Petri net column '" + inSpec.getColumnSpec(columnIndex).getName() + "'.");
            }
        }

        if (columnIndex < 0) {
            String error = column == null ? "No Petri net column in input"
                : "No such Petri net column in input table: " + column;
            throw new InvalidSettingsException(error);
        }
        DataColumnSpec columnSpec = inSpec.getColumnSpec(columnIndex);
        if (!columnSpec.getType().getCellClass().equals(PetriNetCell.class)) {
            throw new InvalidSettingsException("Column \"" + column + "\" does not contain Petri nets");
        }

        return new PortObjectSpec[]{new PetriNetPortObjectSpec() };
    }


    @Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        return new PortObject[]{pnPO};
    }
	
	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		
		inTable = (BufferedDataTable)inObjects[0];
        // check for empty table
        if (inTable.size() == 0) {
            throw new IllegalArgumentException("Input table is empty.");
        }
        // warn if more than one row
        if (inTable.size() > 1) {
            setWarningMessage("Input data table has more than one rows! "
                    + "Using first row only.");
        }

        String column = null;
        DataTableSpec inSpec = inTable.getDataTableSpec();
        int columnIndex = inSpec.findColumnIndex(column);
        if (columnIndex < 0) {
            columnIndex = findPetriNetColumnIndex(inSpec);
        }

        final RowIterator it = inTable.iterator();
        while (it.hasNext()) {
            DataRow row = it.next();
            DataCell cell = row.getCell(columnIndex);
            if (!cell.isMissing()) {
                String stringPN = ((PetriNetCell)cell).getStringValue();

                AcceptingPetriNet pn = PetriNetUtil.stringToPetriNet(stringPN);
                pnPO = new PetriNetPortObject(pn);
                final String dotstr;
        		JSGraphVizViewRepresentation representation = getViewRepresentation();

        		synchronized (getLock()) {
        			AbstractDotPanelPortObject port_obj = (AbstractDotPanelPortObject) pnPO;
        			Dot dot =  port_obj.getDotPanel().getDot();
        			dotstr = dot.toString();
        		}
        		representation.setDotstr(dotstr);
        		return;

            } else {
                setWarningMessage("Found missing Petri net cell, skipping it...");
            }
        }
        throw new IllegalArgumentException(
                "Input table contains only missing cells.");
        		
	}    


	private static int findPetriNetColumnIndex(final DataTableSpec spec) {
        for (int i = 0; i < spec.getNumColumns(); i++) {     
//        	System.out.println(spec.getColumnSpec(i).getType());
            if (spec.getColumnSpec(i).getType().getCellClass().equals(PetriNetCell.class))
            {
            	return i;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
//    	m_pnColSettingsModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
//    	m_pnColSettingsModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
//    	m_pnColSettingsModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // Nothing to do ...
    }
    
    
    @Override
	public PortObject[] getInternalPortObjects() {
		// TODO Auto-generated method stub
		return new PortObject[] {inTable};
	}


	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		inTable = (BufferedDataTable) portObjects[0];
		
	}
    
    @Override
	protected void performReset() {
	}

	@Override
	protected void useCurrentValueAsDefault() {
	}

	
	@Override
    protected boolean generateImage() {
        return false;
    }
	
	
	@Override
	public JSGraphVizViewRepresentation createEmptyViewRepresentation() {
		return new JSGraphVizViewRepresentation();
	}

	@Override
	public JSGraphVizViewValue createEmptyViewValue() {
		return new JSGraphVizViewValue();
	}
	
	@Override
	public boolean isHideInWizard() {
		return false;
	}

	@Override
	public void setHideInWizard(boolean hide) {
	}

	@Override
	public ValidationError validateViewValue(JSGraphVizViewValue viewContent) {
		return null;
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
	}
	
	@Override
	public String getJavascriptObjectID() {
		return "org.pm4knime.node.visualizations.jsgraphviz.component";
	}
    
}
