/*
 *  Copyright 2015-2016 José Ronquillo Rivera <josrr@ymail.com>
 *  This file is part of CREPL.
 *
 *  CREPL is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CREPL is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CREPL.  If not, see <http://www.gnu.org/licenses/>.
 */

package lisparm.clandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import jackpal.androidterm.emulatorview.EmulatorView;

public class CLAndroid extends Activity
{
    private static String TAG = Constants.TAG;
    private static String RESOURCES_DIR = "lisp";
    private static String APP_RESOURCES_DIR = "resources";
    private static String ARCH_EDITOR = "texto.txt";
    private static boolean DEBUG = true;
    private static boolean teclado = false;

    private static AssetManager assetManager;
    private static File uncompressedFilesDir;
    private ResponseReceiver receiver;
    private ECLSession termSession;
    private EmulatorView emulatorView;
    private Activity esta;

    private class ResponseReceiver extends BroadcastReceiver
    {
	@Override
	public void onReceive(Context context, Intent intent) {
	    int iniciado = intent.getIntExtra("iniciado", 0);
	    Log.w(TAG, "iniciado:" + iniciado);
	    if ( iniciado == 1 ) {
		Log.w(TAG, "onReceive iniciado: " + iniciado);
		ECLSession session = new ECLSession(esta);
		session.setDefaultUTF8Mode(true);
		emulatorView.attachSession(session);
		//session.initializeEmulator(80,24);
		enviaCodigo("(format t \"ECL (Embeddable Common-Lisp) ~A (git:~D)~% Copyright (C) 1984 Taiichi Yuasa and Masami Hagiya~% Copyright (C) 1993 Giuseppe Attardi~% Copyright (C) 2000 Juan J. Garcia-Ripoll~% Copyright (C) 2015 Daniel Kochmanski~% ECL is free software, and you are welcome to redistribute it~% under certain conditions; see file 'Copyright' for details.\" (sys::lisp-implementation-version) (ext:lisp-implementation-vcs-id))");
		//enviaCodigo("(defvar *r* (loop repeat 10000 collect (random 10000)))");
		termSession = session;
		//muestraTeclado();
	    } else {
		String resultado = intent.getStringExtra("resultado");
		Log.w(TAG, "resultado: " + resultado);
		termSession.writeResult(resultado + "\r\n> ");
	    }
	}
    }

    /*
    private View.OnKeyListener mKeyListener = new View.OnKeyListener()
	{
	    public boolean onKey(View v, int keyCode, KeyEvent event) {
		Log.w(TAG, "onKey: " + keyCode);
		termSession.write("(* 23 (/ 2 34) 3)\r\n");
		return true;
	    }
	};
    */

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
	Log.w(TAG, "keyCode: " + keyCode);
	return false;
    }

    private void muestraTeclado(){
	InputMethodManager imm = (InputMethodManager)
	    getSystemService(Context.INPUT_METHOD_SERVICE);
	if ( teclado ) {
	    teclado = false;
	    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
	} else {
	    teclado = true;
	    imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
	}
    }

    private class EmulatorViewGestureListener extends SimpleOnGestureListener {
        private EmulatorView view;

        public EmulatorViewGestureListener(EmulatorView view) {
            this.view = view;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Let the EmulatorView handle taps if mouse tracking is active
	    Log.w(TAG,"onSingleTapUp: " + e);
            //if (view.isMouseTrackingActive()) return false;
	    muestraTeclado();
            return true;
        }

	/*
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float absVelocityX = Math.abs(velocityX);
            float absVelocityY = Math.abs(velocityY);
	    Log.w(TAG,"onFling X - " + velocityX + "  Y - " + velocityY);
            if (absVelocityX > Math.max(1000.0f, 2.0 * absVelocityY)) {
                if (velocityX > 0) {
                } else {
                }
                return true;
            } else {
                return false;
            }
        }
	*/
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	esta = this;
	Log.w(TAG,"onCreate");
	assetManager = getAssets();
	SharedPreferences settings = getPreferences(MODE_PRIVATE);
	boolean assetsUncompressed = settings.getBoolean("assetsUncompressed", false);
        uncompressedFilesDir = getDir(APP_RESOURCES_DIR,MODE_PRIVATE);
	Log.w(TAG, "assetsUncompressed:" + assetsUncompressed);
        if ( !assetsUncompressed ) {
	    Log.w(TAG, "descomprimiendo en:" + uncompressedFilesDir);
	    uncompressDir(RESOURCES_DIR,uncompressedFilesDir);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean("assetsUncompressed", true);
	    editor.commit();
        }
	assetsUncompressed = settings.getBoolean("assetsUncompressed", false);
	Log.w(TAG, "assetsUncompressed:" + assetsUncompressed);
	setContentView(R.layout.main);
	IntentFilter filter = new IntentFilter(Constants.BROADCAST_ACTION);
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);

	DisplayMetrics metrics = new DisplayMetrics();
	getWindowManager().getDefaultDisplay().getMetrics(metrics);
	emulatorView = (EmulatorView) findViewById(R.id.emulatorView);
	Log.w(TAG, "View: " + emulatorView + "\nMetrics: " + metrics);
	emulatorView.setDensity(metrics);
	emulatorView.setExtGestureListener(new EmulatorViewGestureListener(emulatorView));

	Intent intentECL;
	intentECL = new Intent(this, ECLTopLevelService.class);
	Log.w(TAG, "intentECL:"+intentECL);
	this.startService(intentECL);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
	super.onSaveInstanceState(outState);
	/*enviaCodigo("(util:escribe-archivo #P\""+ ARCH_EDITOR + "\" \"" +
	  miEditText.getText().toString() + "\")");*/
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState){
	super.onRestoreInstanceState(savedInstanceState);
	/*enviaCodigo("(util:lee-archivo  #P\"" + ARCH_EDITOR + "\" )");*/
    }

    public void enviaCodigo(String codigo) {
	Intent intentECL;
	Log.w(TAG,"enviaCodigo");
	intentECL = new Intent(this, ECLTopLevelService.class);
	intentECL.putExtra("code", codigo);
	this.startService(intentECL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch ( item.getItemId() ) {
	  case R.id.menu_salir:
	      finish();
	      /*uncompressDir(RESOURCES_DIR,uncompressedFilesDir);*/
	      return true;
	  default:
	      return super.onOptionsItemSelected(item);
        }
    }

    public static String getResourcesPath()
    {
	return uncompressedFilesDir.getAbsolutePath();
    }

    public void uncompressDir(String in, File out) {
        try {
            String[] files = assetManager.list(in);
            Log.w(TAG,"Uncompressing: " + files.length + " files");
            for(int i=0; i<files.length; i++) {
                Log.w(TAG,"Uncompressing: " + files[i]);
                File fileIn = new File(in,files[i]);
                File fileOut = new File(out,files[i]);

                try {
                    uncompressFile(fileIn,fileOut);
                } catch(FileNotFoundException e) {
                    // fileIn is a directory, uncompress the subdir
                    if(!fileOut.isDirectory()) {
                        Log.w(TAG,"Creating dir: " + fileOut.getAbsolutePath());
                        fileOut.mkdir();
		    }
                    uncompressDir(fileIn.getPath(), fileOut);
                }
            }
	} catch(IOException e) {
	    e.printStackTrace();
	}
    }

    public static void uncompressFile(File fileIn,File fileOut) throws IOException
    {
        InputStream in = assetManager.open(fileIn.getPath(),
                                           android.content.res.AssetManager.ACCESS_RANDOM);
	OutputStream out = new FileOutputStream(fileOut);

	byte[] buf = new byte[1024];
	int len;
	while ((len = in.read(buf)) > 0) {
	    out.write(buf, 0, len);
	}

	in.close();
	out.close();
	Log.i(TAG,"File copied.");
    }
}
