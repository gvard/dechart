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
	private static final int REQUEST_CODE_PICK_100 = 201;
	private static final int REQ_PICK_BIN = 301;
	private static final int REQ_PICK_FDS = 302;

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

	public void onclick(View v) {
		switch (v.getId()) {
		case R.id.btnReadBin:
			try {
				// Validate input
				String orendText = oren.getText().toString().trim();
				if (orendText.isEmpty()) {
					Toast.makeText(getApplicationContext(),
							"Please enter a value for 'orend' (end order)", Toast.LENGTH_LONG)
							.show();
					return;
				}

				Log.d(LOG_TAG, "Begin readbinfil");
				orend = Integer.parseInt(orendText);

				// Validate orend value
				if (orend <= 0) {
					Toast.makeText(getApplicationContext(),
							"'orend' must be a positive number", Toast.LENGTH_LONG)
							.show();
					return;
				}

				// Read 100 file from assets:
				// try (BufferedReader reader = new BufferedReader(
				// 		new InputStreamReader(assetManager.open("data.100"), "UTF-8"))) {
				// 	String line;
				// 	while ((line = reader.readLine()) != null) {
				// 		// Process each line
				// 	}
				// } catch (IOException e) {
				// 	e.printStackTrace();
				// }

				datas = ReadBinFil();
				String a = "Datz dims: " + String.valueOf(onum) + " "
						+ String.valueOf(olen);
				Log.d(LOG_TAG, a);
				Toast.makeText(getApplicationContext(),
						"Binary file loaded successfully. " + a, Toast.LENGTH_SHORT)
						.show();
			} catch (NumberFormatException e) {
				Toast.makeText(getApplicationContext(),
						"Invalid number format for 'orend'. Please enter a valid integer.", Toast.LENGTH_LONG)
						.show();
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error reading binary file", e);
				Toast.makeText(getApplicationContext(),
						"Error reading binary file: " + e.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
			try {
				fdz = ReadFds();
				int l = fdz.length;
				int n = fdz[0].length;
				String a = "FDS dims: " + String.valueOf(l) + " "
						+ String.valueOf(n);
				Log.d(LOG_TAG, a);
				Toast.makeText(getApplicationContext(),
						"FDS file loaded successfully. " + a, Toast.LENGTH_SHORT)
						.show();
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error reading FDS file", e);
				Toast.makeText(getApplicationContext(),
						"Error reading FDS file: " + e.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
			break;

		case R.id.btnChOrd:
			try {
				// Validate input
				String orbegText = orbg.getText().toString().trim();
				String orendText = oren.getText().toString().trim();

				if (orbegText.isEmpty()) {
					Toast.makeText(getApplicationContext(),
							"Please enter a value for 'orbeg' (begin order)", Toast.LENGTH_LONG)
							.show();
					return;
				}

				if (orendText.isEmpty()) {
					Toast.makeText(getApplicationContext(),
							"Please enter a value for 'orend' (end order)", Toast.LENGTH_LONG)
							.show();
					return;
				}

				orbeg = Integer.parseInt(orbegText);
				orend = Integer.parseInt(orendText);

				// Validate values
				if (orbeg <= 0) {
					Toast.makeText(getApplicationContext(),
							"'orbeg' must be a positive number", Toast.LENGTH_LONG)
							.show();
					return;
				}

				if (orend <= 0) {
					Toast.makeText(getApplicationContext(),
							"'orend' must be a positive number", Toast.LENGTH_LONG)
							.show();
					return;
				}

				// Check if data has been loaded
				if (datas == null) {
					Toast.makeText(getApplicationContext(),
							"Please load binary data first using 'Read Bin Files' button", Toast.LENGTH_LONG)
							.show();
					return;
				}

				if (fdz == null) {
					Toast.makeText(getApplicationContext(),
							"Please load FDS data first using 'Read Bin Files' button", Toast.LENGTH_LONG)
							.show();
					return;
				}

				// Validate range
				if (orbeg > orend) {
					Toast.makeText(getApplicationContext(),
							"'orbeg' cannot be greater than 'orend'", Toast.LENGTH_LONG)
							.show();
					return;
				}

				if (orend > datas.length) {
					Toast.makeText(getApplicationContext(),
							"'orend' (" + orend + ") exceeds available data (" + datas.length + ")", Toast.LENGTH_LONG)
							.show();
					return;
				}

				// Validate that we have enough data in each array
				if (datas.length > 0 && datas[0].length < fcut) {
					Toast.makeText(getApplicationContext(),
							"Data arrays are too short for the current fcut value (" + fcut + ")", Toast.LENGTH_LONG)
							.show();
					return;
				}

				String a = "Values: " + String.valueOf(orbeg) + " "
						+ String.valueOf(orend);
				Log.d(LOG_TAG, a);
				String b = "Datas dims: " + String.valueOf(datas.length) + " "
						+ String.valueOf(datas[0].length);
				Log.d(LOG_TAG, b);

				if (olen > 13000) {
					lcut = 13000;
				} else {
					lcut = olen - fcut;
				}

				// Validate lcut
				if (lcut <= fcut) {
					Toast.makeText(getApplicationContext(),
							"Invalid data range: lcut (" + lcut + ") must be greater than fcut (" + fcut + ")", Toast.LENGTH_LONG)
							.show();
					return;
				}

				Bundle bu = new Bundle();
				for (int i = orbeg - 1; i < orend; i++) {
					try {
						int[] dada = Arrays.copyOfRange(datas[i], fcut, lcut);
						double[] xaxa = Arrays.copyOfRange(fdz[i], fcut, lcut);
						bu.putSerializable(String.valueOf(i), xaxa);
						bu.putSerializable(String.valueOf(i + 500), dada);
					} catch (Exception e) {
						Log.e(LOG_TAG, "Error copying data for order " + (i + 1), e);
						Toast.makeText(getApplicationContext(),
								"Error processing data for order " + (i + 1) + ": " + e.getMessage(), Toast.LENGTH_LONG)
								.show();
						return;
					}
				}
				Intent intent = new Intent(this, Plotter.class);
				intent.putExtra("orbeg", orbeg);
				intent.putExtra("orend", orend);
				intent.putExtras(bu);
				startActivity(intent);
			} catch (NumberFormatException e) {
				Toast.makeText(getApplicationContext(),
						"Invalid number format. Please enter valid integers.", Toast.LENGTH_LONG)
						.show();
			} catch (Exception e) {
				Log.e(LOG_TAG, "Error in choose order", e);
				Toast.makeText(getApplicationContext(),
						"Error processing data: " + e.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
			break;

		case R.id.btnDlgBin:
			Intent intentBin = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intentBin.addCategory(Intent.CATEGORY_OPENABLE);
			intentBin.setType("*/*"); // Позволяет выбирать файлы с любым расширением

			// Сразу открываем локальную папку Download
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				android.net.Uri downloadsUri = android.provider.DocumentsContract.buildDocumentUri(
					"com.android.externalstorage.documents", "primary:Download"
				);
				intentBin.putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, downloadsUri);
			}
			startActivityForResult(intentBin, REQ_PICK_BIN);
			break;

		case R.id.btnDlgFds:
			Intent intentFds = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intentFds.addCategory(Intent.CATEGORY_OPENABLE);
			intentFds.setType("*/*");

			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				android.net.Uri downloadsUri = android.provider.DocumentsContract.buildDocumentUri(
					"com.android.externalstorage.documents", "primary:Download"
				);
				intentFds.putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, downloadsUri);
			}
			startActivityForResult(intentFds, REQ_PICK_FDS);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && data != null && data.getData() != null) {
			android.net.Uri fileUri = data.getData();

			if (requestCode == REQ_PICK_BIN) {
				// Копируем выбранный бинарник во внутреннюю память
				File cacheFile = copyUriToAppStorage(fileUri, "selected_data.100");
				if (cacheFile != null) {
					vec_fil = cacheFile;
					Log.d(LOG_TAG, "SAF: cached bin file to " + vec_fil.getAbsolutePath());
					Toast.makeText(getApplicationContext(), "Binary file selected: " + vec_fil.getName(), Toast.LENGTH_SHORT).show();
					updateFileStatus();
				}
			}
			else if (requestCode == REQ_PICK_FDS) {
				// Копируем выбранный FDS файл во внутреннюю память
				File cacheFile = copyUriToAppStorage(fileUri, "selected_waves.fds");
				if (cacheFile != null) {
					fds_fil = cacheFile;
					Log.d(LOG_TAG, "SAF: cached fds file to " + fds_fil.getAbsolutePath());
					Toast.makeText(getApplicationContext(), "FDS file selected: " + fds_fil.getName(), Toast.LENGTH_SHORT).show();
					updateFileStatus();
				}
			}
		}
	}

	// Универсальный метод, обходящий Scoped Storage: читает файл из Загрузок по URI
	// и легально сохраняет его копию в доступную для приложения папку
	private File copyUriToAppStorage(android.net.Uri uri, String defaultName) {
		try {
			java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
			File targetDir = getExternalFilesDir(null);
			if (targetDir != null && !targetDir.exists()) {
				targetDir.mkdirs();
			}

			File outputCacheFile = new File(targetDir, defaultName);
			java.io.FileOutputStream outputStream = new java.io.FileOutputStream(outputCacheFile);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();
			return outputCacheFile;
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error caching file via SAF SAF: " + e.getMessage());
			Toast.makeText(this, "File access error!", Toast.LENGTH_LONG).show();
			return null;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    	);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.params);

		File storageDir = getExternalFilesDir(VECDIR);
		if (storageDir != null && !storageDir.exists()) {
			storageDir.mkdirs();
		}
		mPath = storageDir;

		// Copy files from assets
		vec_fil = getFileFromAssets(this, "data.100");
		fds_fil = getFileFromAssets(this, "waves.fds");

		txtView = (TextView) findViewById(R.id.txtView);
		orbg = (EditText) findViewById(R.id.edtOrbeg);
		oren = (EditText) findViewById(R.id.edtOrend);

		orbg.setText("1");
		oren.setText("3");

		updateFileStatus();
	}

	/**
	 * Update the TextView to show currently selected files
	 */
	private void updateFileStatus() {
		try {
			if (vec_fil != null && vec_fil.exists()) {
			}
			if (fds_fil != null && fds_fil.exists()) {
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Silent storage check bypass: " + e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.params, menu);
		return true;
	}

	public int[][] ReadBinFil() throws IOException {
		// Check if file is selected
		if (vec_fil == null) {
			throw new IOException("No binary file selected. Please use 'Select Bin File' button.");
		}

		RandomAccessFile file = new RandomAccessFile(vec_fil, "r");
		int fl = (int) file.length();

		// Check minimum file size (at least 8 bytes for header)
		if (fl < 8) {
			file.close();
			throw new IOException("Binary file is too small. Expected at least 8 bytes for header.");
		}

			int next = 0;
			byte[] buffer = new byte[2];
			file.seek(10);
			file.read(buffer);
			onum = little2big(buffer);
			file.read(buffer);
			olen = little2big(buffer);
			String a = ".100 file loaded. Orders & length: " + String.valueOf(onum)
					+ " x " + String.valueOf(olen);
			Log.d(LOG_TAG, "Begin " + a);
			if (onum < orend) {
				orend = onum;
				oren.setText(String.valueOf(onum));
			}
			onum = orend;
			int[][] datz = new int[onum][olen];
			for (int i = 0; i < onum; i++) {
				for (int j = 0; j < olen; j++) {

					file.read(buffer);
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
			file.close();
			return datz;

	}

	public double[][] ReadFds() throws IOException {
		// Check if file is selected
		if (fds_fil == null) {
			throw new IOException("No FDS file selected. Please use 'Select FDS File' button.");
		}

		RandomAccessFile file = new RandomAccessFile(fds_fil, "r");
			int next = 0;
			byte[] buffer = new byte[4];
			double[][] fdz = new double[onum][olen];
			for (int i = 0; i < onum; i++) {
				for (int j = 0; j < olen; j++) {
					file.read(buffer);
					next = lit2big(buffer);
					fdz[i][j] = (double) next / 10000;
				}
			}
			String a = "Complete Reading fds! Orders & Length: "
					+ String.valueOf(onum) + " " + String.valueOf(olen);
			String b = String.valueOf(fdz[0][0]) + " - "
					+ String.valueOf(fdz[onum - 1][olen - 1]);
			Log.d(LOG_TAG, a + ", wvrange " + b);
			// txtView.setText(a + ", wvrange " + b);
			file.close();
			return fdz;
	}
	private File getFileFromAssets(android.content.Context context, String assetFileName) {
		File cacheFile = new File(context.getCacheDir(), assetFileName);

		if (cacheFile.exists()) {
			return cacheFile;
		}

		// Copy file from assets if not found in cache
		try (java.io.InputStream inputStream = context.getAssets().open(assetFileName);
			 java.io.FileOutputStream outputStream = new java.io.FileOutputStream(cacheFile)) {

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
			return cacheFile;
		} catch (java.io.IOException e) {
			android.util.Log.e(LOG_TAG, "Error copying asset: " + assetFileName, e);
			return null;
		}
	}
}
