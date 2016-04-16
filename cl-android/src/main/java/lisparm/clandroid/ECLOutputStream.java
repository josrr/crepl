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
import android.util.Log;
import android.content.Intent;

import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
/*import java.util.Arrays;*/

public class ECLOutputStream extends OutputStream
{
    private static String TAG = Constants.TAG;
    private Activity activity;
    /*private Boolean isResult = false;*/

    public ECLOutputStream (Activity a) {
	activity = a;
    }

    public void write(int value) throws IOException {
	Log.w(TAG, "ECLOutputStream write value:" + value);
    }

    private byte[] linea = new byte[Constants.SESSION_BUFFER_SIZE];
    private int posicion = 0;
    private String lastWord;

    public byte[] copyOfRange(byte[] from, int start, int end){
        int length = end - start;
        byte[] result = new byte[length];
        System.arraycopy(from, start, result, 0, length);
        return result;
    }

    /*private void coloursLastWord() {
	int clObjectType = ECLSymbolAnalize(lastWord);
	if ( clObjectType > 0 ) {
	    
	}
    }*/

    public void write(byte[] bytes, int offset, int count) throws IOException {
	String str = new String(bytes, offset, count, "UTF-8");
	Log.w(TAG, "ECLOutputStream write string:" + str.length() +
	      " count:" + count);
	/* Todo esto es probablemente evitable -> debo usar (cl:read) */
	if ( count == 1 &&
	     (bytes[0] == '\n' || bytes[0] == '\r' || bytes[0] == 127 )) {
	    if ( bytes[0] == 127 && posicion > 0 ) {
		Log.w(TAG, "borrando " + ((int) linea[posicion-1] & 0xFF) +" antes:   " + posicion);
		if ( (linea[(posicion-1)] & 0x80) == 0 ) {
		    posicion--;
		} else {
		    while ( posicion>0 && ((linea[((posicion--)-1)] & 0xC0) != 0xC0) ) {
			Log.w(TAG, "borrado  " + ((int) linea[posicion-1] & 0xFF) + "   while: " + posicion);
		    }
		}
		Log.w(TAG, "borrado  " + ((int) linea[posicion] & 0xFF) + " después: " + posicion);
		//} else if (  bytes[0] == ' ' || bytes[0] == '\t' && lastWord.length() > 0 ) {
		/* Analizamos la palabra anterior y la coloreamos según el tipo de objeto que sea */
		//coloursLastWord(clObjectType);
	    } else /*if ( bytes[0] == '\n' || bytes[0] == '\r' )*/ {
		String codigo = new String(copyOfRange(linea, 0, posicion), "UTF-8");
		if ( codigo.length() > 0 ) {
		    Log.w(TAG,"ECLSession - enviando Código: " + codigo);
		    Intent intentECL = new Intent(activity,
						  ECLTopLevelService.class);
		    intentECL.putExtra("code", codigo);
		    activity.startService(intentECL);
		    posicion = 0;
		}
	    }
	    return;
	}

	try {
	    byte[] b = {};
	    String s = new String(bytes, offset, count, "UTF-8");
	    b = s.getBytes();
	    if ( posicion > (Constants.SESSION_BUFFER_SIZE - 1) ) {
		posicion = 0;
	    }
	    for (int i=offset; i<offset+count; i++)
		linea[posicion++] = b[i];
	    Log.w(TAG, "\tstr:" + s + "\tbytes:" + new String(b, "UTF-8"));
	} catch (UnsupportedEncodingException e) {
	    Log.e(TAG, "Not supported character encoding.", e);
	}
    }
}
