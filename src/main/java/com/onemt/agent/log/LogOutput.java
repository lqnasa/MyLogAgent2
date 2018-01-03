package com.onemt.agent.log;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.onemt.agent.annotation.TraceMethod;

import zipkin2.Span;
import zipkin2.Span.Builder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

public class LogOutput {

	private static final String apiUrl = "http://35.157.199.209:9411/api/v2/spans";
	//private static final String bootstrapServers = "127.0.0.1:9092";
	
	private static final Gson gson=new GsonBuilder().create();
	private static final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(100);
	private static final String regex ="(\")?url(\")?\\s*[:=]\\s*[\"']([^\"']*)[\"']";
	private static final Pattern compile = Pattern.compile(regex);
	
	
	public static void spanOutput(final Builder builder,final Method method,final Object[] arguments,final Object retVal,final Throwable throwable) {
		newFixedThreadPool.execute(()->{
			//AsyncReporter<Span> reporter = AsyncReporter.create(URLConnectionSender.create(apiUrl));
			//KafkaSender kafkaSender = KafkaSender.newBuilder().bootstrapServers(bootstrapServers).topic("zipkin").encoding(Encoding.JSON).build();
			TraceMethod annotation = method.getAnnotation(TraceMethod.class);
			boolean isStart = annotation.isStart();
			if(arguments!=null){
				Parameter[] parameters = method.getParameters();
				for (int i = 0; i < parameters.length; i++) {
					String parameterName = parameters[i].getName();
					Object parameterValue =arguments[i];
					if(parameterValue instanceof Serializable){
						 String parameterVal = parameterValue instanceof String?gson.toJson(new String[]{(String) parameterValue}):gson.toJson(parameterValue);
						builder.putTag("parameterName:"+parameterName, parameterVal);
						if(isStart){
							Matcher matcher = compile.matcher(parameterVal);
							while(matcher.find()){
								builder.putTag("url", gson.toJson(new String[]{matcher.group(3)}));
								break;
							}
						}
					}else{
						builder.putTag("parameterName:"+parameterName, parameterValue!=null?parameterValue.toString():null);
					}
				}
			}
			
			if(retVal !=null){
				if(retVal instanceof Serializable){
					builder.putTag("retVal", retVal instanceof String?gson.toJson(new String[]{(String) retVal}):gson.toJson(retVal));
				}else{
					builder.putTag("retVal",retVal.toString());
				}
			}
			
			if(throwable != null){
				builder.putTag("error", LogOutput.printStackTraceToString(throwable));
			}
			Span span = builder.build();
			
			AsyncReporter<Span> reporter = AsyncReporter.create(URLConnectionSender.create(apiUrl));
			reporter.report(span);
			//AsyncReporter.CONSOLE.report(span);
		});
	}

	public static String printStackTraceToString(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw, true));
		return sw.getBuffer().toString();
	}
	
	public static void main(String[] args) {
		String regex ="(\")?url(\")?\\s*[:=]\\s*[\"']([^\"']*)[\"']";
		String parameterVal="equest=Request{url='http://www.alriyadh.com/1564426', method='null'";
		Pattern compile = Pattern.compile(regex);
		Matcher matcher = compile.matcher(parameterVal);
		while(matcher.find()){
			System.out.println(matcher.group(3));
		}
	}

}
