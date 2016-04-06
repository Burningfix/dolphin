package org.dolphin.http.server;

import com.google.gson.annotations.Expose;
import org.dolphin.lib.progaurd.KeepAttr;

/**
 * Created by hanyanan on 2015/11/4.
 */
public class FileBean implements KeepAttr {
    public String path;
    @Expose
    public long size;
    @Expose
    public long modifyTime;
    @Expose
    public String name;
    @Expose
    public String url;
    @Expose
    public String type;
}
