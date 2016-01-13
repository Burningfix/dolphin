package org.dolphin.arch;

import org.dolphin.lib.ValueUtil;

import java.io.Serializable;

/**
 * Created by hanyanan on 2015/12/4.
 * <p/>
 * 需要保存数据
 */
public class PageViewModel implements Serializable {
    public static final String DEFAULT_TOKEN = "default.token";
    // 每个pageViewModel都有一个单独的token由于区分是否有冲突
    public String token = DEFAULT_TOKEN;
    // 错误码，0为正确，负数为端上错误，如-1为网络异常
    public long error;
    // 错误信息, 若成功则为
    public String msg;
    // 失败的异常信息
    public Throwable throwable;


    @Override
    public int hashCode() {
        if (ValueUtil.isEmpty(token)) {
            return DEFAULT_TOKEN.hashCode();
        }
        return token.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (PageViewModel.class.isInstance(o)) {
            PageViewModel other = (PageViewModel) o;
            if (other.token == this.token) return true;
            if (other.token == null || this.token == null) return false;
            return other.token.equals(this.token);
        }
        return super.equals(o);
    }
}
