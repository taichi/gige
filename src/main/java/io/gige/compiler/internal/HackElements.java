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
package io.gige.compiler.internal;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.apt.model.AnnotationMirrorImpl;
import org.eclipse.jdt.internal.compiler.apt.model.ElementsImpl;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * @author taichi
 */
class HackElements extends ElementsImpl {
	public HackElements(BaseProcessingEnvImpl env) {
		super(env);
	}

	// 以下の条件を満たすコードをアノテーションプロセッサで書くとコード上は問題なくてもコンパイルエラーになってしまう事への対処
	// * アノテーションとアノテーションプロセッサが同一のクラスパスにある
	// * 当該アノテーションをコンパイル対象として明示的に指定している。
	// ** -sourcepathによる推移的なコンパイルの場合関係ない。
	// * 当該アノテーションが、default値としてLong.MAX_VALUEのような変数経由のリテラルを参照している。
	// ** 数値リテラルや文字列リテラルなら問題ない。
	// * 当該アノテーションのコンパイルが完了する前に、
	// アノテーションプロセッサがElements#getElementValuesWithDefaultsで値を読み取る。
	// 値を読み取る部分では特に問題なく処理が完了するが、その後のコンパイルにおいてコンパイルエラーになる。
	// この問題はeclipse のGUI上からアノテーションプロセッサを実行すると発生するが、CLI上で実行するなら発生しない。
	// 問題の所在を確認できる最小限のコードは https://gist.github.com/taichi/ce7004a8cf1526b100cf
	@Override
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(
			AnnotationMirror a) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> map = super
				.getElementValuesWithDefaults(a);
		if (a instanceof AnnotationMirrorImpl) {
			AnnotationMirrorImpl impl = (AnnotationMirrorImpl) a;
			ReferenceBinding annoType = impl._binding.getAnnotationType();
			for (MethodBinding method : annoType.methods()) {
				MethodBinding originalMethod = method.original();
				AbstractMethodDeclaration methodDeclaration = originalMethod
						.sourceMethod();
				if (methodDeclaration instanceof AnnotationMethodDeclaration) {
					AnnotationMethodDeclaration amd = (AnnotationMethodDeclaration) methodDeclaration;
					Expression exp = amd.defaultValue;
					if (exp instanceof QualifiedNameReference) {
						QualifiedNameReference qae = (QualifiedNameReference) exp;
						qae.bits |= ASTNode.RestrictiveFlagMASK;
					}
				}
			}
		}
		return map;
	}
}