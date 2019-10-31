package org.pm4knime.settingsmodel;

import java.util.Collection;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.pm4knime.util.PetriNetUtil;

/**
 * this class will have a specified cost table for the parameter
 * 
 * @author kefang-pads
 *
 */
public class SMAlignmentReplayParameterWithCT extends SMAlignmentReplayParameter {
	private final String delimiter = "::";
	private DefaultTableModel[] m_costTMs;
	private boolean m_withTM = false;
	
	public SMAlignmentReplayParameterWithCT(String configName) {
		// TODO Auto-generated constructor stub
		super(configName);

		m_costTMs = new DefaultTableModel[3];
		// we need to initialze the table model, but later??
		for(int i=0; i< CFG_COST_TYPE_NUM; i++){
			m_costTMs[i] = createTM(i);
		}
	}
	
	public DefaultTableModel[] getMCostTMs() {
		return m_costTMs;
	}

	public void setMCostTMs(DefaultTableModel[] m_costTMs) {
		this.m_costTMs = m_costTMs;
	}
	
	public boolean isMWithTM() {
		return m_withTM;
	}

	public void setMWithTM(boolean m_withTM) {
		this.m_withTM = m_withTM;
	}

	@Override
	protected void loadSettingsPure(NodeSettingsRO settings) throws InvalidSettingsException {
		// super.loadSettingsForModel(settings);
		// also load the cost values for the table, but at first to learn how to store
		super.loadSettingsPure(settings);
		m_withTM = settings.getBoolean("withTM");
		if(!m_withTM) {
			// System.out.println("There is no table model available!");
			throw new InvalidSettingsException("There is no table model available!");
		}
		
		for (int i = 0; i < CFG_COST_TYPE_NUM; i++) {
			loadTMFrom(i, settings);
		}
	}

	@Override
	protected void saveSettingsPure(NodeSettingsWO settings) {
		// here we can not override it, it will create more subSettings. 
		super.saveSettingsPure(settings);
		
		// use one indicator to say, we have saved the value here
		// on the contrast, we should also need to load the values from there
		settings.addBoolean("withTM", m_withTM);
		if(m_withTM) {
			for (int idx = 0; idx < CFG_COST_TYPE_NUM; idx++) {
				TableModel tModel = m_costTMs[idx];
				for (int i = 0; i < tModel.getRowCount(); i++) {
					settings.addString("T" + idx + delimiter + i,
							tModel.getValueAt(i, 0) + delimiter + tModel.getValueAt(i, 1));
					
				}
			}
		}
	}

	private DefaultTableModel createTM(int idx) {
		DefaultTableModel tModel = new DefaultTableModel() {
			// private static final long serialVersionUID = 5238526181467190856L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return (column != 0);
			};
		};

		tModel.addColumn(CFG_MOVE_KEY[idx]);
		tModel.addColumn(CFG_MCOST_KEY[idx]);

		return tModel;
	}

	public void setCostTM(Collection<String> nameList, int idx) {
		
		// if this DataTable is new created, add column  names to it
		if(m_costTMs[idx].getDataVector().size() < 1) {
			// TODO : to distinguish the invisible transitions and set its cost to 0 
			// we have two parts from this.
   			for(String name : nameList ) {
   				if(PetriNetUtil.isTauName(name))
   					m_costTMs[idx].addRow(new Object[] {name, 0});
   				else
   					m_costTMs[idx].addRow(new Object[] {name, CFG_DEFAULT_MCOST[idx]});
			}
			
		}else {
			m_costTMs[idx].getDataVector().clear();
			int i=0;
			for(String name : nameList ) {
				m_costTMs[idx].setValueAt(name, i, 0);
				if(PetriNetUtil.isTauName(name)) {
					m_costTMs[idx].setValueAt(0, i, 1);
				}else {
   					m_costTMs[idx].setValueAt(CFG_DEFAULT_MCOST[idx], i, 1);
   				}
				i++;
			}
		}
		
	}
	private void loadTMFrom(int idx, NodeSettingsRO settings) throws InvalidSettingsException {

		DefaultTableModel tModel = m_costTMs[idx];
		if (tModel == null) {
			tModel = createTM(idx);
		}

		tModel.getDataVector().clear();

		// assign values to tables by adding rows there
		for (String key : settings.keySet()) {
			String[] splitStr = key.split(delimiter);
			if (splitStr[0].equals("T" + idx)) {
				// TODO: if settings save data in order or not
				// we meet the first values there, is there a need to do this??
				int rowIdx = Integer.valueOf(splitStr[1]);
				// System.out.println("the current read row id is " + rowIdx);
				// even if we want to add them, but how to do this?? are they in order at
				// first?? I don't think so
				String value = settings.getString(key);
				String[] sValues = value.split(delimiter);
				tModel.addRow(sValues);

			}
		}
		m_costTMs[idx] = tModel;
	}

	public static void main(String[] args) {
		// test this class... firstly about the clear function for TableModel
		SMAlignmentReplayParameterWithCT param = new SMAlignmentReplayParameterWithCT("test parameter");

		DefaultTableModel tModel = param.createTM(0);
		String[] sValues = { "reg", 1 + "" };
		tModel.addRow(sValues);
		tModel.addRow(sValues);

		tModel.setValueAt("name", 1, 0);

		tModel.getDataVector().clear();
		// after clear, there is no row, we need to take steps to add rows
		tModel.addRow(sValues);
		tModel.setValueAt("name", 1, 0);

	}
}
