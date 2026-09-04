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
import org.achartengine.tools.Zoom;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.graphics.DashPathEffect;

public class Plotter extends AppCompatActivity {

	private GraphicalView mChart;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private org.achartengine.tools.Zoom mPinchZoomEngine;
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

			mCurrentRenderer.setPointStyle(org.achartengine.chart.PointStyle.POINT);

			mCurrentRenderer.setLineWidth(2f);
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
			// mRenderer.setAxisTitleTextSize(24f);
			mRenderer.setLegendTextSize(32f);
			// mRenderer.setClickEnabled(true);
			mRenderer.setZoomButtonsVisible(false);
			mRenderer.setZoomEnabled(true);
			mRenderer.setPanEnabled(true);
			mRenderer.setExternalZoomEnabled(false);
			mRenderer.setMargins(new int[] { 6, 90, 40, 6 });
			mRenderer.setYLabelsAlign(android.graphics.Paint.Align.RIGHT);
			mRenderer.setXLabelsAlign(android.graphics.Paint.Align.CENTER);
			mRenderer.setFitLegend(true);
			mRenderer.setInScroll(false);
			// mRenderer.setPointSize(4f);
			mRenderer.setShowGrid(true);

			mRenderer.setGridColor(android.graphics.Color.parseColor("#333333"));
			mRenderer.setAxesColor(android.graphics.Color.parseColor("#777777"));
			mRenderer.setLabelsColor(android.graphics.Color.parseColor("#777777"));
			mRenderer.setXLabelsColor(android.graphics.Color.parseColor("#777777"));
            mRenderer.setYLabelsColor(0, android.graphics.Color.parseColor("#777777"));

			mRenderer.setApplyBackgroundColor(true);
			mRenderer.setBackgroundColor(Color.BLACK);
			mRenderer.setMarginsColor(Color.parseColor("#121212"));
			mRenderer.setTextTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));

			Log.d(LOG_TAG, "Entering ChartFactory!");
			mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
			layout.addView(mChart);

			mPinchZoomEngine = new org.achartengine.tools.Zoom(mChart.getChart(), true, 1);

			final double initialXMin = mRenderer.getXAxisMin();
			final double initialXMax = mRenderer.getXAxisMax();
			final double initialYMin = mRenderer.getYAxisMin();
			final double initialYMax = mRenderer.getYAxisMax();
			updateAdaptivePointSizing(true);

			final android.view.GestureDetector nativeGestureDetector = new android.view.GestureDetector(this,
                new android.view.GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(android.view.MotionEvent e) {

                        mRenderer.setPanEnabled(false, false);
                        mRenderer.setXAxisMin(initialXMin);
                        mRenderer.setXAxisMax(initialXMax);
                        mRenderer.setYAxisMin(initialYMin);
                        mRenderer.setYAxisMax(initialYMax);
						updateAdaptivePointSizing(true);
                        mChart.repaint();
                        return true;
                    }
                });

            mChart.setOnTouchListener(new View.OnTouchListener() {
                private float startX = -1, startY = -1, startX2 = -1, startY2 = -1;
                private boolean isPinch = false;
                private long lastPinchTime = 0;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction() & MotionEvent.ACTION_MASK;
                    long currentTime = System.currentTimeMillis();

                    nativeGestureDetector.onTouchEvent(event);

                    if (action == MotionEvent.ACTION_DOWN) {
                        startX = event.getX(0);
                        startY = event.getY(0);

                        if (currentTime - lastPinchTime < 300) {
                            mRenderer.setPanEnabled(true, true);
                        }
                    }

                    if (!isPinch && (currentTime - lastPinchTime < 300)) {
                        if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                            mRenderer.setPanEnabled(false, false);
                            return true;
                        }
                    } else if (!isPinch) {
                        mRenderer.setPanEnabled(true, true);
                    }

                    if (event.getPointerCount() == 1 && action == MotionEvent.ACTION_MOVE) {
                        startX = event.getX(0);
                        startY = event.getY(0);
                    }

                    if (event.getPointerCount() > 1) {
                        isPinch = true;
                        mRenderer.setPanEnabled(false, false);

                        if (action == MotionEvent.ACTION_POINTER_DOWN) {
                            startX = event.getX(0); startY = event.getY(0);
                            startX2 = event.getX(1); startY2 = event.getY(1);
                            return true;
                        }

                        if (action == MotionEvent.ACTION_MOVE && startX >= 0 && startY >= 0 && startX2 >= 0 && startY2 >= 0) {
                            float newX = event.getX(0);
                            float newY = event.getY(0);
                            float newX2 = event.getX(1);
                            float newY2 = event.getY(1);

                            float currentDeltaX = Math.abs(newX - newX2);
                            float currentDeltaY = Math.abs(newY - newY2);
                            float startDeltaX = Math.abs(startX - startX2);
                            float startDeltaY = Math.abs(startY - startY2);

                            float tan1 = Math.abs(newY - startY) / (Math.abs(newX - startX) + 0.001f);
                            float tan2 = Math.abs(newY2 - startY2) / (Math.abs(newX2 - startX2) + 0.001f);

                            float zoomRateX = currentDeltaX / (startDeltaX + 0.001f);
                            float zoomRateY = currentDeltaY / (startDeltaY + 0.001f);

                            // Горизонтальный жест — масштабируем X
                            if (tan1 <= 0.7f && tan2 <= 0.7f) {
                                mPinchZoomEngine.setZoomRate(zoomRateX);
                                mPinchZoomEngine.apply(org.achartengine.tools.Zoom.ZOOM_AXIS_X);
								updateAdaptivePointSizing(false);
                            }
                            // Вертикальный жест — масштабируем Y
                            else if (tan1 >= 1.4f && tan2 >= 1.4f) {
                                mPinchZoomEngine.setZoomRate(zoomRateY);
                                mPinchZoomEngine.apply(org.achartengine.tools.Zoom.ZOOM_AXIS_Y);
								updateAdaptivePointSizing(false);
                            }
                            // Диагональный жест — масштабируем обе оси
                            else {
                                float combinedRate = (Math.abs(newX - startX) >= Math.abs(newY - startY)) ? zoomRateX : zoomRateY;
                                mPinchZoomEngine.setZoomRate(combinedRate);
                                mPinchZoomEngine.apply(org.achartengine.tools.Zoom.ZOOM_AXIS_XY);
								updateAdaptivePointSizing(false);
                            }

                            startX = newX; startY = newY; startX2 = newX2; startY2 = newY2;
                            mChart.repaint();
                            return true;
                        }
                        return true;
                    }

                    // Завершение жеста мультитача
                    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                        if (isPinch) {
                            isPinch = false;
                            lastPinchTime = System.currentTimeMillis();
                            mRenderer.setPanEnabled(false, false);
                            startX = -1; startY = -1; startX2 = -1; startY2 = -1;
                            mChart.repaint();
                            return true;
                        }
                    }
                    return false;
                }
            });
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

    private void updateAdaptivePointSizing(boolean forceMicroPoints) {
        if (mRenderer != null && mChart != null) {
            double xMin = mRenderer.getXAxisMin();
            double xMax = mRenderer.getXAxisMax();
            double currentRange = xMax - xMin;

            // Вычисляем оптимальный размер точек
            float calculatedSize = 4.0f;
            boolean useMicroPoints = forceMicroPoints; // Если передан флаг — сразу включаем микро-точки

            if (!useMicroPoints) {
                if (currentRange > 150 || currentRange == 0.0) {
                    calculatedSize = 1.0f;
                    useMicroPoints = true;
                } else if (currentRange > 50) {
                    calculatedSize = 2.0f;
                } else {
                    calculatedSize = 4.0f;
                }
            } else {
                calculatedSize = 1.0f;
            }

            mRenderer.setPointSize(calculatedSize);

            org.achartengine.chart.PointStyle[] styls = new org.achartengine.chart.PointStyle[] {
                org.achartengine.chart.PointStyle.CIRCLE,
                org.achartengine.chart.PointStyle.DIAMOND,
                org.achartengine.chart.PointStyle.TRIANGLE,
                org.achartengine.chart.PointStyle.SQUARE
            };

            int seriesCount = mRenderer.getSeriesRendererCount();
            for (int i = 0; i < seriesCount; i++) {
                org.achartengine.renderer.SimpleSeriesRenderer r = mRenderer.getSeriesRendererAt(i);
                if (r instanceof org.achartengine.renderer.XYSeriesRenderer) {
                    org.achartengine.renderer.XYSeriesRenderer xyr = (org.achartengine.renderer.XYSeriesRenderer) r;

                    if (useMicroPoints) {
                        xyr.setPointStyle(org.achartengine.chart.PointStyle.POINT);
                        xyr.setPointStrokeWidth(1f);
                    } else {
                        // Режим зума
                        xyr.setPointStyle(styls[i % styls.length]);
                        xyr.setPointStrokeWidth(calculatedSize >= 4.0f ? 5f : 1f);
                    }
                }
            }
        }
    }
}
