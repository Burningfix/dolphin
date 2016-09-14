package org.dolphin.secret.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

import org.dolphin.lib.util.ValueUtil;
import org.dolphin.secret.R;

import java.util.Arrays;

public class PermissionActivity extends Activity {
    private static final int REQUEST_PERMISSION = 0x11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new View(this));
        final Parcelable[] parcelables = getIntent().getParcelableArrayExtra("permission");
        if (ValueUtil.isEmpty(parcelables)) {
            this.finish();
            return;
        }
        final PermissionSpec[] permissionSpecs = Arrays.copyOf(parcelables, parcelables.length, PermissionSpec[].class);
        if (!PermissionProcessor.shouldShowRequestPermissionRationale(this, permissionSpecs)) {
            showMessageOKCancel(getResources().getString(R.string.need_write_storage),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PermissionProcessor.requestPermission(PermissionActivity.this, permissionSpecs, REQUEST_PERMISSION);
                        }
                    });
            return;
        }
        PermissionProcessor.requestPermission(this, permissionSpecs, REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_PERMISSION == requestCode) {
            for (int code : grantResults) {
                if (code != PackageManager.PERMISSION_GRANTED) {
                    throw new PermissionDeniedException();
                }
            }
            throw new PermissionGrantedException();
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
