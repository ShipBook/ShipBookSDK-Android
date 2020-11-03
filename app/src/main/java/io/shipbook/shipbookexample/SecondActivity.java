package io.shipbook.shipbookexample;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

import io.shipbook.shipbooksdk.Log;
import io.shipbook.shipbooksdk.ShipBook;

/*
 *
 * Created by Elisha Sterngold on 11/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

public class SecondActivity extends Activity {
    static Log log = ShipBook.getLogger("SecondActivity");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        ShipBook.screen("second screen");
        log.e("error message");
        log.w("warning message");
        log.i("info message");
        log.d("debug message");
        log.v("verbose message");
    }
}
