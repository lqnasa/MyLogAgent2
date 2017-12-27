package com.onemt.agent.log;

import java.io.PrintWriter;
import java.io.StringWriter;

import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

public class LogOutput {

	private static final String apiUrl = "http://localhost:9411/api/v2/spans";
	public static void spanOutput(final Span span) {
		AsyncReporter<Span> reporter = AsyncReporter.create(URLConnectionSender.create(apiUrl));
		reporter.report(span);
		AsyncReporter.CONSOLE.report(span);
	}

	public static String printStackTraceToString(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw, true));
		return sw.getBuffer().toString();
	}

}
