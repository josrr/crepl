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

import android.util.Log;
import android.content.Intent;

import java.io.InputStream;
import java.io.IOException;
import java.lang.Math.*;

public class ECLInputStream extends InputStream
{
    private static String TAG = Constants.TAG;
    private static byte[] data; //new byte[Constants.OUTPUT_BUFFER_SIZE];
    private static boolean dirty = false;
    private static int size = 0;
    private static int position = 0;
    /*
      public ECLInputStream () {
      }
    */
    @Override
    public int available() throws IOException{
	return dirty ? (size-position) : 0;
    }

    @Override
    public int read() throws IOException {
	Log.w(TAG, "read");
	if ( dirty && position == size ) {
	    dirty = false;
	    size = position = 0;
	}
	Log.w(TAG, "\t\tesperando");
	while ( !dirty );
	Log.w(TAG, " \t\tterminó la espera: " + data[position] +
	      "\tpos: " + position);
	return data[position++];
    }

    @Override
    public int read(byte[] bytes) throws IOException {
	if ( ! dirty )
	    return 0;

	int num_bytes = java.lang.Math.min(bytes.length, available());
	int bytes_available = available();
	Log.w(TAG, "num_bytes: " + num_bytes + " available: " +
	      bytes_available);
	if ( num_bytes > 0 ) {
	    System.arraycopy(data, position, bytes, 0, num_bytes);
	    if ( num_bytes == bytes_available ) {
		dirty = false;
		size = position = 0;
	    } else {
		position += num_bytes;
	    }
	}
	return num_bytes;
    }

    /*public void setData(byte[] new_data) {
	Log.i(TAG, "setData new_data: " + count + " bytes");
	dirty = true;
	data = new_data;
	//System.arraycopy(new_data, 0, data, 0, count);
	size = count;
	}*/
    public void setData(String string) {
	Log.i(TAG, "setData: " + string.length() + " characters");
	if ( dirty ) return;
	//byte[] bytes =
	dirty = true;
	data = string.getBytes();
	size = data.length;
	//setData(bytes, bytes.length);
    }
}
