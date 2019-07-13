package com.github.xuse.jmxspy.util.args;

import org.apache.commons.lang3.StringUtils;

public class StringValue extends AbstractArg<StringValue> {
	private String value;

	public boolean isEmpty() {
		return StringUtils.isEmpty(value);
	}
	
	
	public StringValue defaultIs(String value) {
		if (StringUtils.isEmpty(this.value)) {
			this.value = value;
		}
		return this;
	}

	/**
	 * 
	 * @return
	 * @throws IllegalArgumentException 如果值为空抛出
	 */
	public StringValue assertNotEmpy() {
		if (StringUtils.isEmpty(value)) {
			throw new IllegalArgumentException(getName() + " is empty!");
		}
		return this;
	}

	/**
	 * 设置参数名称，一般用在断言之前
	 * 
	 * @param argName
	 * @return
	 */
	public StringValue argName(String argName) {
		this.argName = argName;
		return this;
	}

	public String get() {
		return value;
	}

	@Override
	void set(String value) {
		this.value = value;
	}
}