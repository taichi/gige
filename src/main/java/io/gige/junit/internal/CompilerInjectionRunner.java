/*
 * Copyright 2014 - 2015 SATO taichi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.gige.junit.internal;

import io.gige.Compilers.Type;
import io.gige.CompilerContext;
import io.gige.util.ExceptionalConsumer;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * @author taichi
 */
public class CompilerInjectionRunner extends BlockJUnit4ClassRunner {

	final Type provider;

	public CompilerInjectionRunner(Class<?> klass, Type provider)
			throws InitializationError {
		super(klass);
		this.provider = provider;
	}

	public static Runner safeRunnerForClass(Class<?> clazz, Type type) {
		try {
			return new CompilerInjectionRunner(clazz, type);
		} catch (Throwable e) {
			return new ErrorReportingRunner(clazz, e);
		}
	}

	@Override
	protected String getName() {
		return this.provider.name();
	}

	@Override
	protected String testName(FrameworkMethod method) {
		return method.getName() + " #" + getName();
	}

	@Override
	protected Object createTest() throws Exception {
		Object test = getTestClass().getJavaClass().newInstance();
		handleCompilerFields(f -> {
			for (Type t : f.getAnnotation(io.gige.Compilers.class).value()) {
				if (CompilerContext.class.isAssignableFrom(f.getType())
						&& t.equals(this.provider)) {
					f.set(test, new CompilerContext(t));
				}
			}
		});
		return test;
	}

	protected void handleCompilerFields(ExceptionalConsumer<Field> fn)
			throws Exception {
		List<FrameworkField> fields = getTestClass().getAnnotatedFields(
				io.gige.Compilers.class);
		for (FrameworkField ff : fields) {
			Field f = ff.getField();
			f.setAccessible(true);
			fn.accept(f);
		}
	}

	@Override
	protected Statement withAfters(FrameworkMethod method, Object target,
			Statement statement) {
		Statement stmt = super.withAfters(method, target, statement);
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					stmt.evaluate();
				} finally {
					closeBuilders(target);
				}
			}
		};
	}

	protected void closeBuilders(Object target) throws Exception {
		handleCompilerFields(f -> {
			Object member = f.get(target);
			if (member instanceof CompilerContext) {
				CompilerContext cb = (CompilerContext) member;
				cb.close();
			}
		});
	}

	@Override
	protected Statement classBlock(RunNotifier notifier) {
		return childrenInvoker(notifier);
	}
}