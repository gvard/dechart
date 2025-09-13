package ru.ex.dechart;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class Plotter extends Activity {

	private GraphicalView mChart;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private XYSeries mCurrentSeries;
	private XYSeriesRenderer mCurrentRenderer;
	private Button butzout;
	private Button butzin;
	private Button butone;
	final String LOG_TAG = "myLogs";
	List<double[]> x = new ArrayList<double[]>();
	List<int[]> vals = new ArrayList<int[]>();
	double[] xes;
	int[] vls;
	int orbeg;
	int orend;

	public void mkSeriesRenderer() {
		int lengt = orend - orbeg + 1;
		String[] titles = new String[lengt];
		for (int i = orbeg; i <= orend; i++) {
			titles[i - orbeg] = "Order #" + String.valueOf(i);
		}
		int[] clrs = new int[] { Color.BLUE, Color.GREEN, Color.CYAN,
				Color.YELLOW, Color.RED, Color.LTGRAY, Color.MAGENTA,
				Color.WHITE }; // Color.DKGRAY
		int[] colors = new int[lengt];
		for (int i = 0; i <= orend - orbeg; i++) {
			colors[i] = clrs[i % clrs.length];
		}

		PointStyle[] styls = new PointStyle[] { PointStyle.CIRCLE,
				PointStyle.DIAMOND, PointStyle.TRIANGLE, PointStyle.SQUARE };
		PointStyle[] styles = new PointStyle[lengt];
		for (int i = 0; i <= orend - orbeg; i++) {
			Log.d(LOG_TAG, String.valueOf(i % clrs.length));
			styles[i] = styls[i % styls.length];
		}
		int scale = 0;
		for (int i = 0; i < lengt; i++) {
			mCurrentSeries = new XYSeries(titles[i], scale);
			double[] xV = x.get(i);
			int[] yV = vals.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				mCurrentSeries.add(xV[k], yV[k]);
			}
			mDataset.addSeries(mCurrentSeries);
			mCurrentRenderer = new XYSeriesRenderer();
			mCurrentRenderer.setColor(colors[i]);
			mCurrentRenderer.setPointStyle(styles[i]);
			mRenderer.addSeriesRenderer(mCurrentRenderer);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plot);
		Log.d(LOG_TAG, "333");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		butzin = (Button) findViewById(R.id.butzin);
		butzout = (Button) findViewById(R.id.butzout);
		butone = (Button) findViewById(R.id.butone);

		Intent intent = getIntent();
		Bundle bu = intent.getExtras();
		orbeg = intent.getIntExtra("orbeg", 0);
		orend = intent.getIntExtra("orend", 0);
		for (int i = orbeg - 1; i < orend; i++) {
			String alab = String.valueOf(i);
			String blab = String.valueOf(500 + i);
			xes = (double[]) bu.getSerializable(alab);
			x.add(xes);
			vls = (int[]) bu.getSerializable(blab);
			vals.add(vls);
		}
		Log.d(LOG_TAG, "Complete gettin arrays!");

		butzin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mChart.zoomIn();
			}
		});
		butzout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mChart.zoomOut();
			}
		});
		butone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mChart.zoomReset();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
		if (mChart == null) {
			mkSeriesRenderer();
			// mRenderer.setInScroll(true);
			Log.d(LOG_TAG, "Setin props!");
			mRenderer.setXLabels(9);
			mRenderer.setYLabels(6);
			// mRenderer.setAxisTitleTextSize(15);
			mRenderer.setLabelsTextSize(15);
			// mRenderer.setClickEnabled(true);
			mRenderer.setZoomButtonsVisible(true);
			mRenderer.setZoomEnabled(true);
			mRenderer.setExternalZoomEnabled(true);
			mRenderer.setMargins(new int[] { 0, 18, -110, 0 });
			mRenderer.setPointSize(1f);
			mRenderer.setShowGrid(true);
			mRenderer.setApplyBackgroundColor(true);
			mRenderer.setBackgroundColor(Color.BLACK);
			mRenderer.setMarginsColor(Color.DKGRAY);
			Log.d(LOG_TAG, "Entering ChartFactory!");
			mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
			// mChart = ChartFactory.getCubeLineChartView(this, mDataset,
			// mRenderer, 1f);
			layout.addView(mChart);
		} else {
			mChart.repaint();
		}
	}
}