/*
   Copyright (C) 2015 José Ronquillo Rivera <josrr@ymail.com>
   This file is part of CREPL.

   CREPL is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   CREPL is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with CREPL.  If not, see <http://www.gnu.org/licenses/>.
*/

package lisparm.clandroid;

import android.content.Intent;
import android.app.Service;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;
import android.os.Bundle;
import android.util.Log;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.lang.Thread;
import java.lang.ref.WeakReference;

class FromECLHandler extends Handler {
    private static String TAG = Constants.TAG;
    ECLTopLevelService mService;
    FromECLHandler(ECLTopLevelService s) {
	mService=s;
    }

    @Override
    public void handleMessage(Message msg) {
	Log.w(TAG, "sHandler msg.arg1: " + msg.arg1);
	Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
	localIntent.addCategory(Intent.CATEGORY_DEFAULT);
	if ( msg.arg1 == 0 ) {
	    Log.w(TAG, "resultado: " + (String)msg.obj);
	    localIntent.putExtra("resultado", (String)msg.obj);
	} else if ( msg.arg1 == Constants.ECL_INICIADO ) {
	    Log.w(TAG, "ecl iniciado: " + msg.arg1);
	    localIntent.putExtra("iniciado", 1);
	}
	mService.sendBroadcast(localIntent);
    }
};

public class ECLTopLevelService extends Service {
    private static String TAG = Constants.TAG;
    private static boolean eclListo = false;
    private static Thread eclThread;
    private static Thread eclToplevelThread;
    private static Handler mHandler;

    private void iniciaECL() {
	Log.w(TAG,"ECL Starting...");
	String user_dir = this.getApplicationInfo().dataDir + "/app_resources";
	Log.w(TAG, "user_dir=" + user_dir);
	eclsetup(user_dir);
	eclstart(user_dir);
	eclListo = true;
        Log.w(TAG,"ECL Started");
    }

    private final Handler sHandler = new FromECLHandler(this);
	/*{
	public void handleMessage(Message msg) {
	    Log.w(TAG, "sHandler msg.arg1: " + msg.arg1);
	    Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
	    localIntent.addCategory(Intent.CATEGORY_DEFAULT);
	    if ( msg.arg1 == 0 ) {
		//Log.w(TAG, "resultado: " + (String)msg.obj);
		localIntent.putExtra("resultado", (String)msg.obj);
	    } else if ( msg.arg1 == Constants.ECL_INICIADO ) {
		Log.w(TAG, "ecl iniciado: " + msg.arg1);
		localIntent.putExtra("iniciado", 1);
	    }
	    sendBroadcast(localIntent);
	}
	};*/

    void crea_servicio(Intent intent) {
	if ( !eclListo ) {
	    eclThread = new Thread(new Runnable() {
		    public void run() {
			iniciaECL();
			Looper.prepare();
			mHandler =
			    new Handler() {
				public void handleMessage(Message msg) {
				    Log.w(TAG, "msg: " + msg.obj);
				    Message msgResult = Message.obtain();
				    msgResult.obj =
					eclexec("(crepl:execute-sexp " +
						(String)msg.obj + ")");
				     Log.w(TAG, "despues de eclExec:" + msgResult.obj);
				    sHandler.sendMessage(msgResult);
				}
			    };
			Message msgResult = Message.obtain();
			msgResult.arg1 = Constants.ECL_INICIADO;
			sHandler.sendMessage(msgResult);
			Looper.loop();
		    }
		    public void exit() {
			mHandler.getLooper().quit();
		    }
		});
	    eclThread.start();
	    Toast.makeText(this, " ECLTopLevel created ", Toast.LENGTH_LONG).show();
	} else {
	    Message msg = Message.obtain();
	    msg.obj = intent.getStringExtra("code");
	    Log.w(TAG, "sendMessage: " + (String)msg.obj);
	    mHandler.sendMessage(msg);
	}
    }

    @Override
    public void onCreate() {
    }

    @Override
    public IBinder onBind(Intent intent) {
    	return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	crea_servicio(intent);
	return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
	Toast.makeText(this, " ECLTopLevel destroyed ", Toast.LENGTH_LONG).show();
	Log.w(TAG, " ECL destroyed");
    }

    public native void eclstart(String path);
    public native void eclsetup(String path);
    public native String eclexec(String string);
    //public native String eclToplevel(String string);

    static {
	System.loadLibrary("ecl");
        System.loadLibrary("android-ecl");
        Log.w(TAG,"Se ha cargado la biblioteca android-ecl");
    }
}
