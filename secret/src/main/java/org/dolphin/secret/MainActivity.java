package org.dolphin.secret;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.dolphin.secret.core.FileDecodeOperator;
import org.dolphin.secret.core.FileEncodeOperator;
import org.dolphin.secret.core.FileInfo;
import org.dolphin.secretremotecontroller.R;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final String FilePath = "/sdcard/test.jpg";
    public static final FileEncodeOperator fileEncodeOperator = new FileEncodeOperator();
    public static final FileDecodeOperator fileDecodeOperator = new FileDecodeOperator();
    TextView tv1, tv2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
    }

    public void encode(View view) {
        try {
            FileInfo fileInfo = fileEncodeOperator.operate(new File(FilePath));
            tv1.setText(fileInfo.toString());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void decode(View view) {
        try {
            FileInfo fileInfo = fileDecodeOperator.operate(new File(FilePath));
            tv2.setText(fileInfo.toString());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
