package org.dolphin.dexhotpatch;

import java.util.List;

/**
 * Created by hanyanan on 2015/11/12.
 */
public interface DexLoadObserver {
    void onLoadExtraDex(String dexName, DexConfigBean config);

    void onLoadDexFailed(String dexName, String reason);
}
