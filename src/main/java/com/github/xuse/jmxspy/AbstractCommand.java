package com.github.xuse.jmxspy;

import com.github.xuse.jmxspy.util.StringUtils;

public abstract class AbstractCommand implements Command {
	protected ExtensionContext context;

	@Override
	public void setContext(ExtensionContext env) {
		this.context = env;
	}

	protected String getGlobal(String key) {
		return getGlobal(key,null);
		
	}
	protected String getGlobal(String key,String defaultValue) {
		String value=context.getProperty(key);
		if(StringUtils.isEmpty(value)) {
			return defaultValue;
		}
		return value;
	}
	
	
	protected String getParamValueNotEmpty(String contextName, String paramName) {
		String value=getParamValue(contextName, paramName);
		if(StringUtils.isEmpty(value)) {
			throw new IllegalArgumentException("参数"+paramName+"不能为空");
		}
		return value;
	}
	
	protected String getParamValue(String contextName, String paramName) {
		String value=context.getProperty(contextName + "." + paramName);
		if(value==null) {
			value=context.getProperty("db."+paramName);
		}
		return value;
	}
}
