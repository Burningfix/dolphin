package org.dolphin.secret.env;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;

public class PermissionActivity extends Activity {
    private static final int REQUEST_PERMISSION = 0x11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new View(this));

        ActivityCompat.requestPermissions(this, PermissionProcessor.checkUnauthorizedPermission(), REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_PERMISSION == requestCode) {
            if (grantResults != null) {
                for (int code : grantResults) {
                    if (code != PackageManager.PERMISSION_GRANTED) {
                        throw new PermissionDeniedException();
                    }
                }
                throw new PermissionGrantedException();
            }
            throw new PermissionDeniedException();
        }
    }
}
