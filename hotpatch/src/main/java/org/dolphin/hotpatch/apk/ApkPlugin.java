package org.dolphin.hotpatch.apk;

/**
 * Created by yananh on 2015/11/19.
 */
public interface ApkPlugin {
    /**
     * Apk file path store on disk!
     */
    public String getPath();

    /**
     * 加载该apk的classLoader
     */
    public ClassLoader getClassLoader();

    /* <p/>
    * /**
    * 当前插件的名称
    */
    public String getName();

    /**
     * 唯一标识
     */
    public String getId();

    /**
     * 该apk的描述信息
     */
    public String getDescriptor();

    /**
     * 该apk的配置信息
     */
    public String getExtensionConfig();

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

    public static class Builder {
        private long riseCost;
        private long riseTime;
        private long size;
        private String sign;
        public String extensionConfig;
        private String descriptor;
        private String id;
        private String name;
        private String path;
        private ClassLoader classLoader;

        public Builder setExtensionConfig(String extensionConfig) {
            this.extensionConfig = extensionConfig;
            return this;
        }

        public Builder setRiseCost(long riseCost) {
            this.riseCost = riseCost;
            return this;
        }

        public Builder setRiseTime(long riseTime) {
            this.riseTime = riseTime;
            return this;
        }

        public Builder setSize(long size) {
            this.size = size;
            return this;
        }

        public Builder setSign(String sign) {
            this.sign = sign;
            return this;
        }

        public Builder setDescriptor(String descriptor) {
            this.descriptor = descriptor;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public ApkPlugin build() {
            return new ApkPlugin() {
                @Override
                public String getPath() {
                    return path;
                }

                @Override
                public ClassLoader getClassLoader() {
                    return classLoader;
                }

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getId() {
                    return id;
                }

                @Override
                public String getDescriptor() {
                    return descriptor;
                }

                @Override
                public String getExtensionConfig() {
                    return extensionConfig;
                }

                @Override
                public String getSign() {
                    return sign;
                }

                @Override
                public long getSize() {
                    return size;
                }

                @Override
                public long riseTime() {
                    return riseTime;
                }

                @Override
                public long riseCost() {
                    return riseCost;
                }
            };
        }

    }
}
