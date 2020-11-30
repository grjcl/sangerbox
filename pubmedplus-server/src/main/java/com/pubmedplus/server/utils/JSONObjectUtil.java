package com.pubmedplus.server.utils;

import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

public class JSONObjectUtil {

	public static JsonConfig getJsonConfig() {
		var config = new JsonConfig(); 
        config.setJsonPropertyFilter(new PropertyFilter() {
            
            @Override
            public boolean apply(Object source, String name, Object value) {
                if (value == null || "".equals(value) || "isComment".equals(name) || "userPhone".equals(name)) {
                    return true;
                }
                return false;
            }
        });
        return config;
	}
	
}
