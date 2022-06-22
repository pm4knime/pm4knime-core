package org.pm4knime.node.jsvis;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pm4knime.node.jsvis.DataExplorerNodeValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class DataExplorerNodeValue extends JSONViewContent {

//    private static final String CFG_SELECTION = "selection";
    private String[] m_selection;

    private static final String CFG_SELECT_ALL = "selectAll";
    private boolean m_selectAll;

    private static final String CFG_SELECT_ALL_INDETERMINATE = "selectAllIndeterminate";
    private static final boolean DEFAULT_SELECT_ALL_INDETERMINATE = false;
    private boolean m_selectAllIndeterminate;

    private static final String CFG_PAGE_SIZE = "pageSize";
    private int m_pageSize;

    private static final String CFG_CURRENT_PAGE = "currentPage";
    private int m_currentPage;

    private static final String CFG_FILTER_STRING = "filterString";
    private String m_filterString;

    private static final String CFG_CURRENT_ORDER = "currentOrder";
    private Object[][] m_currentOrder = new Object[0][];

    /**
     * @return the selection
     */
    public String[] getSelection() {
        return m_selection;
    }

    /**
     * @param selection the selection to set
     */
    public void setSelection(final String[] selection) {
        m_selection = selection;
    }

    /**
     * @return the selectAll
     */
    public boolean isSelectAll() {
        return m_selectAll;
    }

    /**
     * @param selectAll the selectAll to set
     */
    public void setSelectAll(final boolean selectAll) {
        m_selectAll = selectAll;
    }

    /**
     * @return the selectAllIndeterminate
     */
    public boolean isSelectAllIndeterminate() {
        return m_selectAllIndeterminate;
    }

    /**
     * @return the currentOrder
     */
    public Object[][] getCurrentOrder() {
        return m_currentOrder;
    }

    /**
     * @param currentOrder the currentOrder to set
     */
    public void setCurrentOrder(final Object[][] currentOrder) {
        m_currentOrder = currentOrder;
    }

    /**
     * @param selectAllIndeterminate the selectAllIndeterminate to set
     */
    public void setSelectAllIndeterminate(final boolean selectAllIndeterminate) {
        m_selectAllIndeterminate = selectAllIndeterminate;
    }

    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return m_pageSize;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(final int pageSize) {
        m_pageSize = pageSize;
    }

    /**
     * @return the currentPage
     */
    public int getCurrentPage() {
        return m_currentPage;
    }

    /**
     * @param currentPage the currentPage to set
     */
    public void setCurrentPage(final int currentPage) {
        m_currentPage = currentPage;
    }

    /**
     * @return the filterString
     */
    public String getFilterString() {
        return m_filterString;
    }

    /**
     * @param filterString the filterString to set
     */
    public void setFilterString(final String filterString) {
        m_filterString = filterString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void saveToNodeSettings(final NodeSettingsWO settings) {
        settings.addStringArray(DataExplorerConfig.CFG_SELECTION, m_selection);
        settings.addBoolean(CFG_SELECT_ALL, m_selectAll);
        settings.addInt(CFG_PAGE_SIZE, m_pageSize);
        settings.addInt(CFG_CURRENT_PAGE, m_currentPage);
        settings.addString(CFG_FILTER_STRING, m_filterString);
        NodeSettingsWO orderSettings = settings.addNodeSettings(CFG_CURRENT_ORDER);
        orderSettings.addInt("numSettings", m_currentOrder.length);
        for (int i = 0; i < m_currentOrder.length; i++) {
            NodeSettingsWO sO = orderSettings.addNodeSettings("order_" + i);
            sO.addInt("col", (Integer)m_currentOrder[i][0]);
            sO.addString("dir", (String)m_currentOrder[i][1]);
        }
        settings.addBoolean(CFG_SELECT_ALL_INDETERMINATE, m_selectAllIndeterminate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void loadFromNodeSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_selection = settings.getStringArray(DataExplorerConfig.CFG_SELECTION);
        m_selectAll = settings.getBoolean(CFG_SELECT_ALL);
        m_pageSize = settings.getInt(CFG_PAGE_SIZE);
        m_currentPage = settings.getInt(CFG_CURRENT_PAGE);
        m_filterString = settings.getString(CFG_FILTER_STRING);
        NodeSettingsRO orderSettings = settings.getNodeSettings(CFG_CURRENT_ORDER);
        int numSettings = orderSettings.getInt("numSettings");
        m_currentOrder = new Object[numSettings][];
        for (int i = 0; i < numSettings; i++) {
            NodeSettingsRO sO = orderSettings.getNodeSettings("order_" + i);
            int col = sO.getInt("col");
            String dir = sO.getString("dir");
            m_currentOrder[i] = new Object[]{col, dir};
        }
        m_selectAllIndeterminate = settings.getBoolean(CFG_SELECT_ALL_INDETERMINATE, DEFAULT_SELECT_ALL_INDETERMINATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        DataExplorerNodeValue other = (DataExplorerNodeValue)obj;
        return new EqualsBuilder()
                .append(m_selection, other.m_selection)
                .append(m_selectAll, other.m_selectAll)
                .append(m_selectAllIndeterminate, other.m_selectAllIndeterminate)
                .append(m_pageSize, other.m_pageSize)
                .append(m_currentPage, other.m_currentPage)
                .append(m_filterString, other.m_filterString)
                .append(m_currentOrder, other.m_currentOrder)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(m_selection)
                .append(m_selectAll)
                .append(m_selectAllIndeterminate)
                .append(m_pageSize)
                .append(m_currentPage)
                .append(m_filterString)
                .append(m_currentOrder)
                .toHashCode();
    }

}
