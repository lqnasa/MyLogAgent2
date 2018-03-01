package com.onemt.agent.util;

import zipkin2.Span;

public class ThreadLocalUtils {
	
	private static ThreadLocal<Span> spanThreadLocal = new InheritableThreadLocal<Span>();
	
	public static Span get(){
		return spanThreadLocal.get();
	}
	
	public static void set(Span span){
		spanThreadLocal.set(span);
	}

}
