package com.github.xuse.jmxspy;

import java.util.Map;

import com.github.xuse.jmxspy.util.Args;

public interface Command {

	void setContext(ExtensionContext env);
	
	void run(Args args) throws Exception;
	
	/**
	 * 任务提供支持的参数说明 
	 * @return
	 */
	Map<String,String> getParamDesc();

	default String getName() {
		return this.getClass().getSimpleName();
	};

}
