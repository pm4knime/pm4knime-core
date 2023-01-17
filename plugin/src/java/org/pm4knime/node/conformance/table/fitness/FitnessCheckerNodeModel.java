package org.pm4knime.node.conformance.table.fitness;

import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.RepResultPortObjectSpecTable;
import org.pm4knime.portobject.RepResultPortObjectTable;
import org.pm4knime.util.ReplayerUtil;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
/**
 * <code>NodeModel</code> for the "ConformanceChecker" node.
 *
 * @author 
 */
public class FitnessCheckerNodeModel extends DefaultNodeModel implements PortObjectHolder {
	private static final NodeLogger logger = NodeLogger.getLogger(FitnessCheckerNodeModel.class);
	
	private DataTableSpec m_tSpec;
	RepResultPortObjectTable repResultPO;
	
	// it seems there is no parameters to add in this result shown...
    /**
     * Constructor for the node model.
     */
    protected FitnessCheckerNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { RepResultPortObjectTable.TYPE}, new PortType[] {BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Start: Unified PNReplayer Conformance Checking");
    	repResultPO = (RepResultPortObjectTable) inData[0];
// check cancellation of node before sync
    	checkCanceled(null, exec);
    	// make the transitions in replay result and transitions corresponding!!
    	ReplayerUtil.adjustRepResult(repResultPO.getRepResult(), repResultPO.getNet());
    	
    	BufferedDataContainer buf = exec.createDataContainer(m_tSpec);
    	// one warning here, if we could get the fitness information , or not.
    	// if the types are changed from this step, then exceptions happen.
// check cancellation of node before writing
    	checkCanceled(null, exec);
    	Map<String, Object> info = repResultPO.getRepResult().getInfo();
    	int i=0;
    	for(String key : info.keySet()) {
    		
    		Object origValue = info.get(key);
    		
    		if(!(origValue instanceof Double))
    			continue;
    		
    		Double value = (Double) origValue;
    		
    		DataCell[] currentRow = new DataCell[2];
    		currentRow[0] = new StringCell(key);
    		currentRow[1] = new DoubleCell(value);
    		buf.addRowToTable(new DefaultRow(i+"", currentRow));
    		i++;
    	}
    	buf.close();
    	BufferedDataTable bt = buf.getTable();
    	
// check cancellation of node before closing
    	checkCanceled(null, exec);
        return new PortObject[]{bt};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	if (!inSpecs[0].getClass().equals(RepResultPortObjectSpecTable.class))
			throw new InvalidSettingsException("Input is not a valid replay result!");

    	// here we should give its spec for statistics info
		DataColumnSpec[] cSpec = new DataColumnSpec[2];
    	cSpec[0] = new DataColumnSpecCreator("Type", StringCell.TYPE).createSpec();
    	cSpec[1] = new DataColumnSpecCreator("Value", DoubleCell.TYPE).createSpec();
    	
    	m_tSpec = new DataTableSpec("Fitness Statistic", cSpec);
		
        return new PortObjectSpec[]{m_tSpec};
    }

    public RepResultPortObjectTable getRepResultPO() {
		// TODO Auto-generated method stub
		return repResultPO;
	}
	
	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		repResultPO = (RepResultPortObjectTable)portObjects[0];
	}

	@Override
	public PortObject[] getInternalPortObjects() {
		// TODO Auto-generated method stub
		return new PortObject[] {repResultPO};
	}


}

