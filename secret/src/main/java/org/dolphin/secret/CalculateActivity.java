package org.dolphin.secret;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.dolphin.secret.util.ContextUtils;
import org.dolphin.secret.util.DialogUtil;

public class CalculateActivity extends AppCompatActivity {
    private final StringBuilder sb = new StringBuilder("");
    TextView result;
    private transient String passwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate);

        result = (TextView) this.findViewById(R.id.result);

        passwd = ContextUtils.getStringFromSharedPreferences(this.getApplicationContext(), "data", null);


        if (TextUtils.isEmpty(passwd)) {
            Dialog dlg = DialogUtil.showDialog(this, "输入密码", new TextView(this), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    passwd = null;
                }
            });
            dlg.setCancelable(false);
            dlg.show();
        }
    }


    public void onNumberClick(View view) {
        Log.d("ddd", "" + view);
        TextView tv = (TextView) view;
        int id = view.getId();
        switch (id) {
            case R.id.calculate_number_0:
            case R.id.calculate_number_1:
            case R.id.calculate_number_2:
            case R.id.calculate_number_3:
            case R.id.calculate_number_4:
            case R.id.calculate_number_5:
            case R.id.calculate_number_6:
            case R.id.calculate_number_7:
            case R.id.calculate_number_8:
            case R.id.calculate_number_9:
            case R.id.calculate_number_dot:
            case R.id.calculate_number_add:
            case R.id.calculate_number_sub:
            case R.id.calculate_number_mul:
            case R.id.calculate_number_div:
                inputNumber(tv.getText());
                break;
            case R.id.calculate_number_del:
                clear();
                break;
            case R.id.calculate_number_equal:
                calculate();
                break;
        }
    }


    private void inputNumber(CharSequence number) {
        sb.append(number);
        result.setText(sb.toString());
    }


    private void clear() {
        sb.delete(0, sb.length());
        result.setText("");
    }

    private void calculate() {

    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }
}
