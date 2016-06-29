package org.dolphin.secret;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class CalculateActivity extends AppCompatActivity {
    private final StringBuilder sb = new StringBuilder("");
    TextView result;
    private String passwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate);

        result = (TextView) this.findViewById(R.id.result);

        SharedPreferences sharedata = getSharedPreferences("data", MODE_PRIVATE);
        if(null != sharedata){
            passwd = sharedata.getString("pass", null);
        }

        if(TextUtils.isEmpty(passwd)){
            AlertDialog dlg = new AlertDialog.Builder(this).create();
            dlg.setMessage(getString(R.string.no_pass_tips, passwd));
            dlg.setButton(AlertDialog.BUTTON_POSITIVE,getString(R.string.OK),(DialogInterface.OnClickListener)null);
            dlg.show();
        }
    }


    public void onNumberClick(View view){
        Log.d("ddd", "" + view);
        TextView tv = (TextView)view;
        int id= view.getId();
        switch (id){
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


    private void inputNumber(CharSequence number){
        sb.append(number);
        result.setText(sb.toString());
    }


    private void clear(){
        sb.delete(0,sb.length());
        result.setText("");
    }

    private void calculate(){
        final String pass = sb.toString();
        if(TextUtils.isEmpty(this.passwd)){
            AlertDialog dlg = new AlertDialog.Builder(this).create();
            dlg.setMessage(getString(R.string.put_pass_tips, pass));
            dlg.setCancelable(false);
            dlg.setButton(AlertDialog.BUTTON_POSITIVE,getString(R.string.OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences sharedata = getSharedPreferences("data", MODE_PRIVATE);
                    if(null != sharedata){
                        CalculateActivity.this.passwd = pass;
                        sharedata.edit().putString("pass",pass).commit();
                        Intent intent = new Intent(CalculateActivity.this, BrowserMainActivity.class);
                        startActivity(intent);
                    }
                }
            });
            dlg.setButton(AlertDialog.BUTTON_NEGATIVE,getString(R.string.cancel), (DialogInterface.OnClickListener)null);
            dlg.show();
        }else{
            if(this.passwd.equals(pass)){
                Intent intent = new Intent(CalculateActivity.this, BrowserMainActivity.class);
                startActivity(intent);
            }
        }

        sb.delete(0,sb.length());

        try {
            String res = MathUtil.mixOperation(pass);
            if (null != res) result.setText(res);
            else result.setText("");
        }catch (Throwable throwable){
            throwable.printStackTrace();
            result.setText("错误");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }
}
