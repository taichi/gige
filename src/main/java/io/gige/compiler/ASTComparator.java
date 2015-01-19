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
package io.gige.compiler;

import java.util.Comparator;

import javax.lang.model.element.Element;

import org.eclipse.jdt.internal.compiler.apt.model.ExecutableElementImpl;
import org.eclipse.jdt.internal.compiler.apt.model.TypeElementImpl;
import org.eclipse.jdt.internal.compiler.apt.model.VariableElementImpl;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.AptSourceLocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

/**
 * @author taichi
 */
public class ASTComparator implements Comparator<Element> {

	@Override
	public int compare(Element left, Element right) {
		return Integer.compare(sourceStart(left), sourceStart(right));
	}

	protected int sourceStart(Element element) {
		switch (element.getKind()) {
		case ANNOTATION_TYPE:
		case INTERFACE:
		case CLASS:
		case ENUM:
			if (element instanceof TypeElementImpl) {
				Binding binding = ((TypeElementImpl) element)._binding;
				if (binding instanceof SourceTypeBinding) {
					ASTNode node = (TypeDeclaration) ((SourceTypeBinding) binding).scope
							.referenceContext();
					return sourceStart(node);
				}
			}
			break;
		case CONSTRUCTOR:
		case METHOD:
			if (element instanceof ExecutableElementImpl) {
				Binding binding = ((ExecutableElementImpl) element)._binding;
				if (binding instanceof MethodBinding) {
					ASTNode node = ((MethodBinding) binding).sourceMethod();
					return sourceStart(node);
				}
			}
			break;
		case FIELD:
		case PARAMETER:
			if (element instanceof VariableElementImpl) {
				Binding binding = ((VariableElementImpl) element)._binding;
				if (binding instanceof FieldBinding) {
					ASTNode node = ((FieldBinding) binding).sourceField();
					return sourceStart(node);
				}
				if (binding instanceof AptSourceLocalVariableBinding) {
					ASTNode node = ((AptSourceLocalVariableBinding) binding).declaration;
					return sourceStart(node);
				}
			}
			break;
		default:
			break;
		}
		return 0;
	}

	protected int sourceStart(ASTNode node) {
		return node == null ? 0 : node.sourceStart();
	}
}
