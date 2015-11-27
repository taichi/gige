/*
 * Copyright 2015 SATO taichi
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
import java.util.Arrays;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

/**
 * @author taichi
 */
public class ResourceProxyJavaFileManager
		extends ForwardingJavaFileManager<StandardJavaFileManager> {

	public ResourceProxyJavaFileManager(StandardJavaFileManager fileManager) {
		super(fileManager);
	}

	@Override
	public FileObject getFileForInput(Location location, String packageName,
			String relativeName) throws IOException {
		FileObject fo = findFromInput(location, packageName, relativeName);
		if (fo == null) {
			fo = super.getFileForInput(location, packageName, relativeName);
		}
		return fo;
	}

	protected FileObject findFromInput(Location location, String packageName,
			String relativeName) throws IOException {
		if (location == StandardLocation.CLASS_OUTPUT) {
			for (StandardLocation sl : Arrays.asList(
					StandardLocation.SOURCE_PATH,
					StandardLocation.CLASS_PATH)) {
				try {
					FileObject fo = super.getFileForInput(sl, packageName,
							relativeName);
					if (fo != null) {
						return fo;
					}
				} catch (FileNotFoundException ignore) {
				}
			}
		}
		return null;
	}

	@Override
	public FileObject getFileForOutput(Location location, String packageName,
			String relativeName, FileObject sibling)
					throws IOException {
		FileObject fo = findFromInput(location, packageName, relativeName);
		if (fo == null) {
			fo = super.getFileForOutput(location, packageName, relativeName,
					sibling);
		}
		return fo;
	}
}
