package org.pm4knime.node.logmanipulation.sample.knimetable;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.util.defaultnode.DefaultTableNodeModel;

/**
 * This is the model implementation of SampleLog.
 * Sample the event log by giving number or a precentage of whole size
 *
 * @author Kefang
 */
public class SampleLogTableNodeModel extends DefaultTableNodeModel {
	private static final NodeLogger logger = NodeLogger
            .getLogger(SampleLogTableNodeModel.class);
	// but actually, we can choose the overlapped sampling, or not overlapped sampling.. on it
	public static final String CFG_SAMPLING_PERFERENCE = "Use Percentage";
	// public static final String CFG_SAMPLE_NUM = "Sample Number";
	public static final String CFG_SAMPLE_PERCENTAGE = "Sampling Number";
	
	private final SettingsModelBoolean m_samplePref = createSamplePerference();
	// private static final SettingsModelInteger m_sampleNum = null;
	private final SettingsModelDoubleBounded m_samplePercentage = createSamplePercentage();
	
	static SettingsModelBoolean createSamplePerference() {
		return new SettingsModelBoolean(CFG_SAMPLING_PERFERENCE, true);
	}
	
	// make sure the value is non negative
	static SettingsModelDoubleBounded createSamplePercentage() {
		return new SettingsModelDoubleBounded(CFG_SAMPLE_PERCENTAGE, 0.3, 0, Double.MAX_VALUE );
	}
    /**
     * Constructor for the node model.
     */
    protected SampleLogTableNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { BufferedDataTable.TYPE },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: we judge the type of valid incoming and then choose the good ones for it
    	// to test the valid setting, we need to set one button to see that we choose the percentage marking
    	// one simple way is to use the doubleBounded, but with one checkbox to define it is for the integer number
    	// one is the doubleBounded, only allowing it greater than 0.
    	logger.info("Begin: Sample the Event Log");
    	// assign the log
    	BufferedDataTable log = (BufferedDataTable) inData[0];
    	BufferedDataTable[] logs;
    	

		if(m_samplePref.getBooleanValue()) {
			// if use the percentage, we need to do what ??
			double percentage = m_samplePercentage.getDoubleValue();
			logs = SampleUtil.sampleLog(log, percentage, m_variantCase.getStringValue(), exec);
			
		}else {
			// conver the double value into the number 
			int num =  (int) m_samplePercentage.getDoubleValue();
			// check here if the num is bigger than the log size
			logs = SampleUtil.sampleLog(log, num, m_variantCase.getStringValue(), exec);
		}
		checkCanceled(exec);

		
		logger.info("End : Sample the Event Log");
        return new PortObject[]{logs[0], logs[1]};
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configureOutSpec(final DataTableSpec inSpecs) {

    	DataTableSpec[] m_outSpecs = new DataTableSpec[getNrOutPorts()];
    	m_outSpecs[0] = inSpecs;
		m_outSpecs[1] = inSpecs;
		
		return new PortObjectSpec[] {m_outSpecs[0], m_outSpecs[1]};
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSpecificSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_samplePref.saveSettingsTo(settings);
    	//m_sampleNum.saveSettingsTo(settings);
    	m_samplePercentage.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSpecificValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_samplePref.loadSettingsFrom(settings);
    	//m_sampleNum.loadSettingsFrom(settings);
    	m_samplePercentage.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc} this method only validates the values but not assign the values to the parameters. 
     * Let us check another way to verify the configuration
     */
    @Override
    protected void validateSpecificSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_samplePref.validateSettings(settings);
    	
    	m_samplePercentage.validateSettings(settings);
    	
    	boolean usePercentage = settings.getBoolean(CFG_SAMPLING_PERFERENCE);
    	double value = settings.getDouble(CFG_SAMPLE_PERCENTAGE);
    	
    	if(usePercentage) {
    		// check the value if it is between 0 - .10
    		if(value > 1.0) {
    			throw new InvalidSettingsException("The current percentage " + value + 
    					"  is out of bounds. Please give value between 0 - 1.0 ");
    		}
    		
    	}else {
    		// it must be an integer value here
    		if(value == Math.floor(value) && !Double.isInfinite(value)) {
    			// this is on integer value here, also we don't allow it to be negative!! 
    		}else
    			throw new InvalidSettingsException("The current value  " + value + 
    					"  is not an integer. Please give an integer value");
    	}
    }



}

