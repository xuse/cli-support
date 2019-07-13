package com.github.xuse.jmxspy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Args {

	private Map<String, String> argMap = new HashMap<String, String>();
	private List<String> defaultArgs=new ArrayList<>();
	private String[] args;

	public Args(String[] args) {
		this.args = args;
		initArg();
	}

	/**
	 * 是否带参数(横线后面的部分)
	 * 
	 * @param key 多个参数中任意一个有就返回true
	 * @return
	 */
	public boolean containsAny(String... key) {
		for (String s : key) {
			if (argMap.containsKey(s)) {
				return true;
			}
			;
		}
		return false;
	}

	/**
	 * 获得参数
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String get(String key, String defaultValue) {
		String value = argMap.get(key);
		return value == null ? defaultValue : value;
	}
	
	/**
	 * 获得参数，如果传入的是一个单词，那么还会检索首字母的缩写配置
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getWithAbbrev(String key, String defaultValue) {
		String value = argMap.get(key);
		if(value==null && key.length()>1) {
			String firstChar=key.substring(0,1);
			value = argMap.get(firstChar);
		}
		return value == null ? defaultValue : value;
	}
	
	/**
	 * 获得默认参数	
	 * @return
	 */
	public String getFirstDefault() {
		return defaultArgs.isEmpty()? null: defaultArgs.get(0);
	}

	/**
	 * 获得默认参数
	 * @param index
	 * @return
	 */
	public String getDefault(int index) {
		return defaultArgs.size()>index? defaultArgs.get(index):null;
	}
	
	/**
	 * 获得缺省参数
	 * @param index
	 * @param msg
	 * @return
	 */
	public String getDefaultOrThrow(int index, String msg) {
		String v=getDefault(index);
		if(StringUtils.isEmpty(v)) {
			throw new IllegalArgumentException("未指定参数"+msg);
		}
		return v;
		
	}
	/**
	 * 获得参数，如果没有则抛出一异常
	 * @param key
	 * @param msg
	 * @return
	 */
	public String getOrThrow(String key, String msg) {
		String value = argMap.get(key);
		if(StringUtils.isEmpty(value)) {
			throw new IllegalArgumentException("未指定参数"+msg);
		}
		return value;
		
	}

	/**
	 * 获得参数，如果为空也返回默认值
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getNonEmpty(String key, String defaultValue) {
		String value = argMap.get(key);
		return StringUtils.isEmpty(value) ? defaultValue : value;
	}

	/**
	 * 获得Int类型参数，支持缩写
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int getIntWithAbbrev(String key, int defaultValue) {
		String value = argMap.get(key);
		if(StringUtils.isEmpty(value) && key.length()>1) {
			String firstChar=key.substring(0,1);
			value = argMap.get(firstChar);
		}
		if (StringUtils.isEmpty(value)) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	/**
	 * 获得Int类型参数
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int getInt(String key, int defaultValue) {
		String value = argMap.get(key);
		if (StringUtils.isEmpty(value)) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 获得第n个参数,如果没有该参数抛出异常
	 * 
	 * @param index 从0开始
	 * @param name
	 * @return
	 */
	public String getOrThrow(int index, String name) {
		if (args.length <= index) {
			throw new IllegalArgumentException("参数个数不够，不能获得第[" + index + "]个参数:" + name);
		}
		return args[index];
	}

	/**
	 * 获得第n个参数,如果没有该参数返回null
	 * 
	 * @param index 从0开始
	 * @return
	 */
	public String getOptional(int index) {
		if (args.length <= index) {
			return null;
		}
		return args[index];
	}

	// 命令行参数解析
	private void initArg() {
		String lastKey = null;
		for (String s : args) {
			String key = null;
			if (s.startsWith("--")) {
				key = s.substring(2);
			} else if (s.startsWith("-")) {
				key = s.substring(1);
			}
			if (key != null) {
				if (lastKey != null) {
					argMap.put(lastKey, "");
				}
				lastKey = key;
			} else {
				if (lastKey != null) {
					argMap.put(lastKey, s);
					lastKey = null;
				} else {
					defaultArgs.add(s);
				}
			}
		}
		if(lastKey!=null) {
			argMap.put(lastKey, "");
		}
	}

	public static void main(String[] args) {
		String[] s = new String[] { "tail", "-l20", "-f", "--name", "c:\\asadsad\\file.exe", "-c", "utf-8", "main arg" ,"-k"};
		Args ag = new Args(s);
		System.out.println(ag.argMap);

	}
}
