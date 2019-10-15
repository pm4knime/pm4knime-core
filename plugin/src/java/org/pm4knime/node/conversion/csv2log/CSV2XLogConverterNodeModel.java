package org.pm4knime.node.conversion.csv2log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;

/**
 * <code>NodeModel</code> for the "CVS2XLogConverter" node. This node is coded without ProM plugin. 
 * The reasons are : 
 *    1. for a universal use of cvs files to read from and manipulated. 
 *    2. it is simpler than creating another PortObject of CVSFile, and save it here. 
 *       simpler than reading directly from a cvs file and convert it to an event log. 
 *       [In this direct way, we can't create an unified workflow in a loop]
 * 
 * Input: CVS in DataTable format
 * Output: XLogPortObject
 * Parameters: a lot ! But in two kinds:
 *    1. to assign the caseId and eventID, timestamp according to the Spec from the cvs
 *    2. to choose the storage format of XLogPortObject
 * 
 * For later use, if we want to convert event log into cvs file, and then store them, what to do then?? 
 * By using the prom Plugin, we have the exporter and importer available. And then after this, we get the 
 * @author Kefang Ding
 */
public class CSV2XLogConverterNodeModel extends NodeModel {
    
	
	// here we have optional item, but now just this two
	static CSV2XLogConfigModel m_config =  new CSV2XLogConfigModel();
	XLogPortObject logPO;
    /**
     * Constructor for the node model.
     */
    protected CSV2XLogConverterNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{XLogPortObject.TYPE});
    }

    /**
     * they must portOBject to PortObject, else not working.
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: accept input DataTable, use the configuration columnNames, 
    	// convert the data into XLog
    	// how to check the type for this?
    	BufferedDataTable csvData = (BufferedDataTable) inData[0];
    	
    	// sort the table w.r.t. caseID column
    	List<String> m_inclList = new ArrayList<String>();
    	m_inclList.add(m_config.getMCaseID().getStringValue());
    	// here we might need to make sure they mean this
    	boolean[] m_sortOrder = {true};
    	boolean m_missingToEnd = false;
    	boolean m_sortInMemory = false;
    	BufferedDataTableSorter sorter = new BufferedDataTableSorter(
    			csvData, m_inclList, m_sortOrder, m_missingToEnd);
    	
        sorter.setSortInMemory(m_sortInMemory);
        BufferedDataTable sortedTable = sorter.sort(exec);
    	
    	// convert the string to date and sort them according to caseID? So we can read them easier for rows
    	// it creates the corresponding column spec and create another DataTable for it.
    	// one thing to remember, it is not so important to have order of timestamp. 
    	ToXLogConverter handler = new ToXLogConverter();
    	handler.setConfig(m_config);
    	
    	handler.convertCVS2Log(sortedTable);
    	XLog log = handler.getXLog();
    	logPO = new XLogPortObject(log);
    	
        return new PortObject[]{logPO};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	
        // Input PortObject is a DataTableSpec, output Spec is XLogPortObject
    	if(!inSpecs[0].getClass().equals(DataTableSpec.class)) 
    		throw new InvalidSettingsException("Input is not a CSV File!");
    	
    	// return new PortObjectSpec[]{inSpecs[0]};
    	return new PortObjectSpec[]{new XLogPortObjectSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	// we can add additional info into settings
    	// settings.addStringArray("Column Names", m_possibleColumns);
    	m_config.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	
    	// in this way, we get the values here, but do we need those values ??
    	// not necessary if we have m_caseID and m_eventID already
    	// m_possibleColumns = settings.getStringArray("Column Names");
    	
    	m_config.loadSettings(settings);
    	
    	System.out.println(m_config.getErrorHandlingMode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

	public XLogPortObject getLogPO() {
		// TODO Auto-generated method stub
		return logPO;
	}

}

