package com.github.xuse.jmxspy.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class IOUtils {
	private static final int DEFAULT_BUFFER_SIZE = 4096;

	public static void closeQuietly(Closeable c) {
		try {
			if (c != null)
				c.close();
		} catch (IOException e) {
			// do nothing
		}
	}

	/**
	 * 检查/创建 文件夹
	 * 
	 * @param path
	 */
	public static void createFolder(String path) {
		createFolder(new File(path));
	}

	public static void createFolder(File file) {
		if (file.exists() && file.isFile()) {
			throw new RuntimeException("Duplicate name file exist. can't create directory " + file.getPath());
		} else if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * 将URL的数据以BufferedInputStream的方式获取
	 * 
	 * @param url
	 * @return BufferedInputStream
	 */
	public static BufferedInputStream getInputStream(URL url) {
		try {
			URLConnection conn = url.openConnection();
			return new BufferedInputStream(conn.getInputStream());
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * 获得二进制文件写入句柄
	 * 
	 * @Title: getInputStream
	 */
	public static BufferedInputStream getInputStream(File file) {
		try {
			return new BufferedInputStream(new FileInputStream(file));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * 文件(目录)重新命名
	 * 
	 * @param file     要处理的文件或目录
	 * @param newName  修改后的文件名（不含路径）。
	 * @param overwite 覆盖模式，如果目标文件已经存在，则删除目标文件后再改名
	 * @return 如果成功改名，返回改名后的file对象，否则返回null。
	 */
	public static File rename(File file, String newName, boolean overwite) {
		File target = new File(file.getParentFile(), newName);
		if (target.exists()) {
			if (overwite) {
				if (!target.delete())
					return null;
			} else {
				return null;
			}
		}
		return file.renameTo(target) ? target : null;
	}

	/*
	 * Copies the contents of the given {@link InputStream} to the given {@link
	 * OutputStream}.
	 * 
	 * @param pIn The input stream, which is being read. It is guaranteed, that
	 * {@link InputStream#close()} is called on the stream.
	 * 关于InputStram在何时关闭的问题，我一直认为应当是成对操作的（即在哪个方法中生成Stream，就要在使用完后关闭），
	 * 因此不打算在这里使用close方法。 但是后来我又考虑到，InputStream在使用完后，其内部标记已经发生了变化，无法再次使用。
	 * (reset方法的效果和实现有关，并不能保证回复到Stream使用前的状态。)
	 * 因此考虑这里统一关闭以防止疏漏，外面再关一次也不会有问题(作为好习惯，还是应该成对打开和关闭)。
	 * 
	 * @param pOut 输出流，可以为null,此时输入流中的相应数据将丢弃
	 * 
	 * @param pClose True guarantees, that {@link OutputStream#close()} is called on
	 * the stream. False indicates, that only {@link OutputStream#flush()} should be
	 * called finally.
	 * 
	 * @param pBuffer Temporary buffer, which is to be used for copying data.
	 * 
	 * @return Number of bytes, which have been copied.
	 * 
	 * @throws IOException An I/O error occurred.
	 */
	private static long copy(InputStream in, OutputStream out, boolean inClose, boolean outClose, byte[] pBuffer) throws IOException {
		if (in == null)
			throw new NullPointerException();
		long total = 0;
		try {
			int res;
			while ((res = in.read(pBuffer)) != -1) {
				if (out != null) {
					out.write(pBuffer, 0, res);
				}
				total += res;
			}
			if (out != null)
				out.flush();
		} finally {
			if (outClose)
				closeQuietly(out);
			if (inClose)
				closeQuietly(in);
		}
		return total;
	}

	/*
	 * 同上、READER和Writer之间的拷贝
	 */
	private static long copy(Reader in, Writer out, boolean inClose, boolean outClose, char[] pBuffer) throws IOException {
		if (in == null)
			throw new NullPointerException();
		long total = 0;
		try {
			int res;
			while ((res = in.read(pBuffer)) != -1) {
				if (out != null) {
					out.write(pBuffer, 0, res);
				}
				total += res;
			}
			if (out != null)
				out.flush();
		} finally {
			if (outClose && out != null)
				closeQuietly(out);
			if (inClose)
				closeQuietly(in);
		}
		return total;
	}

	/**
	 * 流之间拷贝
	 * 
	 * @param in       输入
	 * @param out      输出
	 * @param inClose  关闭输入流？
	 * @param outClose 关闭输出流?
	 * @return
	 * @throws IOException
	 */
	public static long copy(InputStream in, OutputStream out, boolean inClose, boolean outClose) throws IOException {
		return copy(in, out, inClose, outClose, new byte[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * 流之间拷贝
	 * 
	 * @param in       输入
	 * @param out      输出
	 * @param inClose  关闭输入流
	 * @param outClose 关闭输出流
	 * @return
	 * @throws IOException
	 */
	public static long copy(Reader in, Writer out, boolean inClose, boolean outClose) throws IOException {
		return copy(in, out, inClose, outClose, new char[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * 流之间拷贝
	 * 
	 * @param in     输入
	 * @param out    输出
	 * @param pClose 关闭输出流?
	 * @return 拷贝长度
	 * @throws IOException
	 */
	public static long copy(Reader in, Writer out, boolean pClose) throws IOException {
		return copy(in, out, true, pClose, new char[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * 流之间拷贝
	 * 
	 * @param in             输入
	 * @param out            输出
	 * @param closeOutStream 关闭输出流? (输入流默认关闭)
	 * @return
	 * @throws IOException
	 */
	public static long copy(InputStream in, OutputStream out, boolean closeOutStream) throws IOException {
		return copy(in, out, true, closeOutStream, new byte[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * 获得文本文件写入流
	 * 
	 * @param target
	 * @param charSet
	 * @param append
	 * @return
	 * @throws IOException
	 */
	public static BufferedWriter getWriter(File target, Charset charSet, boolean append) {
		ensureParentFolder(target);
		try {
			OutputStream os = new FileOutputStream(target, append);
			if (charSet == null)
				charSet = Charset.defaultCharset();
			OutputStreamWriter osw = new OutputStreamWriter(os, charSet);
			return new BufferedWriter(osw);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 获得文本文件写入流
	 * 
	 * @param target
	 * @param charSet
	 * @return
	 * @throws IOException
	 */
	public static BufferedWriter getWriter(File target, Charset charSet) {
		return getWriter(target, charSet, false);
	}

	/**
	 * 将OutputStream封装为BufferedWriter
	 * 
	 * @param out
	 * @param charSet
	 * @return
	 */
	public static BufferedWriter getWriter(OutputStream out, Charset charSet) {
		if (charSet == null)
			charSet = Charset.defaultCharset();
		OutputStreamWriter osw;
		osw = new OutputStreamWriter(out, charSet);
		return new BufferedWriter(osw);
	}

	/**
	 * 返回创建文件的流
	 * 
	 * @param file
	 * @return
	 */
	public static BufferedOutputStream getOutputStream(File file) {
		return getOutputStream(file, OverWrittenMode.YES);
	}

	/**
	 * 返回创建文件的流
	 * 
	 * @param file
	 * @param mode
	 * @return
	 */
	public static BufferedOutputStream getOutputStream(File file, OverWrittenMode mode) {
		if (file.exists()) {
			if (mode == OverWrittenMode.NO) {
				return null;
			} else if (mode == OverWrittenMode.ESCAPE_NAME || mode == OverWrittenMode.AUTO) {
				file = IOUtils.escapeExistFile(file);
			} else if (mode == OverWrittenMode.YES && file.isDirectory()) {
				throw new IllegalArgumentException("the folder " + file.getAbsolutePath() + " is already exists");
			}
		}
		ensureParentFolder(file);
		try {
			return new BufferedOutputStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * 检查/创建文件在所的文件夹。 如果该文件所在的文件夹已存在，什么也不做。 如果该文件所在的文件夹不存在，则创建
	 * 
	 * @param file 要检查的路径
	 */
	public static void ensureParentFolder(File file) {
		File f = file.getParentFile();
		if (f != null && !f.exists()) {
			f.mkdirs();
		} else if (f != null && f.isFile()) {
			throw new RuntimeException(f.getAbsolutePath() + " is a exist file, can't create directory.");
		}
	}

	/**
	 * 给定一个File,确认其不存在于在磁盘上，如果存在就改名以回避 <br>
	 * 这个方法用于向磁盘输出文件时使用。<br>
	 * 比如输出名为 report.txt时，如果发现上一次的report.txt还在那么就会返回 "report(1).txt"。
	 * 如果"report(1).txt"也存在就会返回"report(2).txt"。 以此类推。
	 * 
	 * @param file 目标文件
	 * @return 如果目标文件不存在，返回本身。如果目标文件已存在，就返回一个带后缀而磁盘上不存在的文件。
	 */
	public static File escapeExistFile(File file) {
		if (!file.exists())
			return file;
		int pos = file.getName().lastIndexOf(".");
		String path = file.getParent();
		if (StringUtils.isEmpty(path)) {
			throw new IllegalArgumentException(file.getAbsolutePath() + " has no valid parent folder.");
		}
		String baseFilename = null;
		String extName = null;
		if (pos > -1) {
			baseFilename = file.getName().substring(0, pos);
			extName = file.getName().substring(pos + 1);
		} else {
			baseFilename = file.getName();
		}
		int n = 1;
		while (file.exists()) {
			file = new File(path + "/" + baseFilename + "(" + n + ")" + ((extName == null) ? "" : "." + extName));
			n++;
		}
		return file;
	}

}
