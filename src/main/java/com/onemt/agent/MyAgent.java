package com.onemt.agent;

import java.lang.instrument.Instrumentation;

import com.onemt.agent.annotation.TraceClass;
import com.onemt.agent.annotation.TraceMethod;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
/**
 * 
 * 项目名称：MyLogAgent2
 * 类名称：MyAgent
 * 类描述： https://www.jianshu.com/p/fe1448bf7d31
 * 创建人：Administrator 
 * 创建时间：2017年12月25日 下午5:54:03
 * 修改人：Administrator 
 * 修改时间：2017年12月25日 下午5:54:03
 * 修改备注： 
 * @version
 */
public class MyAgent {

	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("this is an perform monitor agent.");

		AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {

			@Override
			public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
					JavaModule module) {
				System.out.println("=============transform=============");
				return builder.method(ElementMatchers.isAnnotatedWith(TraceMethod.class).and(ElementMatchers.not(ElementMatchers.isStatic())))// 拦截任意方法
						.intercept(MethodDelegation.to(TimeInterceptor.class)); // 委托
			}
			
		};

		AgentBuilder.Listener listener = new AgentBuilder.Listener() {

			@Override
			public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
			}

			@Override
			public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
					boolean loaded, DynamicType dynamicType) {
			}

			@Override
			public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
					boolean loaded) {
			}

			@Override
			public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
					Throwable throwable) {
				System.out.println("=============onError============="+throwable);
			}

			@Override
			public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
			}
			
		};

		new AgentBuilder.Default().type(ElementMatchers.nameStartsWith("com.onemt")
				.and(ElementMatchers.not(ElementMatchers.isInterface()))
				.and(ElementMatchers.not(ElementMatchers.isStatic())))
				.and(ElementMatchers.isAnnotatedWith(TraceClass.class))// 指定需要拦截的类
				.transform(transformer).with(listener).installOn(inst);
	}
}
