package org.pm4knime.node.io.petrinet.writer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.FileUtil;

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
public class PetrinetWriterNodeDialog extends DefaultNodeSettingsPane {

    
    
    protected PetrinetWriterNodeDialog() {
        
    	SettingsModelString fileModel = createFileMode();
        
        final DialogComponentFileChooser fileChooser = new DialogComponentFileChooser(fileModel, "pnml.writer.history",
        		JFileChooser.SAVE_DIALOG, false, createFlowVariableModel(fileModel), ".pnml", ".xml");
         
        fileChooser.setDialogTypeSaveWithExtension(".pnml");

        fileChooser.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                String selFile = ((SettingsModelString) fileChooser.getModel()).getStringValue();
                if ((selFile != null) && !selFile.isEmpty()) {
                    try {
                        URL newUrl = FileUtil.toURL(selFile);
                        Path path = FileUtil.resolveToPath(newUrl);
                    } catch (IOException | URISyntaxException | InvalidPathException ex) {
                        // ignore
                    }
                }

            }
        });
        fileChooser.setBorderTitle("Output location");  
        addDialogComponent(fileChooser);
    }

	public static SettingsModelString createFileMode() {
		// TODO Auto-generated method stub
		return new SettingsModelString("Petri net WriterFile", "");
	}
}

