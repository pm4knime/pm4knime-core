package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.petrinet.visualization.AlignmentConstants;
import org.processmining.plugins.pnalignanalysis.visualization.projection.CaseFilterPanel;
import org.processmining.plugins.pnalignanalysis.visualization.projection.GlobalStatPanel;
import org.processmining.plugins.pnalignanalysis.visualization.projection.LegendPanelConf;
import org.processmining.plugins.pnalignanalysis.visualization.projection.ProjectionVisPanel;
import org.processmining.plugins.pnalignanalysis.visualization.projection.TransConfDecorator;
import org.processmining.plugins.pnalignanalysis.visualization.projection.ViewPanel;
import org.processmining.plugins.pnalignanalysis.visualization.projection.util.GraphBuilder;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerScrollBarUI;

/**
 * @author aadrians Oct 26, 2011
 * 
 */
public class PNLogReplayProjectedVisPanelTable extends ProjectionVisPanel {
	
	private static final long serialVersionUID = -6674503536171244970L;
	private static int logExportCounter = 0;
	protected StatPanelTable statPanel;

	// GUI component
	private ViewPanel viewPanel;
	//private JComponent exportPanel;
	private GlobalStatPanel globalStatPanel;
	//private TableLogSource logExporter;
	protected CoreInfoProviderTable provider;

	protected final TransEvClassMappingTable map;
	protected CaseFilterPanel caseFilterPanel;
	protected AlignmentFilterPanelTable alignFilterPanel;
	protected FitnessFilterPanelTable fitnessFilterPanel;

	// for graph visualization
	protected boolean[] placeWithMoveOnLog;

	protected Color involvedMoveOnLogColor = new Color(255, 0, 0, 200);
	protected Color transparentColor = new Color(255, 255, 255, 0);

	// transition coloring
	public static Color NOOCCURRENCE = new Color(239, 243, 255);
	public static Color LOW = new Color(198, 219, 239);
	public static Color LOWMED = new Color(158, 202, 225);
	public static Color MED = new Color(107, 174, 214);
	public static Color MEDHIGH = new Color(49, 130, 189);
	public static Color HIGH = new Color(8, 81, 156);


	public PNLogReplayProjectedVisPanelTable(PluginContext context, PetrinetGraph origNet, Marking origMarking, TableEventLog log,
			TransEvClassMappingTable map, PNRepResult logReplayResult)
			throws ConnectionCannotBeObtained {
		super(context, origNet, origMarking, null, null, logReplayResult);
		this.map = map;
		this.log = log;
		super_const(origNet, origMarking, null, map, logReplayResult);
	

		//set exporter
		//this.logExporter = logExporter;

		initialize(context);
	}
	
	protected SlickerFactory factory;
	protected SlickerDecorator decorator;

	// for graph visualization
	protected ProMJGraph graph;

	// GUI component
	protected ScalableComponent scalable;
	protected JScrollPane scroll;

	// zoom-related properties
	// The maximal zoom factor for the primary view on the transition system.
	public static final int MAX_ZOOM = 1200;

	// reference to log replay result
	//	private TransEvClassMapping mapping;
	
	protected PNRepResult logReplayResult;

	// mapping elements to index
	protected Map<Transition, Integer> mapTrans2Idx;
	protected Map<String, Integer> mapEc2Int;

	protected GraphLayoutConnection oldLayoutConn;

	protected PetrinetGraph net;

	protected Marking marking;

	protected Map<Transition, TransConfDecorator> decoratorMap = new HashMap<Transition, TransConfDecorator>();
	protected TableEventLog log;

	public void super_const(PetrinetGraph origNet, Marking origMarking, TableEventLog log,
			TransEvClassMappingTable map, PNRepResult logReplayResult) throws ConnectionCannotBeObtained {
		this.net = origNet;
		this.marking = origMarking;
		/**
		 * Get some Slickerbox stuff, required by the Look+Feel of some objects.
		 */
		factory = SlickerFactory.instance();
		decorator = SlickerDecorator.instance();

		/**
		 * Shared stuffs
		 */
		this.logReplayResult = logReplayResult;

	}

	public JComponent getComponent() {
		return scalable.getComponent();
	}

	/**
	 * @return the logReplayResult
	 */
	public PNRepResult getLogReplayResult() {
		return logReplayResult;
	}

	/**
	 * @return the scalable
	 */
	public ScalableComponent getScalable() {
		return scalable;
	}

	public JViewport getViewport() {
		return scroll.getViewport();
	}

	public void setScale(double d) {
		double b = Math.max(d, 0.01);
		b = Math.min(b, MAX_ZOOM / 100.);
		scalable.setScale(b);
	}

	public double getScale() {
		return scalable.getScale();
	}

	public Component getVerticalScrollBar() {
		return scroll.getVerticalScrollBar();
	}

	public Component getHorizontalScrollBar() {
		return scroll.getHorizontalScrollBar();
	}

	protected ViewPanel createViewPanel(ProjectionVisPanel mainPanel, int maxZoom) {
		return new ViewPanel(this, maxZoom);
	}

	public void addInteractionViewports(final ViewPanel viewPanel) {
		this.scroll.addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent arg0) {
			}

			public void componentMoved(ComponentEvent arg0) {
			}

			public void componentResized(ComponentEvent arg0) {

				if (arg0.getComponent().isValid()) {

					Dimension size = arg0.getComponent().getSize();

					int width = 250, height = 250;

					if (size.getWidth() > size.getHeight())
						height *= size.getHeight() / size.getWidth();
					else
						width *= size.getWidth() / size.getHeight();

					viewPanel.getPIP().setPreferredSize(new Dimension(width, height));
					viewPanel.getPIP().initializeImage();

					viewPanel.getZoom().computeFitScale();
				}
			}

			public void componentShown(ComponentEvent arg0) {
			}

		});
	}

	protected float getAppropriateStrokeWidth(double value) {
		// update width of incoming and outgoing arcs
		float suggestedArcWidth = 0.5f;

		if (Double.compare(value, 0) > 0) {
			suggestedArcWidth += new Float(Math.log(Math.E) * Math.log10(value));
		}

		return suggestedArcWidth;
	}


	protected void initialize(PluginContext context) {

		// calculate info
		provider = createCoreInfoProvider(log, map, logReplayResult);
		this.placeWithMoveOnLog = new boolean[provider.getNumPlaces()];

		// create mapping from transition to index
		mapTrans2Idx = provider.getTrans2Int();

		// create mapping from event class to index
		mapEc2Int = provider.getEC2Int();

		/**
		 * TAB INFO
		 */
		// add info
		statPanel = createStatPanel(provider);

		/**
		 * Main visualization (has to be after creating provider)
		 */

		scalable = GraphBuilder.buildJGraph(net, oldLayoutConn);
		graph = (ProMJGraph) scalable;

		graph.addGraphSelectionListener(new GraphSelectionListener() {
			public void valueChanged(GraphSelectionEvent e) {
				// selection of a transition would change the stats
				if (e.getCell() instanceof ProMGraphCell) {
					DirectedGraphNode cell = ((ProMGraphCell) e.getCell()).getNode();
					if (cell instanceof Transition) {
						statPanel.setTransition((Transition) cell);
					} else if (cell instanceof Place) {
						statPanel.setPlace((Place) cell);
					}

					boolean[] involvedPlacesFlag = statPanel.getInvolvedPlaces();
					Place[] placeArray = provider.getPlaceArray();
					graph.getModel().beginUpdate();
					for (int i = 0; i < involvedPlacesFlag.length; i++) {
						if (involvedPlacesFlag[i]) {
							graph.getViewSpecificAttributes().putViewSpecific(placeArray[i], AttributeMap.FILLCOLOR,
									involvedMoveOnLogColor);
						} else {
							if (placeWithMoveOnLog[i]) {
								graph.getViewSpecificAttributes().putViewSpecific(placeArray[i],
										AttributeMap.FILLCOLOR, AlignmentConstants.MOVELOGCOLOR);
							} else {
								graph.getViewSpecificAttributes().putViewSpecific(placeArray[i],
										AttributeMap.FILLCOLOR, transparentColor);
							}
						}
					}
					graph.getModel().endUpdate();
					graph.refresh();
				}
			}
		});

		for (Place p : marking) {
			String label = "" + marking.occurrences(p);
			graph.getViewSpecificAttributes().putViewSpecific(p, AttributeMap.LABEL, label);
			graph.getViewSpecificAttributes().putViewSpecific(p, AttributeMap.SHOWLABEL, !label.equals(""));
		}

		scroll = new JScrollPane(scalable.getComponent());
		decorator.decorate(scroll, Color.WHITE, Color.GRAY, Color.DARK_GRAY);
		setLayout(new BorderLayout());
		add(scroll);

		// add legend panel
		JPanel legendPanel = new LegendPanelConf();

		// add view panel (zoom in/out)
		viewPanel = createViewPanel(this, MAX_ZOOM);
		addInteractionViewports(viewPanel);

		// add global stats panel
		globalStatPanel = createGlobalStatePanel(logReplayResult);

		// set scroll pane 
		JScrollPane hscrollPane = new JScrollPane(globalStatPanel);
		hscrollPane.setOpaque(true);
		hscrollPane.setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		hscrollPane.getViewport().setOpaque(true);
		hscrollPane.getViewport().setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		hscrollPane.setBorder(BorderFactory.createEmptyBorder());
		hscrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		hscrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollBar hBar = hscrollPane.getHorizontalScrollBar();
		hBar.setUI(new SlickerScrollBarUI(hBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
				WidgetColors.COLOR_NON_FOCUS, 4, 12));
		hBar.setOpaque(true);
		hBar.setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		hBar = hscrollPane.getHorizontalScrollBar();
		hBar.setUI(new SlickerScrollBarUI(hBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
				WidgetColors.COLOR_NON_FOCUS, 4, 12));
		hBar.setOpaque(true);
		hBar.setBackground(WidgetColors.PROPERTIES_BACKGROUND);

		addInfo("Legend", legendPanel);
		addInfo("View", viewPanel);
		addInfo("Elements Statistics", statPanel);
		addInfo("Global Statistics (non-filtered traces)", hscrollPane);

		/**
		 * TAB DISPLAY SETTING
		 */
		// add additional tab for display settings
		JPanel displayMP = getInspector().addTab("Display");

		// add filtering
		ShowHideMovementPanelTable visFilter = createVisualizationPanel(this);

		// add elements
		getInspector().addGroup(displayMP, "Show/Ignore Movements in Projection", visFilter);

		/**
		 * TAB FILTER ALIGNMENT
		 */
		// add additional tab for filtering alignments
		JPanel filterMP = getInspector().addTab("Filter");

		caseFilterPanel = createCaseFilterPanelTable();
		caseFilterPanel.setPreferredSize(new Dimension(400, 400));
		getInspector().addGroup(filterMP, "Case Filter", caseFilterPanel);

		alignFilterPanel = createAlignmentFilter();
		getInspector().addGroup(filterMP, "Movement Containment Filter", alignFilterPanel);

		fitnessFilterPanel = createFitnessFilterPanel();
		getInspector().addGroup(filterMP, "Aggregated Alignment Statistics (before any filter is applied)",
				fitnessFilterPanel);

		// initialize decorator for transitions
		Transition[] transArr = provider.getTransArray();
		int pointer = 0;
		while (pointer < provider.getNumTrans()) {
			int[] info = provider.getInfoNode(pointer);
			TransConfDecorator dec = createTransitionDecorator(transArr, pointer, info);
			decoratorMap.put(transArr[pointer], dec);
			graph.getViewSpecificAttributes().putViewSpecific(transArr[pointer], AttributeMap.SHAPEDECORATOR, dec);
			graph.getViewSpecificAttributes().putViewSpecific(transArr[pointer], AttributeMap.SHOWLABEL, false);

			pointer++;
		}

		/**
		 * TAB EXPORT
		 */
		// add additional tab for export
		//JPanel exportTab = getInspector().addTab("Export");

		// add export image and log
		//exportPanel = createExportPanel(context, scalable);

		//getInspector().addGroup(exportTab, "Export", exportPanel);

		// attach zoom to 
		scroll.addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() > 0) {
					viewPanel.getZoom().zoomOut();
				} else if (e.getWheelRotation() < 0) {
					viewPanel.getZoom().zoomIn();
				}

			}
		});

		constructVisualization(graph.getViewSpecificAttributes(), true, true);
		constructPlaceVisualization(graph.getViewSpecificAttributes());

		validate();
		repaint();
	}

	private CaseFilterPanel createCaseFilterPanelTable() {
		List<String> caseLabels = new ArrayList<String>(log.getTraces().size());
		for (int id: log.getTraces().keySet()) {
			caseLabels.add(log.getTraceName(id));
		}
		return new CaseFilterPanel(this, caseLabels);
	}

	protected TransConfDecorator createTransitionDecorator(Transition[] transArr, int pointer, int[] info) {
		return new TransConfDecorator(info[3], info[0], transArr[pointer].getLabel());
	}

	protected CoreInfoProviderTable createCoreInfoProvider(TableEventLog log, TransEvClassMappingTable map, PNRepResult logReplayResult) {
		return new CoreInfoProviderTable(net, marking, map, log, logReplayResult);
	}

	private GlobalStatPanel createGlobalStatePanel(PNRepResult logReplayResult) {
		return new GlobalStatPanel(logReplayResult);
	}

	private FitnessFilterPanelTable createFitnessFilterPanel() {
		return new FitnessFilterPanelTable(this, this.logReplayResult);
	}

	protected void constructPlaceVisualization(ViewSpecificAttributeMap map) {
		// update place visualization
		Place[] placeArr = provider.getPlaceArray();
		int[] freq = provider.getPlaceFreq();

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		for (int i = 0; i < freq.length; i++) {
			if (freq[i] < min) {
				min = freq[i];
			}
			if (freq[i] > max) {
				max = freq[i];
			}
		}

		double median = ((double) (max - min)) / 2;
		int medianPlaceRadius = 30;
		int flexibility = 10;
		for (int i = 0; i < placeArr.length; i++) {
			int size = medianPlaceRadius + (int) Math.floor((freq[i] - median) * flexibility / median);
			if (freq[i] > 0) {
				map.putViewSpecific(placeArr[i], AttributeMap.FILLCOLOR, AlignmentConstants.MOVELOGCOLOR);
				this.placeWithMoveOnLog[i] = true;
			} else {
				map.putViewSpecific(placeArr[i], AttributeMap.FILLCOLOR, transparentColor);
				this.placeWithMoveOnLog[i] = false;
			}
			map.putViewSpecific(placeArr[i], AttributeMap.SIZE, new Dimension(size, size));
		}
	}

	public void constructVisualization(ViewSpecificAttributeMap map, boolean isShowMoveLogModel, boolean isShowMoveModel) {
		graph.getModel().beginUpdate();

		doUpdateInternal(map, isShowMoveLogModel, isShowMoveModel);

		graph.getModel().endUpdate();
		graph.refresh();
		graph.revalidate();
		graph.repaint();
	}

	protected void doUpdateInternal(ViewSpecificAttributeMap map, boolean isShowMoveLogModel, boolean isShowMoveModel) {
		/**
		 * Update main visualization (add decoration, size)
		 */
		int[] minMaxFreq = provider.getMinMaxFreq(isShowMoveLogModel, isShowMoveModel);

		Transition[] transArr = provider.getTransArray();
		int pointer = 0;

		while (pointer < transArr.length) {
			TransConfDecorator dec = decoratorMap.get(transArr[pointer]);
			int[] info = provider.getInfoNode(pointer);
			int occurrence = 0;
			if (isShowMoveLogModel) {
				dec.setMoveSyncFreq(info[0]);
				occurrence += info[0];
			} else {
				dec.setMoveSyncFreq(0);
			}
			if (isShowMoveModel) {
				if (!transArr[pointer].isInvisible()) {
					if (info[3] > 0) {
						map.putViewSpecific(transArr[pointer], AttributeMap.STROKECOLOR, Color.RED);
					} else {
						map.putViewSpecific(transArr[pointer], AttributeMap.STROKECOLOR, Color.BLACK);
					}
				}
				dec.setMoveOnModelFreq(info[3]);
				occurrence += info[3];
			} else {
				map.putViewSpecific(transArr[pointer], AttributeMap.STROKECOLOR, Color.BLACK);
				dec.setMoveOnModelFreq(0);
			}
			if (occurrence > 0) {
				map.putViewSpecific(transArr[pointer], AttributeMap.STROKECOLOR, Color.BLACK);
			}
			float suggestedArcWidth = getAppropriateStrokeWidth(occurrence);

			int intensity = minMaxFreq[1] > 0 ? 100 - (int) ((50.0 * (occurrence - minMaxFreq[0])) / (minMaxFreq[1] - minMaxFreq[0]))
					: 100;

			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = net
					.getInEdges(transArr[pointer]);
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
				setPetrinetEdgeAttributes(map, suggestedArcWidth, intensity, edge);
			}
			edges = net.getOutEdges(transArr[pointer]);
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
				setPetrinetEdgeAttributes(map, suggestedArcWidth, intensity, edge);
			}
			colorTransition(map, transArr[pointer], occurrence, minMaxFreq[0], minMaxFreq[1]);
			pointer++;
		}
	}

	protected void setPetrinetEdgeAttributes(ViewSpecificAttributeMap map, float suggestedArcWidth, int intensity,
			PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge) {
		map.putViewSpecific(edge, AttributeMap.EDGECOLOR, new Color(intensity, intensity, intensity));
		map.putViewSpecific(edge, AttributeMap.LINEWIDTH, suggestedArcWidth);
	}

	/**
	 * Color transitions based on 5 scaling
	 * 
	 * @param transition
	 * @param occurrence
	 * @param min
	 * @param max
	 */
	private void colorTransition(ViewSpecificAttributeMap map, Transition transition, int occurrence, int min, int max) {
		if (!transition.isInvisible()) {
			map.putViewSpecific(transition, AttributeMap.FILLCOLOR, Color.LIGHT_GRAY);
			// use 5 color scale
			if ((min == max) || (occurrence == max)) {
				map.putViewSpecific(transition, AttributeMap.FILLCOLOR, HIGH);
				decoratorMap.get(transition).setLightColorLabel(true);
			} else {
				if (occurrence == 0) {
					map.putViewSpecific(transition, AttributeMap.FILLCOLOR, NOOCCURRENCE);
					decoratorMap.get(transition).setLightColorLabel(false);
				} else {
					int scale = (occurrence - min) * 5 / (max - min);
					if (scale == 0) {
						map.putViewSpecific(transition, AttributeMap.FILLCOLOR, LOW);
						decoratorMap.get(transition).setLightColorLabel(false);
					} else if (scale == 1) {
						map.putViewSpecific(transition, AttributeMap.FILLCOLOR, LOWMED);
						decoratorMap.get(transition).setLightColorLabel(false);
					} else if (scale == 2) {
						map.putViewSpecific(transition, AttributeMap.FILLCOLOR, MED);
						decoratorMap.get(transition).setLightColorLabel(false);
					} else if (scale == 3) {
						map.putViewSpecific(transition, AttributeMap.FILLCOLOR, MEDHIGH);
						decoratorMap.get(transition).setLightColorLabel(true);
					} else if (scale == 4) {
						map.putViewSpecific(transition, AttributeMap.FILLCOLOR, HIGH);
						decoratorMap.get(transition).setLightColorLabel(true);
					}
				}
			}
		} else {
			decoratorMap.get(transition).setLightColorLabel(true);
		}
	}

	private AlignmentFilterPanelTable createAlignmentFilter() {
		return new AlignmentFilterPanelTable(this, provider.getTransArray(), provider.getEvClassArray());
	}

	private ShowHideMovementPanelTable createVisualizationPanel(PNLogReplayProjectedVisPanelTable mainPanel) {
		return new ShowHideMovementPanelTable(mainPanel);
	}

//	private JComponent createExportPanel(final PluginContext context, final ScalableComponent graph) {
//		JPanel panel = new ExportPanel(graph);
//
//		// export filtered log to framework
//		JButton exportCases = factory.createButton("Export shown cases as new Log");
//		exportCases.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				if (caseFilterPanel.getSelectedIndex().length > 0) {
//					String input = JOptionPane.showInputDialog("Write the name of exported log", "Filtered "
//							+ "LOG" + "-" + logExportCounter++);
//					if (input != null) {
//						logExporter.export(input, caseFilterPanel.getSelectedIndex());
//
//						JOptionPane.showMessageDialog(new JPanel(), "Sucessfully exported " + input);
//					}
//				} else {
//					JOptionPane.showMessageDialog(new JPanel(),
//							"No cases is selected. Choose at least one case in the \"Filter\" panel.");
//				}
//			}
//		});
//		panel.add(exportCases);
//
//		return panel;
//	}

	protected StatPanelTable createStatPanel(CoreInfoProviderTable provider) {
		return new StatPanelTable(provider);
	}

	/**
	 * Recalculate all info as alignment is filtered
	 * 
	 * @param existMoveSync
	 * @param existsMoveModelOnly
	 * @param existsMoveLogOnly
	 */
	public void filterAlignment(boolean[] existMoveSync, boolean[] existsMoveModelOnly, boolean[] existsMoveLogOnly) {

		// now filter all syncRepResult
		boolean[] filter = new boolean[logReplayResult.size()];
		boolean[] caseFilter = new boolean[log.getTraces().size()];
		Arrays.fill(filter, false);

		int idx = 0;
		repResultLoop: for (SyncReplayResult repResult : this.logReplayResult) {
			Iterator<Object> nit = repResult.getNodeInstance().iterator();
			Iterator<StepTypes> sit = repResult.getStepTypes().iterator();
			while (sit.hasNext()) {
				switch (sit.next()) {
					case L :
						if (existsMoveLogOnly[mapEc2Int.get(nit.next())]) {
							filter[idx++] = true;
							flagTraceIndices(caseFilter, repResult);
							continue repResultLoop;
						}
						break;
					case MINVI :
					case MREAL :
						if (existsMoveModelOnly[mapTrans2Idx.get(nit.next())]) {
							filter[idx++] = true;
							flagTraceIndices(caseFilter, repResult);
							continue repResultLoop;
						}
						break;
					case LMGOOD :
						if (existMoveSync[mapTrans2Idx.get(nit.next())]) {
							filter[idx++] = true;
							flagTraceIndices(caseFilter, repResult);
							continue repResultLoop;
						}
						break;
					default :
						// do nothing
						break;
				}
			}
			// not wanted by all filtering
			filter[idx++] = false;
		}

		this.provider.extractInfo(filter, null);

		// reconstruct stats panel
		statPanel.setInfoProvider(provider);
		statPanel.repaint();

		// select corresponding cases in filter panel
		caseFilterPanel.selectCases(caseFilter);

		constructVisualization(graph.getViewSpecificAttributes(), true, true);
		constructPlaceVisualization(graph.getViewSpecificAttributes());

		validate();
		repaint();
	}

	protected void flagTraceIndices(boolean[] caseFilter, SyncReplayResult repResult) {
		for (int idx : repResult.getTraceIndex()) {
			caseFilter[idx] = true;
		}
	}

	public void filterAlignmentPreserveIndex(Set<Integer> preservedIndex) {
		// if index of a trace exists in the preservedIndex array, the trace is visualized
		this.provider.extractInfo(null, preservedIndex);

		// select the corresponding cases
		boolean[] filter = new boolean[this.log.getTraces().size()];
		Arrays.fill(filter, false);
		for (int i : preservedIndex) {
			filter[i] = true;
		}
		caseFilterPanel.selectCases(filter);

		// reconstruct stats panel
		statPanel.setInfoProvider(provider);
		statPanel.repaint();

		constructVisualization(graph.getViewSpecificAttributes(), true, true);
		constructPlaceVisualization(graph.getViewSpecificAttributes());

		validate();
		repaint();
	}

	public ViewSpecificAttributeMap getViewSpecificAttributeMap() {
		return graph.getViewSpecificAttributes();
	}
	
}

