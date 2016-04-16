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

public final class Constants {
    public static final int    ECL_INICIADO = 1;
    public static final int    SESSION_BUFFER_SIZE = 24*80*4;
    public static final int    OUTPUT_BUFFER_SIZE = 4096;
    public static final String TAG = "-CLAndroid-";
    public static final String INIT_ACTION = "lisparm.clandroid.INIT";
    public static final String BROADCAST_ACTION = "lisparm.clandroid.BROADCAST";
    public static final String EXECUTE_ACTION = "lisparm.clandroid.EXECUTE";
}
