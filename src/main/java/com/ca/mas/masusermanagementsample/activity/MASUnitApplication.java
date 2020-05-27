/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.masusermanagementsample.activity;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.ca.mas.foundation.MAS;

public class MASUnitApplication extends MultiDexApplication {
    private static SharedPreferences mPreferences;
    final static String SP_KEY = MASUnitApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
        ParameterBuilder.getInstance().init(this);
//        MAS.start(this);
//        MAS.debug();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static SharedPreferences getPreferences() {
        return mPreferences;
    }

}
