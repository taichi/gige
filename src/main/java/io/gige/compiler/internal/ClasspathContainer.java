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

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.util.Util;

/** @author taichi */
public class ClasspathContainer extends FileSystem {

  protected ClasspathContainer(Classpath[] paths) {
    super(paths, null, true);
  }

  public static FileSystem configure(StandardJavaFileManager standardManager) {
    var platform = Util.collectPlatformLibraries(Util.getJavaHome()).stream();
    var standards =
        Arrays.asList(
                StandardLocation.SOURCE_PATH,
                StandardLocation.SOURCE_OUTPUT,
                StandardLocation.CLASS_PATH)
            .stream()
            .map(loc -> Optional.ofNullable(standardManager.getLocation(loc)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .<File>flatMap(files -> StreamSupport.stream(files.spliterator(), false))
            .map(
                file -> {
                  return FileSystem.getClasspath(file.getAbsolutePath(), null, null);
                })
            .filter(cp -> cp != null);
    Classpath[] paths = Stream.concat(platform, standards).toArray(Classpath[]::new);
    return new ClasspathContainer(paths);
  }
}
