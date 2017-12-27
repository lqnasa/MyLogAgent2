package com.onemt.agent;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.onemt.agent.annotation.TraceMethod;
import com.onemt.agent.log.LogOutput;
import com.onemt.agent.util.InetAddressUtils;
import com.onemt.agent.util.ThreadLocalUtils;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.Span.Builder;


public class TimeInterceptor {

	private static final Gson gson = new GsonBuilder().create();
	
	@RuntimeType
	public static Object intercept(@Origin Class<?> clazz, @Origin Method method, @AllArguments Object[] arguments,
			@This Object that, @SuperCall Callable<?> callable) throws Exception {
		
		String id = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
		
		 Instant startNow = Instant.now();
		long startTime = startNow.toEpochMilli()*1000+startNow.getNano()/1000;
		String methodName = method.getName();
		String className = clazz.getName();
		Class<?> returnType = method.getReturnType();
		TraceMethod annotation = method.getAnnotation(TraceMethod.class);
		boolean isStart = annotation.isStart();
		
		Span span = ThreadLocalUtils.get();
		if(!isStart && span != null){
			String traceId =span.traceId();
			String parentId =span.id();
			span = Span.newBuilder().traceId(traceId).id(id).parentId(parentId).build();
		}else{
			String traceId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);;
			span = Span.newBuilder().traceId(traceId).id(id).build();
		}
		ThreadLocalUtils.set(span);
		Builder builder = span.toBuilder();
		
		Object retVal = null;
		try {
			retVal = callable.call();
		} catch (Throwable e) {
			builder.putTag("error", LogOutput.printStackTraceToString(e));
			throw e;
		} finally {
			Instant endNow = Instant.now();
			long endTime = endNow.toEpochMilli()*1000+endNow.getNano()/1000;
			Endpoint endpoint = Endpoint.newBuilder().ip(InetAddressUtils.getInetAddress()).serviceName("news-crawler").build();

			span = builder.duration(endTime-startTime)
			.localEndpoint(endpoint)
			.addAnnotation(startTime, "start")
			.addAnnotation(endTime, "end")
			.timestamp(startTime)
			.name(methodName)
			.putTag("threadName", Thread.currentThread().getName())
			.putTag("className", className)
			.putTag("methodName", methodName)
			//.putTag("arguments",arguments.length>0?arguments.toString():"")
			//.putTag(returnType.getName(),retVal==null?"":retVal.toString())
			.build();
			
			LogOutput.spanOutput(span);
		}
		
		return retVal;
	}

}
