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
package io.gige.compiler.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * @author taichi
 */
public class FilerImpl implements Filer {

	protected final JavaFileManager _fileManager;
	protected final BaseProcessingEnvImpl _env;
	protected final HashSet<URI> _createdFiles = new HashSet<>();

	public FilerImpl(JavaFileManager fileManager, BaseProcessingEnvImpl env) {
		this._fileManager = fileManager;
		this._env = env;
	}

	public void addNewUnit(ICompilationUnit unit) {
		_env.addNewUnit(unit);
	}

	public void addNewClassFile(ReferenceBinding binding) {
		_env.addNewClassFile(binding);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.annotation.processing.Filer#createClassFile(java.lang.CharSequence,
	 * javax.lang.model.element.Element[])
	 */
	@Override
	public JavaFileObject createClassFile(CharSequence name,
			Element... originatingElements) throws IOException {
		JavaFileObject jfo = _fileManager.getJavaFileForOutput(
				StandardLocation.CLASS_OUTPUT, name.toString(),
				JavaFileObject.Kind.CLASS, null);
		URI uri = jfo.toUri();
		if (_createdFiles.contains(uri)) {
			throw new FilerException("Class file already created : " + name); //$NON-NLS-1$
		}

		_createdFiles.add(uri);
		return new HookedJavaFileObject(jfo, jfo.getName(), name.toString(),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.annotation.processing.Filer#createResource(javax.tools.
	 * JavaFileManager .Location, java.lang.CharSequence,
	 * java.lang.CharSequence, javax.lang.model.element.Element[])
	 */
	@Override
	public FileObject createResource(Location location,
			CharSequence pkg,
			CharSequence relativeName,
			Element... originatingElements) throws IOException {
		validateName(relativeName);
		FileObject fo = _fileManager.getFileForOutput(location, pkg.toString(),
				relativeName.toString(), null);
		URI uri = fo.toUri();
		if (_createdFiles.contains(uri)) {
			throw new FilerException("Resource already created : " + location //$NON-NLS-1$
					+ '/' + pkg + '/' + relativeName);
		}

		_createdFiles.add(uri);
		return fo;
	}

	private static void validateName(CharSequence relativeName) {
		int length = relativeName.length();
		if (length == 0) {
			throw new IllegalArgumentException("relative path cannot be empty"); //$NON-NLS-1$
		}
		String path = relativeName.toString();
		if (path.indexOf('\\') != -1) {
			// normalize the path with '/'
			path = path.replace('\\', '/');
		}
		if (path.charAt(0) == '/') {
			throw new IllegalArgumentException("relative path is absolute"); //$NON-NLS-1$
		}
		boolean hasDot = false;
		for (int i = 0; i < length; i++) {
			switch (path.charAt(i)) {
			case '/':
				if (hasDot) {
					throw new IllegalArgumentException("relative name " //$NON-NLS-1$
							+ relativeName + " is not relative"); //$NON-NLS-1$
				}
				break;
			case '.':
				hasDot = true;
				break;
			default:
				hasDot = false;
			}
		}
		if (hasDot) {
			throw new IllegalArgumentException(
					"relative name " + relativeName + " is not relative"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.annotation.processing.Filer#createSourceFile(java.lang.CharSequence
	 * , javax.lang.model.element.Element[])
	 */
	@Override
	public JavaFileObject createSourceFile(CharSequence name,
			Element... originatingElements) throws IOException {
		JavaFileObject jfo = _fileManager.getJavaFileForOutput(
				StandardLocation.SOURCE_OUTPUT, name.toString(),
				JavaFileObject.Kind.SOURCE, null);
		URI uri = jfo.toUri();
		if (_createdFiles.contains(uri)) {
			throw new FilerException("Source file already created : " + name); //$NON-NLS-1$
		}

		_createdFiles.add(uri);
		// hook the file object's writers to create compilation unit and add to
		// addedUnits()
		return new HookedJavaFileObject(jfo, jfo.getName(), name.toString(),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.annotation.processing.Filer#getResource(javax.tools.JavaFileManager
	 * .Location, java.lang.CharSequence, java.lang.CharSequence)
	 */
	@Override
	public FileObject getResource(Location location, CharSequence pkg,
			CharSequence relativeName) throws IOException {
		validateName(relativeName);
		FileObject fo;
		if (location.isOutputLocation()) {
			fo = _fileManager.getFileForOutput(location, pkg.toString(),
					relativeName.toString(), null);
		} else {
			fo = _fileManager.getFileForInput(location, pkg.toString(),
					relativeName.toString());
		}

		if (fo == null) {
			throw new FileNotFoundException("Resource does not exist : " //$NON-NLS-1$
					+ location + '/' + pkg + '/' + relativeName);
		}
		URI uri = fo.toUri();
		if (_createdFiles.contains(uri)) {
			throw new FilerException("Resource already created : " + location //$NON-NLS-1$
					+ '/' + pkg + '/' + relativeName);
		}

		_createdFiles.add(uri);
		return fo;
	}
}
