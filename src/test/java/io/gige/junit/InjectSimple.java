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
package io.gige.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.gige.CompilerContext;
import io.gige.Compilers;
import io.gige.Compilers.Type;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author taichi
 */
@RunWith(CompilerRunner.class)
public class InjectSimple {
	@Compilers({ Type.Standard })
	CompilerContext builder;
	static int i = 0;

	@Test
	public void runningTests() throws Exception {
		i++;
		assertNotNull(builder);

	}

	@AfterClass
	public static void afterClass() {
		assertEquals(1, i);
	}
}
