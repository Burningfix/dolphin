package org.dolphin.secret.permission;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;

import org.dolphin.lib.util.ValueUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanyanan on 2016/6/12.
 */
public class PermissionProcessor {
    public static final List<PermissionSpec> ESSENTIAL_PERMISSIONS =
            new LinkedList<PermissionSpec>();

    static {
        ESSENTIAL_PERMISSIONS.add(new PermissionSpec(Manifest.permission.WRITE_EXTERNAL_STORAGE));
        ESSENTIAL_PERMISSIONS.add(new PermissionSpec(Manifest.permission.READ_EXTERNAL_STORAGE, Build.VERSION_CODES.JELLY_BEAN));
    }

    public static PermissionSpec[] checkUnauthorizedPermission(Context base) {
        final List<PermissionSpec> permissions = new ArrayList<PermissionSpec>();
        for (PermissionSpec permissionSpec : ESSENTIAL_PERMISSIONS) {
            if (!checkPermissionSpec(base, permissionSpec)) {
                permissions.add(permissionSpec);
            }
        }

        return permissions.toArray(new PermissionSpec[0]);
    }

    /**
     * @param base
     * @return
     * @see android.support.v4.content.ContextCompat#checkSelfPermission(Context, String)
     * @see PermissionChecker#checkSelfPermission(Context, String)
     */
    public static boolean checkRunningPermission(Context base) {
        for (PermissionSpec permissionSpec : ESSENTIAL_PERMISSIONS) {
            if (!checkPermissionSpec(base, permissionSpec)) {
                return false;
            }
        }

        return true;
    }

    private static boolean checkPermissionSpec(Context context, PermissionSpec permissionSpec) {
        if (permissionSpec.isSupportOnDevice()) {
            int selfPermission = ContextCompat.checkSelfPermission(context, permissionSpec.permission);
            return selfPermission == PackageManager.PERMISSION_GRANTED;
        }
        // 当前平台不支持,一般是因为设备版本太低，默认为true
        return true;
    }

    /**
     * 申请权限，通过Activity回调activity的onRequestPermissionsResult的接口。
     * 同时支持{@link android.support.v4.app.FragmentActivity}和{@link android.support.v4.app.Fragment#requestPermissions(String[], int)},
     * fragment的requestPermissions实际上是调用Activity的requestPermissions
     *
     * @param activity        请求和回调的Activity
     * @param permissionSpecs 请求的权限
     * @param requestCode     请求的code
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     * @see Activity#requestPermissions(String[], int)
     * @see android.support.v4.app.FragmentActivity#requestPermissions(String[], int)
     * @see android.support.v4.app.FragmentActivity#onRequestPermissionsResult(int, String[], int[])
     * @see android.support.v4.app.Fragment#requestPermissions(String[], int)
     * @see android.support.v4.app.Fragment#onRequestPermissionsResult(int, String[], int[])
     * @see android.app.Fragment#requestPermissions(String[], int)
     * @see android.app.Fragment#onRequestPermissionsResult(int, String[], int[])
     * @see ActivityCompat#requestPermissions(Activity, String[], int)
     */
    public static void requestPermission(Activity activity, PermissionSpec[] permissionSpecs, int requestCode) {
        if (ValueUtil.isEmpty(permissionSpecs)) {
            throw new IllegalArgumentException("PermissionSpec is Empty!");
        }
        List<String> permissions = new ArrayList<String>();

        for (PermissionSpec permissionSpec : permissionSpecs) {
            if (permissionSpec.isSupportOnDevice()) {
                permissions.add(permissionSpec.permission);
            }
        }

        if (permissions.isEmpty()) {
            throw new IllegalArgumentException("PermissionSpec is Empty!");
        }
        // 会调用Activity的onRequestPermissionsResult回调
        ActivityCompat.requestPermissions(activity, permissions.toArray(new String[permissions.size()]), requestCode);
    }

    /**
     * 是否显示请求权限的弹框， 有时候当弹出权限提示时，有的用户会选中‘不再提示’选项
     *
     * @param activity
     * @param permissionSpecs
     * @return
     * @see ActivityCompat#shouldShowRequestPermissionRationale(Activity, String)
     */
    public static boolean shouldShowRequestPermissionRationale(Activity activity, PermissionSpec[] permissionSpecs) {
        if (ValueUtil.isEmpty(permissionSpecs)) {
            return true;
        }

        for (PermissionSpec permissionSpec : permissionSpecs) {
            if (permissionSpec.isSupportOnDevice()
                    && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionSpec.permission)) {
                return false;
            }
        }

        return true;
    }

    public static boolean makeSurePermissionsAllowed(Context context) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalThreadStateException("Must running on UI Thread!");
        }

        PermissionSpec[] permissionSpecs = checkUnauthorizedPermission(context);
        if (ValueUtil.isEmpty(permissionSpecs)) {
            return true;
        }
        Intent intent = new Intent(context, PermissionActivity.class);
        intent.putExtra("permission", permissionSpecs);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
        try {
            Looper.loop();
            throw new UnsupportedOperationException("Cannot running here!!");
        } catch (PermissionGrantedException exception) {
            return true;
        } catch (PermissionDeniedException ex) {
            return false;
        }
    }
}
