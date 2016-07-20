package org.dolphin.secret;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.dolphin.lib.util.SecurityUtil;
import org.dolphin.lib.util.ValueUtil;
import org.dolphin.secret.calculator.Calculator;
import org.dolphin.secret.util.ContextUtils;
import org.dolphin.secret.util.DialogUtil;

public class CalculateActivity extends AppCompatActivity {
    private TextView result;
    private transient String passwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate);

        result = (TextView) this.findViewById(R.id.result);
        findViewById(R.id.calculate_number_del).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onDelLongClick(v);
                return false;
            }
        });

        passwd = ContextUtils.getStringFromSharedPreferences(this.getApplicationContext(), "sign", null);
        if (TextUtils.isEmpty(passwd)) {
            final EditText editText = new EditText(this);
            final AlertDialog dlg = DialogUtil.showDialog(this, getResources().getString(R.string.input_pass),
                    editText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            passwd = SecurityUtil.md5(editText.getText().toString());
                            ContextUtils.putStringToSharedPreferences(CalculateActivity.this, "sign", passwd);
                            DialogUtil.showDialog(CalculateActivity.this, "", getResources().getString(R.string.enter_browser_tip),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(CalculateActivity.this, BrowserMainActivity.class);
                                            startActivity(intent);
                                        }
                                    }).show();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            editText.setKeyListener(DigitsKeyListener.getInstance("1234567890-+*/."));
            editText.setHint(R.string.input_pass_hint);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (null == s || s.length() < 6) {
                        dlg.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(false);
                    } else {
                        dlg.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(true);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            dlg.setCancelable(false);
            dlg.show();
        }
    }

    public void onDelLongClick(View view) {
        result.setText("0");
    }

    public void onNumberClick(View view) {
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
                backOne();
                break;
            case R.id.calculate_number_equal:
                calculate();
                break;
        }
    }

    private void inputNumber(CharSequence number) {
        CharSequence expression = result.getText();
        if (ValueUtil.isEmpty(expression)) {
            expression = "";
        }
        expression = expression.toString() + number;
        result.setText(trimExpression(expression));
    }


    private void backOne() {
        CharSequence expression = result.getText();
        if (ValueUtil.isEmpty(expression) || expression.length() <= 1) {
            result.setText("0");
            return;
        }
        expression = expression.subSequence(0, expression.length() - 1);
        result.setText(trimExpression(expression));
    }

    private void calculate() {
        CharSequence expression = result.getText();
        if (ValueUtil.isEmpty(expression) || expression.length() <= 1) {
            return;
        }
        String md5 = SecurityUtil.md5(expression.toString());
        if (TextUtils.equals(md5, this.passwd)) {
            Intent intent = new Intent(this, BrowserMainActivity.class);
            startActivity(intent);
            result.setText("0");
            return;
        }

        try {
            result.setText("" + Calculator.compute(expression));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * 删除头部所有的0
     *
     * @param expression
     */
    private CharSequence trimExpression(CharSequence expression) {
        if (ValueUtil.isEmpty(expression)) {
            return "0";
        }

        if (expression.charAt(0) == '0') {
            return trimExpression(expression.subSequence(1, expression.length()));
        }

        return expression;
    }
}
