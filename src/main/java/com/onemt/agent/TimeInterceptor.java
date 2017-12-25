package com.onemt.agent;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.Callable;

import com.onemt.agent.util.ThreadLocalUtils;
import com.onemt.agent.vo.Trace;
import com.onemt.agent.vo.TraceVo;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;



public class TimeInterceptor {
	
	@RuntimeType
	public static Object intercept(@Origin Class<?> clazz,@Origin Method method,@AllArguments Object[] arguments,@This Object that, @SuperCall Callable<?> callable) throws Exception {
		
		/*.append("\ncom.onemt.agent.vo.Trace trace = com.onemt.agent.util.ThreadLocalUtils.get();")
		.append("\ncom.onemt.agent.vo.TraceVo traceVo = new com.onemt.agent.vo.TraceVo();")
		.append("\ntraceVo.setHostIp(trace.getHostIp());")
		.append("\ntraceVo.setTraceId(trace.getTraceId());")
		.append("\ntraceVo.setClassName(\""+className+"\");")
		.append("\ntraceVo.setMethodName(\""+methodName+"\");")
		.append("\ntraceVo.setThreadName(Thread.currentThread().getName());");
		if(length>0){
			bodyStr.append("\njava.util.List inParams = new java.util.ArrayList();");
			for (int i=0;i<length;i++) {
				bodyStr.append("\njava.util.Map inParam"+i+" = new java.util.HashMap();")
				//parameterTypes[i].getName()
				.append("\ninParam"+i+".put(\""+attribute.variableName(i+pos)+"\", $"+(i+1)+");")
				.append("\ninParams.add(inParam"+i+");");
			}
			bodyStr.append("\ntraceVo.setInParams(inParams);");
		}
		bodyStr.append("\ntraceVo.setParentId(trace.getSpanId());")
		.append("\nString spanId = java.util.UUID.randomUUID().toString();")
		.append("\ntraceVo.setSpanId(spanId);")
		.append("\ntrace.setSpanId(spanId);")
		.append("\ntrace.setParentId(traceVo.getSpanId());")
		.append("\ntraceVo.setIsEntry(Boolean.valueOf("+isEntry+"));")
		;
		if (!"void".equals(returnName)) {
				bodyStr.append("\n"+returnName+" retVal = null; ");
			}
		bodyStr.append("\nlong startTime = System.currentTimeMillis();")
		.append("\ntry {\n");
		if (!"void".equals(returnName)) {
			bodyStr.append("retVal = ");
		}
		bodyStr.append(newMethodName + "($$);")
		.append("\n} catch (Throwable e) {")
		.append("\ntraceVo.setErrCode(Integer.valueOf(1));")
		.append("\ntraceVo.setErrorMessage(e);")
		.append("\nthrow e;")
		.append("\n}finally{")
		.append("\nlong endTime = System.currentTimeMillis();")
		.append("\ntraceVo.setCreateTime(Long.valueOf(startTime));")
		.append("\ntraceVo.setReturnTime(Long.valueOf(endTime));")
		.append("\ntraceVo.setCallTime(Long.valueOf(endTime-startTime));");
		if (!"void".equals(returnName)) {
			bodyStr.append("\njava.util.Map returnOjbect = new java.util.HashMap();")
			.append("\nreturnOjbect.put(\""+returnName+"\",retVal);")
			.append("\ntraceVo.setRetVal(returnOjbect);");
		}
		bodyStr.append("\ncom.onemt.agent.log.LogOutput.output(traceVo);")
		.append("\ncom.onemt.agent.util.ThreadLocalUtils.set(trace);")
		.append("\n}");
		
		if (!"void".equals(returnName)) {
			bodyStr.append("\nreturn retVal;");
		}	
		bodyStr.append("\n}");*/
		
		String methodName = method.getName();
		String className = clazz.getName();
		
		Trace trace = ThreadLocalUtils.get();
		TraceVo traceVo = new TraceVo();
		traceVo.setHostIp(trace.getHostIp());
		traceVo.setTraceId(trace.getTraceId());
		traceVo.setClassName(className);
		traceVo.setMethodName(methodName);
		traceVo.setThreadName(Thread.currentThread().getName());
		
		int parameterCount = method.getParameterCount();
		Class<?> returnType = method.getReturnType();
		
		for (Object object : arguments) {
			System.out.println(object);
		}
		
		System.out.println("parameterCount:"+parameterCount+" returnType:"+returnType.getSimpleName());
		
		long start = System.currentTimeMillis();
		try {
			// 原有函数执行
			return callable.call();
		}catch(Throwable e){
			throw e;
		} finally {
			System.out.println("className:"+className+" method:"+method + ": took " + (System.currentTimeMillis() - start) + "ms");
		}
	}

}
