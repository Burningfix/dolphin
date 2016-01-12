package org.dolphin.arch;

import java.io.Serializable;

/**
 * Created by hanyanan on 2015/12/4.
 * <p/>
 * 需要保存数据
 */
public class PageViewModel implements Serializable {
    public long error;
    public String msg;
    public Throwable throwable;


}
