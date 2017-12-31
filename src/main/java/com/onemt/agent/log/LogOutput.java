package com.onemt.agent.log;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import zipkin2.Span;
import zipkin2.Span.Builder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

public class LogOutput {

	private static final String apiUrl = "http://localhost:9411/api/v2/spans";
	//private static final String bootstrapServers = "127.0.0.1:9092";
	
	private static final Gson gson=new GsonBuilder().create();
	private static final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(100);
	
	
	public static void spanOutput(final Builder builder,final Object[] arguments,final Object retVal,final Throwable throwable) {
		newFixedThreadPool.execute(()->{
			//AsyncReporter<Span> reporter = AsyncReporter.create(URLConnectionSender.create(apiUrl));
			//KafkaSender kafkaSender = KafkaSender.newBuilder().bootstrapServers(bootstrapServers).topic("zipkin").encoding(Encoding.JSON).build();
			List<String> argumentList = new ArrayList<>();
			if(arguments!=null){
				for (Object object : arguments) {
					if(object != null){
						if(object instanceof Serializable){
							argumentList.add(gson.toJson(object));
						}else{
							argumentList.add(object.toString());
						}
					}
				}
				builder.putTag("arguments", gson.toJson(argumentList));
			}
			
			if(retVal !=null){
				builder.putTag("retVal", retVal instanceof Serializable?gson.toJson(retVal):retVal.toString());
			}
			
			if(throwable != null){
				builder.putTag("error", LogOutput.printStackTraceToString(throwable));
			}
			Span span = builder.build();
			
			AsyncReporter<Span> reporter = AsyncReporter.create(URLConnectionSender.create(apiUrl));
			reporter.report(span);
			AsyncReporter.CONSOLE.report(span);
		});
	}

	public static String printStackTraceToString(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw, true));
		return sw.getBuffer().toString();
	}

}
