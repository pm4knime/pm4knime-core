package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import javax.swing.JPanel;

import org.processmining.plugins.manifestanalysis.visualization.performance.ManifestPerfPanel;
import org.processmining.plugins.manifestanalysis.visualization.performance.PIPManifestPanel;
import org.processmining.plugins.manifestanalysis.visualization.performance.ZoomManifestPanel;


import info.clearthought.layout.TableLayout;

public class ManifestViewPanelTable<N extends ManifestEvClassPatternTable, C extends PerfCounterTable> extends JPanel {
	private static final long serialVersionUID = 826617115617858896L;

	private PIPManifestPanelTable pip;
	private ZoomManifestPanelTable zoom;
	public ManifestViewPanelTable(ManifestPerfPanelTable mainPanel, int maxZoom) {
		double[][] size = new double[][]{ {TableLayout.FILL}, {TableLayout.FILL, TableLayout.PREFERRED}} ;
		setLayout(new TableLayout(size));
		
		pip = new PIPManifestPanelTable(mainPanel);
		zoom = new ZoomManifestPanelTable(mainPanel, pip, maxZoom);

		add(pip, "0,0");
		add(zoom, "0,1");
	}

	
	public PIPManifestPanelTable getPIP() {
		return pip;
	}
	
	public ZoomManifestPanelTable getZoom(){
		return zoom;
	}
}
