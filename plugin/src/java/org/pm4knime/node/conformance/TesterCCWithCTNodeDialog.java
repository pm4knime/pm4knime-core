package org.pm4knime.node.conformance;

import java.awt.Dimension;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.settingsmodel.SMAlignmentReplayParameter;
import org.pm4knime.settingsmodel.SMAlignmentReplayParameterWithCT;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.XLogUtil;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;

/**
 * <code>NodeDialog</code> for the "TesterCCWithCT" node.
 * 
 * @author Kefang Ding
 */
public class TesterCCWithCTNodeDialog  extends DataAwareNodeDialogPane{
	
	JPanel m_compositePanel;
	SMAlignmentReplayParameterWithCT m_parameter;
	final String[] strategyList = TesterCCNodeModel.strategyList;
    /**
     * New pane for configuring the TesterCC node.
     */
    protected TesterCCWithCTNodeDialog() {
    	
    	m_compositePanel = new JPanel();
        m_compositePanel.setLayout(new BoxLayout(m_compositePanel,
                BoxLayout.Y_AXIS));
    	
    	// we are going to use the customized SettingsModel for the node
    	// how to import and output the fields from the model?? 
    	// how to create the related dialog component? 
    	m_parameter  = new SMAlignmentReplayParameterWithCT("Parameter in Tester with CT");
    	// we need to assign the classifier names tehre 
    	List<String> classifierNames  =  TesterCCNodeDialog.getECNames(TesterCCNodeModel.classifierList);
    	DialogComponentStringSelection m_classifierComp = new DialogComponentStringSelection(
    			m_parameter.getMClassifierName(), "Select Classifier Name", classifierNames );
    	addDialogComponent(m_classifierComp);
    	
    	DialogComponentStringSelection m_strategyComp = new DialogComponentStringSelection(
    			m_parameter.getMStrategy(), "Select Replay Strategy", strategyList);
    	addDialogComponent(m_strategyComp);
    	
    	
    	Box cbox = new Box(BoxLayout.X_AXIS);
    	DialogComponentNumberEdit[] defaultCostComps = new DialogComponentNumberEdit[SMAlignmentReplayParameter.CFG_COST_TYPE_NUM];
    	for( int i=0; i< SMAlignmentReplayParameter.CFG_COST_TYPE_NUM ; i++) {
    		defaultCostComps[i] = new DialogComponentNumberEdit(m_parameter.getMDefaultCosts()[i], 
    				SMAlignmentReplayParameter.CFG_MCOST_KEY[i] ,5);
    		cbox.add(defaultCostComps[i].getComponentPanel());
    	}
    	m_compositePanel.add(cbox);
    	// here we need to change the direction and make it vertically
    	Box tbox = new Box(BoxLayout.Y_AXIS);
    	for( int i=0; i< SMAlignmentReplayParameterWithCT.CFG_COST_TYPE_NUM ; i++) {
    		tbox.add(createTable(i));
		}
    	
    	m_compositePanel.add(tbox);
    	
    	super.addTab("Options", m_compositePanel);
    }
    
    private void addDialogComponent(final DialogComponent diaC) {
		// TODO Auto-generated method stub
    	m_compositePanel.add(diaC.getComponentPanel());
	}

    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObject[] input) throws NotConfigurableException {
    	// not know the situation of m_parameter.loadSettingsFrom, what can we get?? 
    	// what's the values now in settings?? how to cooperate the values from PortObjectInput and current settings?
    	
    	// 1. settings are from the saved workflow, we can get the settings there
    	// 2. settings is empty, so we don't touch it 
    	// check the values in settings
    	
    	try {
			m_parameter.loadSettingsFrom(settings);
			
		} catch (InvalidSettingsException e) {
			// TODO if there is not with TM, we set it from the input PortObject
			if(!m_parameter.isMWithTM()) {
				if (!(input[TesterCCWithCTNodeModel.INPORT_LOG] instanceof XLogPortObject))
					throw new NotConfigurableException("Input is not a valid event log!");

				if (!(input[TesterCCWithCTNodeModel.INPORT_PETRINET] instanceof PetriNetPortObject))
					throw new NotConfigurableException("Input is not a valid Petri net!");
				
				XLogPortObject logPO = (XLogPortObject) input[TesterCCWithCTNodeModel.INPORT_LOG];
				PetriNetPortObject netPO = (PetriNetPortObject) input[TesterCCWithCTNodeModel.INPORT_PETRINET];
				
				XLog log = logPO.getLog();
				// TODO: different classifier available
				XEventClassifier eventClassifier = new XEventNameClassifier();
				
				// here, no need to show the dummy event class name here
//				XEventClass evClassDummy = TesterCCWithCTNodeModel.getDummyEC();
				List<String> ecNames = XLogUtil.extractAndSortECNames(log, eventClassifier);
//				ecNames.add(evClassDummy.getId());
				m_parameter.setCostTM(ecNames, 0);
				
				AcceptingPetriNet anet = netPO.getANet();
				List<String> tNames = PetriNetUtil.extractTransitionNames(anet.getNet());
				m_parameter.setCostTM(tNames, 1);
				
				// if only names from transition side are in need, show the transitions names for dialog
				// no need to refer dummy event classes
				m_parameter.setCostTM(tNames, 2);	
				m_parameter.setMWithTM(true);
			}
		}finally {
			m_compositePanel.repaint();
		}
    	
    }
    

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_parameter.saveSettingsTo(settings);
	}

	
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			m_parameter.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * create a cost Table given input: -- default cost Value for all rows -- column
	 * names -- costTable passed through the NodeModel and JTable
	 */
	private JScrollPane createTable(int idx) {
		DefaultTableModel costTM = m_parameter.getMCostTMs()[idx];
		
		JTable table = new JTable(costTM) {
			@Override
			public Dimension getPreferredScrollableViewportSize() {
				return new Dimension(super.getPreferredSize().width, getRowHeight() * getRowCount());
			}
		};

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		JScrollPane tPane = new JScrollPane(table);
		if (idx < 1)
			tPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		else
			tPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		return tPane;
	}

}
