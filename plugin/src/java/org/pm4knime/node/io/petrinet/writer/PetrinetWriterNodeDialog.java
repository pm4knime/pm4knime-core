package org.pm4knime.node.io.petrinet.writer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.util.FileUtil;
import org.knime.filehandling.core.data.location.variable.FSLocationVariableType;
import org.knime.filehandling.core.defaultnodesettings.filechooser.writer.DialogComponentWriterFileChooser;
import org.knime.filehandling.core.util.SettingsUtils;

/**
 * <code>NodeDialog</code> for the "PetrinetWriter" Node.
 * Write Petri net into file to implement the serialization.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author 
 */
public class PetrinetWriterNodeDialog extends NodeDialogPane {

	DialogComponentWriterFileChooser m_filePanel;
    private PetrinetWriterNodeModel model;
	private static final String FILE_HISTORY_ID = "PetriNet_reader_writer";
	
	
    protected PetrinetWriterNodeDialog(final NodeCreationConfiguration creationConfig, PetrinetWriterNodeModel model) {
    	this.model = model;
    	addTab("Settings", createMainOptionsPanel(creationConfig));
    }

	private JPanel createMainOptionsPanel(NodeCreationConfiguration creationConfig) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;

        final JPanel mainOptionsPanel = new JPanel(new GridBagLayout());

        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        mainOptionsPanel.add(createFilePanel(creationConfig), gbc);

        return mainOptionsPanel;
    }

	private JPanel createFilePanel(NodeCreationConfiguration creationConfig) {
        FlowVariableModel fvm = createFlowVariableModel(Stream.concat(Stream.of("settings"), Arrays.stream(model.m_fileChooserModel.getKeysForFSLocation()))
                .toArray(String[]::new), FSLocationVariableType.INSTANCE);
        m_filePanel = new DialogComponentWriterFileChooser(model.m_fileChooserModel, FILE_HISTORY_ID,
            fvm);
        final JPanel filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
        filePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Output location"));
        filePanel.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, m_filePanel.getComponentPanel().getPreferredSize().height));
        filePanel.add(m_filePanel.getComponentPanel());
        filePanel.add(Box.createHorizontalGlue());
        
        return filePanel;
	}

	@Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        m_filePanel.loadSettingsFrom(SettingsUtils.getOrEmpty(settings, SettingsUtils.CFG_SETTINGS_TAB), specs);
        try {
			model.loadValidatedSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			throw new NotConfigurableException(e.getMessage());
		}
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        m_filePanel.saveSettingsTo(SettingsUtils.getOrAdd(settings, SettingsUtils.CFG_SETTINGS_TAB));
        model.saveSettingsTo(settings);
    }
    
    @Override
    public void onClose() {
        m_filePanel.onClose();
    }
    
}

