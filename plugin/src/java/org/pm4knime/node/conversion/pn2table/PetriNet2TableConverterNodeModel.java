package org.pm4knime.node.conversion.pn2table;

import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pm4knime.util.PetriNetUtil;
import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.conversion.log2table.FromXLogConverter;
import org.pm4knime.node.conversion.log2table.XLog2TableConverterNodeModel;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.knime.core.node.tableview.TableView;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.def.StringCell.StringCellFactory;


public class PetriNet2TableConverterNodeModel extends NodeModel {
	
	private static final NodeLogger logger = NodeLogger.getLogger(PetriNet2TableConverterNodeModel.class);
	
	static final String CFG_TABLE_NAME = "Converted Data Table from Petri Net";
	
	private PetriNetPortObjectSpec m_inSpec;

	protected PetriNet2TableConverterNodeModel() {
		super( new PortType[]{PetriNetPortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE});
		// TODO Auto-generated constructor stub
	}
	
	@Override
    protected BufferedDataTable[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Start : Convert Petri Net to DataTable" );
    	PetriNetPortObject pnPortObject = null ;
    	for(PortObject obj: inData)
        	if(obj instanceof PetriNetPortObject) {
        		pnPortObject = (PetriNetPortObject)obj;
        		break;
        	}
    	
    	AcceptingPetriNet anet = pnPortObject.getANet();
    	
    	DataTableSpec outSpec = createSpec();
    	
    	BufferedDataContainer bufCon = exec.createDataContainer(outSpec);
    	
    	String text = PN2XmlConverter.convert(anet, bufCon, exec);
    	
    	DataRow eventRow = new DefaultRow("Row 0", StringCellFactory.create(text));
    	
    	bufCon.addRowToTable(eventRow);
  
    	bufCon.close();
    	logger.info("End : Convert Petri Net to DataTable" );
        return new BufferedDataTable[]{bufCon.getTable()};
    }
	
	private DataTableSpec createSpec() {
		
		List<String> attrNames = new ArrayList();
		List<DataType> attrTypes = new ArrayList();
		
		attrNames.add("Petri Net(serialized)");
	
		attrTypes.add(StringCell.TYPE);
		
		DataTableSpec outSpec = new DataTableSpec(CFG_TABLE_NAME, 
				attrNames.toArray(new String[0]), attrTypes.toArray(new DataType[0]));
		
    	return outSpec;
		
	}
	
	@Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        // TODO: create a new DataTable there
		PetriNetPortObjectSpec spec = (PetriNetPortObjectSpec) inSpecs[0];

    	if(!spec.getClass().equals(PetriNetPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Petri Net!");
    	
//    	if( spec.getGTraceAttrMap().isEmpty()|| spec.getClassifiersMap().isEmpty()) {
//    		throw new InvalidSettingsException("Log Spec Object is Empty. Probably because the reader node got reset");
//    	}
   	
    	m_inSpec = spec;
    	
        return new PortObjectSpec[]{null};
    }

	
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub
		
	}

}
