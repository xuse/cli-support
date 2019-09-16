package com.github.xuse.jmxspy.util.args;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.xuse.jmxspy.util.StringUtils;

/**
 * 参数解析与提供
 * 
 * @author Joey
 *
 */
public class Args {
	/**
	 * 有名称的参数
	 */
	private Map<String, String> argMap = new HashMap<String, String>();
	/**
	 * 无名称的参数
	 */
	private List<String> defaultArgs = new ArrayList<>();
	/**
	 * 原始参数
	 */
	private String[] args;
	/**
	 * 第一个无名参数作为命令看待（不作为是参数）
	 */
	private boolean firstArgAsCmd;

	/**
	 * 是否有参数
	 * 
	 * @return
	 */
	public boolean isArgEmpty() {
		return argMap.isEmpty() && defaultArgs.size() <= (firstArgAsCmd ? 1 : 0);
	}

	/**
	 * 无名参数个数
	 * 
	 * @return
	 */
	public int getAnonymousArgCount() {
		return defaultArgs.size() - (firstArgAsCmd ? 1 : 0);
	}

	/**
	 * 有名参数个数
	 * 
	 * @return
	 */
	public int getNamedArgCount() {
		return argMap.size();
	}

	public Args(String[] args, boolean firstAsCmd,Collection<String> noValueArgs) {
		this.args = args;
		this.firstArgAsCmd = firstAsCmd;
		initArg(noValueArgs);
		if (firstAsCmd && defaultArgs.isEmpty()) {
			throw new IllegalArgumentException("No command found.");
		}
	}

	public Args(String[] args) {
		this(args, false,Collections.emptySet());
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
	public String get(int key, String defaultValue) {
		return defaultArgs.size() > key ? defaultArgs.get(key) : defaultValue;
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
		return StringUtils.isEmpty(value) ? defaultValue : value;
	}

	/**
	 * 获得参数，如果传入的是一个单词，那么还会检索首字母的缩写配置
	 * 
	 * @deprecated use {@link #get(String)}
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getWithAbbrev(String key, String defaultValue) {
		String value = argMap.get(key);
		if (value == null && key.length() > 1) {
			String firstChar = key.substring(0, 1);
			value = argMap.get(firstChar);
		}
		return value == null ? defaultValue : value;
	}

	/**
	 * 获得默认参数
	 * 
	 * @deprecated use {@link #get(int)}
	 * @param index
	 * @return
	 */
	public String getDefault(int index) {
		return defaultArgs.size() > index ? defaultArgs.get(index) : "";
	}

	/**
	 * 获得缺省参数
	 * 
	 * @deprecated use {@link #get(int)}
	 * @param index
	 * @param msg
	 * @return
	 */
	public String getDefaultOrThrow(int index, String msg) {
		String v = getDefault(index);
		if (StringUtils.isEmpty(v)) {
			throw new IllegalArgumentException("未指定参数" + msg);
		}
		return v;

	}

	/**
	 * 获得参数，如果没有则抛出一异常
	 * 
	 * @deprecated use {@link #get(String)}
	 * @param key
	 * @param msg
	 * @return
	 */
	public String getOrThrow(String key, String msg) {
		String value = argMap.get(key);
		if (StringUtils.isEmpty(value)) {
			throw new IllegalArgumentException("未指定参数" + msg);
		}
		return value;

	}

	/**
	 * 获得参数，如果为空也返回默认值
	 * 
	 * @deprecated use {@link #get(String)}
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
	 * @deprecated
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int getIntWithAbbrev(String key, int defaultValue) {
		String value = argMap.get(key);
		if (StringUtils.isEmpty(value) && key.length() > 1) {
			String firstChar = key.substring(0, 1);
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

	public class AbbrevArg<T extends AbstractArg<T>> {
		private String key;
		private T rawArg;

		/**
		 * 直接获得参数
		 * 
		 * @return
		 */
		public T get() {
			String value = argMap.get(key);
			rawArg.set(value == null ? "" : value);
			return rawArg;
		}

		/**
		 * 支持按首字母缩写获取参数
		 * 
		 * @return
		 */
		public T abbrev() {
			String value = argMap.get(key);
			if (StringUtils.isEmpty(value) && key.length() > 1) {
				String firstChar = key.substring(0, 1);
				value = argMap.get(firstChar);
			}
			rawArg.set(value == null ? "" : value);
			return rawArg;
		}

		AbbrevArg(String key, T getter) {
			this.key = key;
			this.rawArg = getter;
		}
	}

	/**
	 * 得到整数值
	 * 
	 * @param key
	 * @return
	 */
	public IntValue getInt(int key) {
		String value = defaultArgs.size() > key ? defaultArgs.get(key) : "";
		IntValue i = new IntValue();
		i.set(value);
		return i;
	}

	/**
	 * 得到整数值
	 * 
	 * @param key
	 * @return
	 */
	public AbbrevArg<IntValue> getInt(String key) {
		return new AbbrevArg<>(key, new IntValue());
	}

	/**
	 * 得到字符串值
	 * 
	 * @param key
	 * @return
	 */
	public StringValue get(int key) {
		String value = defaultArgs.size() > key ? defaultArgs.get(key) : "";
		StringValue i = new StringValue();
		i.set(value);
		return i;
	}

	/**
	 * 得到字符串值
	 * 
	 * @param key
	 * @return
	 */
	public AbbrevArg<StringValue> get(String key) {
		return new AbbrevArg<>(key, new StringValue());
	}

	/**
	 * 得到布尔值
	 * 
	 * @param key
	 * @return
	 */
	public BooleanValue getBoolean(int key) {
		String value = defaultArgs.size() > key ? defaultArgs.get(key) : "";
		BooleanValue i = new BooleanValue();
		i.set(value);
		return i;
	}

	/**
	 * 得到布尔值
	 * 
	 * @param key
	 * @return
	 */
	public AbbrevArg<BooleanValue> getBoolean(String key) {
		return new AbbrevArg<>(key, new BooleanValue());
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
	 * @deprecated
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
	 * @deprecated
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
	private void initArg(Collection<String> noValueArgs) {
		String lastKey = null;
		for (String s : args) {
			String key = null;
			if (s.startsWith("--")) {
				key = s.substring(2);
			} else if (s.startsWith("-")) {
				key = s.substring(1);
			}
			if(key!=null && noValueArgs.contains(key)) {
				argMap.put(key, "");
				continue;
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
		if (lastKey != null) {
			argMap.put(lastKey, "");
		}
	}

	public static void main(String[] args) {
		String[] s = new String[] { "tail", "-l20", "-f", "--name", "c:\\asadsad\\file.exe", "-c", "utf-8", "main arg", "-k" };
		Args ag = new Args(s);
		System.out.println(ag.argMap);
		System.out.println(ag.defaultArgs);
	}

	public static final LinkedList<String> spliteToken(String text, char de) {
		LinkedList<String> tokens = new LinkedList<String>();
		if (text == null || text.length() == 0) {
			return tokens;
		}
		int total = text.length();
		int begin = 0;
		boolean inQuote = false;
		for (int i = 0; i < total; i++) {
			char c = text.charAt(i);
			if (c == de && !inQuote) {
				int len = i - begin;
				if (len > 0) {
					tokens.add(text.substring(begin, i));
				}
				begin = i + 1;
			} else if (c == '"') {
				if (inQuote) {
					int len = i - begin;
					if (len > 0) {
						tokens.add(text.substring(begin, i));
					}
					inQuote = false;
				} else {
					inQuote = true;
				}
				begin = i + 1;
			}
		}
		if (begin < total) {
			tokens.add(text.substring(begin, total));
		}
		return tokens;
	}
	
	public static Args of(String command, boolean hasCommand) {
		return of(command,hasCommand,Collections.emptySet());
	}
			
	public static Args of(String command, boolean hasCommand,Collection<String> noValueArgs) {
		LinkedList<String> args = spliteToken(command, ' ');
		return new Args(args.toArray(new String[args.size()]),hasCommand,noValueArgs);
	}

	@Override
	public String toString() {
		return defaultArgs + argMap.toString();
	}

	/**
	 * 获得所有的匿名参数
	 * @return
	 */
	public List<String> getAnonymousArgs() {
		if (firstArgAsCmd) {
			return defaultArgs.subList(1, defaultArgs.size());
		} else {
			return Collections.unmodifiableList(defaultArgs);
		}
	}
}
