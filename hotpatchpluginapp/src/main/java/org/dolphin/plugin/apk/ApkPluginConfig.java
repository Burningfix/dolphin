package org.dolphin.plugin.apk;

import org.dolphin.hotpatch.apk.ApkPluginInterface;

/**
 * Created by hanyanan on 2015/11/23.
 */
public class ApkPluginConfig extends ApkPluginInterface {
    public ApkPluginConfig() {
        super();
        this.descriptor = "Hello World!";
        this.id = "1";
        this.name = "first_plugin";
        this.pageSpecList.add(new PageSpec() {
            @Override
            public String name() {
                return "PluginFragment";
            }

            @Override
            public String id() {
                return "1";
            }

            @Override
            public String path() {
                return "org.dolphin.hotpatchpluginapp.CustomFragment";
            }

            @Override
            public boolean hasOption() {
                return false;
            }
        });
    }
}
