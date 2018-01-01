package com.onemt.agent;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.concurrent.Callable;

import com.onemt.agent.annotation.TraceMethod;
import com.onemt.agent.log.LogOutput;
import com.onemt.agent.util.IdWorker;
import com.onemt.agent.util.InetAddressUtils;
import com.onemt.agent.util.ThreadLocalUtils;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.Span.Builder;

public class TimeInterceptor {
	
	private static final IdWorker idWorker=new IdWorker(1,1);
	private static final InetAddress inetAddress = InetAddressUtils.getInetAddress();
	private static final Endpoint endpoint = Endpoint.newBuilder().ip(inetAddress).serviceName("news-crawler")
			.build();
	

	@RuntimeType
	public static Object intercept(@Origin Class<?> clazz, @Origin Method method, @AllArguments Object[] arguments,
			@SuperCall Callable<?> callable) throws Exception {
		String id = idWorker.nextId()+"";
		long startTime = getTimestamp();
		String methodName = method.getName();
		String className = clazz.getName();
		Class<?> returnType = method.getReturnType();
		TraceMethod annotation = method.getAnnotation(TraceMethod.class);
		boolean isStart = annotation.isStart();
		Span span = ThreadLocalUtils.get();
		if (!isStart && span != null) {
			String traceId = span.traceId();
			String parentId = span.id();
			span = Span.newBuilder().traceId(traceId).id(id).parentId(parentId).build();
		} else {
			String traceId = idWorker.nextId()+"";
			span = Span.newBuilder().traceId(traceId).id(id).build();
		}
		ThreadLocalUtils.set(span);
		Builder builder = span.toBuilder();
		
		Throwable throwable = null;
		Object retVal = null;
		try {
			retVal = callable.call();
		} catch (Throwable e) {
			throwable=e;
			throw e;
		} finally {
			long endTime = getTimestamp();
			builder.duration(endTime - startTime).localEndpoint(endpoint).addAnnotation(startTime, "start")
					.addAnnotation(endTime, "end").timestamp(startTime).name(clazz.getSimpleName()+"-->"+methodName)
					.putTag("threadName", Thread.currentThread().getName())
					.putTag("className", className)
					.putTag("methodName", method.toGenericString())
					.putTag("returnType", returnType.getSimpleName());
			LogOutput.spanOutput(builder,method,arguments,retVal,throwable);
		}
		return retVal;
	}

	private static long getTimestamp() {
		Instant now = Instant.now();
		return now.getEpochSecond()*1000000+now.getLong(ChronoField.MICRO_OF_SECOND);
	}

}
