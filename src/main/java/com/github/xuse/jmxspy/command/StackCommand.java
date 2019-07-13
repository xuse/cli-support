package com.github.xuse.jmxspy.command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.github.xuse.jmxspy.AbstractCommand;
import com.github.xuse.jmxspy.beans.JavaLang;
import com.github.xuse.jmxspy.util.StringUtils;
import com.github.xuse.jmxspy.util.Threads;
import com.github.xuse.jmxspy.util.ZipUtils;
import com.github.xuse.jmxspy.util.args.Args;
import com.sun.management.ThreadMXBean;

public class StackCommand extends AbstractCommand{

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void main(String[] args) throws Exception {
		StackCommand t = new StackCommand();
		t.run(new Args(args));
	}

	private static final int MAX_COUNT = 8;
	private static final int interval = 3;


	@Override
	public void run(Args args) throws Exception {
		String url = args.getDefaultOrThrow(0,"Connect host:port");
		String filebase = args.getOrThrow("f", "filename(-f)");
		int count = args.getInt("count").abbrev().defaultIs(2).get();
		String user = args.get("user").abbrev().get();
		String password = args.get("password").abbrev().get();
//		boolean zip=args.containsAny("z","zip");
		
		JMXConnector connector = getConnection(url, user, password);
		List<File> dumps=new ArrayList<File>();
		try {
			MBeanServerConnection connection = connector.getMBeanServerConnection();
			int myCount=Math.min(Integer.valueOf(count), MAX_COUNT);
			ThreadMXBean threadMx = JMX.newMXBeanProxy(connection, new ObjectName(JavaLang.Threading), ThreadMXBean.class);
			for (int i = 1; i <= myCount; i++) {
				File dump1 = new File(filebase + i + ".txt");
				if(i>1) {
					Threads.doSleep(interval * 1000);
				}
				doDump(threadMx, dump1);
				dumps.add(dump1);
			}	
		}finally {
			connector.close();
		}
		//打包并删除旧文件
		if(dumps.size()>0) {
			File zipped=ZipUtils.zip(new File(filebase+".zip"), dumps);
			System.out.println("file "+zipped+" generated.");
			if(zipped.exists()) {
				for(File old: dumps) {
					old.delete();
				}
			}
		}
	}

	private void doDump(ThreadMXBean threadMx, File dump1) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dump1), "US-ASCII"));
		try {
			ThreadInfo[] infos = threadMx.dumpAllThreads(false, false);
			writer.write(df.format(new Date()));
			writer.write("\n");
			writer.write("Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.172-b11 mixed mode):");
			writer.write("\n\n");
			int idx = 0;
			for (ThreadInfo t : infos) {
				writer.write(format(t, idx++));
			}
		} finally {
			close(writer);
		}
		System.out.println("File " + dump1.getAbsolutePath() + " generated.");
	}

	// "JDWP Transport Listener: dt_socket" #5 daemon prio=10 os_prio=0
	// tid=0x00002b819c0bf000 nid=0x4f04 runnable [0x0000000000000000]
	
//	 Deadlock
//	 Runnable
//	  Waiting on condition
//	  Waiting on monitor
//	  Suspended
//	  Object.wait()
//	  Blocked
//	  Parked
	 
	 
	private String format(ThreadInfo t, int idx) {
		StringBuilder sb = new StringBuilder(256);
		sb.append('"').append(t.getThreadName()).append('"').append(' ');
		sb.append('#').append(idx);
		sb.append(" prio=").append(5);
		sb.append(" os_prio=").append(1);
		sb.append(" tid=").append("0x").append(Long.toHexString(t.getThreadId()));
		sb.append(" nid=").append("0x").append(Long.toHexString(t.getThreadId()));
		
		
		StackTraceElement top=t.getStackTrace().length>0? t.getStackTrace()[0]:null;
		if(top!=null && "java.lang.Object".equals(top.getClassName()) && "wait".equals(top.getMethodName())) {
			sb.append(" in Object.wait() ").append("[0x" + Long.toHexString(t.getLockInfo().getIdentityHashCode()) + "]");
		}else if (t.getLockName() != null) {
			sb.append(" waiting on condition [0x" + Long.toHexString(t.getLockInfo().getIdentityHashCode()) + "]");
			if (t.getLockOwnerName() != null) {
				sb.append(" owned by \"" + t.getLockOwnerName() + "\" Id=" + t.getLockOwnerId());
			}
		}else if (t.isSuspended()) {
			sb.append(" suspended");
//		}else if (t.isInNative()) {
//			sb.append(" (in native)");
		}else {
			sb.append(" runnable");
		}
		sb.append('\n');
		sb.append("\tjava.lang.Thread.State: ");
		sb.append(t.getThreadState().name()).append('\n');
		
		
		
		int i = 0;
		StackTraceElement[] stackTrace = t.getStackTrace();
		for (; i < stackTrace.length; i++) {
			StackTraceElement ste = stackTrace[i];
			sb.append("\tat " + ste.toString());
			sb.append('\n');
			if (i == 0 && t.getLockInfo() != null) {
				Thread.State ts = t.getThreadState();
				switch (ts) {
				case BLOCKED:
					sb.append("\t-  blocked on " + t.getLockInfo());
					sb.append('\n');
					break;
				case WAITING:
					sb.append("\t-  waiting on " + t.getLockInfo());
					sb.append('\n');
					break;
				case TIMED_WAITING:
					sb.append("\t-  waiting on " + t.getLockInfo());
					sb.append('\n');
					break;
				default:
				}
			}

			for (MonitorInfo mi : t.getLockedMonitors()) {
				if (mi.getLockedStackDepth() == i) {
					sb.append("\t-  locked " + mi);
					sb.append('\n');
				}
			}
		}
		if (i < stackTrace.length) {
			sb.append("\t...");
			sb.append('\n');
		}

		LockInfo[] locks = t.getLockedSynchronizers();
		if (locks.length > 0) {
			sb.append("\n\tNumber of locked synchronizers = " + locks.length);
			sb.append('\n');
			for (LockInfo li : locks) {
				sb.append("\t- " + li);
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	private JMXConnector getConnection(String url, String user, String password) throws IOException {
		Map<String, String[]> map = null;
		if (StringUtils.isNotEmpty(user)) {
			map = new HashMap<String, String[]>();
			map.put(JMXConnector.CREDENTIALS, new String[] { user, password });
		}
		JMXServiceURL jmxURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + url + "/jmxrmi");
		JMXConnector connector = JMXConnectorFactory.connect(jmxURL, map);
		return connector;
	}

	private void close(BufferedWriter writer) {
		try {
			writer.close();
		} catch (IOException e) {
		}
	}


	@Override
	public Map<String, String> getParamDesc() {
		Map<String, String> m=new HashMap<String, String>();
		m.put("", "Connection-host:Port");
		m.put("-f", "文件名");
		m.put("-count", "抓取线程次数");
		m.put("-user", "用户名");
		m.put("-password", "密码");
		return m;
	}

	@Override
	public String getName() {
		return "线程dump";
	}

}
