package com.github.xuse.jmxspy.util.zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.github.xuse.jmxspy.util.Assert;
import com.github.xuse.jmxspy.util.FileName;
import com.github.xuse.jmxspy.util.IOUtils;

public class VolSwitchAbleOutputStream extends SwitchAbleOutputStream {
	private File templateFile;
	private File firstFile;

	public VolSwitchAbleOutputStream(File firstFile, long volumnSize) throws FileNotFoundException {
		super(new FileOutputStream(firstFile), volumnSize);
		this.templateFile = firstFile.getAbsoluteFile();
		this.firstFile = firstFile;
	}

	@Override
	protected OutputStream getNextVolOutputStream(int currentIndex) {
		File parent = templateFile.getParentFile();
		Assert.folderExist(parent);
		FileName names = FileName.valueOf(templateFile.getName());
		if (currentIndex == 1) {// 第一个文件自动改名
			String first = names.append(".part" + currentIndex).get();
			this.firstFile = IOUtils.rename(templateFile, first, true);
			if (firstFile == null) {
				throw new IllegalAccessError("Can not rename file " + templateFile.getAbsolutePath() + " to " + first);
			}
		}
		try {
			File newFile = names.append(".part" + (currentIndex + 1)).asFileInDirectory(parent);
			return new FileOutputStream(newFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public File getFirstVolFile() {
		return firstFile;
	}
}
