/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   6 Jul 2017 (albrecht): created
 */
package org.pm4knime.node.jsvis;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pm4knime.node.jsvis.JSONStatisticColumn;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class JSONStatisticColumn {

    private static final String CFG_NAME = "name";
    private static final String CFG_COL_TYPE = "colType";
    private static final String CFG_VALUES = "values";
    private static final String CFG_NO_VALUES = "noValues";
    private static final String CFG_SINGLE_VALUE = "value_";
    private static final String CFG_VALUE_NAME = "valName";
    private static final String CFG_VALUE_DBL = "valDbl";

    private String m_name;
    private String m_colType;
    private Map<String, Double> m_values;

    /**
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * @return the colType
     */
    public String getColType() {
        return m_colType;
    }

    /**
     * @param colType the colType to set
     */
    public void setColType(final String colType) {
        m_colType = colType;
    }

    /**
     * @return the values
     */
    public Map<String, Double> getValues() {
        return m_values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(final Map<String, Double> values) {
        m_values = values;
    }

    void saveToNodeSettings(final NodeSettingsWO settings) {
        settings.addString(CFG_NAME, m_name);
        settings.addString(CFG_COL_TYPE, m_colType);
        NodeSettingsWO valSettings = settings.addNodeSettings(CFG_VALUES);
        int noValues = m_values == null ? 0 : m_values.size();
        valSettings.addInt(CFG_NO_VALUES, noValues);
        int i = 0;
        Iterator<java.util.Map.Entry<String, Double>> iterator = m_values.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Double> entry = iterator.next();
            NodeSettingsWO singleVal = valSettings.addNodeSettings(CFG_SINGLE_VALUE + i);
            singleVal.addString(CFG_VALUE_NAME, entry.getKey());
            singleVal.addDouble(CFG_VALUE_DBL, entry.getValue());
            i++;
        }
    }

    void loadFromNodeSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_name = settings.getString(CFG_NAME);
        m_colType = settings.getString(CFG_COL_TYPE);
        NodeSettingsRO valSettings = settings.getNodeSettings(CFG_VALUES);
        int noValues = valSettings.getInt(CFG_NO_VALUES);
        if (noValues > 0) {
            m_values = new LinkedHashMap<String, Double>();
            for (int i = 0; i < noValues; i++) {
                NodeSettingsRO singleVal = valSettings.getNodeSettings(CFG_SINGLE_VALUE + i);
                String name = singleVal.getString(CFG_VALUE_NAME);
                double dbl = singleVal.getDouble(CFG_VALUE_DBL);
                m_values.put(name, dbl);
            }
        }
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
        JSONStatisticColumn other = (JSONStatisticColumn)obj;
        return new EqualsBuilder()
                .append(m_name, other.m_name)
                .append(m_colType, other.m_colType)
                .append(m_values, other.m_values)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(m_name)
                .append(m_colType)
                .append(m_values)
                .toHashCode();
    }

}
