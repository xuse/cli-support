package com.github.xuse.jmxspy;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public interface ExtensionContext {
	/**
	 * 获得属性
	 * @param key
	 * @return
	 */
	String getProperty(String key);
	/**
	 * 获得属性，如果为空抛出异常
	 * @param key
	 * @return
	 */
	String getPropertyNotEmpty(String key);
	/**
	 * 是否存在属性
	 * @param key
	 * @return
	 */
	boolean contains(String key);
	
	/**
	 * 得到文件读取数据
	 * @param fileName
	 * @return
	 */
	BufferedReader getReader(String fileName);
	
	BufferedWriter getWriter(String fileName);
	
	/**
	 * 输出错误
	 * @param string
	 */
	void error(String string);
}
