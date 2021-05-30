package io.shipbook.shipbookexample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

        final Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                ShipBook.logout();
            }
        });

        final Button login2 = findViewById(R.id.login2);
        login2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                ShipBook.registerUser("2");
            }
        });

        final Button logs = findViewById(R.id.logs);
        logs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                log.d("created log");
            }
        });

    }
}
