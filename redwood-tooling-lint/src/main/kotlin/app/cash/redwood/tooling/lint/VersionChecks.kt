/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.redwood.tooling.lint

import app.cash.redwood.tooling.lint.VersionChecks.SdkIntAnnotation.Companion.findSdkIntAnnotation
import com.android.sdklib.SdkVersionInfo
import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.client.api.LintClient
import com.android.tools.lint.detector.api.ApiConstraint
import com.android.tools.lint.detector.api.ApiConstraint.Companion.above
import com.android.tools.lint.detector.api.ApiConstraint.Companion.atLeast
import com.android.tools.lint.detector.api.ApiConstraint.Companion.atMost
import com.android.tools.lint.detector.api.ApiConstraint.Companion.below
import com.android.tools.lint.detector.api.ApiConstraint.Companion.range
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.ConstantEvaluator
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Project
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity.INFORMATIONAL
import com.android.tools.lint.detector.api.getMethodName
import com.intellij.codeInsight.AnnotationUtil
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiCompiledElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiParameterList
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiVariable
import com.intellij.psi.util.PsiTreeUtil
import kotlin.math.min
import org.jetbrains.uast.UAnnotated
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UAnonymousClass
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UExpressionList
import org.jetbrains.uast.UFile
import org.jetbrains.uast.UIfExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.ULocalVariable
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UObjectLiteralExpression
import org.jetbrains.uast.UParenthesizedExpression
import org.jetbrains.uast.UPolyadicExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.USwitchClauseExpression
import org.jetbrains.uast.USwitchClauseExpressionWithBody
import org.jetbrains.uast.USwitchExpression
import org.jetbrains.uast.UThrowExpression
import org.jetbrains.uast.UUnaryExpression
import org.jetbrains.uast.UYieldExpression
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.UastFacade
import org.jetbrains.uast.UastPrefixOperator
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.isUastChildOf
import org.jetbrains.uast.skipParenthesizedExprDown
import org.jetbrains.uast.visitor.AbstractUastVisitor

private typealias ApiLevelLookup = (UElement) -> Int

/**
 * Helper for checking whether a given element is surrounded (or
 * preceded!) by an API check using SDK_INT (or other version checking
 * utilities such as BuildCompat#isAtLeastN)
 */
internal class VersionChecks(
    private val client: LintClient,
    private val evaluator: JavaEvaluator,
    private val project: Project?,
    private val lowerBound: Boolean = true
) {
    companion object {
        /**
         * The `SdkIntDetector` analyzes methods and looks for SDK_INT
         * checks inside method bodies. If it recognizes that something
         * is a version check, it will record this as partial analysis
         * data. This mechanism needs an associated issue to tie the
         * data to. We want to peek at this data from the version
         * checking utility (such that if we see "if (someFunction())"
         * is actually checking an SDK_INT result), but we cannot
         * reference that detector's issue from here since it's in a
         * downstream dependency, lint-checks rather than lint-api.
         * So instead, we've created a special marker issue here (the
         * "_" prefix in the issue id is a special prefix recognized
         * by lint as meaning it's not a real issue, also used by
         * various tests), and the sdk int detector will store its
         * data using this issue id instead of its reporting issue.
         */
        val SDK_INT_VERSION_DATA = Issue.create(
            id = "_ChecksSdkIntAtLeast",
            briefDescription = "Version Storage",
            explanation = "_",
            category = Category.LINT,
            severity = INFORMATIONAL,
            androidSpecific = true,
            // Not a real implementation
            implementation = Implementation(Detector::class.java, Scope.EMPTY)
        )

        const val SDK_INT = "SDK_INT"
        const val CHECKS_SDK_INT_AT_LEAST_ANNOTATION = "androidx.annotation.ChecksSdkIntAtLeast"

        const val REQUIRES_DISPLAY_ANNOTATION = "app.cash.redwood.protocol.compose.RequiresDisplay"

        fun getTargetApiAnnotation(
            evaluator: JavaEvaluator,
            scope: UElement?,
            isApiLevelAnnotation: (String) -> Boolean = Companion::isTargetAnnotation
        ): Pair<UAnnotation?, Int> {
            var current = scope
            while (current != null) {
                if (current is UAnnotated) {
                    //noinspection AndroidLintExternalAnnotations
                    for (annotation in current.uAnnotations) {
                        val target = getTargetApiForAnnotation(annotation, isApiLevelAnnotation)
                        if (target != -1) {
                            return annotation to target
                        }
                    }
                }
                if (current is UFile) {
                    // Also consult any package annotations
                    val pkg = evaluator.getPackage(current.javaPsi ?: current.sourcePsi)
                    if (pkg != null) {
                        for (psiAnnotation in pkg.annotations) {
                            val annotation = UastFacade.convertElement(psiAnnotation, null) as? UAnnotation ?: continue
                            val target = getTargetApiForAnnotation(annotation, isApiLevelAnnotation)
                            if (target != -1) {
                                return annotation to target
                            }
                        }
                    }

                    break
                }
                current = current.uastParent
            }

            return NO_ANNOTATION_FOUND
        }

        /**
         * Return value for no annotation found from
         * [getTargetApiAnnotation]
         */
        private val NO_ANNOTATION_FOUND: Pair<UAnnotation?, Int> = null to -1

        fun isTargetAnnotation(fqcn: String): Boolean {
            return isRequiresApiAnnotation(fqcn)
        }

        fun isRequiresApiAnnotation(fqcn: String): Boolean {
            return REQUIRES_DISPLAY_ANNOTATION == fqcn ||
                fqcn == "RequiresDisplay" // With missing imports
        }

        fun getTargetApiForAnnotation(annotation: UAnnotation, isApiLevelAnnotation: (String) -> Boolean): Int {
            val fqcn = annotation.qualifiedName
            if (fqcn != null && isApiLevelAnnotation(fqcn)) {
                val attributeList = annotation.attributeValues
                for (attribute in attributeList) {
                    val expression = attribute.expression
                    if (expression is ULiteralExpression) {
                        val value = expression.value
                        if (value is Int) {
                            return value
                        }
                    } else {
                        val apiLevel = ConstantEvaluator.evaluate(null, expression) as? Int
                        if (apiLevel != null) {
                            return apiLevel
                        }
                    }
                }
            } else if (fqcn == null) {
                // Work around bugs in UAST type resolution for file annotations:
                // parse the source string instead.
                val psi = annotation.sourcePsi ?: return -1
                if (psi is PsiCompiledElement) {
                    return -1
                }
                val text = psi.text
                val start = text.indexOf('(')
                if (start == -1) {
                    return -1
                }
                val colon = text.indexOf(':') // skip over @file: etc
                val annotationString = text.substring(if (colon < start) colon + 1 else 0, start)
                if (isApiLevelAnnotation(annotationString)) {
                    val end = text.indexOf(')', start + 1)
                    if (end != -1) {
                        var name = text.substring(start + 1, end)
                        // Strip off attribute name and qualifiers, e.g.
                        //   @RequiresApi(api = Build.VERSION.O) -> O
                        var index = name.indexOf('=')
                        if (index != -1) {
                            name = name.substring(index + 1).trim()
                        }
                        index = name.indexOf('.')
                        if (index != -1) {
                            name = name.substring(index + 1)
                        }
                        if (name.isNotEmpty() && name[0].isDigit()) {
                            val api = Integer.parseInt(name)
                            if (api > 0) {
                                return api
                            }
                        }
                    }
                }
            }

            return -1
        }

        fun isWithinVersionCheckConditional(
            context: JavaContext,
            element: UElement,
            api: Int,
            lowerBound: Boolean = true
        ): Boolean {
            val client = context.client
            val evaluator = context.evaluator
            val project = context.project
            val check = VersionChecks(client, evaluator, project, lowerBound)
            val constraint = check.getWithinVersionCheckConditional(
                evaluator = evaluator, element = element, apiLookup = null
            ) ?: return false
            return constraint.not().isAtLeast(api)
        }

        fun isPrecededByVersionCheckExit(
            context: JavaContext,
            element: UElement,
            api: Int
        ): Boolean {
            val client = context.client
            val evaluator = context.evaluator
            val project = context.project
            // TODO: Switch to constraints!
            return isPrecededByVersionCheckExit(client, evaluator, element, api, project)
        }

        fun isPrecededByVersionCheckExit(
            client: LintClient,
            evaluator: JavaEvaluator,
            element: UElement,
            api: Int,
            project: Project? = null
        ): Boolean {
            val check = VersionChecks(client, evaluator, project)
            var prev = element
            var current: UExpression? = prev.getParentOfType(
                UExpression::class.java,
                true,
                UMethod::class.java,
                UClass::class.java
            )
            while (current != null) {
                val visitor = check.VersionCheckWithExitFinder(prev, api)
                current.accept(visitor)
                if (visitor.found()) {
                    return true
                }
                prev = current
                current = current.getParentOfType(
                    UExpression::class.java,
                    true,
                    UMethod::class.java,
                    UClass::class.java
                )
                // TODO: what about lambdas?
            }
            return false
        }

        /**
         * Returns the actual API constraint enforced by the given
         * SDK_INT comparison.
         */
        fun getVersionCheckConditional(binary: UBinaryExpression): ApiConstraint? {
            val tokenType = binary.operator
            if (tokenType === UastBinaryOperator.GREATER ||
                tokenType === UastBinaryOperator.GREATER_OR_EQUALS ||
                tokenType === UastBinaryOperator.LESS_OR_EQUALS ||
                tokenType === UastBinaryOperator.LESS ||
                tokenType === UastBinaryOperator.EQUALS ||
                tokenType === UastBinaryOperator.IDENTITY_EQUALS
            ) {
                val left = binary.leftOperand
                val right = binary.rightOperand
                return getVersionCheckBinaryConditional(left, right, tokenType)
                    // reverse order, SDK_INT on the right. Handle it but reverse the constraint
                    ?: getVersionCheckBinaryConditional(right, left, tokenType.flip() ?: return null)
            }
            return null
        }

        // From "X op Y" to "Y op X" -- e.g. "a > b" = "b < a" and "a >= b" = "b <= a"
        private fun UastBinaryOperator.flip(): UastBinaryOperator? {
            return when (this) {
                UastBinaryOperator.GREATER -> UastBinaryOperator.LESS
                UastBinaryOperator.GREATER_OR_EQUALS -> UastBinaryOperator.LESS_OR_EQUALS
                UastBinaryOperator.LESS_OR_EQUALS -> UastBinaryOperator.GREATER_OR_EQUALS
                UastBinaryOperator.LESS -> UastBinaryOperator.GREATER
                else -> null
            }
        }

        private fun getVersionCheckBinaryConditional(
            leftOperand: UExpression,
            rightOperand: UExpression,
            tokenType: UastBinaryOperator
        ): ApiConstraint? {
            val left = leftOperand.skipParenthesizedExprDown() ?: return null
            if (left is UReferenceExpression) {
                if (SDK_INT == left.resolvedName) {
                    val right = rightOperand.skipParenthesizedExprDown() ?: return null
                    var level = -1
                    if (right is UReferenceExpression) {
                        val codeName = right.resolvedName ?: return null
                        level = SdkVersionInfo.getApiByBuildCode(codeName, true)
                    } else if (right is ULiteralExpression) {
                        val value = right.value
                        if (value is Int) {
                            level = value
                        }
                    }
                    if (level != -1) {
                        if (tokenType === UastBinaryOperator.GREATER_OR_EQUALS) {
                            // SDK_INT >= ICE_CREAM_SANDWICH
                            return atLeast(level)
                        } else if (tokenType === UastBinaryOperator.GREATER) {
                            // SDK_INT > ICE_CREAM_SANDWICH
                            return above(level)
                        } else if (tokenType === UastBinaryOperator.LESS_OR_EQUALS) {
                            return atMost(level)
                        } else if (tokenType === UastBinaryOperator.LESS) {
                            // SDK_INT < ICE_CREAM_SANDWICH
                            return below(level)
                        } else if (tokenType === UastBinaryOperator.EQUALS ||
                            tokenType === UastBinaryOperator.IDENTITY_EQUALS
                        ) {
                            return range(level, level + 1)
                        }
                    }
                }
            }
            return null
        }
    }

    private fun isUnconditionalReturn(statement: UExpression): Boolean {

        @Suppress("UnstableApiUsage") // UYieldExpression not yet stable
        if (statement is UBlockExpression) {
            statement.expressions.lastOrNull()?.let { return isUnconditionalReturn(it) }
        } else if (statement is UExpressionList) {
            statement.expressions.lastOrNull()?.let { return isUnconditionalReturn(it) }
        } else if (statement is UYieldExpression) {
            // (Kotlin when statements will sometimes be represented using yields in the UAST representation)
            val yieldExpression = statement.expression
            if (yieldExpression != null) {
                return isUnconditionalReturn(yieldExpression)
            }
        } else if (statement is UParenthesizedExpression) {
            return isUnconditionalReturn(statement.expression)
        }

        if (statement is UReturnExpression || statement is UThrowExpression) {
            return true
        } else if (statement is UCallExpression) {
            val methodName = getMethodName(statement)
            // Look for Kotlin runtime library methods that unconditionally exit
            if ("error" == methodName || "TODO" == methodName) {
                return true
            }
        }

        return false
    }

    private fun getWithinVersionCheckConditional(
        evaluator: JavaEvaluator,
        element: UElement,
        apiLookup: ApiLevelLookup?
    ): ApiConstraint? {
        var current = element.uastParent
        var prev = element
        while (current != null) {

            if (current is UPolyadicExpression) {
                val anded = getAndedWithConditional(current, prev)
                if (anded != null) {
                    return anded
                }
                val ored = getOredWithConditional(current, prev)
                if (ored != null) {
                    return ored
                }
            }

            if (current is UIfExpression) {
                val ifStatement = current
                val condition = ifStatement.condition
                if (prev !== condition) {
                    val fromThen = prev == ifStatement.thenExpression
                    getVersionCheckConditional(
                        element = condition,
                        and = fromThen,
                        prev = prev,
                        apiLookup = apiLookup
                    )?.let { return it }
                }
            } else if (current is USwitchClauseExpressionWithBody) {
                for (condition in current.caseValues) {
                    getVersionCheckConditional(
                        element = condition,
                        and = true,
                        prev = prev,
                        apiLookup = apiLookup
                    )?.let { return it }
                }

                val switch = current.getParentOfType(USwitchExpression::class.java, true)
                val entries = switch?.body?.expressions?.filterIsInstance<USwitchClauseExpression>() ?: emptyList()
                val switchExpression = switch?.expression
                val casesAreApiLevels = switchExpression != null && isSdkInt(switchExpression)
                if (casesAreApiLevels && lowerBound) {
                    if (current.caseValues.isNotEmpty()) {
                        // It's for a specific set of API values listed by the case values.
                        // Take the min of these values and now we can allow methods for that API level and up
                        val apiLevel = current.caseValues.fold(-1) { min, expression ->
                            val apiLevel = getApiLevel(expression, apiLookup)
                            if (min == -1) apiLevel else min(min, apiLevel)
                        }
                        if (apiLevel != -1) {
                            return atMost(apiLevel)
                        }
                    } else {
                        val apiLevels = mutableSetOf<Int>()
                        // it's the else clause: subtract out all the other API levels in the case
                        // statements
                        for (entry in entries) {
                            for (case in entry.caseValues) {
                                apiLevels.add(getApiLevel(case, apiLookup))
                            }
                        }
                        val (_, target) = getTargetApiAnnotation(evaluator, switch)
                        val min = kotlin.math.max(target, project?.minSdk ?: -1)
                        var firstMissing = min + 1
                        while (true) {
                            if (!apiLevels.contains(firstMissing)) {
                                break
                            }
                            firstMissing++
                        }
                        return atMost(firstMissing - 1)
                    }
                } else {
                    var prevConstraint: ApiConstraint? = null
                    for (entry in entries) {
                        if (entry === current) {
                            break
                        }

                        for (case in entry.caseValues) {
                            val constraint = getVersionCheckConditional(
                                element = case,
                                and = true,
                                apiLookup = apiLookup
                            ) ?: getVersionCheckConditional(
                                element = case,
                                and = false,
                                apiLookup = apiLookup
                            )

                            prevConstraint = if (prevConstraint == null) constraint else prevConstraint and constraint
                        }
                    }

                    if (prevConstraint != null) {
                        return prevConstraint.adjust(0, -2).not()
                    }
                }
            } else if (current is UCallExpression &&
                (prev as? UExpression)?.skipParenthesizedExprDown() is ULambdaExpression
            ) {
                // If the API violation is in a lambda that is passed to a method,
                // see if the lambda parameter is invoked inside that method, wrapped within
                // a suitable version conditional.
                //
                // Optionally also see if we're passing in the API level as a parameter
                // to the function.
                //
                // Algorithm:
                //  (1) Figure out which parameter we're mapping the lambda argument to.
                //  (2) Find that parameter invoked within the function
                //  (3) From the invocation see if it's a suitable version conditional
                //
                val call = current
                val method = call.resolve()
                if (method != null) {
                    val annotation =
                      app.cash.redwood.tooling.lint.VersionChecks.SdkIntAnnotation.get(method)
                    if (annotation != null) {
                        val value = annotation.getApiLevel(evaluator, method, call)
                        if (value != null) {
                            return atMost(value)
                        } // else: lambda
                    }

                    val mapping = evaluator.computeArgumentMapping(call, method)
                    val parameter = mapping[prev]
                    if (parameter != null) {
                        val lambdaInvocation = getLambdaInvocation(parameter, method)
                        if (lambdaInvocation != null) {
                            val constraint = getWithinVersionCheckConditional(
                                evaluator = evaluator,
                                element = lambdaInvocation,
                                apiLookup = getReferenceApiLookup(call)
                            )
                            if (constraint != null) {
                                return constraint
                            }
                        }
                    }
                }
            } else if (current is UCallExpression &&
                (prev as? UExpression)?.skipParenthesizedExprDown() is UObjectLiteralExpression
            ) {
                val method = current.resolve()
                if (method != null) {
                    val annotation =
                      app.cash.redwood.tooling.lint.VersionChecks.SdkIntAnnotation.get(method)
                    if (annotation != null) {
                        val value = annotation.getApiLevel(evaluator, method, current)
                        if (value != null) {
                            return atMost(value)
                        } // else: lambda
                    }

                    val mapping = evaluator.computeArgumentMapping(current, method)
                    val parameter = mapping[prev]
                    if (parameter != null) {
                        val lambdaInvocation = getLambdaInvocation(parameter, method)
                        if (lambdaInvocation != null) {
                            val constraint = getWithinVersionCheckConditional(
                                evaluator = evaluator,
                                element = lambdaInvocation,
                                apiLookup = getReferenceApiLookup(current)
                            )
                            if (constraint != null) {
                                return constraint
                            }
                        }
                    }
                }
            } else if (current is UMethod) {
                if (current.uastParent !is UAnonymousClass) return null
            } else if (current is PsiFile) {
                return null
            }
            prev = current
            current = current.uastParent
        }
        return null
    }

    private fun getReferenceApiLookup(call: UCallExpression): (UElement) -> Int {
        return { reference ->
            var apiLevel = -1
            if (reference is UReferenceExpression) {
                val resolved = reference.resolve()
                if (resolved is PsiParameter) {
                    val parameterList =
                        PsiTreeUtil.getParentOfType(
                            resolved, PsiParameterList::class.java
                        )
                    if (parameterList != null) {
                        call.resolve()?.let { method ->
                            val mapping = evaluator.computeArgumentMapping(call, method)
                            for ((argument, parameter) in mapping) {
                                if (parameter == resolved) {
                                    apiLevel = getApiLevel(argument, null)
                                    break
                                }
                            }
                        } ?: run {
                            val index = parameterList.getParameterIndex(resolved)
                            val arguments = call.valueArguments
                            if (index != -1 && index < arguments.size) {
                                apiLevel = getApiLevel(arguments[index], null)
                            }
                        }
                    }
                }
            }
            apiLevel
        }
    }

    private fun getLambdaInvocation(
        parameter: PsiParameter,
        method: PsiMethod
    ): UCallExpression? {
        if (method is PsiCompiledElement) {
            return null
        }
        val uMethod = UastFacade.convertElementWithParent(
            method,
            UMethod::class.java
        ) as UMethod? ?: return null

        val match = Ref<UCallExpression>()
        val parameterName = parameter.name
        uMethod.accept(
            object : AbstractUastVisitor() {
                override fun visitCallExpression(
                    node: UCallExpression
                ): Boolean {
                    val receiver = node.receiver?.skipParenthesizedExprDown()
                    if (receiver is USimpleNameReferenceExpression) {
                        val name = receiver.identifier
                        if (name == parameterName) {
                            match.set(node)
                        }
                    } else if (receiver is UReferenceExpression) {
                        val name = receiver.resolvedName
                        if (name == parameterName) {
                            match.set(node)
                        }
                    }

                    val callName = getMethodName(node)
                    if (callName == parameterName) {
                        // Potentially not correct due to scopes, but these lambda
                        // utility methods tend to be short and for lambda function
                        // calls, resolve on call returns null
                        match.set(node)
                    }

                    return super.visitCallExpression(node)
                }
            })

        return match.get()
    }

    private fun getVersionCheckConditional(
        element: UElement,
        and: Boolean,
        prev: UElement? = null,
        apiLookup: ApiLevelLookup? = null
    ): ApiConstraint? {
        if (element is UPolyadicExpression) {
            if (element is UBinaryExpression) {
                getVersionCheckConditional(
                    fromThen = and,
                    binary = element,
                    apiLevelLookup = apiLookup
                )?.let { return it }
            }
            val tokenType = element.operator
            if (and && tokenType === UastBinaryOperator.LOGICAL_AND) {
                val constraint = getAndedWithConditional(element, prev)
                if (constraint != null) {
                    return constraint
                }
            } else if (!and && tokenType === UastBinaryOperator.LOGICAL_OR) {
                val constraint = getOredWithConditional(element, prev)
                if (constraint != null) {
                    return constraint
                }
            }
        } else if (element is UCallExpression) {
            return getValidVersionCall(and, element)
        } else if (element is UReferenceExpression) {
            // Constant expression for an SDK version check?
            val resolved = element.resolve()
            if (resolved is PsiField) {
                @Suppress("UnnecessaryVariable")
                val field = resolved

                val validFromInferredAnnotation = getValidFromInferredAnnotation(field)
                if (validFromInferredAnnotation != null) {
                    return validFromInferredAnnotation
                }
                val modifierList = field.modifierList
                if (modifierList != null && modifierList.hasExplicitModifier(PsiModifier.STATIC)) {
                    val initializer = UastFacade.getInitializerBody(field)?.skipParenthesizedExprDown()
                    if (initializer != null) {
                        val constraint = getVersionCheckConditional(
                            element = initializer,
                            and = and
                        )
                        if (constraint != null) {
                            return constraint
                        }
                    }
                }
            } else if (resolved is PsiMethod &&
                element is UQualifiedReferenceExpression &&
                element.selector is UCallExpression
            ) {
                val call = element.selector as UCallExpression
                return getValidVersionCall(and, call)
            } else if (resolved is PsiMethod) {
                // Method call via Kotlin property syntax
                return getValidVersionCall(
                    and = and,
                    call = element,
                    method = resolved
                )
            } else if (resolved == null && element is UQualifiedReferenceExpression) {
                val selector = element.selector
                if (selector is UCallExpression) {
                    return getValidVersionCall(
                        and = and,
                        call = selector
                    )
                }
            }
        } else if (element is UUnaryExpression) {
            if (element.operator === UastPrefixOperator.LOGICAL_NOT) {
                val operand = element.operand
                getVersionCheckConditional(element = operand, and = !and)?.let { return it }
            }
        } else if (element is UParenthesizedExpression) {
            return getVersionCheckConditional(element.expression, and, element, apiLookup)
        }
        return null
    }

    private fun getValidFromAnnotation(
        owner: PsiModifierListOwner,
        call: UCallExpression? = null
    ): ApiConstraint? {
        return null
    }

    /**
     * When we come across SDK_INT comparisons in library, we'll store
     * that as an implied @ChecksSdkIntAtLeast annotation (to match the
     * existing support for actual @ChecksSdkIntAtLeast annotations).
     * Here, when looking up version checks we'll check the given method
     * or field and see if we've stashed any implied version checks when
     * analyzing the dependencies.
     */
    private fun getValidFromInferredAnnotation(
        owner: PsiModifierListOwner,
        call: UCallExpression? = null
    ): ApiConstraint? {
        if (!client.supportsPartialAnalysis()) {
            return null
        }
        if (project == null || owner !is PsiCompiledElement) {
            return null
        }
        val annotation = when (owner) {
            is PsiMethod -> findSdkIntAnnotation(client, evaluator, project, owner) ?: return null
            is PsiField -> findSdkIntAnnotation(client, evaluator, project, owner) ?: return null
            else -> return null
        }
        val value = annotation.getApiLevel(evaluator, owner, call) ?: return null
        return atMost(value)
    }

    private fun getValidVersionCall(
        and: Boolean,
        call: UCallExpression
    ): ApiConstraint? {
        val method = call.resolve() ?: return null
        return getValidVersionCall(and, call, method)
    }

    private fun getValidVersionCall(
        and: Boolean,
        call: UElement,
        method: PsiMethod
    ): ApiConstraint? {
        val callExpression = call as? UCallExpression

        val validFromInferredAnnotation = getValidFromInferredAnnotation(method, callExpression)
        if (validFromInferredAnnotation != null) {
            return validFromInferredAnnotation
        }

        // Unconditional version utility method? If so just attempt to call it
        if (!method.hasModifierProperty(PsiModifier.ABSTRACT)) {
            val body = UastFacade.getMethodBody(method) ?: return null
            val expressions: List<UExpression> = if (body is UBlockExpression) {
                body.expressions
            } else {
                listOf(body)
            }
            if (expressions.size == 1) {
                val statement = expressions[0].skipParenthesizedExprDown()
                val returnValue = if (statement is UReturnExpression) {
                    statement.returnExpression?.skipParenthesizedExprDown()
                } else {
                    // Kotlin: may not have an explicit return statement
                    statement
                } ?: return null
                val arguments = if (call is UCallExpression) call.valueArguments else emptyList()
                if (arguments.isEmpty()) {
                    if (returnValue is UPolyadicExpression ||
                        returnValue is UCallExpression ||
                        returnValue is UQualifiedReferenceExpression
                    ) {
                        val constraint =
                            getVersionCheckConditional(element = returnValue, and = and)
                        if (constraint != null) {
                            return constraint
                        }
                    }
                } else if (arguments.size == 1) {
                    // See if we're passing in a value to the version utility method
                    val constraint = getVersionCheckConditional(
                        element = returnValue,
                        and = and,
                        apiLookup = { reference ->
                            var apiLevel = -1
                            if (reference is UReferenceExpression) {
                                val resolved = reference.resolve()
                                if (resolved is PsiParameter) {
                                    val parameterList =
                                        PsiTreeUtil.getParentOfType(
                                            resolved, PsiParameterList::class.java
                                        )
                                    if (parameterList != null) {
                                        val index = parameterList.getParameterIndex(resolved)
                                        if (index != -1 && index < arguments.size) {
                                            apiLevel = getApiLevel(arguments[index], null)
                                        }
                                    }
                                }
                            }
                            apiLevel
                        }
                    )
                    if (constraint != null) {
                        return constraint
                    }
                }
            }
        }

        return null
    }

    private fun isSdkInt(element: PsiElement): Boolean {
        if (element is PsiReferenceExpression) {
            if (SDK_INT == element.referenceName) {
                return true
            }
            val resolved = element.resolve()
            if (resolved is PsiVariable) {
                val initializer = resolved.initializer
                if (initializer != null) {
                    return isSdkInt(initializer)
                }
            }
        }
        return false
    }

    private fun isSdkInt(element: UElement): Boolean {
        if (element is UReferenceExpression) {
            if (SDK_INT == element.resolvedName) {
                return true
            }
            val resolved = element.resolve()
            if (resolved is ULocalVariable) {
                val initializer = resolved.uastInitializer
                if (initializer != null) {
                    return isSdkInt(initializer)
                }
            } else if (resolved is PsiVariable) {
                val initializer = resolved.initializer
                if (initializer != null) {
                    return isSdkInt(initializer)
                }
            }
        } else if (element is UParenthesizedExpression) {
            return isSdkInt(element.expression)
        }
        return false
    }

    /**
     * For a given SDK_INT check, this method returns the API
     * constraints for **safe** API level usages within the then-body of
     * this if-check. If [fromThen] is false, the code is in the else
     * clause instead.
     *
     * For example, this code:
     *
     *     if (SDK_INT >= 28) {
     *         requiresN()
     *
     * is safe for values of N from 1 up through 28, so it will return
     *
     * an API constraint of API <= 28, whereas the
     * [getVersionCheckConditional] method returns API >= 28.
     */
    private fun getVersionCheckConditional(
        binary: UBinaryExpression,
        fromThen: Boolean,
        apiLevelLookup: ApiLevelLookup? = null
    ): ApiConstraint? {
        @Suppress("NAME_SHADOWING")
        var fromThen = fromThen
        val tokenType = binary.operator
        if (tokenType === UastBinaryOperator.GREATER ||
            tokenType === UastBinaryOperator.GREATER_OR_EQUALS ||
            tokenType === UastBinaryOperator.LESS_OR_EQUALS ||
            tokenType === UastBinaryOperator.LESS ||
            tokenType === UastBinaryOperator.EQUALS ||
            tokenType === UastBinaryOperator.IDENTITY_EQUALS ||
            tokenType === UastBinaryOperator.NOT_EQUALS ||
            tokenType === UastBinaryOperator.IDENTITY_NOT_EQUALS
        ) {
            val left = binary.leftOperand
            val level: Int
            val right: UExpression
            if (!isSdkInt(left)) {
                right = binary.rightOperand
                if (isSdkInt(right)) {
                    fromThen = !fromThen
                    level = getApiLevel(left, apiLevelLookup)
                } else {
                    return null
                }
            } else {
                right = binary.rightOperand
                level = getApiLevel(right, apiLevelLookup)
            }

            if (level != -1) {
                if (tokenType === UastBinaryOperator.GREATER_OR_EQUALS) {
                    return if (lowerBound) {
                        // Here we we know that we're on ice cream sandwich or later.
                        // That means it's safe to use call from all older versions
                        // up to and including ice cream sandwich itself.
                        if (fromThen) atMost(level) else null
                    } else {
                        // if (SDK_INT >= ICE_CREAM_SANDWICH) {  } else { <here> }
                        // so in <here>, SDK_INT < ICE_CREAM_SANDWICH
                        if (!fromThen) atLeast(level - 2) else null
                    }
                } else if (tokenType === UastBinaryOperator.GREATER) {
                    return if (lowerBound) {
                        if (fromThen) atMost(level + 1) else null
                    } else {
                        if (!fromThen) atLeast(level - 1) else null
                    }
                } else if (tokenType === UastBinaryOperator.LESS_OR_EQUALS) {
                    return if (lowerBound) {
                        if (!fromThen) atMost(level + 1) else null
                    } else {
                        if (fromThen) atLeast(level - 2) else null
                    }
                } else if (tokenType === UastBinaryOperator.LESS) {
                    return if (lowerBound) {
                        if (!fromThen) atMost(level) else null
                    } else {
                        if (fromThen) atLeast(level - 3) else null
                    }
                } else if (tokenType === UastBinaryOperator.EQUALS ||
                    tokenType === UastBinaryOperator.IDENTITY_EQUALS
                ) {
                    // if (SDK_INT == ICE_CREAM_SANDWICH) { <call> } else { ... }
                    return if (lowerBound) {
                        if (fromThen) atMost(level) else null
                    } else {
                        if (fromThen) atLeast(level) else null
                    }
                } else if (tokenType === UastBinaryOperator.NOT_EQUALS ||
                    tokenType === UastBinaryOperator.IDENTITY_NOT_EQUALS
                ) {
                    // if (SDK_INT != ICE_CREAM_SANDWICH) { ... } else { <call> }
                    return if (!fromThen) range(level, level + 1) else null
                } else {
                    assert(false) { tokenType }
                }
            }
        }

        return null
    }

    private fun getApiLevel(
        element: UExpression?,
        apiLevelLookup: ApiLevelLookup?
    ): Int {
        var level = -1
        if (element is UReferenceExpression) {
            val codeName = element.resolvedName
            if (codeName != null) {
                level = SdkVersionInfo.getApiByBuildCode(codeName, false)
            }
            if (level == -1) {
                val constant = ConstantEvaluator.evaluate(null, element)
                if (constant is Number) {
                    level = constant.toInt()
                }
            }
        } else if (element is ULiteralExpression) {
            val value = element.value
            if (value is Int) {
                level = value
            }
        } else if (element is UParenthesizedExpression) {
            return getApiLevel(element.expression, apiLevelLookup)
        }
        if (level == -1 && apiLevelLookup != null && element != null) {
            level = apiLevelLookup(element)
        }
        return level
    }

    @Suppress("SpellCheckingInspection")
    private fun getOredWithConditional(
        element: UElement,
        before: UElement?
    ): ApiConstraint? {
        if (element is UBinaryExpression) {
            if (element.operator === UastBinaryOperator.LOGICAL_OR) {
                val left = element.leftOperand
                if (before !== left) {
                    getVersionCheckConditional(element = left, and = false)?.let { return it }
                    val right = element.rightOperand
                    getVersionCheckConditional(element = right, and = false)?.let { return it }
                }
            }
            getVersionCheckConditional(fromThen = false, binary = element)?.let { return it }
        } else if (element is UPolyadicExpression) {
            if (element.operator === UastBinaryOperator.LOGICAL_OR) {
                for (operand in element.operands) {
                    if (operand == before) {
                        break
                    } else {
                        val constraint = getOredWithConditional(
                            element = operand,
                            before = before
                        )
                        if (constraint != null) {
                            return constraint
                        }
                    }
                }
            }
        } else if (element is UParenthesizedExpression) {
            return getOredWithConditional(element.expression, element)
        }
        return null
    }

    @Suppress("SpellCheckingInspection")
    private fun getAndedWithConditional(
        element: UElement,
        before: UElement?
    ): ApiConstraint? {
        if (element is UBinaryExpression) {
            if (element.operator === UastBinaryOperator.LOGICAL_AND) {
                val left = element.leftOperand
                if (before !== left) {
                    getVersionCheckConditional(element = left, and = true)?.let { return it }
                    val right = element.rightOperand
                    getVersionCheckConditional(element = right, and = true)?.let { return it }
                }
            }
            getVersionCheckConditional(fromThen = true, binary = element)?.let { return it }
        } else if (element is UPolyadicExpression) {
            if (element.operator === UastBinaryOperator.LOGICAL_AND) {
                for (operand in element.operands) {
                    if (operand == before) {
                        break
                    } else {
                        val constraint = getVersionCheckConditional(operand, and = true)
                        if (constraint != null) {
                            return constraint
                        }
                    }
                }
            }
        } else if (element is UParenthesizedExpression) {
            return getAndedWithConditional(element.expression, element)
        }
        return null
    }

    private inner class VersionCheckWithExitFinder(
        private val endElement: UElement,
        private val api: Int,
    ) : AbstractUastVisitor() {
        private var found = false
        private var done = false
        override fun visitElement(node: UElement): Boolean {
            if (done) {
                return true
            }
            if (node === endElement) {
                done = true
            }
            return done
        }

        override fun visitIfExpression(node: UIfExpression): Boolean {
            val exit = super.visitIfExpression(node)
            if (done) {
                return true
            }
            if (endElement.isUastChildOf(node, true)) {
                // Even if there is an unconditional exit, endElement will occur before it!
                done = true
                return true
            }
            val thenBranch = node.thenExpression
            val elseBranch = node.elseExpression
            if (thenBranch != null) {
                val constraint = getVersionCheckConditional(
                    element = node.condition,
                    and = false
                )
                if (constraint?.isAtLeast(api) == true) {
                    // See if the body does an immediate return
                    if (isUnconditionalReturn(thenBranch)) {
                        found = true
                        done = true
                    }
                }
            }
            if (elseBranch != null) {
                val constraint = getVersionCheckConditional(
                    element = node.condition,
                    and = true
                )
                if (constraint?.isAtLeast(api) == true) {
                    if (isUnconditionalReturn(elseBranch)) {
                        found = true
                        done = true
                    }
                }
            }
            return exit
        }

        override fun visitSwitchExpression(node: USwitchExpression): Boolean {
            val exit = super.visitSwitchExpression(node)
            if (done) {
                return true
            }
            if (endElement.isUastChildOf(node, true)) {
                // Even if there is an unconditional exit, endElement will occur before it!
                done = true
                return true
            }

            if (node.expression == null) {
                var knownConstraint: ApiConstraint? = null
                for (entry in node.body.expressions) {
                    if (entry is USwitchClauseExpression) {
                        if (entry is USwitchClauseExpressionWithBody) {
                            for (case in entry.caseValues) {
                                val constraint = getVersionCheckConditional(
                                    element = case,
                                    and = false
                                )
                                if (constraint != null) {
                                    if (constraint.isAtLeast(api)) {
                                        // See if the body does an immediate return
                                        if (isUnconditionalReturn(entry.body)) {
                                            found = true
                                            done = true
                                        }
                                    }
                                }
                            }
                        }

                        for (case in entry.caseValues) {
                            val constraint = getVersionCheckConditional(
                                element = case,
                                and = true
                            )
                            if (constraint != null) {
                                knownConstraint = constraint and knownConstraint
                            }
                        }

                        if (knownConstraint != null &&
                            entry is USwitchClauseExpressionWithBody &&
                            isUnconditionalReturn(entry.body)
                        ) {
                            if (knownConstraint.neverAtMost(api + 2)) {
                                // We've had earlier clauses which checked the API level.
                                // No, this isn't right; we need to lower the level
                                found = true
                                done = true
                            }
                        }
                    }
                }
            }

            return exit
        }

        fun found(): Boolean {
            return found
        }
    }

    /**
     * Unpacked version of `@androidx.annotation.ChecksSdkIntAtLeast`
     */
    class SdkIntAnnotation(
        val api: Int?,
        val codename: String?,
        val parameter: Int?,
        val lambda: Int?
    ) {
        constructor(annotation: PsiAnnotation) : this(
            annotation.getAnnotationIntValue("api"),
            annotation.getAnnotationStringValue("codename"),
            annotation.getAnnotationIntValue("parameter"),
            annotation.getAnnotationIntValue("lambda")
        )

        /**
         * Returns the API level for this annotation in the given
         * context.
         */
        fun getApiLevel(
            evaluator: JavaEvaluator,
            owner: PsiModifierListOwner,
            call: UCallExpression?
        ): Int? {
            val apiLevel = apiLevel()
            if (apiLevel != null) {
                return apiLevel
            }

            val index = parameter ?: lambda ?: return null
            if (owner is PsiMethod && call != null) {
                val argument = findArgumentFor(evaluator, owner, index, call)
                if (argument != null) {
                    val v = ConstantEvaluator.evaluate(null, argument)
                    return (v as? Number)?.toInt()
                }
            }

            return null
        }

        private fun apiLevel(): Int? {
            return if (codename != null && codename.isNotEmpty()) {
                val level = SdkVersionInfo.getApiByBuildCode(codename, false)
                if (level != -1) {
                    level
                } else {
                    SdkVersionInfo.getApiByPreviewName(codename, true)
                }
            } else if (api != -1) {
                api
            } else {
                null
            }
        }

        private fun findArgumentFor(
            evaluator: JavaEvaluator,
            calledMethod: PsiMethod,
            parameterIndex: Int,
            call: UCallExpression
        ): UExpression? {
            val parameters = calledMethod.parameterList.parameters
            if (parameterIndex >= 0 && parameterIndex < parameters.size) {
                val target = parameters[parameterIndex]
                val mapping = evaluator.computeArgumentMapping(call, calledMethod)
                for ((key, value1) in mapping) {
                    if (value1 === target || value1.isEquivalentTo(target)) {
                        return key
                    }
                }
            }

            return null
        }

        companion object {
            /**
             * Looks up the @ChecksSdkIntAtLeast annotation for the
             * given method or field.
             */
            fun get(owner: PsiModifierListOwner): SdkIntAnnotation? {
                val annotation = AnnotationUtil.findAnnotation(
                    owner, true, CHECKS_SDK_INT_AT_LEAST_ANNOTATION
                ) ?: return null
                return SdkIntAnnotation(annotation)
            }

            private fun getMethodKey(
                evaluator: JavaEvaluator,
                method: PsiMethod
            ): String {
                val desc = evaluator.getMethodDescription(
                    method,
                    includeName = false,
                    includeReturn = false
                )
                val cls = method.containingClass?.let { evaluator.getQualifiedName(it) }
                return "$cls#${method.name}$desc"
            }

            private fun getFieldKey(
                evaluator: JavaEvaluator,
                field: PsiField
            ): String {
                val cls = field.containingClass?.let { evaluator.getQualifiedName(it) }
                return "$cls#${field.name}"
            }

            fun findSdkIntAnnotation(
                client: LintClient,
                evaluator: JavaEvaluator,
                project: Project,
                owner: PsiModifierListOwner
            ): SdkIntAnnotation? {
                val key = when (owner) {
                    is PsiMethod -> getMethodKey(evaluator, owner)
                    is PsiField -> getFieldKey(evaluator, owner)
                    else -> return null
                }
                val map = client.getPartialResults(project, SDK_INT_VERSION_DATA).map()
                val args = map[key] ?: return null
                val api = findAttribute(args, "api")?.toIntOrNull()
                val codename = findAttribute(args, "codename")
                val parameter = findAttribute(args, "parameter")?.toIntOrNull()
                val lambda = findAttribute(args, "lambda")?.toIntOrNull()
                return SdkIntAnnotation(api, codename, parameter, lambda)
            }

            private fun findAttribute(args: String, name: String): String? {
                val key = "$name="
                val index = args.indexOf(key)
                if (index == -1) {
                    return null
                }
                val start = index + key.length
                val end = args.indexOf(',', start).let { if (it == -1) args.length else it }
                return args.substring(start, end)
            }
        }
    }
}

private fun PsiAnnotation.getAnnotationIntValue(
    attribute: String,
    defaultValue: Int = -1
): Int {
    val psiValue = findAttributeValue(attribute) ?: return defaultValue
    val value = ConstantEvaluator.evaluate(null, psiValue)
    if (value is Number) {
        return value.toInt()
    }

    return defaultValue
}

private fun PsiAnnotation.getAnnotationStringValue(
    attribute: String,
    defaultValue: String = ""
): String {
    val psiValue = findAttributeValue(attribute) ?: return defaultValue
    return ConstantEvaluator.evaluateString(null, psiValue, false)
        ?: defaultValue
}
