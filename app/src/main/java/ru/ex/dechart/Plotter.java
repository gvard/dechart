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

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Plotter extends AppCompatActivity {

	private GraphicalView mChart;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private XYSeries mCurrentSeries;
	private XYSeriesRenderer mCurrentRenderer;
	final String LOG_TAG = "myLogs";
	List<double[]> x = new ArrayList<double[]>();
	List<int[]> vals = new ArrayList<int[]>();
	double[] xes;
	int[] vls;
	int orbeg;
	int orend;

	public void mkSeriesRenderer() {
		// Use actual data size instead of expected range
		int actualDataCount = Math.min(x.size(), vals.size());
		if (actualDataCount == 0) {
			Log.e(LOG_TAG, "No data available to render");
			return;
		}

		String[] titles = new String[actualDataCount];
		for (int i = 0; i < actualDataCount; i++) {
			titles[i] = "Order #" + String.valueOf(orbeg + i);
		}

		int[] clrs = new int[] { Color.BLUE, Color.GREEN, Color.CYAN,
				Color.YELLOW, Color.RED, Color.LTGRAY, Color.MAGENTA,
				Color.WHITE }; // Color.DKGRAY
		int[] colors = new int[actualDataCount];
		for (int i = 0; i < actualDataCount; i++) {
			colors[i] = clrs[i % clrs.length];
		}

		PointStyle[] styls = new PointStyle[] { PointStyle.CIRCLE,
				PointStyle.DIAMOND, PointStyle.TRIANGLE, PointStyle.SQUARE };
		PointStyle[] styles = new PointStyle[actualDataCount];
		for (int i = 0; i < actualDataCount; i++) {
			Log.d(LOG_TAG, String.valueOf(i % clrs.length));
			styles[i] = styls[i % styls.length];
		}

		int scale = 0;
		for (int i = 0; i < actualDataCount; i++) {
			mCurrentSeries = new XYSeries(titles[i], scale);
			double[] xV = x.get(i);
			int[] yV = vals.get(i);

			// Ensure arrays are not null and have data
			if (xV != null && yV != null && xV.length > 0 && yV.length > 0) {
				int seriesLength = Math.min(xV.length, yV.length);
				for (int k = 0; k < seriesLength; k++) {
					mCurrentSeries.add(xV[k], yV[k]);
				}
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

		Intent intent = getIntent();
		Bundle bu = intent.getExtras();
		if (bu != null) {
			orbeg = intent.getIntExtra("orbeg", 0);
			orend = intent.getIntExtra("orend", 0);

			// Validate received data
			if (orbeg <= 0 || orend <= 0 || orend < orbeg) {
				Log.e(LOG_TAG, "Invalid orbeg/orend values: orbeg=" + orbeg + ", orend=" + orend);
				finish(); // Close activity if invalid data
				return;
			}

			for (int i = orbeg - 1; i < orend; i++) {
				String alab = String.valueOf(i);
				String blab = String.valueOf(500 + i);
				xes = (double[]) bu.getSerializable(alab);
				vls = (int[]) bu.getSerializable(blab);

				// Check if data exists
				if (xes == null || vls == null) {
					Log.e(LOG_TAG, "Missing data for order " + (i + 1) + ": xes=" + (xes != null) + ", vls=" + (vls != null));
					continue; // Skip this order if data is missing
				}

				// Check if arrays have the same length
				if (xes.length != vls.length) {
					Log.e(LOG_TAG, "Array length mismatch for order " + (i + 1) + ": xes.length=" + xes.length + ", vls.length=" + vls.length);
					continue; // Skip this order if arrays don't match
				}

				x.add(xes);
				vals.add(vls);
			}
		} else {
			Log.e(LOG_TAG, "No bundle data received");
			finish(); // Close activity if no data
			return;
		}

		// Check if we have any valid data
		if (x.isEmpty() || vals.isEmpty()) {
			Log.e(LOG_TAG, "No valid data to plot");
			finish(); // Close activity if no data to plot
			return;
		}

		Log.d(LOG_TAG, "Complete gettin arrays!");

	}

	@Override
	protected void onResume() {
		super.onResume();
		LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
		if (mChart == null) {
			mkSeriesRenderer();

			// Check if we have any series to display
			if (mDataset.getSeriesCount() == 0) {
				Log.e(LOG_TAG, "No chart series created - no data to display");
				Toast.makeText(this, "No data available to plot", Toast.LENGTH_LONG).show();
				finish(); // Close activity if no data
				return;
			}

			// mRenderer.setInScroll(true);
			Log.d(LOG_TAG, "Setin props!");
			mRenderer.setXLabels(9);
			mRenderer.setYLabels(6);
			mRenderer.setLabelsTextSize(26f);
			// mRenderer.setAxisTitleTextSize(15);
			// mRenderer.setAxisTitleTextSize(24f);
			mRenderer.setLegendTextSize(32f);
			// mRenderer.setClickEnabled(true);
			mRenderer.setZoomButtonsVisible(false);
			mRenderer.setZoomEnabled(true);
			mRenderer.setExternalZoomEnabled(true);
			mRenderer.setMargins(new int[] { 6, 90, 40, 6 });
			mRenderer.setYLabelsAlign(android.graphics.Paint.Align.RIGHT);
			mRenderer.setXLabelsAlign(android.graphics.Paint.Align.CENTER);
			mRenderer.setFitLegend(true);
			mRenderer.setInScroll(false);
			mRenderer.setPointSize(1f);
			mRenderer.setShowGrid(true);
			mRenderer.setApplyBackgroundColor(true);
			mRenderer.setBackgroundColor(Color.BLACK);
			mRenderer.setMarginsColor(Color.parseColor("#121212"));
			Log.d(LOG_TAG, "Entering ChartFactory!");
			mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
			// mChart = ChartFactory.getCubeLineChartView(this, mDataset,
			// mRenderer, 1f);
			layout.addView(mChart);

			final android.view.GestureDetector gestureDetector = new android.view.GestureDetector(this,
                new android.view.GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(android.view.MotionEvent e) {
                        if (mChart != null) {
                            mChart.zoomReset();
                            Toast.makeText(Plotter.this, "Zoom reset", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });

            mChart.setOnTouchListener(new android.view.View.OnTouchListener() {
                @Override
                public boolean onTouch(android.view.View v, android.view.MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return false;
                }
            });
			// Log.d(LOG_TAG, "End of onResume");
		} else {
			mChart.repaint();
		}
	}
	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mChart != null) {
			mChart.repaint();
		}
	}
}
