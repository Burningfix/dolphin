package org.dolphin.hotpatch.dex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.dolphin.dexhotpatch.R;

import dolphin.hotdexpatch.Main2Activity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void next(View view){
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
    }
}
