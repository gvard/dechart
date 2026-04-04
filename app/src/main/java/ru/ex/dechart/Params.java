package ru.ex.dechart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Params extends AppCompatActivity {

	private static final int PERMISSION_REQUEST_CODE = 100;
	
	EditText orbg;
	EditText oren;
	TextView txtView;
	final String LOG_TAG = "myLogs";
	final String VECDIR = "Vectors";
	private File mPath;
	private File vec_fil;
	private File fds_fil;
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
	
	/**
	 * Check and request permissions for file access
	 */
	private void checkAndRequestPermissions() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
							Manifest.permission.WRITE_EXTERNAL_STORAGE },
					PERMISSION_REQUEST_CODE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
			int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSION_REQUEST_CODE) {
			if ((grantResults.length > 0)
					&& (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
				Log.d(LOG_TAG, "Permission granted");
			} else {
				Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
			}
		}
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
		
		// Initialize file paths
		File storageDir = getExternalFilesDir(VECDIR);
		if (storageDir != null && !storageDir.exists()) {
			storageDir.mkdirs();
		}
		mPath = storageDir;
		vec_fil = new File(mPath.getAbsolutePath() + "/data.100");
		fds_fil = new File(mPath.getAbsolutePath() + "/waves.fds");
		
		txtView = (TextView) findViewById(R.id.txtView);
		orbg = (EditText) findViewById(R.id.edtOrbeg);
		oren = (EditText) findViewById(R.id.edtOrend);
		
		// Check permissions
		checkAndRequestPermissions();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.params, menu);
		return true;
	}

	public int[][] ReadBinFil() throws IOException {
		RandomAccessFile file = new RandomAccessFile(vec_fil, "r");
		int fl = (int) file.length();
		byte[] b = new byte[4];

		// read header
		file.read(b);
		onum = lit2big(b);
		file.read(b);
		olen = lit2big(b);

		Log.d(LOG_TAG, "Onum: " + onum + " Olen: " + olen);

		int[][] myarray = new int[onum][olen];

		for (int i = 0; i < onum; i++) {
			for (int j = 0; j < olen; j++) {
				file.read(b);
				myarray[i][j] = lit2big(b);
			}
		}
		file.close();
		return myarray;
	}

	public double[][] ReadFds() throws IOException {
		RandomAccessFile file = new RandomAccessFile(fds_fil, "r");
		byte[] b = new byte[2];

		// read header
		file.read(b);
		int xlen = little2big(b);
		file.read(b);
		int ylen = little2big(b);

		Log.d(LOG_TAG, "Xlen: " + xlen + " Ylen: " + ylen);

		double[][] myarray = new double[ylen][xlen];

		for (int i = 0; i < ylen; i++) {
			for (int j = 0; j < xlen; j++) {
				file.read(b);
				myarray[i][j] = (double) little2big(b);
			}
		}
		file.close();
		return myarray;
	}
}
