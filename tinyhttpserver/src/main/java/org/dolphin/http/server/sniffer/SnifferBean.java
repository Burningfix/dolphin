package org.dolphin.http.server.sniffer;

import org.dolphin.lib.KeepAttr;

/**
 * Created by hanyanan on 2015/11/6.
 */
public class SnifferBean implements KeepAttr {
    public int tcpListenPort; // 正在监听的tcp端口
    public String statue; // 当前的状态，默认为online
    public String msg; // 失败的信息
    public int errorno; // 错误代码，默认成功是0
}
