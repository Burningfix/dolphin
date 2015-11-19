package org.dolphin.hotpatch.apk;

/**
 * Created by yananh on 2015/11/19.
 */
public interface ApkPlugin {

    /**
     * 加载该apk的classLoader
     * /
     * public ClassLoader getClassLoader();
     * <p/>
     * /**
     * 当前插件的名称
     */
    public String getName();

    /**
     * 唯一标识
     */
    public String getIdentify();

    /**
     * 该apk的描述信息
     */
    public String getDescriptor();

    /**
     * 该apk的配置信息
     */
    public ApkConfig getConfig();

    /**
     * 签名
     */
    public String getSign();

    /**
     * apk的大小
     */
    public long getSize();

    /**
     * 调起时间
     */
    public long riseTime();

    /**
     * 调起消耗时间
     */
    public long riseCost();
}
