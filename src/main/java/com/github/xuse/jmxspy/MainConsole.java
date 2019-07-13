package com.github.xuse.jmxspy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.github.xuse.jmxspy.util.Args;
import com.github.xuse.jmxspy.util.IOUtils;
import com.github.xuse.jmxspy.util.StringUtils;

public class MainConsole implements ExtensionContext {
	private static final String PROMPT = "Alarm>";
	private final Map<String, String> env = new LinkedHashMap<>();
	private final File root;
	private final Map<String, Command> extension = new HashMap<String, Command>();

	public static void main(String[] args) throws IOException {
		MainConsole console = new MainConsole();
		console.start();
	}

	public MainConsole() throws IOException {
		this.root = new File(System.getProperty("user.dir"));
		loadEnv();
		initExtenstion();
	}

	private void loadEnv() throws IOException {
		File config = new File(root, "settings.properties");
		if (config.exists()) {
			load(config.toURI().toURL());
		} else {
			load(this.getClass().getResource("/settings.properties"));
		}
	}

	private void initExtenstion() throws IOException {
		URL url = this.getClass().getResource("/extension.properties");
		Properties p = new Properties();
		try (InputStream in = url.openStream()) {
			p.load(in);
		}
		for (Map.Entry<Object, Object> e : p.entrySet()) {
			String key = String.valueOf(e.getKey());
			String value = String.valueOf(e.getValue());
			Command task = loadInstance(value);
			extension.put(key, task);
		}
	}

	private Command loadInstance(String value) {
		Class<?> clz;
		try {
			clz = Class.forName(value);
			Command task = (Command) clz.newInstance();
			task.setContext(this);
			return task;
		} catch (Exception e) {
			System.out.println("加载失败:" + value);
			throw new RuntimeException(e);
		}
	}

	private void load(URL url) throws IOException {
		if (url == null) {
			throw new IOException("No settings.");
		}
		System.out.println("Loading config:" + url);
		Properties p = new Properties();
		try (InputStream in = url.openStream()) {
			p.load(new InputStreamReader(in, StringUtils.UTF8));
		}
		for (Map.Entry<Object, Object> e : p.entrySet()) {
			env.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
		}
	}

	private void start() throws IOException {
		InputStream in = System.in;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			String s;
			System.out.print(PROMPT);
			while ((s = reader.readLine()) != null) {
				s = s.trim();
				if (StringUtils.isEmpty(s)) {
					continue;
				}
				if ("q".equalsIgnoreCase(s) || "exit".equals(s)) {
					break;
				}
				if ("help".equalsIgnoreCase(s)) {
					help();
				} else if ("env".equalsIgnoreCase(s)) {
					showEnv();
				} else if ("env load".equalsIgnoreCase(s)) {
					loadEnv();
				} else {
					LinkedList<String> argss = spliteToken(s, ' ');
					if (argss.isEmpty()) {
						continue;
					}
					String command = argss.removeFirst();
					try {
						executeCommand(command, argss.toArray(new String[argss.size()]));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				System.out.print(PROMPT);
			}
			System.out.println("Bye bye!");
		}
	}

	private void help() {
		System.out.println("q|exit             \t退出");
		System.out.println("env             \t显示配置");
		System.out.println("env load         \t重新加载配置");
		System.out.println("help             \t显示此帮助");
		System.out.println("help [command]    \t指定命令的帮助");
		for (Map.Entry<String, Command> entry : extension.entrySet()) {
			System.out.println(StringUtils.toFixLengthString(entry.getKey(), 16, false, ' ') + "\t" + entry.getValue().getName());
		}
	}

	private void showEnv() {
		for (Map.Entry<String, String> e : env.entrySet()) {
			System.out.println(e.getKey() + " \t" + e.getValue());
		}
	}

	private void executeCommand(String cmd, String[] args) throws Exception {
		if ("help".equals(cmd)) {
			Command cmdShell = extension.get(args[0]);
			if (cmdShell == null) {
				help();
			} else {
				helpWithCommand(cmdShell, args[0]);
			}
			return;
		}
		System.out.println("执行" + cmd + "\t" + Arrays.toString(args));
		Command cmdShell = extension.get(cmd);
		if (cmdShell == null) {
			print("无效命令:" + cmd);
		} else {
			cmdShell.run(new Args(args));
		}
	}

	private void helpWithCommand(Command cmdShell, String command) {
		System.out.println(cmdShell.getName());
		System.out.print(command);
		if (cmdShell.getParamDesc() != null) {
			String mainParam = null;
			for (Entry<String, String> entry : cmdShell.getParamDesc().entrySet()) {
				if (entry.getKey().isEmpty()) {
					mainParam = entry.getValue();
				} else if (entry.getKey().startsWith("-")) {
					System.out.print(" [" + entry.getKey() + "]");
				}
			}
			if (mainParam != null) {
				System.out.print(" <" + mainParam + ">");
			}
			System.out.println();
			for (Entry<String, String> entry : cmdShell.getParamDesc().entrySet()) {
				if (entry.getKey().isEmpty()) {
					continue;
				}
				System.out.println(StringUtils.toFixLengthString(entry.getKey(), 24, false, ' ') + "\t" + entry.getValue());
			}
		}
	}

	private void print(String string) {
		System.out.println(string);
	}

	public void error(String string) {
		System.err.println(string);
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

	@Override
	public String getProperty(String key) {
		return env.get(key);
	}

	@Override
	public String getPropertyNotEmpty(String key) {
		String value = env.get(key);
		if (StringUtils.isEmpty(value)) {
			throw new IllegalArgumentException("参数" + key + "不能为空");
		}
		return value;
	}

	@Override
	public boolean contains(String key) {
		return env.containsKey(key);
	}

	@Override
	public BufferedReader getReader(String fileName) {
		File file = new File(root, fileName);
		if (!fileName.endsWith(".txt") && !file.exists()) {
			file = new File(root, fileName + ".txt");
		}
		if (!file.isFile()) {
			throw new IllegalArgumentException(file.getAbsolutePath() + "不存在");
		}
		try {
			return new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public BufferedWriter getWriter(String fileName) {
		File file = new File(root, fileName);
		file=IOUtils.escapeExistFile(file);
		try {
			return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
