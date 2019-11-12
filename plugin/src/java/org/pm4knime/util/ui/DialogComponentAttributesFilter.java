package org.pm4knime.util.ui;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.filter.InputFilter;

/**
 * this class filters attributes by given their name lists. It refers the codes of 
 * org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter. 
 * But it doesn't limit to the DataTableSpec by providing a name-based filter with help of NameFilterPanel.
 * 
 * One part is the DialogComponent from KNIME which demands the model input. One part is the namePanel
 * to control all the events here.
 * @author kefang-pads
 *
 */
public class DialogComponentAttributesFilter extends DialogComponent {

	AttributesFilterPanel m_filterPanel;
	boolean keepAll = false;
	
	public DialogComponentAttributesFilter(SettingsModelFilterString model) {
		super(model);
		// TODO Auto-generated constructor stub
		InputFilter attrFilter = new InputFilter<String>() {

			@Override
			public boolean include(String name) {
				// TODO Auto-generated method stub
				return true;
			}
			
		};
		m_filterPanel = new AttributesFilterPanel(attrFilter);
		
		// how to connect them together??
		getComponentPanel().add(m_filterPanel);
		
		m_filterPanel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				updateModel();
			}
			
		});
		
		getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateComponent();
            }
        });
		
		updateModel();
	}

	public DialogComponentAttributesFilter(SettingsModelFilterString model, boolean keepAll) {
		this(model);
		// TODO Auto-generated constructor stub
		this.keepAll = keepAll;
		m_filterPanel.setKeepAll(keepAll);
	}

	@Override
	protected void updateComponent() {
		// TODO Auto-generated method stub
		final SettingsModelFilterString filterModel =
                (SettingsModelFilterString)getModel();
		
		final Set<String> compIncl = m_filterPanel.getIncludeList();
        final Set<String> compExcl = m_filterPanel.getExcludeList();
        final boolean compKeepAll = m_filterPanel.isKeepAll();
        
        final Set<String> modelIncl =
                new LinkedHashSet<String>(filterModel.getIncludeList());
        final Set<String> modelExcl =
                new LinkedHashSet<String>(filterModel.getExcludeList());
        final boolean modelKeepAll = filterModel.isKeepAllSelected();
        
        boolean update =
                (compIncl.size() != modelIncl.size())
                        || (compExcl.size() != modelExcl.size()
                                || compKeepAll != modelKeepAll);
        
        if (!update) {
            // one way check, because size is equal
            update = !modelIncl.containsAll(compIncl);
        }
        if (!update) {
            // one way check, because size is equal
            update = !modelExcl.containsAll(compExcl);
        }
        
        List<String> allValues = new LinkedList<String>();
        allValues.addAll(modelIncl);
        allValues.addAll(modelExcl);
        
        if (update) {
        	System.out.println("Update the filter panel");
        	m_filterPanel.update(filterModel.getIncludeList(), filterModel.getExcludeList(), allValues.toArray(new String[0]));
        }
        
		// also update the enable status
	    setEnabledComponents(filterModel.isEnabled());
	}
	
	private void updateModel() {
		final SettingsModelFilterString filterModel =
                (SettingsModelFilterString)getModel();
		
		final Set<String> inclList = m_filterPanel.getIncludeList();
        final Set<String> exclList = m_filterPanel.getExcludeList();
		
		filterModel.setNewValues(inclList, exclList, keepAll);
	}
	
	public Set<String> getValidExcludeColumns() {
        return m_filterPanel.getExcludeList();
    }

	public Set<String> getValidIncludeColumns() {
        return m_filterPanel.getIncludeList();
    }

	@Override
	protected void validateSettingsBeforeSave() throws InvalidSettingsException {
		// TODO Auto-generated method stub
		updateModel();
	}

	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs) throws NotConfigurableException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setEnabledComponents(boolean enabled) {
		// TODO Auto-generated method stub
		m_filterPanel.setEnabled(enabled);
	}

	@Override
	public void setToolTipText(String text) {
		// TODO Auto-generated method stub
		m_filterPanel.setToolTipText(text);
	}

}

