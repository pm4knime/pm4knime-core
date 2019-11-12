package org.pm4knime.util.ui;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.NameFilterPanel;

/**
 * this class extends NameFilterFilter to set attributes> How about the other use??
 * If it is directly from the attributes, we can give back directly the chosen values. 
 * Is this a good way for this?? Not sure about it, because for the attributes, 
 * we need to give them different values there. So keep string as it is. 
 * @author kefang-pads
 *
 */
public class AttributesFilterPanel extends NameFilterPanel<String>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean keepAll = false;
	
	public AttributesFilterPanel(InputFilter<String> filter) {
		// initilaize the lists here for the include and excluded list??
		super(false, filter);
	}
	
	public final boolean isKeepAll() {
		return keepAll;
	}
	
	public final void setKeepAll(boolean keepAll) {
		this.keepAll = keepAll;
	}
	
	@Override
	protected ListCellRenderer getListCellRenderer() {
		// TODO Auto-generated method stub
		return new DefaultListCellRenderer();
	}

	@Override
	protected TableCellRenderer getTableCellRenderer() {
		// TODO Auto-generated method stub
		return new DefaultTableCellRenderer();
	}

	@Override
	protected String getEntryDescription() {
		// TODO Auto-generated method stub
		return "attributes filter panel";
	}

	@Override
	protected String getTforName(String name) {
		// TODO How to differ it here, no idea yet
//		if(getIncludeList().contains(name) || this.getExcludeList().contains(name))
//			return name;
		return name;
	}

	@Override
	protected String getNameForT(String t) {
		// TODO Auto-generated method stub
		return t;
	}

}
