package ru.ex.dechart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Params extends Activity {

	EditText orbg;
	EditText oren;
	TextView txtView;
	final String LOG_TAG = "myLogs";
	final String VECDIR = "Vectors";
	private File mPath = new File(Environment.getExternalStorageDirectory()
			+ "/" + VECDIR);
	private File vec_fil = new File(mPath.getAbsolutePath() + "/data.100");
	private File fds_fil = new File(mPath.getAbsolutePath() + "/waves.fds");
	int[][] datas;
	double[][] fdz;
	private int fcut = 8;
	private int lcut = 2030;
	int onum = 0;
	int olen = 0;
	int orbeg = 0;
	int orend = 0;

	public int lit2big(byte[] b) {
		return ((b[3] & 0xff) << 24) + ((b[2] & 0xff) << 16)
				+ ((b[1] & 0xff) << 8) + (b[0] & 0xff);
	}

	public int little2big(byte[] b) {
		return ((b[1] & 0xff) << 8) + (b[0] & 0xff);
	}

	public void onclick(View v) {
		switch (v.getId()) {
		case R.id.btnReadBin:
			try {
				Log.d(LOG_TAG, "Begin readbinfil");
				orend = Integer.parseInt(oren.getText().toString());
				datas = ReadBinFil();
				String a = "Datz dims: " + String.valueOf(onum) + " "
						+ String.valueOf(olen);
				Log.d(LOG_TAG, a);
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(),
						"Problems: " + e.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
			try {
				fdz = ReadFds();
				int l = fdz.length;
				int n = fdz[0].length;
				String a = "Fdz dims: " + String.valueOf(l) + " "
						+ String.valueOf(n);
				Log.d(LOG_TAG, a);
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(),
						"Problems: " + e.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
			break;

		case R.id.btnChOrd:
			orbeg = Integer.parseInt(orbg.getText().toString());
			// orend = Integer.parseInt(oren.getText().toString());
			String a = "Values: " + String.valueOf(orbeg) + " "
					+ String.valueOf(orend);
			// txtView.setText(a);
			Log.d(LOG_TAG, a);
			String b = "Datas dims: " + String.valueOf(datas.length) + " "
					+ String.valueOf(datas[0].length);
			Log.d(LOG_TAG, b);
			if (olen > 13000) {
				lcut = 13000;
			} else {
				lcut = olen - fcut;
			}
			Bundle bu = new Bundle();
			for (int i = orbeg - 1; i < orend; i++) {
				int[] dada = Arrays.copyOfRange(datas[i], fcut, lcut);
				double[] xaxa = Arrays.copyOfRange(fdz[i], fcut, lcut);
				bu.putSerializable(String.valueOf(i), xaxa);
				bu.putSerializable(String.valueOf(i + 500), dada);
			}
			Intent intent = new Intent(this, Plotter.class);
			intent.putExtra("orbeg", orbeg);
			intent.putExtra("orend", orend);
			intent.putExtras(bu);
			startActivity(intent);
			break;

		case R.id.btnDlgBin:
			FileDialog fileDlg1 = new FileDialog(this, mPath);
			// fileDialog.setFileEndsWith(FTYP_BIN);
			fileDlg1.addFileListener(new FileDialog.FileSelectedListener() {
				@Override
				public void fileSelected(File file) {
					vec_fil = file;
					Log.d(LOG_TAG, "selected file " + vec_fil);
				}
			});
			fileDlg1.setSelectDirectoryOption(false);
			fileDlg1.showDialog();
			break;

		case R.id.btnDlgFds:
			FileDialog fileDlg2 = new FileDialog(this, mPath);
			// fileDialog.setFileEndsWith(FTYP_FDS);
			fileDlg2.addFileListener(new FileDialog.FileSelectedListener() {
				@Override
				public void fileSelected(File file) {
					fds_fil = file;
					Log.d(LOG_TAG, "selected file " + fds_fil);
				}
			});
			fileDlg2.setSelectDirectoryOption(false);
			fileDlg2.showDialog();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.params);
		txtView = (TextView) findViewById(R.id.txtView);
		orbg = (EditText) findViewById(R.id.edtOrbeg);
		oren = (EditText) findViewById(R.id.edtOrend);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.params, menu);
		return true;
	}

	public int[][] ReadBinFil() throws IOException {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Log.d(LOG_TAG,
					"SDcard not avaliable: "
							+ Environment.getExternalStorageState());
			return null;
		}
		try {
			RandomAccessFile fp = new RandomAccessFile(vec_fil, "r");
			int next = 0;
			byte[] buffer = new byte[2];
			fp.seek(10);
			fp.read(buffer);
			onum = little2big(buffer);
			fp.read(buffer);
			olen = little2big(buffer);
			String a = "reading 100! Orders & Length: " + String.valueOf(onum)
					+ " " + String.valueOf(olen);
			Log.d(LOG_TAG, "Begin " + a);
			if (onum < orend) {
				orend = onum;
				oren.setText(String.valueOf(onum));
			}
			onum = orend;
			int[][] datz = new int[onum][olen];
			for (int i = 0; i < onum; i++) {
				for (int j = 0; j < olen; j++) {

					fp.read(buffer);
					next = little2big(buffer);
					datz[i][j] = next;
				}
			}
			int l = datz.length;
			int n = datz[0].length;
			Log.d(LOG_TAG,
					"Complete " + a + ", Datz dims: " + String.valueOf(l) + " "
							+ String.valueOf(n));
			txtView.setText(a);
			fp.close();
			return datz;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public double[][] ReadFds() throws IOException {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Log.d(LOG_TAG,
					"SDcard not avaliable: "
							+ Environment.getExternalStorageState());
			return null;
		}
		int next = 0;
		Log.d(LOG_TAG, "ReadFds beg with len " + String.valueOf(onum) + ", "
				+ String.valueOf(olen));
		try {
			RandomAccessFile fp = new RandomAccessFile(fds_fil, "r");
			byte[] buffer = new byte[4];
			double[][] fdz = new double[onum][olen];
			for (int i = 0; i < onum; i++) {
				for (int j = 0; j < olen; j++) {
					fp.read(buffer);
					next = lit2big(buffer);
					fdz[i][j] = (double) next / 10000;
				}
			}
			String a = "Complete Reading fds! Orders & Length: "
					+ String.valueOf(onum) + " " + String.valueOf(olen);
			String b = String.valueOf(fdz[0][0]) + " - "
					+ String.valueOf(fdz[onum - 1][olen - 1]);
			Log.d(LOG_TAG, a + ", wvrange " + b);
			txtView.setText(a + ", wvrange " + b);
			fp.close();
			return fdz;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}