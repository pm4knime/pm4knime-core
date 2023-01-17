package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;

public class ZoomManifestPanelTable <N extends ManifestEvClassPatternTable, C extends PerfCounterTable> extends JPanel {

	private static final long serialVersionUID = -7355984875795224279L;

	// GUI component
	private final JSlider slider;
	private JLabel sliderMinValue, sliderMaxValue;
	JButton sliderFitValue;
	JLabel sliderValue;
	PIPManifestPanelTable<N, C> pip;

	// internal component
	public static final int ZOOMHEIGHT = 200;
	protected int fitZoom;
	protected final ManifestPerfPanelTable<N,C> mainView;

	public ZoomManifestPanelTable(ManifestPerfPanelTable<N,C> mainPanel, PIPManifestPanelTable<N,C> pip, int maximumZoom) {
		this.mainView = mainPanel;
		this.pip = pip;

		double[][] size = new double[][] { { TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED },
				{ 30, TableLayout.PREFERRED, 30 } };
		setLayout(new TableLayout(size));

		this.slider = SlickerFactory.instance().createSlider(1);

		this.slider.setMinimum(1);
		this.slider.setMaximum(maximumZoom);
		this.slider.setValue(fitZoom);
		this.slider.setOrientation(JSlider.HORIZONTAL);

		this.slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				update();
			}
		});

		this.sliderMinValue = SlickerFactory.instance().createLabel("0%");
		this.sliderMaxValue = SlickerFactory.instance().createLabel(maximumZoom + "%");
		this.sliderFitValue = SlickerFactory.instance().createButton("Fit >");
		this.sliderFitValue.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				fit();
			}
		});
		this.sliderValue = SlickerFactory.instance().createLabel(fitZoom + "%");

		this.sliderMinValue.setHorizontalAlignment(SwingConstants.CENTER);
		this.sliderMaxValue.setHorizontalAlignment(SwingConstants.CENTER);

		this.sliderMinValue.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
		this.sliderMaxValue.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
		this.sliderValue.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));

		this.sliderMinValue.setForeground(Color.GRAY);
		this.sliderMaxValue.setForeground(Color.GRAY);
		this.sliderFitValue.setForeground(Color.GRAY);
		this.sliderValue.setForeground(Color.DARK_GRAY);

		this.add(this.slider, "0,0,2,0,c,b");
		this.add(this.sliderMinValue, "0,1,l,t");
		this.add(this.sliderMaxValue, "2,1,r,t");
		this.add(this.sliderFitValue, "0,2,2,2");
		this.add(this.sliderValue, "1,1,c,t");

		this.setBackground(Color.LIGHT_GRAY);

		slider.setValue((int) Math.floor(100 * this.mainView.getScalable().getScale()));
	}

	private void update() {
		int value = this.slider.getValue();
		this.sliderValue.setText(value + "%");
		this.mainView.getScalable().setScale((double)(slider.getValue()) / 100);
	}

	public void fit() {
		slider.setValue(fitZoom);
	}
	
	public void zoomIn(){
		if (slider.getValue() + 5 < slider.getMaximum()){
			slider.setValue(slider.getValue() + 5);
		} else {
			slider.setValue(slider.getMaximum());
		}
	}

	public void zoomOut(){
		if (slider.getValue() - 5 > slider.getMinimum()){
			slider.setValue(slider.getValue() - 5);
		} else {
			slider.setValue(slider.getMinimum());
		}
	}
	
	public void computeFitScale() {

		double scale = this.mainView.getScalable().getScale();
		Dimension b = this.mainView.getScalable().getComponent().getPreferredSize();
		double w = b.getWidth() / scale;
		double h = b.getHeight() / scale;
		double rx = this.mainView.getViewport().getExtentSize().getWidth() / w;
		double ry = this.mainView.getViewport().getExtentSize().getHeight() / h;

		setFitValue((int) (Math.min(rx, ry) * 100));
	}

	private void setFitValue(int value) {

		this.fitZoom = value;

		int span = this.slider.getMaximum() - this.slider.getMinimum();
		int position = (int) (33 + Math.floor(((float) (this.slider.getMaximum() - value) / (float) span * (this.slider
				.getBounds().height - 28))));
		this.sliderFitValue.setBounds(0, position, 45, 25);
	}
}

