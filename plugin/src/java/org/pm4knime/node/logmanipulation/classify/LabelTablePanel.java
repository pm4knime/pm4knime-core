package org.pm4knime.node.logmanipulation.classify;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.pm4knime.util.ui.ButtonColumn;
/**
 * refer at : https://examples.javacodegeeks.com/desktop-java/swing/java-swing-table-example/ 
 * @author kefang-pads
 *
 */
public class LabelTablePanel extends JPanel {
	JTable table ;
	DefaultTableModel tModel;
	
	private static final long serialVersionUID = 1L;

	public LabelTablePanel() {
		super(new GridLayout(1,0));
		
		// make it editable
		tModel = new DefaultTableModel() {
			@Override
		    public boolean isCellEditable(int row, int column) {
		       //all cells false
		       return true;
		    }
		};
		tModel.addColumn(ClassifyConfig.CFG_LABEL_VALUES);
		tModel.addColumn(ClassifyConfig.CFG_LABEL_VALUE_PERCENTAGES);
//		tModel.addColumn("Manage");
		tModel.addColumn("Manage");
		
		table = new JTable(tModel);
		
		Action delete = new AbstractAction()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        JTable table = (JTable)e.getSource();
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        ((DefaultTableModel)table.getModel()).removeRow(modelRow);
		    }
		};
		
		ButtonColumn bCol = new ButtonColumn(table, delete, 2);
		bCol.setMnemonic(KeyEvent.VK_D);
		
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        // get the last element
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
 
        //Add the scroll pane to this panel.
        add(scrollPane);
	}
	
	public DefaultTableModel getTableModel() {
		return tModel;
	}
}