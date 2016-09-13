package org.dolphin.secret.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;

/**
 * Created by hanyanan on 2016/6/30.
 */
public class DialogUtil {
    public static Dialog showDialog(Activity activity, String title, String message, DialogInterface.OnClickListener listener) {
        return showDialog(activity, title, message, "确定", listener);
    }

    public static Dialog showDialog(Activity activity, String title, String message, String button,
                                    DialogInterface.OnClickListener listener) {
        AlertDialog dlg = createDialog(activity, title, message);
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, button, listener);
        dlg.show();
        return dlg;
    }

    public static Dialog showDialog(Activity activity, String title, String message, String button,
                                    DialogInterface.OnClickListener listener, DialogInterface.OnDismissListener dismissListener) {
        AlertDialog dlg = createDialog(activity, title, message);
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, button, listener);
        dlg.setOnDismissListener(dismissListener);
        dlg.show();
        return dlg;
    }

    public static Dialog showDialog(Activity activity, String title, String message,
                                    String positiveText, DialogInterface.OnClickListener positiveListener,
                                    String negativeText, DialogInterface.OnClickListener negativeListener) {
        AlertDialog dlg = createDialog(activity, title, message);
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, positiveText, positiveListener);
        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, negativeText, negativeListener);
        dlg.show();
        return dlg;
    }

    public static Dialog showDialog(Activity activity, String title, String message,
                                    String positiveText, DialogInterface.OnClickListener positiveListener,
                                    String negativeText, DialogInterface.OnClickListener negativeListener,
                                    DialogInterface.OnDismissListener dismissListener) {
        AlertDialog dlg = createDialog(activity, title, message);
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, positiveText, positiveListener);
        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, negativeText, negativeListener);
        dlg.setOnDismissListener(dismissListener);
        dlg.show();
        return dlg;
    }

    public static Dialog showDialog(Activity activity, String title, View view, DialogInterface.OnClickListener listener) {
        AlertDialog dlg = createDialog(activity, title, view);
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, "确定", listener);
        dlg.show();
        return dlg;
    }

    public static AlertDialog showDialog(Activity activity, String title, View view, DialogInterface.OnClickListener okListener,
                                         DialogInterface.OnClickListener cancelListener) {
        AlertDialog dlg = createDialog(activity, title, view);
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, "确定", okListener);
        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", cancelListener);
        dlg.show();
        return dlg;
    }

    public static Dialog showDialog(Activity activity, String title, View view, String button,
                                    DialogInterface.OnClickListener listener) {
        AlertDialog dlg = createDialog(activity, title, view);
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, button, listener);
        dlg.show();
        return dlg;
    }

    public static AlertDialog createDialog(Activity activity, String title, String message) {
        AlertDialog dlg = new AlertDialog.Builder(activity).create();
        dlg.setTitle(title);
        dlg.setMessage(message);
        return dlg;
    }

    // http://stackoverflow.com/questions/6040883/what-is-difference-between-dialog-setcontentview-view-alertdialog-setview
    public static AlertDialog createDialog(Activity activity, String title, View view) {
        AlertDialog dlg = new AlertDialog.Builder(activity).create();
        dlg.setTitle(title);
        dlg.setView(view);
        return dlg;
    }
}
