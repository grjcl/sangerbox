package com.video.server.utils;

import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

public class JSONObjectUtil {

    /**
     * JSONObject转换配置方法
     *
     * @return
     */
    public static JsonConfig getJsonConfig() {
        var config = new JsonConfig();
        config.setJsonPropertyFilter(new PropertyFilter() {

            @Override
            public boolean apply(Object source, String name, Object value) {
                if (value == null || "".equals(value) || "userPhone".equals(name)) {
                    return true;
                }
                return false;
            }
        });
        return config;
    }

}
