package com.proj.proxyservice.util;

public class ConfigParamsUtil{
	
	public static PropertiesUtil propertiesReader = PropertiesUtil.getInstance("config.properties");

	
	public static String getProp(String key){
		return propertiesReader.getConfigItem(key);
	}
}

