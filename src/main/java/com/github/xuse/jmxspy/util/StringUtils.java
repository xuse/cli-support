/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xuse.jmxspy.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class StringUtils {
	public static final byte CR = 0x0D;
	public static final byte LF = 0x0A;
	public static final byte[] CRLF = { CR, LF };
	public static final String CRLF_STR = "\r\n";

	public static final Charset UTF8 = Charset.forName("UTF-8");


	/**
	 * 将字符串格式化为固定大小
	 * 
	 * @param number
	 * @param length
	 * @return
	 */
	public static String toFixLengthString(String text, int length, boolean padOnLeft, char padChar) {
		if (text.length() == length) {
			return text;
		} else if (text.length() > length) {
			return text.substring(0, length);
		}
		StringBuilder sb = new StringBuilder(length);
		if (padOnLeft) {
			repeat(sb, padChar, length - text.length());
		}
		sb.append(text);
		if (!padOnLeft) {
			repeat(sb, padChar, length - text.length());
		}
		return sb.toString();
	}
	
	/**
	 * 在StringBuilder或各种Appendable中重复添加某个字符串若干次
	 * 
	 * @param sb
	 *            源
	 * @param str
	 *            要添加的字符
	 * @param n
	 *            重复次数，如果传入小于等于0的值，不作处理
	 */
	public static void repeat(Appendable sb, char str, int n) {
		if (n <= 0)
			return;
		try {
			for (int i = 0; i < n; i++) {
				sb.append(str);
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * 对象转文本
	 * 
	 * @param obj
	 * @return
	 */
	public static String toString(Object obj) {
		if (obj == null)
			return "";
		return obj.toString();
	}

	/**
	 * <p>
	 * Splits the provided text into an array, separator specified. This is an
	 * alternative to using StringTokenizer.
	 * </p>
	 *
	 * <p>
	 * The separator is not included in the returned String array. Adjacent
	 * separators are treated as one separator. For more control over the split use
	 * the StrTokenizer class.
	 * </p>
	 *
	 * <p>
	 * A <code>null</code> input String returns <code>null</code>.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.split(null, *)         = null
	 * StringUtils.split("", *)           = []
	 * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
	 * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
	 * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
	 * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
	 * </pre>
	 *
	 * @param str           the String to parse, may be null
	 * @param separatorChar the character used as the delimiter
	 * @return an array of parsed Strings, <code>null</code> if null String input
	 * @since 2.0
	 */
	public static String[] split(String str, char separatorChar) {
		return splitWorker(str, separatorChar, false);
	}

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	/**
	 * Performs the logic for the <code>split</code> and
	 * <code>splitPreserveAllTokens</code> methods that do not return a maximum
	 * array length.
	 *
	 * @param str               the String to parse, may be <code>null</code>
	 * @param separatorChar     the separate character
	 * @param preserveAllTokens if <code>true</code>, adjacent separators are
	 *                          treated as empty token separators; if
	 *                          <code>false</code>, adjacent separators are treated
	 *                          as one separator.
	 * @return an array of parsed Strings, <code>null</code> if null String input
	 */
	private static String[] splitWorker(String str, char separatorChar, boolean preserveAllTokens) {
		// Performance tuned for 2.0 (JDK1.4)

		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return EMPTY_STRING_ARRAY;
		}
		List<String> list = new ArrayList<>();
		int i = 0, start = 0;
		boolean match = false;
		boolean lastMatch = false;
		while (i < len) {
			if (str.charAt(i) == separatorChar) {
				if (match || preserveAllTokens) {
					list.add(str.substring(start, i));
					match = false;
					lastMatch = true;
				}
				start = ++i;
				continue;
			}
			lastMatch = false;
			match = true;
			i++;
		}
		if (match || (preserveAllTokens && lastMatch)) {
			list.add(str.substring(start, i));
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	public static boolean isEmpty(String user) {
		return user == null || user.length() == 0;
	}

	public static boolean isNotEmpty(String user) {
		return user != null && user.length() > 0;
	}

	/**
	 * 文本转换到整数int
	 * 
	 * @param o
	 * @param defaultValue
	 * @return
	 */
	public static int toInt(String o, Integer defaultValue) {
		if (isBlank(o))
			return defaultValue;// 空白则返回默认值，即便默认值为null也返回null
		try {
			return Integer.valueOf(o);
		} catch (NumberFormatException e) {
			if (defaultValue == null)// 默认值为null，且数值非法的情况下抛出异常
				throw e;
			return defaultValue;
		}
	}

	/**
	 * 检查一个字符串是否符合数字的格式
	 * 
	 * @Title: isNumericOrMinus @param isFloat 是否允许小数 @return boolean 返回类型 @throws
	 */
	public static boolean isNumericOrMinus(String str, boolean isFloat) {
		if (str == null)
			return false;
		int sz = str.length();
		if (sz == 0)
			return false;
		short hasPoint = 0;
		short start = 0;
		if (str.charAt(0) == '-')
			start = 1;
		for (int i = start; i < sz; i++) {
			char c = str.charAt(i);
			if (!Character.isDigit(c)) {
				if (c == '.' && hasPoint == 0 && isFloat) {
					hasPoint = 1;
				} else {
					return false;
				}
			}
		}
		return (sz - start) > hasPoint;
	}

	/**
	 * 判断是否为合法的数字（包括负数，但不能为小数）
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumericOrMinus(String str) {
		return isNumericOrMinus(str, false);
	}

	/**
	 * 判断是否为合法的数字（包括负数、小数）
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isValidNumer(String str) {
		return isNumericOrMinus(str, false);
	}

	/**
	 * <p>
	 * Replaces all occurrences of Strings within another String.
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> reference passed to this method is a no-op, or if any
	 * "search string" or "string to replace" is null, that replace will be ignored.
	 * This will not repeat. For repeating replaces, call the overloaded method.
	 * </p>
	 * 
	 * <pre>
	 *  StringUtils.replaceEach(null, *, *)        = null
	 *  StringUtils.replaceEach("", *, *)          = ""
	 *  StringUtils.replaceEach("aba", null, null) = "aba"
	 *  StringUtils.replaceEach("aba", new String[0], null) = "aba"
	 *  StringUtils.replaceEach("aba", null, new String[0]) = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, null)  = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""})  = "b"
	 *  StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"})  = "aba"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"})  = "wcte"
	 *  (example of how it does not repeat)
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"})  = "dcte"
	 * </pre>
	 * 
	 * @param text            text to search and replace in, no-op if null
	 * @param searchList      the Strings to search for, no-op if null
	 * @param replacementList the Strings to replace them with, no-op if null
	 * @return the text with any replacements processed, <code>null</code> if null
	 *         String input
	 * @throws IndexOutOfBoundsException if the lengths of the arrays are not the
	 *                                   same (null is ok, and/or size 0)
	 * @since 2.4
	 */
	public static String replaceEach(String text, String[] searchList, String[] replacementList) {
		return replaceEach(text, searchList, replacementList, false, 0);
	}

	/**
	 * <p>
	 * Replaces all occurrences of Strings within another String.
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> reference passed to this method is a no-op, or if any
	 * "search string" or "string to replace" is null, that replace will be ignored.
	 * </p>
	 * 
	 * <pre>
	 *  StringUtils.replaceEach(null, *, *, *) = null
	 *  StringUtils.replaceEach("", *, *, *) = ""
	 *  StringUtils.replaceEach("aba", null, null, *) = "aba"
	 *  StringUtils.replaceEach("aba", new String[0], null, *) = "aba"
	 *  StringUtils.replaceEach("aba", null, new String[0], *) = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, null, *) = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}, *) = "b"
	 *  StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"}, *) = "aba"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}, *) = "wcte"
	 *  (example of how it repeats)
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, false) = "dcte"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, true) = "tcte"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}, *) = IllegalArgumentException
	 * </pre>
	 * 
	 * @param text            text to search and replace in, no-op if null
	 * @param searchList      the Strings to search for, no-op if null
	 * @param replacementList the Strings to replace them with, no-op if null
	 * @param repeat          if true, then replace repeatedly until there are no
	 *                        more possible replacements or timeToLive < 0
	 * @param timeToLive      if less than 0 then there is a circular reference and
	 *                        endless loop
	 * @return the text with any replacements processed, <code>null</code> if null
	 *         String input
	 * @throws IllegalArgumentException  if the search is repeating and there is an
	 *                                   endless loop due to outputs of one being
	 *                                   inputs to another
	 * @throws IndexOutOfBoundsException if the lengths of the arrays are not the
	 *                                   same (null is ok, and/or size 0)
	 * @since 2.4
	 */
	private static String replaceEach(String text, String[] searchList, String[] replacementList, boolean repeat, int timeToLive) {

		// mchyzer Performance note: This creates very few new objects (one major goal)
		// let me know if there are performance requests, we can create a harness to
		// measure

		if (text == null || text.length() == 0 || searchList == null || searchList.length == 0 || replacementList == null || replacementList.length == 0) {
			return text;
		}

		// if recursing, this shouldnt be less than 0
		if (timeToLive < 0) {
			throw new IllegalStateException("TimeToLive of " + timeToLive + " is less than 0: " + text);
		}

		int searchLength = searchList.length;
		int replacementLength = replacementList.length;

		// make sure lengths are ok, these need to be equal
		if (searchLength != replacementLength) {
			throw new IllegalArgumentException("Search and Replace array lengths don't match: " + searchLength + " vs " + replacementLength);
		}

		// keep track of which still have matches
		boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

		// index on index that the match was found
		int textIndex = -1;
		int replaceIndex = -1;
		int tempIndex = -1;

		// index of replace array that will replace the search string found
		// NOTE: logic duplicated below START
		for (int i = 0; i < searchLength; i++) {
			if (noMoreMatchesForReplIndex[i] || searchList[i] == null || searchList[i].length() == 0 || replacementList[i] == null) {
				continue;
			}
			tempIndex = text.indexOf(searchList[i]);

			// see if we need to keep searching for this
			if (tempIndex == -1) {
				noMoreMatchesForReplIndex[i] = true;
			} else {
				if (textIndex == -1 || tempIndex < textIndex) {
					textIndex = tempIndex;
					replaceIndex = i;
				}
			}
		}
		// NOTE: logic mostly below END

		// no search strings found, we are done
		if (textIndex == -1) {
			return text;
		}

		int start = 0;

		// get a good guess on the size of the result buffer so it doesnt have to double
		// if it goes over a bit
		int increase = 0;

		// count the replacement text elements that are larger than their corresponding
		// text being replaced
		for (int i = 0; i < searchList.length; i++) {
			if (searchList[i] == null || replacementList[i] == null) {
				continue;
			}
			int greater = replacementList[i].length() - searchList[i].length();
			if (greater > 0) {
				increase += 3 * greater; // assume 3 matches
			}
		}
		// have upper-bound at 20% increase, then let Java take over
		increase = Math.min(increase, text.length() / 5);

		StringBuffer buf = new StringBuffer(text.length() + increase);

		while (textIndex != -1) {

			for (int i = start; i < textIndex; i++) {
				buf.append(text.charAt(i));
			}
			buf.append(replacementList[replaceIndex]);

			start = textIndex + searchList[replaceIndex].length();

			textIndex = -1;
			replaceIndex = -1;
			tempIndex = -1;
			// find the next earliest match
			// NOTE: logic mostly duplicated above START
			for (int i = 0; i < searchLength; i++) {
				if (noMoreMatchesForReplIndex[i] || searchList[i] == null || searchList[i].length() == 0 || replacementList[i] == null) {
					continue;
				}
				tempIndex = text.indexOf(searchList[i], start);

				// see if we need to keep searching for this
				if (tempIndex == -1) {
					noMoreMatchesForReplIndex[i] = true;
				} else {
					if (textIndex == -1 || tempIndex < textIndex) {
						textIndex = tempIndex;
						replaceIndex = i;
					}
				}
			}
			// NOTE: logic duplicated above END

		}
		int textLength = text.length();
		for (int i = start; i < textLength; i++) {
			buf.append(text.charAt(i));
		}
		String result = buf.toString();
		if (!repeat) {
			return result;
		}

		return replaceEach(result, searchList, replacementList, repeat, timeToLive - 1);
	}

	// Equals
	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Compares two Strings, returning <code>true</code> if they are equal.
	 * </p>
	 *
	 * <p>
	 * <code>null</code>s are handled without exceptions. Two <code>null</code>
	 * references are considered to be equal. The comparison is case sensitive.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.equals(null, null)   = true
	 * StringUtils.equals(null, "abc")  = false
	 * StringUtils.equals("abc", null)  = false
	 * StringUtils.equals("abc", "abc") = true
	 * StringUtils.equals("abc", "ABC") = false
	 * </pre>
	 *
	 * @see java.lang.String#equals(Object)
	 * @param str1 the first String, may be null
	 * @param str2 the second String, may be null
	 * @return <code>true</code> if the Strings are equal, case sensitive, or both
	 *         <code>null</code>
	 */
	public static boolean equals(String str1, String str2) {
		return str1 == null ? str2 == null : str1.equals(str2);
	}

	/**
	 * <p>
	 * Compares two Strings, returning <code>true</code> if they are equal ignoring
	 * the case.
	 * </p>
	 *
	 * <p>
	 * <code>null</code>s are handled without exceptions. Two <code>null</code>
	 * references are considered equal. Comparison is case insensitive.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.equalsIgnoreCase(null, null)   = true
	 * StringUtils.equalsIgnoreCase(null, "abc")  = false
	 * StringUtils.equalsIgnoreCase("abc", null)  = false
	 * StringUtils.equalsIgnoreCase("abc", "abc") = true
	 * StringUtils.equalsIgnoreCase("abc", "ABC") = true
	 * </pre>
	 *
	 * @see java.lang.String#equalsIgnoreCase(String)
	 * @param str1 the first String, may be null
	 * @param str2 the second String, may be null
	 * @return <code>true</code> if the Strings are equal, case insensitive, or both
	 *         <code>null</code>
	 */
	public static boolean equalsIgnoreCase(String str1, String str2) {
		return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
	}

	/**
	 * <p>
	 * Checks if a String is whitespace, empty ("") or null.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 *
	 * @param str the String to check, may be null
	 * @return <code>true</code> if the String is null, empty or whitespace
	 * @since 2.0
	 */
	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>
	 * Checks if a String is not empty (""), not null and not whitespace only.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.isNotBlank(null)      = false
	 * StringUtils.isNotBlank("")        = false
	 * StringUtils.isNotBlank(" ")       = false
	 * StringUtils.isNotBlank("bob")     = true
	 * StringUtils.isNotBlank("  bob  ") = true
	 * </pre>
	 *
	 * @param str the String to check, may be null
	 * @return <code>true</code> if the String is not empty and not null and not
	 *         whitespace
	 * @since 2.0
	 */
	public static boolean isNotBlank(String str) {
		return !StringUtils.isBlank(str);
	}

	/**
	 * <p>
	 * Removes all occurrences of a substring from within the source string.
	 * </p>
	 *
	 * <p>
	 * A <code>null</code> source string will return <code>null</code>. An empty
	 * ("") source string will return the empty string. A <code>null</code> remove
	 * string will return the source string. An empty ("") remove string will return
	 * the source string.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.remove(null, *)        = null
	 * StringUtils.remove("", *)          = ""
	 * StringUtils.remove(*, null)        = *
	 * StringUtils.remove(*, "")          = *
	 * StringUtils.remove("queued", "ue") = "qd"
	 * StringUtils.remove("queued", "zz") = "queued"
	 * </pre>
	 *
	 * @param str    the source String to search, may be null
	 * @param remove the String to search for and remove, may be null
	 * @return the substring with the string removed if found, <code>null</code> if
	 *         null String input
	 * @since 2.1
	 */
	public static String remove(String str, String remove) {
		if (isEmpty(str) || isEmpty(remove)) {
			return str;
		}
		return replace(str, remove, "", -1);
	}

	/**
	 * <p>
	 * Replaces all occurrences of a String within another String.
	 * </p>
	 *
	 * <p>
	 * A <code>null</code> reference passed to this method is a no-op.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.replace(null, *, *)        = null
	 * StringUtils.replace("", *, *)          = ""
	 * StringUtils.replace("any", null, *)    = "any"
	 * StringUtils.replace("any", *, null)    = "any"
	 * StringUtils.replace("any", "", *)      = "any"
	 * StringUtils.replace("aba", "a", null)  = "aba"
	 * StringUtils.replace("aba", "a", "")    = "b"
	 * StringUtils.replace("aba", "a", "z")   = "zbz"
	 * </pre>
	 *
	 * @see #replace(String text, String searchString, String replacement, int max)
	 * @param text         text to search and replace in, may be null
	 * @param searchString the String to search for, may be null
	 * @param replacement  the String to replace it with, may be null
	 * @return the text with any replacements processed, <code>null</code> if null
	 *         String input
	 */
	public static String replace(String text, String searchString, String replacement) {
		return replace(text, searchString, replacement, -1);
	}

	/**
	 * <p>
	 * Replaces a String with another String inside a larger String, for the first
	 * <code>max</code> values of the search String.
	 * </p>
	 *
	 * <p>
	 * A <code>null</code> reference passed to this method is a no-op.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.replace(null, *, *, *)         = null
	 * StringUtils.replace("", *, *, *)           = ""
	 * StringUtils.replace("any", null, *, *)     = "any"
	 * StringUtils.replace("any", *, null, *)     = "any"
	 * StringUtils.replace("any", "", *, *)       = "any"
	 * StringUtils.replace("any", *, *, 0)        = "any"
	 * StringUtils.replace("abaa", "a", null, -1) = "abaa"
	 * StringUtils.replace("abaa", "a", "", -1)   = "b"
	 * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
	 * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
	 * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
	 * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
	 * </pre>
	 *
	 * @param text         text to search and replace in, may be null
	 * @param searchString the String to search for, may be null
	 * @param replacement  the String to replace it with, may be null
	 * @param max          maximum number of values to replace, or <code>-1</code>
	 *                     if no maximum
	 * @return the text with any replacements processed, <code>null</code> if null
	 *         String input
	 */
	public static String replace(String text, String searchString, String replacement, int max) {
		if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
			return text;
		}
		int start = 0;
		int end = text.indexOf(searchString, start);
		if (end == INDEX_NOT_FOUND) {
			return text;
		}
		int replLength = searchString.length();
		int increase = replacement.length() - replLength;
		increase = (increase < 0 ? 0 : increase);
		increase *= (max < 0 ? 16 : (max > 64 ? 64 : max));
		StringBuilder buf = new StringBuilder(text.length() + increase);
		while (end != INDEX_NOT_FOUND) {
			buf.append(text.substring(start, end)).append(replacement);
			start = end + replLength;
			if (--max == 0) {
				break;
			}
			end = text.indexOf(searchString, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}

	/**
	 * <p>
	 * Removes all occurrences of a character from within the source string.
	 * </p>
	 *
	 * <p>
	 * A <code>null</code> source string will return <code>null</code>. An empty
	 * ("") source string will return the empty string.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.remove(null, *)       = null
	 * StringUtils.remove("", *)         = ""
	 * StringUtils.remove("queued", 'u') = "qeed"
	 * StringUtils.remove("queued", 'z') = "queued"
	 * </pre>
	 *
	 * @param str    the source String to search, may be null
	 * @param remove the char to search for and remove, may be null
	 * @return the substring with the char removed if found, <code>null</code> if
	 *         null String input
	 * @since 2.1
	 */
	public static String remove(String str, char remove) {
		if (isEmpty(str) || str.indexOf(remove) == INDEX_NOT_FOUND) {
			return str;
		}
		char[] chars = str.toCharArray();
		int pos = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] != remove) {
				chars[pos++] = chars[i];
			}
		}
		return new String(chars, 0, pos);
	}

	/**
	 * 给定若干字符，从后向前寻找，任意一个匹配的字符。
	 * 
	 * @param str         需要查找的字符串
	 * @param searchChars 需要查找的字符序列
	 * @param startPos    开始位置
	 * @return
	 */
	public static int lastIndexOfAny(String str, char[] searchChars, int startPos) {
		if ((str == null) || (searchChars == null)) {
			return -1;
		}
		if (startPos < 0) {
			startPos = 0;
		}
		for (int i = str.length() - 1; i >= startPos; i--) {
			char c = str.charAt(i);
			for (int j = 0; j < searchChars.length; j++) {
				if (c == searchChars[j]) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 将两个数值的比值作为百分比显示
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static String toPercent(long a, long b) {
		return String.valueOf(10000 * a / b / 100f).concat("%");
	}

	/**
	 * Represents a failed index search.
	 * 
	 * @since 2.1
	 */
	public static final int INDEX_NOT_FOUND = -1;
}
