package org.pm4knime.node.logmanipulation.classify;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObjectSpec;

/**
 * <code>NodeDialog</code> for the "RandomClassifier" Node.
 * RandomClassifier classifies the event log randomly, and assigns labels to the trace
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 *  
 * @Date: Modification on this node. To allow the panel classify. Not just like this. 
 * -- Add one imported panel to it. 
 * -- needs the event logs as inputs 
 * -- no settings are in need
 * 
 * @Date: 27/11/2019
 * -- define the label name and values in the dialog 
 * -- automatically generate the edit options for it to add more values there. 
 * -- one value, one percentage, editor on it.  But make sure the sum is 1.0 
 * 
 * Due to the later use, we will create one config to save all the values there. 
 * @author Kefang Ding
 */
public class RandomClassifierNodeDialog extends DefaultNodeSettingsPane {
	private final double THRESHOLD = .0001;
	private JPanel m_mainPanel ;
	LabelTablePanel tPanel;
	DefaultTableModel tModel;
	// there is another optional panel
	JTextField nameField;
	ClassifyConfig m_config; 
	
	public RandomClassifierNodeDialog() {
		Border blackline  = BorderFactory.createLineBorder(Color.black);
		
		m_mainPanel = (JPanel) this.getTab("Options"); //new JPanel();
		m_mainPanel.setLayout(new BoxLayout(m_mainPanel, BoxLayout.Y_AXIS));
		m_mainPanel.setBorder(blackline);
		
		m_config = new ClassifyConfig(RandomClassifierNodeModel.CFG_KEY_CONFIG);
		
		// create a box to for the label name
		Box nameBox =  new Box(BoxLayout.X_AXIS);
		nameBox.setBorder(blackline);
		// add one label for it
		JLabel nameLabel = new JLabel("Label Name: ");
		nameBox.add(nameLabel);
		
		nameField = new JTextField(10);
		nameField.setMaximumSize(nameField.getPreferredSize() );
		nameBox.add(nameField);
		
		// create add attributes button and remove one attribute botton there
		JButton addValueBtn = new JButton("Add label value");
		addValueBtn.setOpaque(true);
		nameBox.add(addValueBtn);
		
		
		m_mainPanel.add(nameBox);
		// create a big box to contain the values here but allows to have boader
		tPanel = new LabelTablePanel();
		tModel = tPanel.getTableModel();
		m_mainPanel.add(tPanel);
		// after this, when the values changes, how to modify the value there, until the last step!! 
		// There is one button to add more values there, everytime, when we create one values
		// it shows two labels and value text field there..
		// there is no need to do this,
		addValueBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent addEvent) {
				// TODO here to add data to the Panel there
				// before adding a new value, we need to calculate the possible values 
				// left there. If it is the first one, we assign it as the 1.0
//				if(nameField.getText().isEmpty()) {
					// we force the label name not empty to add its value 
					// throw one dialog to make sure values are assigned there..
					// not so necessary at end, we have configuration 
//				}
				
				tModel.addRow(new Object[] {"", "", "Delete"});
				m_mainPanel.revalidate();
				m_mainPanel.repaint();
			}
			
		});
	}
	
	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
		// TODO assign the config from components
		if(tPanel.getTable().isEditing()){
			tPanel.getTable().getCellEditor().stopCellEditing();
		}
		
		
		m_config.setLabelName(nameField.getText());
		// secondly assign the values to m_config from the table
		int rCount = tModel.getRowCount();
		double sum = 0; // one bug here about the value in sum
		for(int row = 0; row < rCount; row++) {
			String labelValue = tModel.getValueAt(row, 0).toString();
			
			String percentStr = tModel.getValueAt(row, 1).toString();
			if(labelValue.length()<1 || percentStr.length() < 1)
				throw new InvalidSettingsException("Label Value or percent can't be empty!!");
						
			double percent  = Double.parseDouble(percentStr);
			
			if(percent < 0 || percent > 1)
				throw new InvalidSettingsException("Percent is in 0.0 - 1.0!!");
			if(sum + percent > 1.0)
				throw new InvalidSettingsException("Sum is over 1.0, reassign value again");
			sum += percent;
			m_config.addData(labelValue, percent);
			
		}
		
		// do we need to check the total sum of percent over 1.0?? 
		// if it is under 1.0, we assign all the rest value to the last label?
		// we give also warning about this to remind the sum should be 1.0
		if(Math.abs(1 - sum) > THRESHOLD) {
			throw new InvalidSettingsException("Sum is under 1.0, reassign value again");
		}
		
		m_config.saveSettingsTo(settings);
	}
	
	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
		// to load the values from the settings
		try {
			m_config.loadValidatedSettingsFrom(settings);
			nameField.setText(m_config.getLabelName());
			// after this, get the table and assign the values there
			if(tModel.getDataVector().isEmpty()) {
				for(Entry e : m_config.getValueMap().entrySet()) {
					tModel.addRow(new Object[] {e.getKey(), e.getValue(), "Delete"});
				}
			}
			
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}

