/*
 *    Copyright (C) 20129 Henrik Sandklef
 *
 *    This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package se.juneday.android;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import se.juneday.ObjectCache;

public class AndroidObjectCacheHelper {

    private final static String LOG_TAG =
            AndroidObjectCacheHelper.class.getName();

    public static String objectCacheFileName(Context context, Class clazz) throws AndroidObjectCacheHelperException {

        PackageManager m = context.getPackageManager();
        String s = context.getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(s, 0);
            s = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(LOG_TAG, "Error, could not build file name for serialization", e);
            throw new AndroidObjectCacheHelperException(e, "could not build file name for serialization");
        }
        String fileName = s + "/" + clazz.getName();

        return fileName;
    }

    public static class AndroidObjectCacheHelperException extends Exception {
        public AndroidObjectCacheHelperException(Exception e, String msg) {
            super(msg);
        }
    }

}
