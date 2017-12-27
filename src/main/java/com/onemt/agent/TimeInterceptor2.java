package com.onemt.agent;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.onemt.agent.log.LogOutput;
import com.onemt.agent.util.ThreadLocalUtils;
import com.onemt.agent.util.ThreadLocalUtils2;
import com.onemt.agent.vo.Trace;
import com.onemt.agent.vo.TraceVo;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

public class TimeInterceptor2 {

	@RuntimeType
	public static Object intercept(@Origin Class<?> clazz, @Origin Method method, @AllArguments Object[] arguments,
			@This Object that, @SuperCall Callable<?> callable) throws Exception {

		String methodName = method.getName();
		String className = clazz.getName();

		Trace trace = ThreadLocalUtils2.get();
		TraceVo traceVo = new TraceVo();
		traceVo.setHostIp(trace.getHostIp());
		traceVo.setTraceId(trace.getTraceId());
		traceVo.setClassName(className);
		traceVo.setMethodName(methodName);
		traceVo.setThreadName(Thread.currentThread().getName());
		Class<?> returnType = method.getReturnType();

		traceVo.setParentId(trace.getSpanId());
		String spanId = UUID.randomUUID().toString();
		traceVo.setSpanId(spanId);
		trace.setSpanId(spanId);
		trace.setParentId(traceVo.getSpanId());
		traceVo.setIsEntry(Boolean.valueOf(true));
		traceVo.setInParams(arguments);
		long startTime = System.currentTimeMillis();
		Object retVal = null;
		try {
			// 原有函数执行
			retVal = callable.call();
		} catch (Throwable e) {
			traceVo.setErrCode(Integer.valueOf(1));
			traceVo.setErrorMessage(e);
			throw e;
		} finally {
			long endTime = System.currentTimeMillis();
			traceVo.setCreateTime(Long.valueOf(startTime));
			traceVo.setReturnTime(Long.valueOf(endTime));
			traceVo.setCallTime(Long.valueOf(endTime - startTime));
			Map returnOjbect = new java.util.HashMap();
			returnOjbect.put(returnType.getName(), retVal);
			traceVo.setRetVal(returnOjbect);
			LogOutput.output(traceVo);
			ThreadLocalUtils2.set(trace);
		}
		return retVal;
	}

}
