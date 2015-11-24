package org.dolphin.hotpatch.dex;

import android.app.Application;
import android.content.Context;

/**
 * Created by hanyanan on 2015/11/16.
 */
public class DexLoadApplication extends Application {
    private DexHotPatchEngine dexHotPatchEngine;
    public DexLoadApplication() {
        super();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        dexHotPatchEngine = DexHotPatchEngine.instance(this);
//        dexHotPatchEngine.attachToApplication();
    }
}
