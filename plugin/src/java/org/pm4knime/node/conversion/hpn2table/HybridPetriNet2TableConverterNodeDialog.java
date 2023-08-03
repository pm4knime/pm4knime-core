package org.pm4knime.node.conversion.hpn2table;

import java.util.ArrayList;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class HybridPetriNet2TableConverterNodeDialog extends DefaultNodeSettingsPane {


    public HybridPetriNet2TableConverterNodeDialog(HybridPetriNet2TableConverterNodeModel node) {
        SettingsModelString rowIdModel = node.m_rowKeyModel;
        ArrayList<String> list = new ArrayList<>(1);
        list.add(node.DEFAULT_ROWKEY.toString());
        super.addDialogComponent(new DialogComponentStringSelection(rowIdModel, "Row Identifier: ", list, true,
            super.createFlowVariableModel(rowIdModel)));
        final SettingsModelString colNameModel = node.m_columnNameModel;
        super.addDialogComponent(new DialogComponentString(colNameModel, "Column Name: ", true, 15,
            super.createFlowVariableModel(colNameModel)));
    }

}
