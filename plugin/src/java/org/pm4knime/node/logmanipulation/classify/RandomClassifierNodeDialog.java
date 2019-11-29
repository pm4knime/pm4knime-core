package org.pm4knime.node.logmanipulation.classify;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObject;
import org.pm4knime.portobject.XLogPortObject;

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
public class RandomClassifierNodeDialog extends DataAwareNodeDialogPane {

	private final JPanel m_mainPanel;
	// there is another optional panel
	JTextField nameField;
	ClassifyConfig m_config; 
	
	public RandomClassifierNodeDialog() {
		Border blackline  = BorderFactory.createLineBorder(Color.black);
		
		m_mainPanel = new JPanel();
		m_mainPanel.setLayout(new BoxLayout(m_mainPanel, BoxLayout.Y_AXIS));
		m_mainPanel.setBorder(blackline);
		
		m_config = new ClassifyConfig(RandomClassifierNodeModel.CFG_KEY_CONFIG);
		// create a separate component without connection to SettingsModel
		// list them as in a box 
		
		Box nameBox = createBoxComp(10, false, ClassifyConfig.CFG_LABEL_NAME);
		
		// create add attributes button and remove one attribute botton there
		JButton addValueBtn = new JButton("Add label value");
		nameBox.add(addValueBtn);
		m_mainPanel.add(nameBox);
		nameBox.setBorder(blackline);
		// create a big box to contain the values here but allows to have boader
		TitledBorder tBorder;
		tBorder = BorderFactory.createTitledBorder("Add label values and percent");
		// create columns for it with the two box listed there 
		Box vBox =  new Box(BoxLayout.Y_AXIS);
		vBox.setBorder(tBorder);	
		Box hBox = createHeaderBox(ClassifyConfig.CFG_LABEL_VALUES, ClassifyConfig.CFG_LABEL_VALUE_PERCENTAGES, "Edit");
		vBox.add(hBox);
		
		m_mainPanel.add(vBox);
		// after this, when the values changes, how to modify the value there, until the last step!! 
		// There is one button to add more values there, everytime, when we create one values
		// it shows two labels and value text field there..
		addValueBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent addEvent) {
				// TODO add the create Box component to the mainPanel
				Box valueBox = createBoxComp(10, true, ClassifyConfig.CFG_LABEL_VALUES, ClassifyConfig.CFG_LABEL_VALUE_PERCENTAGES);
				vBox.add(valueBox);
				
				m_mainPanel.revalidate();
				m_mainPanel.repaint();
				
			}
			
		});
		
		this.addTab("Options", m_mainPanel);
	}
	
	private Box createHeaderBox(String... names) {
		Box hBox = new Box(BoxLayout.X_AXIS);
		JLabel lComp ;
		for(int i = 0; i< names.length; i++) {
			lComp = new JLabel( names[i]);
			hBox.add(lComp);
		}
		
		return hBox;
	}
	// need to return the values back and add more choices 
	private Box createBoxComp( int width, boolean withRMBtn, String... names) {
		Box cBox = new Box(BoxLayout.X_AXIS);
		
		for(int i = 0; i< names.length; i++) {
			// add labels before the valueFiled
			// we can actually avoid this by setting Label as a head for it.. 
			JTextField valueField = new JTextField(width);
			valueField.setName(names[i]);
			valueField.setMaximumSize(valueField.getPreferredSize() );
			cBox.add(valueField);
		}
		
		// add remove button to it and change the values there
		// I really wish that I have better UI design
		if(withRMBtn) {
			JButton rmBtn = new JButton("Remove");
			cBox.add(rmBtn);
			// action listener to this step, if we press the btn it will remove all components from cBox 
			// then when we check it back, we will need to remove cBox too. How to achive this one??
			rmBtn.setActionCommand("Remove Label Value");
			rmBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent rmEvent) {
					// TODO press rmBtn, the components in this cBox will be removed
					// after this, it will pass it further to the mainPanel, then at this point
					// the button and Box will be removed.
					
					Box tBox =(Box) ((JButton) rmEvent.getSource()).getParent();
					
					// remove components from its parent, that all
					for(Component cComp : tBox.getComponents()) {
						tBox.remove(cComp);
					}
					// dispatch listener to parent to delete the parent from it
					// tBox.getParent().dispatchEvent(myEvent);
					m_mainPanel.remove(tBox);
					// after this, we need to refresh the panel
					m_mainPanel.revalidate();
					m_mainPanel.repaint();
				}
				
			});
			
		}
		return cBox;
	}
	
	// but how to update the config value here?? I think at first, we don't do it, until the last step 
	// we conduct the value assign steps.
	protected void updateComponent() {
		String str = m_config.getLabelName();
		if(!nameField.getText().equals(str)) {
			nameField.setText(str);
		}
	}
	
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO assign the config from components
		// how to connect them together and get the values for the nameFiled there.. 
		// how to combine them together??
//		m_config.setLabelName(nameField.getText());
		
		for(Component pComp : m_mainPanel.getComponents()) {
			if(pComp instanceof Box) {
				// only box can contain values
				// check its components and its names for this
				List<JTextField> tfList = new ArrayList(); 
				for(Component bComp : ((Box) pComp).getComponents()) {
					if(bComp instanceof JTextField) {
						JTextField tf = (JTextField) bComp;
						tfList.add(tf);
						
					}
				}
				
				// one thing to remember is that, we might assign the percentage not 
				// correspond to the value list. How to correct it?? 
				// read all the components in the box at first and 
				// list all the text field there
				if(tfList.size() > 0 && tfList.size() <2 ) {
					JTextField tf = tfList.get(0);
					
					String name = tf.getName();
					String value = tf.getText();
					if(name.equals(ClassifyConfig.CFG_LABEL_NAME)) {
						m_config.setLabelName(value);
					}
					
				}else {
					// get the value and its percentage ??
					String labelValue = "", valuePercent = "0";
					for(JTextField tf : tfList) {
						String name = tf.getName();
						String value = tf.getText();
						
						if(name.equals(ClassifyConfig.CFG_LABEL_VALUES)) {
							labelValue = value;
						}else if(name.equals(ClassifyConfig.CFG_LABEL_VALUE_PERCENTAGES)) {
							valuePercent = value;
						}	
					}
					
					if(m_config.addValueList(labelValue)) {
						m_config.addPercentageList(valuePercent);
					}
					
				}
				
			}
			
		}
		
		m_config.saveSettingsTo(settings);
	}
	
	
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObject[] input) throws NotConfigurableException {
		if(input[0] instanceof XLogPortObject) {
			// we can get the log object and then set all the choice of them, but at first, we need to put all the stuff here again
			XLogPortObject logPortObject = (XLogPortObject) input[0];
			XLog log = logPortObject.getLog();
			
			// here to create the optional panel for the xlog to show..
			// but how to decide it here? We can at first only the common choices
		}
	}
	
}

