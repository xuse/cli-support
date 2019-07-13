package com.github.xuse.jmxspy.beans;

public class JavaLang {
	public static final String CodeCache = 			"java.lang:name=Code Cache,type=MemoryPool";
	public static final String CodeCacheManager = 	"java.lang:name=CodeCacheManager,type=MemoryManager";
	public static final String CompressedClassSp = "java.lang:name=Compressed Class Space,type=MemoryPool";
	public static final String MetaspaceManager = 	"java.lang:name=Metaspace Manager,type=MemoryManager";
	public static final String Metaspace = 			"java.lang:name=Metaspace,type=MemoryPool";
	public static final String PSEdenSpace =	 		"java.lang:name=PS Eden Space,type=MemoryPool";
	public static final String PSMarkSweep = 		"java.lang:name=PS MarkSweep,type=GarbageCollector";
	public static final String PSOldGen = 			"java.lang:name=PS Old Gen,type=MemoryPool";
	public static final String PSScavenge = 			"java.lang:name=PS Scavenge,type=GarbageCollector";
	public static final String PSSurvivorSpace =	"java.lang:name=PS Survivor Space,type=MemoryPool";
	public static final String ClassLoading = 		"java.lang:type=ClassLoading";
	public static final String Compilation = 		"java.lang:type=Compilation";
	public static final String Memory = 				"java.lang:type=Memory";
	public static final String OperatingSystem = 	"java.lang:type=OperatingSystem";
	public static final String Runtime = 			"java.lang:type=Runtime";
	public static final String Threading = 			"java.lang:type=Threading";

}
