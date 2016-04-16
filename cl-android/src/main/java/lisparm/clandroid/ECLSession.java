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

import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.util.Log;
import android.content.Intent;

import jackpal.androidterm.emulatorview.TermSession;

public class ECLSession extends TermSession
{
    private static String TAG = Constants.TAG;
    /*private static Boolean isResult = false;*/

    public ECLSession(Activity a) {
	InputStream strEntrada = new ECLInputStream();
	OutputStream strSalida = new ECLOutputStream(a);
	setTermIn(strEntrada);
        setTermOut(strSalida);
    }

    private static byte[] linea = new byte[Constants.SESSION_BUFFER_SIZE];
    private static int posicion = 0;

    @Override
    protected void processInput(byte[] data, int offset, int count) {
	int num_cr = 0;
	for (int i = offset; i < offset + count; ++i)
	    if ( data[i] == '\n' ) num_cr++;

	byte[] convertida = new byte[count + num_cr];
	int ii = 0;
	for (int i = offset; i < offset + count; ++i ) {
	    if ( data[i] == '\n' ) {
		convertida[ii++] = '\r';
		convertida[ii++] = '\n';
	    } else convertida[ii++] = data[i];
	}
	appendToEmulator(convertida, 0, convertida.length);
    }

    @Override
    public void write(byte[] bytes, int offset, int count) {
	super.write(bytes, offset, count);
	Log.w(TAG, "ECLSession write: " + bytes[0] + " count: " + count);

	if ( count == 1 && bytes[0] == '\n' || bytes[0] == '\r' || bytes[0] == 127 ) {
	    if ( bytes[0] == 127  ) {
		byte[] cmdLeft = { (byte) 27, (byte) '[', (byte) 'D' };
		// ESC [ P (VT100 erase char at cursor)
		byte[] cmdErase = { (byte) 27, (byte) '[', (byte) 'P' };
		super.processInput(cmdLeft, 0, cmdLeft.length);
		super.processInput(cmdErase, 0, cmdErase.length);
	    } else {
		appendToEmulator("\r\n".getBytes(), 0, 2);
	    }
	} else {
	    try {
		byte[] b = {};
		String s = new String(bytes, offset, count, "UTF-8");
		b = s.getBytes();
		appendToEmulator(b, 0, count == 1 ? 1 : b.length);
		Log.w(TAG, "\tstr:" + s + "\tbytes:" + b);
	    } catch (UnsupportedEncodingException e){
		Log.e(TAG, "Not supported character encoding.", e);
	    }
	}
	notifyUpdate();
    }

    public void writeResult(String result)
    {
	ECLInputStream strEntrada = (ECLInputStream) getTermIn();
	strEntrada.setData(result);
    }

    public void emulador(String cadena) {
	byte[] bytes = cadena.getBytes();
	appendToEmulator(bytes, 0, bytes.length);
    }
}
