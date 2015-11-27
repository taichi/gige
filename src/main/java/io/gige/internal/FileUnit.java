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
package io.gige.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;

import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import io.gige.Unit;

/**
 * @author taichi
 */
public class FileUnit implements Unit {
	final String className;

	public FileUnit(String className) {
		this.className = className;
	}

	@Override
	public JavaFileObject apply(StandardJavaFileManager t) {
		try {
			JavaFileObject jfo = t.getJavaFileForInput(
					StandardLocation.SOURCE_PATH, this.className, Kind.SOURCE);
			if (jfo == null) {
				throw new FileNotFoundException(this.className);
			}
			return jfo;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
