/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.util.regex.Pattern;

/**
 * Filters synthetic methods unless they represent bodies of lambda expressions.
 */
public final class SyntheticFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if ((methodNode.access & Opcodes.ACC_SYNTHETIC) == 0) {
			return;
		}

		if (methodNode.name.startsWith("lambda$")) {
			return;
		}

		if (methodNode.name.startsWith("$anonfun$")) {
			return;
		}

    if (isHandledByAspectJFilter(methodNode)) {
      return;
    }

		if (KotlinGeneratedFilter.isKotlinClass(context)) {
			if (KotlinDefaultArgumentsFilter
					.isDefaultArgumentsMethod(methodNode)) {
				return;
			}

			if (KotlinDefaultArgumentsFilter
					.isDefaultArgumentsConstructor(methodNode)) {
				return;
			}

			if (KotlinCoroutineFilter.isLastArgumentContinuation(methodNode)) {
				return;
			}
		}

		output.ignore(methodNode.instructions.getFirst(),
				methodNode.instructions.getLast());
	}

	private static final Pattern ASPECTJ_AROUND_BODY_PATTERN = Pattern.compile(".*_aroundBody\\d+");

	/**
	 * Returns true for AspectJ-generated methods which must not be (fully) excluded,
	 * despite having the {@code ACC_SYNTHETIC} flag.
	 *
	 * @see AspectjFilter
	 */
	private boolean isHandledByAspectJFilter(MethodNode methodNode) {
		return ASPECTJ_AROUND_BODY_PATTERN.matcher(methodNode.name).matches()
				&& methodNode.desc.contains("Lorg/aspectj/lang/JoinPoint");
	}

}
