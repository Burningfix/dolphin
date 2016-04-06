package org.dolphin.http.server.sniffer;

import org.dolphin.lib.progaurd.KeepAttr;
import org.dolphin.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/11/6.
 */
public class SnifferBean implements KeepAttr {
    public int tcpListenPort; // 正在监听的tcp端口
    public int statue; // 当前的状态，0为online, 1为offline, 默认为0
    public String msg; // 失败的信息
    public int errorno; // 错误代码，默认成功是0
    public String ip;

    @Override
    public int hashCode() {
        if(!ValueUtil.isEmpty(ip)) {
            return ip.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        return ""+ip+":"+tcpListenPort;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(SnifferBean.class.isInstance(obj)){
            SnifferBean o = (SnifferBean)obj;
            if(ValueUtil.isEmpty(o.ip)) return false;
            return o.ip.equalsIgnoreCase(this.ip);
        }
        return super.equals(obj);
    }
}
