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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;


public class PetriNet2TableConverterNodeModel extends NodeModel {
	
	private static final NodeLogger logger = NodeLogger.getLogger(PetriNet2TableConverterNodeModel.class);
	
	RowKey DEFAULT_ROWKEY = RowKey.createRowKey(0);
    SettingsModelString m_rowKeyModel = new SettingsModelString("generated_rowkey", DEFAULT_ROWKEY.toString());

    String DEFAULT_COLUMN_LABLE = "Petri Net";
	SettingsModelString m_columnNameModel = new SettingsModelString("columnName", DEFAULT_COLUMN_LABLE);
	
	static String CFG_TABLE_NAME = "Converted Data Table from Petri Net";

	
	
	private PetriNetPortObjectSpec m_inSpec;
//	ImageToTableNodeFactory ifac;
//	ImageToTableNodeModel iMod;

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
    	
    	RowKey rowKey;
        String rowKeyValue = m_rowKeyModel.getStringValue();
        if (rowKeyValue == null || rowKeyValue.trim().isEmpty()) {
            rowKey = DEFAULT_ROWKEY;
        } else {
            rowKey = new RowKey(rowKeyValue);
        }
    	
    	String pnText = PN2XmlConverter.convert(anet, bufCon, exec);
    	
    	DataRow eventRow = new DefaultRow(rowKey, createDataCell(pnText));
//    	DataRow eventRow = new DefaultRow(rowKey, StringCellFactory.create(imageText));
    	
    	bufCon.addRowToTable(eventRow);
  
    	bufCon.close();
    	logger.info("End : Convert Petri Net to DataTable" );
        return new BufferedDataTable[]{bufCon.getTable()};      
        
    }
	
	public DataCell createDataCell(String pnText) {
		try {
			return new PetriNetCell(pnText);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
	}

	private DataTableSpec createSpec() {
		
		List<String> attrNames = new ArrayList();
		List<DataType> attrTypes = new ArrayList();
		
		attrNames.add(m_columnNameModel.getStringValue());
	
		attrTypes.add(PetriNetCell.TYPE);
		
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
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_rowKeyModel.saveSettingsTo(settings);
        m_columnNameModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_rowKeyModel.validateSettings(settings);
        if (settings.containsKey(m_columnNameModel.getKey())) {
          //introduced in KNIME 2.10
            final String colName =
                    ((SettingsModelString)m_columnNameModel.createCloneWithValidatedValue(settings)).getStringValue();
            if (colName == null || colName.trim().isEmpty()) {
                throw new InvalidSettingsException("Please specify a column name.");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_rowKeyModel.loadSettingsFrom(settings);
        if (settings.containsKey(m_columnNameModel.getKey())) {
            //introduced in KNIME 2.10
            m_columnNameModel.loadSettingsFrom(settings);
        }
    }

	@Override
	protected void reset() {
		// TODO Auto-generated method stub
		
	}

}
