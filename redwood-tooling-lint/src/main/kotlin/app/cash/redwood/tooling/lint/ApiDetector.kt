/*
 * Copyright (C) 2012 The Android Open Source Project
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

import app.cash.redwood.tooling.lint.VersionChecks.Companion.REQUIRES_DISPLAY_ANNOTATION
import app.cash.redwood.tooling.lint.VersionChecks.Companion.SDK_INT
import app.cash.redwood.tooling.lint.VersionChecks.Companion.getTargetApiAnnotation
import app.cash.redwood.tooling.lint.VersionChecks.Companion.getTargetApiForAnnotation
import app.cash.redwood.tooling.lint.VersionChecks.Companion.getVersionCheckConditional
import app.cash.redwood.tooling.lint.VersionChecks.Companion.isPrecededByVersionCheckExit
import app.cash.redwood.tooling.lint.VersionChecks.Companion.isTargetAnnotation
import app.cash.redwood.tooling.lint.VersionChecks.Companion.isWithinVersionCheckConditional
import com.android.SdkConstants.ATTR_VALUE
import com.android.SdkConstants.CONSTRUCTOR_NAME
import com.android.sdklib.SdkVersionInfo
import com.android.tools.lint.checks.ApiLookup.equivalentName
import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.AnnotationInfo
import com.android.tools.lint.detector.api.AnnotationUsageInfo
import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.ClassContext.Companion.getFqcn
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.LintMap
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.UastLintUtils.Companion.getLongAttribute
import com.android.tools.lint.detector.api.asCall
import com.android.tools.lint.detector.api.getInternalMethodName
import com.android.tools.lint.detector.api.isInlined
import com.android.tools.lint.detector.api.isKotlin
import com.android.tools.lint.detector.api.minSdkAtLeast
import com.android.tools.lint.detector.api.resolveOperator
import com.android.utils.usLocaleCapitalize
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.CommonClassNames
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiCompiledElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiSuperExpression
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTreeUtil
import java.util.EnumSet
import kotlin.math.max
import kotlin.math.min
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.uast.UAnnotated
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UArrayAccessExpression
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UBinaryExpressionWithType
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UCallableReferenceExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UClassLiteralExpression
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UFile
import org.jetbrains.uast.UIfExpression
import org.jetbrains.uast.UInstanceExpression
import org.jetbrains.uast.ULocalVariable
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.USuperExpression
import org.jetbrains.uast.USwitchClauseExpression
import org.jetbrains.uast.USwitchClauseExpressionWithBody
import org.jetbrains.uast.USwitchExpression
import org.jetbrains.uast.UThisExpression
import org.jetbrains.uast.UTryExpression
import org.jetbrains.uast.UUnaryExpression
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.UastCallKind
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUMethod
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.getQualifiedName
import org.jetbrains.uast.isUastChildOf
import org.jetbrains.uast.kotlin.BaseKotlinUastResolveProviderService
import org.jetbrains.uast.skipParenthesizedExprDown
import org.jetbrains.uast.skipParenthesizedExprUp
import org.jetbrains.uast.util.isConstructorCall
import org.jetbrains.uast.util.isInstanceCheck
import org.jetbrains.uast.util.isMethodCall
import org.jetbrains.uast.util.isTypeCast

/**
 * Looks for usages of APIs that are not supported in all the versions
 * targeted by this application (according to its minimum API
 * requirement in the manifest).
 */
internal class ApiDetector : Detector(), SourceCodeScanner {
    private fun getMinSdk(context: Context): Int {
        return context.mainProject.minSdkVersion.featureLevel
    }

    // ---- implements SourceCodeScanner ----

    override fun applicableAnnotations(): List<String> {
        return listOf(REQUIRES_DISPLAY_ANNOTATION)
    }

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean {
        return when (type) {
            AnnotationUsageType.METHOD_CALL,
            AnnotationUsageType.METHOD_REFERENCE,
            AnnotationUsageType.FIELD_REFERENCE,
            AnnotationUsageType.CLASS_REFERENCE,
            AnnotationUsageType.ANNOTATION_REFERENCE,
            AnnotationUsageType.EXTENDS,
            AnnotationUsageType.DEFINITION -> true
            else -> false
        }
    }

    override fun inheritAnnotation(annotation: String): Boolean {
        return false
    }

    override fun visitAnnotationUsage(
        context: JavaContext,
        element: UElement,
        annotationInfo: AnnotationInfo,
        usageInfo: AnnotationUsageInfo
    ) {
        val annotation = annotationInfo.annotation
        val member = usageInfo.referenced as? PsiMember
        val api = getApiLevel(context, annotation)
        if (api == -1) return
        val minSdk = getMinSdk(context)
        val evaluator = context.evaluator
        if (usageInfo.type == AnnotationUsageType.DEFINITION) {
            val fix = fix()
                .replace().all().with("")
                .range(context.getLocation(annotation))
                .name("Delete @RequiresDisplay")
                .build()

            val (targetAnnotation, target) = getTargetApiAnnotation(evaluator, element.uastParent?.uastParent)
            if (target > api) {
                val outerAnnotation = "@${targetAnnotation?.qualifiedName?.substringAfterLast('.')}($target)"
                val message = "Unnecessary; SDK_INT is always >= $target from outer annotation (`$outerAnnotation`)"
                context.report(Incident(OBSOLETE_SDK, message, context.getLocation(annotation), annotation, fix))
            } else {
                val message = "Unnecessary; SDK_INT is always >= $api"
                context.report(
                    Incident(OBSOLETE_SDK, message, context.getLocation(annotation), annotation, fix),
                    minSdkAtLeast(api)
                )
            }
            return
        }
        if (REQUIRES_DISPLAY_ANNOTATION != annotation.qualifiedName) {
            // These two annotations do not propagate the requirement outwards to callers
            return
        }
        val (targetAnnotation, target) = getTargetApiAnnotation(evaluator, element)
        if (target == -1 || api > target) {
            if (isWithinVersionCheckConditional(context, element, api)) {
                return
            }
            if (isPrecededByVersionCheckExit(context, element, api)) {
                return
            }
            if (isSurroundedByHigherTargetAnnotation(evaluator, targetAnnotation, api)) {
                // Make sure we aren't interpreting a redundant local @RequireApi(x) annotation
                // as implying the API level can be x here if there is an *outer* annotation
                // with a higher API level (we flag those above using [OBSOLETE_SDK_LEVEL] but
                // since that's a warning and this type is an error, make sure we don't have
                // false positives.
                return
            }

            val location: Location
            val fqcn: String?
            if (element is UCallExpression &&
                element.kind != UastCallKind.METHOD_CALL &&
                element.classReference != null
            ) {
                val classReference = element.classReference!!
                location = context.getRangeLocation(element, 0, classReference, 0)
                fqcn = classReference.resolvedName ?: member?.name ?: ""
            } else {
                location = context.getNameLocation(element)
                fqcn = member?.name ?: ""
            }
            val type = when (usageInfo.type) {
                AnnotationUsageType.EXTENDS -> "Extending $fqcn"
                AnnotationUsageType.ANNOTATION_REFERENCE,
                AnnotationUsageType.CLASS_REFERENCE -> "Class"
                AnnotationUsageType.METHOD_RETURN,
                AnnotationUsageType.METHOD_OVERRIDE -> "Method"
                AnnotationUsageType.VARIABLE_REFERENCE,
                AnnotationUsageType.FIELD_REFERENCE -> "Field"
                else -> "Call"
            }
            val field = usageInfo.referenced
            val issue = if (field is PsiField && isInlined(field, context.evaluator)) {
                if (isBenignConstantUsage(
                        element,
                        field.name,
                        field.containingClass?.qualifiedName ?: ""
                    )
                ) {
                    return
                }
                INLINED
            } else {
                UNSUPPORTED
            }

            ApiVisitor(context).report(
                issue, element, location, type, fqcn, api, minSdk, apiLevelFix(api)
            )
        }
    }

    /**
     * Is there an outer annotation (outside [annotation] that specifies
     * an api level requirement of [atLeast]?
     */
    private fun isSurroundedByHigherTargetAnnotation(
        evaluator: JavaEvaluator,
        annotation: UAnnotation?,
        atLeast: Int,
        isApiLevelAnnotation: (String) -> Boolean = VersionChecks.Companion::isTargetAnnotation
    ): Boolean {
        var curr = annotation ?: return false
        while (true) {
            val (outer, target) = getTargetApiAnnotation(evaluator, curr.uastParent?.uastParent, isApiLevelAnnotation)
            if (target >= atLeast) {
                return true
            }
            curr = outer ?: return false
        }
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        if (context.isTestSource && !context.driver.checkTestSources) {
            return null
        }
        return ApiVisitor(context)
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(
            USimpleNameReferenceExpression::class.java,
            ULocalVariable::class.java,
            UTryExpression::class.java,
            UBinaryExpressionWithType::class.java,
            UBinaryExpression::class.java,
            UUnaryExpression::class.java,
            UCallExpression::class.java,
            UClass::class.java,
            UMethod::class.java,
            USwitchExpression::class.java,
            UCallableReferenceExpression::class.java,
            UArrayAccessExpression::class.java
        )
    }

    override fun filterIncident(context: Context, incident: Incident, map: LintMap): Boolean {
        val mainProject = context.mainProject
        val mainMinSdk = mainProject.minSdkVersion.featureLevel

        val requires = map.getInt(KEY_REQUIRES_API) ?: return false
        if (requires <= mainMinSdk) {
            return false
        }

        val target = map.getInt(KEY_MIN_API) ?: return false
        val minSdk = max(target, mainMinSdk)

        // Update the minSdkVersion included in the message
        val formatString = map.getString(KEY_MESSAGE) ?: return false
        incident.message = String.format(formatString, minSdk)
        return true
    }

    private inner class ApiVisitor(private val context: JavaContext) : UElementHandler() {

        fun report(
            issue: Issue,
            node: UElement,
            location: Location,
            type: String,
            sig: String,
            requires: Int,
            minSdk: Int,
            fix: LintFix? = null,
        ) {
            val apiLevel = getApiLevelString(requires, context)
            val typeString = type.usLocaleCapitalize()
            val formatString = "$typeString requires API level $apiLevel (current min is %1\$s): `$sig`"
            report(issue, node, location, formatString, fix, requires, minSdk)
        }

        private fun report(
            issue: Issue,
            node: UElement,
            location: Location,
            formatString: String, // one parameter: minSdkVersion
            fix: LintFix? = null,
            requires: Int,
            min: Int = 1,
        ) {
            val incident = Incident(
                issue = issue,
                message = "", // always formatted in accept() before reporting
                location = location,
                scope = node,
                fix = fix
            )
            val map = map().apply {
                put(KEY_REQUIRES_API, requires)
                put(KEY_MIN_API, max(min, getTargetApi(node)))
                put(KEY_MESSAGE, formatString)
            }
            context.report(incident, map)
        }

        override fun visitSimpleNameReferenceExpression(
            node: USimpleNameReferenceExpression
        ) {
            val resolved = node.resolve()
            if (resolved is PsiField) {
                checkField(node, resolved)
            } else if (resolved is PsiMethod && node is UCallExpression) {
                checkMethodReference(node, resolved)
            }
        }

        override fun visitCallableReferenceExpression(node: UCallableReferenceExpression) {
            val resolved = node.resolve()
            if (resolved is PsiMethod) {
                checkMethodReference(node, resolved)
            }
        }

        private fun checkMethodReference(expression: UReferenceExpression, method: PsiMethod) {
            val apiDatabase = apiDatabase ?: return

            val containingClass = method.containingClass ?: return
            val evaluator = context.evaluator
            val owner = evaluator.getQualifiedName(containingClass)
                ?: return // Couldn't resolve type
            if (!apiDatabase.containsClass(owner)) {
                return
            }

            val name = getInternalMethodName(method)
            val desc = evaluator.getMethodDescription(
                method,
                false,
                false
            ) // Couldn't compute description of method for some reason; probably
                // failure to resolve parameter types
                ?: return

            val api = apiDatabase.getMethodVersion(owner, name, desc)
            if (api == -1) {
                return
            }
            val minSdk = getMinSdk(context)
            if (isSuppressed(context, api, expression, minSdk)) {
                return
            }

            val signature = expression.asSourceString()
            val location = context.getLocation(expression)
            report(
                UNSUPPORTED,
                expression,
                location,
                "Method reference",
                signature,
                api,
                minSdk,
                apiLevelFix(api)
            )
        }

        override fun visitBinaryExpressionWithType(node: UBinaryExpressionWithType) {
            if (node.isTypeCast()) {
                visitTypeCastExpression(node)
            } else if (node.isInstanceCheck()) {
                val typeReference = node.typeReference
                if (typeReference != null) {
                    val type = typeReference.type
                    if (type is PsiClassType) {
                        checkClassReference(typeReference, type)
                    }
                }
            }
        }

        private fun visitTypeCastExpression(expression: UBinaryExpressionWithType) {
            val operand = expression.operand
            val operandType = operand.getExpressionType()
            val castType = expression.type
            if (castType == operandType) {
                return
            }
            if (operandType !is PsiClassType) {
                return
            }
            if (castType !is PsiClassType) {
                return
            }

            val typeReference = expression.typeReference
            if (typeReference != null) {
                if (!checkClassReference(typeReference, castType)) {
                    // Found problem with cast type itself: don't bother also warning
                    // about problem with LHS
                    return
                }
            }

            checkCast(expression, operandType, castType)
        }

        private fun checkClassReference(
            node: UElement,
            classType: PsiClassType
        ): Boolean {
            val apiDatabase = apiDatabase ?: return true
            val evaluator = context.evaluator
            val expressionOwner = evaluator.getQualifiedName(classType) ?: return true
            val api = apiDatabase.getClassVersion(expressionOwner)
            if (api == -1) {
                return true
            }
            val minSdk = getMinSdk(context)
            if (isSuppressed(context, api, node, minSdk)) {
                return true
            }

            val location = context.getLocation(node)
            report(
                UNSUPPORTED,
                node,
                location,
                "Class",
                expressionOwner,
                api,
                minSdk,
                apiLevelFix(api)
            )
            return false
        }

        private fun checkCast(
            node: UElement,
            classType: PsiClassType,
            interfaceType: PsiClassType
        ) {
            if (classType == interfaceType) {
                return
            }
            val evaluator = context.evaluator
            val classTypeInternal = evaluator.getQualifiedName(classType)
            val interfaceTypeInternal = evaluator.getQualifiedName(interfaceType)
            checkCast(node, classTypeInternal, interfaceTypeInternal, implicit = false)
        }

        private fun checkCast(
            node: UElement,
            classType: String?,
            interfaceType: String?,
            implicit: Boolean
        ) {
            if (interfaceType == null || classType == null) {
                return
            }
            if (equivalentName(interfaceType, "java/lang/Object")) {
                return
            }

            val apiDatabase = apiDatabase ?: return
            val api = apiDatabase.getValidCastVersion(classType, interfaceType)
            if (api == -1) {
                return
            }

            val minSdk = getMinSdk(context)
            if (api <= minSdk) {
                return
            }

            if (isSuppressed(context, api, node, minSdk)) {
                return
            }

            // Also see if this cast has been explicitly checked for
            var curr = node
            while (true) {
                when (curr) {
                    is UIfExpression -> {
                        if (node.isUastChildOf(curr.thenExpression, true)) {
                            val condition = curr.condition.skipParenthesizedExprDown()
                            if (condition is UBinaryExpressionWithType) {
                                val type = condition.type
                                // Explicitly checked with surrounding instanceof check
                                if (type is PsiClassType && context.evaluator.getQualifiedName(type) == interfaceType) {
                                    return
                                }
                            }
                        }
                    }
                    is USwitchClauseExpressionWithBody -> {
                        if (node.isUastChildOf(curr.body, true)) {
                            for (case in curr.caseValues) {
                                val condition = case.skipParenthesizedExprDown()
                                if (condition is UBinaryExpressionWithType) {
                                    val type = condition.type
                                    if (type is PsiClassType && context.evaluator.getQualifiedName(type) == interfaceType) {
                                        return
                                    }
                                }
                            }
                        }
                    }
                    is UMethod -> {
                        break
                    }
                }
                curr = curr.uastParent ?: break
            }

            val castType = if (implicit) "Implicit cast" else "Cast"
            val location = context.getLocation(node)
            val message: String
            val to = interfaceType.substringAfterLast('.')
            val from = classType.substringAfterLast('.')
            message = if (interfaceType == classType) {
                "$castType to `$to` requires API level $api (current min is %1\$d)"
            } else {
                "$castType from `$from` to `$to` requires API level $api (current min is %1\$d)"
            }

            report(
                UNSUPPORTED, node, location, message, apiLevelFix(api), requires = api,
                min = minSdk
            )
        }

        override fun visitMethod(node: UMethod) {
            val apiDatabase = apiDatabase ?: return
            val containingClass = node.containingClass

            val buildSdk = context.project.buildSdk
            val name = node.name
            val evaluator = context.evaluator
            var superMethod = evaluator.getSuperMethod(node)
            while (superMethod != null) {
                val cls = superMethod.containingClass ?: break
                var fqcn = cls.qualifiedName ?: break
                if (fqcn.startsWith("android.") ||
                    fqcn.startsWith("java.") && fqcn != CommonClassNames.JAVA_LANG_OBJECT ||
                    fqcn.startsWith("javax.")
                ) {
                    val desc = evaluator.getMethodDescription(superMethod, false, false)
                    if (desc != null) {
                        val owner = evaluator.getQualifiedName(cls) ?: return
                        val api = apiDatabase.getMethodVersion(owner, name, desc)
                        if (api > buildSdk && buildSdk != -1) {
                            if (context.driver
                                .isSuppressed(context, OVERRIDE, node as UElement)
                            ) {
                                return
                            }

                            // TODO: Don't complain if it's annotated with @Override; that means
                            // somehow the build target isn't correct.
                            if (containingClass != null) {
                                var className = containingClass.name
                                val fullClassName = containingClass.qualifiedName
                                if (fullClassName != null) {
                                    className = fullClassName
                                }
                                fqcn = "$className#$name"
                            } else {
                                fqcn = name
                            }

                            val message =
                                "This method is not overriding anything with the current " +
                                    "build target, but will in API level $api (current " +
                                    "target is $buildSdk): `$fqcn`"
                            var locationNode: PsiElement? = node.nameIdentifier
                            if (locationNode == null) {
                                locationNode = node
                            }
                            val location = context.getLocation(locationNode)
                            context.report(Incident(OVERRIDE, node, location, message))
                        }
                    }
                } else {
                    break
                }

                superMethod = evaluator.getSuperMethod(superMethod)
            }
        }

        override fun visitClassLiteralExpression(node: UClassLiteralExpression) {
            val type = node.type
            if (type is PsiClassType) {
                val lhs = node.expression
                val locationElement = lhs ?: node
                checkClassType(locationElement, type, null)
            }
        }

        private fun checkClassType(
            element: UElement,
            classType: PsiClassType,
            descriptor: String?
        ) {
            val owner = context.evaluator.getQualifiedName(classType)
            val fqcn = classType.canonicalText
            if (owner != null) {
                checkClass(element, descriptor, owner, fqcn)
            }
        }

        private fun checkClass(
            element: UElement,
            descriptor: String?,
            owner: String,
            fqcn: String
        ) {
            val apiDatabase = apiDatabase ?: return
            val api = apiDatabase.getClassVersion(owner)
            if (api == -1) {
                return
            }
            val minSdk = getMinSdk(context)
            if (isSuppressed(context, api, element, minSdk)) {
                return
            }

            // It's okay to reference classes from annotations
            if (element.getParentOfType<UElement>(UAnnotation::class.java) != null) {
                return
            }

            val location = context.getNameLocation(element)
            val desc = descriptor ?: "Class"
            report(UNSUPPORTED, element, location, desc, fqcn, api, minSdk, apiLevelFix(api))
        }

        override fun visitCallExpression(node: UCallExpression) {
            val method = node.resolve()
            if (method != null) {
                visitCall(method, node, node)
            }
        }

        private fun visitCall(
            method: PsiMethod,
            call: UCallExpression,
            reference: UElement
        ) {
            val apiDatabase = apiDatabase ?: return
            val containingClass = method.containingClass ?: return
            val parameterList = method.parameterList
            if (parameterList.parametersCount > 0) {
                val parameters = parameterList.parameters
                val arguments = call.valueArguments
                for (i in parameters.indices) {
                    val parameterType = parameters[i].type
                    if (parameterType is PsiClassType) {
                        if (i >= arguments.size) {
                            // We can end up with more arguments than parameters when
                            // there is a varargs call.
                            break
                        }
                        val argument = arguments[i]
                        val argumentType = argument.getExpressionType()
                        if (argumentType == null ||
                            parameterType == argumentType ||
                            argumentType !is PsiClassType ||
                            parameterType.rawType() == argumentType.rawType()
                        ) {
                            continue
                        }
                        checkCast(
                            argument,
                            argumentType,
                            parameterType
                        )
                    }
                }
            }

            if (method !is PsiCompiledElement) {
                // We're only checking the Android SDK below, which should
                // be provided as binary (android.jar) and if we're actually
                // running on sources we don't want to perform this check
                return
            }

            val evaluator = context.evaluator
            val owner = evaluator.getQualifiedName(containingClass)
                ?: return // Couldn't resolve type

            val name = getInternalMethodName(method)

            if (!apiDatabase.containsClass(owner)) {
                return
            }

            val desc = evaluator.getMethodDescription(
                method,
                includeName = false,
                includeReturn = false
            ) // Couldn't compute description of method for some reason; probably
                // failure to resolve parameter types
                ?: return

            visitCall(method, call, reference, containingClass, owner, name, desc)
        }

        private fun visitCall(
            method: PsiMethod,
            call: UCallExpression,
            reference: UElement,
            containingClass: PsiClass,
            owner: String,
            name: String,
            desc: String
        ) {
            val apiDatabase = apiDatabase ?: return
            val evaluator = context.evaluator

            var api = apiDatabase.getMethodVersion(owner, name, desc)
            if (api == -1) {
                return
            }
            val minSdk = getMinSdk(context)
            if (api <= minSdk) {
                return
            }

            var fqcn = containingClass.qualifiedName

            val receiver = if (call.isMethodCall()) {
                call.receiver
            } else {
                null
            }

            // The lint API database contains two optimizations:
            // First, all members that were available in API 1 are omitted from the database,
            // since that saves about half of the size of the database, and for API check
            // purposes, we don't need to distinguish between "doesn't exist" and "available
            // in all versions".

            // Second, all inherited members were inlined into each class, so that it doesn't
            // have to do a repeated search up the inheritance chain.
            //
            // Unfortunately, in this custom PSI detector, we look up the real resolved method,
            // which can sometimes have a different minimum API.
            //
            // For example, SQLiteDatabase had a close() method from API 1. Therefore, calling
            // SQLiteDatabase is supported in all versions. However, it extends SQLiteClosable,
            // which in API 16 added "implements Closable". In this detector, if we have the
            // following code:
            //     void test(SQLiteDatabase db) { db.close }
            // here the call expression will be the close method on type SQLiteClosable. And
            // that will result in an API requirement of API 16, since the close method it now
            // resolves to is in API 16.
            //
            // To work around this, we can now look up the type of the call expression ("db"
            // in the above, but it could have been more complicated), and if that's a
            // different type than the type of the method, we look up *that* method from
            // lint's database instead. Furthermore, it's possible for that method to return
            // "-1" and we can't tell if that means "doesn't exist" or "present in API 1", we
            // then check the package prefix to see whether we know it's an API method whose
            // members should all have been inlined.
            if (call.isMethodCall()) {
                if (receiver != null &&
                    receiver !is UThisExpression &&
                    receiver !is PsiSuperExpression
                ) {
                    val receiverType = receiver.getExpressionType()
                    if (receiverType is PsiClassType) {
                        val containingType = context.evaluator.getClassType(containingClass)
                        val inheritanceChain =
                            getInheritanceChain(receiverType, containingType)
                        if (inheritanceChain != null) {
                            for (type in inheritanceChain) {
                                val expressionOwner = evaluator.getQualifiedName(type)
                                if (expressionOwner != null && expressionOwner != owner) {
                                    val specificApi = apiDatabase.getMethodVersion(
                                        expressionOwner, name, desc
                                    )
                                    if (specificApi == -1) {
                                        if (apiDatabase.isRelevantOwner(expressionOwner)) {
                                            return
                                        }
                                    } else if (specificApi <= minSdk) {
                                        return
                                    } else {
                                        // For example, for Bundle#getString(String,String) the API level
                                        // is 12, whereas for BaseBundle#getString(String,String) the API
                                        // level is 21. If the code specified a Bundle instead of
                                        // a BaseBundle, reported the Bundle level in the error message
                                        // instead.
                                        if (specificApi < api) {
                                            api = specificApi
                                            fqcn = type.canonicalText
                                        }
                                        api = min(specificApi, api)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Unqualified call; need to search in our super hierarchy
                    var cls: PsiClass? = null
                    val receiverType = call.receiverType
                    if (receiverType is PsiClassType) {
                        cls = receiverType.resolve()
                    }

                    if (receiver is UThisExpression || receiver is USuperExpression) {
                        val pte = receiver as UInstanceExpression
                        val resolved = pte.resolve()
                        if (resolved is PsiClass) {
                            cls = resolved
                        }
                    }

                    while (cls != null) {
                        if (cls is PsiAnonymousClass) {
                            // If it's an unqualified call in an anonymous class, we need to
                            // rely on the resolve method to find out whether the method is
                            // picked up from the anonymous class chain or any outer classes
                            var found = false
                            val anonymousBaseType = cls.baseClassType
                            val anonymousBase = anonymousBaseType.resolve()
                            if (anonymousBase != null && anonymousBase.isInheritor(
                                    containingClass,
                                    true
                                )
                            ) {
                                cls = anonymousBase
                                found = true
                            } else {
                                val surroundingBaseType =
                                    PsiTreeUtil.getParentOfType(cls, PsiClass::class.java, true)
                                if (surroundingBaseType != null && surroundingBaseType.isInheritor(
                                        containingClass,
                                        true
                                    )
                                ) {
                                    cls = surroundingBaseType
                                    found = true
                                }
                            }
                            if (!found) {
                                break
                            }
                        }
                        val expressionOwner = evaluator.getQualifiedName(cls)
                        if (expressionOwner == null || equivalentName(
                                expressionOwner,
                                "java/lang/Object"
                            )
                        ) {
                            break
                        }
                        val specificApi =
                            apiDatabase.getMethodVersion(expressionOwner, name, desc)
                        if (specificApi == -1) {
                            if (apiDatabase.isRelevantOwner(expressionOwner)) {
                                break
                            }
                        } else if (specificApi <= minSdk) {
                            return
                        } else {
                            if (specificApi < api) {
                                api = specificApi
                                fqcn = cls.qualifiedName
                            }
                            api = min(specificApi, api)
                            break
                        }
                        cls = cls.superClass
                    }
                }
            }

            if (isSuppressed(context, api, reference, minSdk)) {
                return
            }

            if (receiver != null || call.isMethodCall()) {
                var target: PsiClass? = null
                if (!method.isConstructor) {
                    if (receiver != null) {
                        val type = receiver.getExpressionType()
                        if (type is PsiClassType) {
                            target = type.resolve()
                        }
                    } else {
                        target = call.getContainingUClass()?.javaPsi
                    }
                }

                // Look to see if there's a possible local receiver
                if (target != null) {
                    val methods = target.findMethodsBySignature(method, true)
                    if (methods.size > 1) {
                        for (m in methods) {
                            //noinspection LintImplPsiEquals
                            if (method != m) {
                                val provider = m.containingClass
                                if (provider != null) {
                                    val methodOwner = evaluator.getQualifiedName(provider)
                                    if (methodOwner != null) {
                                        val methodApi = apiDatabase.getMethodVersion(
                                            methodOwner, name, desc
                                        )
                                        if (methodApi == -1 || methodApi <= minSdk) {
                                            // Yes, we found another call that doesn't have an API requirement
                                            return
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // If you're simply calling super.X from method X, even if method X is in a higher
                // API level than the minSdk, we're generally safe; that method should only be
                // called by the framework on the right API levels. (There is a danger of somebody
                // calling that method locally in other contexts, but this is hopefully unlikely.)
                if (receiver is USuperExpression) {
                    val containingMethod = call.getContainingUMethod()?.javaPsi
                    if (containingMethod != null &&
                        name == containingMethod.name &&
                        evaluator.areSignaturesEqual(method, containingMethod) &&
                        // We specifically exclude constructors from this check, because we
                        // do want to flag constructors requiring the new API level; it's
                        // highly likely that the constructor is called by local code so
                        // you should specifically investigate this as a developer
                        !method.isConstructor
                    ) {
                        return
                    }
                }
            }

            val signature: String = if (fqcn == null) {
                name
            } else if (CONSTRUCTOR_NAME == name) {
                if (isKotlin(reference.sourcePsi)) {
                    "$fqcn()"
                } else {
                    "new $fqcn"
                }
            } else {
                "$fqcn${'#'}$name"
            }

            val nameIdentifier = call.methodIdentifier
            val location = if (call.isConstructorCall() && call.classReference != null) {
                context.getRangeLocation(call, 0, call.classReference!!, 0)
            } else if (nameIdentifier != null) {
                context.getLocation(nameIdentifier)
            } else {
                context.getLocation(reference)
            }
            report(
                UNSUPPORTED,
                reference,
                location,
                "Call",
                signature,
                api,
                minSdk,
                apiLevelFix(api),
            )
        }

        override fun visitLocalVariable(node: ULocalVariable) {
            val initializer = node.uastInitializer ?: return

            val initializerType = initializer.getExpressionType() as? PsiClassType ?: return

            val interfaceType = node.type

            if (interfaceType !is PsiClassType) {
                return
            }

            if (initializerType == interfaceType) {
                return
            }

            checkCast(initializer, initializerType, interfaceType)
        }

        override fun visitArrayAccessExpression(node: UArrayAccessExpression) {
            val method = node.resolveOperator() ?: return
            val call = node.asCall(method)
            visitCall(method, call, node)
        }

        override fun visitUnaryExpression(node: UUnaryExpression) {
            // Overloaded operators
            val method = node.resolveOperator()
            if (method != null) {
                val call = node.asCall(method)
                visitCall(method, call, node)
            }
        }

        override fun visitBinaryExpression(node: UBinaryExpression) {
            // Overloaded operators
            val method = node.resolveOperatorWorkaround()
            if (method != null) {
                val call = node.asCall(method)
                visitCall(method, call, node)
            }

            val operator = node.operator
            if (operator is UastBinaryOperator.AssignOperator) {
                // Plain assignment: check casts
                val rExpression = node.rightOperand
                val rhsType = rExpression.getExpressionType() as? PsiClassType ?: return

                val interfaceType = node.leftOperand.getExpressionType()
                if (interfaceType !is PsiClassType) {
                    return
                }

                if (rhsType == interfaceType) {
                    return
                }

                checkCast(rExpression, rhsType, interfaceType)
            }
        }

        /**
         * Checks a Java source field reference. Returns true if the
         * field is known regardless of whether it's an invalid field or
         * not.
         */
        private fun checkField(node: UElement, field: PsiField) {
            val apiDatabase = apiDatabase ?: return
            val name = field.name

            val containingClass = field.containingClass ?: return
            val evaluator = context.evaluator
            var owner = evaluator.getQualifiedName(containingClass) ?: return

            if (SDK_INT == name && "android.os.Build.VERSION" == owner) {
                checkObsoleteSdkVersion(context, node)
            }

            var api = apiDatabase.getFieldVersion(owner, name)
            if (api != -1) {
                val minSdk = getMinSdk(context)
                if (api > minSdk && api > getTargetApi(node)) {
                    // Only look for compile time constants. See JLS 15.28 and JLS 13.4.9.
                    val issue = if (isInlined(field, evaluator)) INLINED else UNSUPPORTED
                    if (issue == UNSUPPORTED) {
                        // Declaring enum constants are safe; they won't be called on older
                        // platforms.
                        val parent = skipParenthesizedExprUp(node.uastParent)
                        if (parent is USwitchClauseExpression) {
                            val conditions = parent.caseValues

                            if (conditions.contains(node)) {
                                return
                            }
                        }
                    } else if (isBenignConstantUsage(node, name, owner)) {
                        return
                    }

                    if (owner == "java.lang.annotation.ElementType") {
                        // TYPE_USE and TYPE_PARAMETER annotations cannot be referenced
                        // on older devices, but it's typically fine to declare these
                        // annotations since they're normally not loaded at runtime; they're
                        // meant for static analysis.
                        val parent: UDeclaration? = node.getParentOfType(
                            parentClass = UDeclaration::class.java,
                            strict = true
                        )
                        if (parent is UClass && parent.isAnnotationType) {
                            return
                        }
                    }

                    if (isSuppressed(context, api, node, minSdk)) {
                        return
                    }

                    // Look to see if it's a field reference for a specific sub class
                    // or interface which defined the field or constant at an earlier
                    // API level.
                    //
                    // For example, for api 28/29 and android.app.TaskInfo,
                    // A number of fields were moved up from ActivityManager.RecentTaskInfo
                    // to the new class TaskInfo in Q; however, these field are almost
                    // always accessed via ActivityManager#taskInfo which is still
                    // a RecentTaskInfo so this code works prior to Q. If you explicitly
                    // access it as a TaskInfo the class reference itself will be
                    // flagged by lint. (The platform change was in
                    // Change-Id: Iaf1731002196bb89319de141a05ab92a7dcb2928)
                    // We can't just unconditionally exit here, since there are existing
                    // API requirements on various fields in the TaskInfo subclasses,
                    // so try to pick out the real type.
                    val parent = node.uastParent
                    if (parent is UQualifiedReferenceExpression) {
                        val receiver = parent.receiver
                        val specificOwner = receiver.getExpressionType()?.canonicalText
                            ?: (receiver as? UReferenceExpression)?.getQualifiedName()
                        val specificApi = if (specificOwner != null)
                            apiDatabase.getFieldVersion(specificOwner, name)
                        else
                            -1
                        if (specificApi != -1 && specificOwner != null) {
                            if (specificApi < api) {
                                // Make sure the error message reflects the correct (lower)
                                // minSdkVersion if we have a more specific match on the field
                                // type
                                api = specificApi
                                owner = specificOwner
                            }
                            if (specificApi > minSdk && specificApi > getTargetApi(node)) {
                                if (isSuppressed(context, specificApi, node, minSdk)) {
                                    return
                                }
                            } else {
                                return
                            }
                        }
                    }

                    // If the reference is a qualified expression, don't just highlight the
                    // field name itself; include the qualifiers too
                    var locationNode = node

                    // But only include expressions to the left; for example, if we're
                    // trying to highlight the field "OVERLAY" in
                    //     PorterDuff.Mode.OVERLAY.hashCode()
                    // we should *not* include the .hashCode() suffix
                    while (locationNode.uastParent is UQualifiedReferenceExpression &&
                        (locationNode.uastParent as UQualifiedReferenceExpression)
                            .selector === locationNode
                    ) {
                        locationNode = locationNode.uastParent ?: node
                    }

                    val location = context.getLocation(locationNode)
                    val fqcn = getFqcn(owner) + '#'.toString() + name
                    report(
                        issue,
                        node,
                        location,
                        "Field",
                        fqcn,
                        api,
                        minSdk,
                        apiLevelFix(api)
                    )
                }
            }
        }
    }

    private fun getApiLevelString(requires: Int, context: JavaContext): String {
        // For preview releases, don't show the API level as a number; show it using
        // a version code
        return if (requires <= SdkVersionInfo.HIGHEST_KNOWN_STABLE_API ||
            requires <= context.project.buildSdk && context.project.buildTarget?.version?.isPreview == false
        ) {
            requires.toString()
        } else {
            SdkVersionInfo.getCodeName(requires) ?: requires.toString()
        }
    }

    private fun checkObsoleteSdkVersion(context: JavaContext, node: UElement) {
        val binary = node.getParentOfType(UBinaryExpression::class.java, true)
        if (binary != null) {
            val minSdk = getMinSdk(context)
            // Note that we do NOT use the app's minSdkVersion here; the library's
            // minSdkVersion should increased instead since it's possible that
            // this library is used elsewhere with a lower minSdkVersion than the
            // main min sdk, and deleting these calls would cause crashes in
            // that usage.
            val constraint = getVersionCheckConditional(binary)
            if (constraint != null) {
                val always = constraint.alwaysAtLeast(minSdk)
                val never = constraint.not().alwaysAtLeast(minSdk)
                val message =
                    when {
                        always -> "Unnecessary; SDK_INT is always >= $minSdk"
                        never -> "Unnecessary; SDK_INT is never < $minSdk"
                        else -> return
                    }
                context.report(
                    Incident(
                        OBSOLETE_SDK,
                        message,
                        context.getLocation(binary),
                        binary,
                        LintFix.create().data(KEY_CONDITIONAL, always)
                    )
                )
            }
        }
    }

    companion object {
        // Lint does not offer a way to pass this in. Ugh.
        var apiDatabase: RedwoodApi? = null

        const val KEY_REQUIRES_API = "requiresApi"
        const val KEY_CONDITIONAL = "conditional"
        private const val KEY_MESSAGE = "message"
        private const val KEY_MIN_API = "minSdk"

        private val JAVA_IMPLEMENTATION = Implementation(ApiDetector::class.java, Scope.JAVA_FILE_SCOPE)

        /** Accessing an unsupported API. */
        val UNSUPPORTED = Issue.create(
            id = "NewApi",
            briefDescription = "Calling new methods on older versions",
            explanation = """
                This check scans through all the Android API calls in the application and \
                warns about any calls that are not available on **all** versions targeted by \
                this application (according to its minimum SDK attribute in the manifest).

                If you really want to use this API and don't need to support older devices \
                just set the `minSdkVersion` in your `build.gradle` or `AndroidManifest.xml` \
                files.

                If your code is **deliberately** accessing newer APIs, and you have ensured \
                (e.g. with conditional execution) that this code will only ever be called on \
                a supported platform, then you can annotate your class or method with the \
                `@TargetApi` annotation specifying the local minimum SDK to apply, such as \
                `@TargetApi(11)`, such that this check considers 11 rather than your manifest \
                file's minimum SDK as the required API level.

                If you are deliberately setting `android:` attributes in style definitions, \
                make sure you place this in a `values-v`*NN* folder in order to avoid running \
                into runtime conflicts on certain devices where manufacturers have added \
                custom attributes whose ids conflict with the new ones on later platforms.

                Similarly, you can use tools:targetApi="11" in an XML file to indicate that \
                the element will only be inflated in an adequate context.
                """,
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            androidSpecific = true,
            implementation = Implementation(
                ApiDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE),
                Scope.JAVA_FILE_SCOPE,
            )
        )

        /** Accessing an inlined API on older platforms. */
        val INLINED = Issue.create(
            id = "InlinedApi",
            briefDescription = "Using inlined constants on older versions",
            explanation = """
                This check scans through all the Android API field references in the \
                application and flags certain constants, such as static final integers and \
                Strings, which were introduced in later versions. These will actually be \
                copied into the class files rather than being referenced, which means that \
                the value is available even when running on older devices. In some cases \
                that's fine, and in other cases it can result in a runtime crash or \
                incorrect behavior. It depends on the context, so consider the code carefully \
                and decide whether it's safe and can be suppressed or whether the code needs \
                to be guarded.

                If you really want to use this API and don't need to support older devices \
                just set the `minSdkVersion` in your `build.gradle` or `AndroidManifest.xml` \
                files.

                If your code is **deliberately** accessing newer APIs, and you have ensured \
                (e.g. with conditional execution) that this code will only ever be called on \
                a supported platform, then you can annotate your class or method with the \
                `@TargetApi` annotation specifying the local minimum SDK to apply, such as \
                `@TargetApi(11)`, such that this check considers 11 rather than your manifest \
                file's minimum SDK as the required API level.
                """,
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            androidSpecific = true,
            implementation = JAVA_IMPLEMENTATION
        )

        /** Method conflicts with new inherited method. */
        val OVERRIDE = Issue.create(
            id = "Override",
            briefDescription = "Method conflicts with new inherited method",
            explanation = """
                Suppose you are building against Android API 8, and you've subclassed \
                Activity. In your subclass you add a new method called `isDestroyed`(). \
                At some later point, a method of the same name and signature is added to \
                Android. Your method will now override the Android method, and possibly break \
                its contract. Your method is not calling `super.isDestroyed()`, since your \
                compilation target doesn't know about the method.

                The above scenario is what this lint detector looks for. The above example is \
                real, since `isDestroyed()` was added in API 17, but it will be true for \
                **any** method you have added to a subclass of an Android class where your \
                build target is lower than the version the method was introduced in.

                To fix this, either rename your method, or if you are really trying to augment \
                the builtin method if available, switch to a higher build target where you can \
                deliberately add `@Override` on your overriding method, and call `super` if \
                appropriate etc.
                """,
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            androidSpecific = true,
            implementation = JAVA_IMPLEMENTATION
        )

        /** Obsolete SDK_INT version check. */
        val OBSOLETE_SDK = Issue.create(
            id = "ObsoleteSdkInt",
            briefDescription = "Obsolete SDK_INT Version Check",
            explanation = """
                This check flags version checks that are not necessary, because the \
                `minSdkVersion` (or surrounding known API level) is already at least as high \
                as the version checked for.

                Similarly, it also looks for resources in `-vNN` folders, such as `values-v14` \
                where the version qualifier is less than or equal to the `minSdkVersion`, \
                where the contents should be merged into the best folder.
                """,
            category = Category.PERFORMANCE,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                ApiDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE),
                Scope.JAVA_FILE_SCOPE,
            )
        )

        private fun apiLevelFix(api: Int): LintFix {
            return LintFix.create().data(KEY_REQUIRES_API, api)
        }

        /**
         * Checks whether the given instruction is a benign usage of
         * a constant defined in a later version of Android than the
         * application's `minSdkVersion`.
         *
         * @param node the instruction to check
         * @param name the name of the constant
         * @param owner the field owner
         * @return true if the given usage is safe on older versions
         *     than the introduction level of the constant
         */
        fun isBenignConstantUsage(
            node: UElement?,
            name: String,
            owner: String
        ): Boolean {
            if (equivalentName(owner, "android/os/Build\$VERSION_CODES")) {
                // These constants are required for compilation, not execution
                // and valid code checks it even on older platforms
                return true
            }
            if (equivalentName(
                    owner,
                    "android/view/ViewGroup\$LayoutParams"
                ) && name == "MATCH_PARENT"
            ) {
                return true
            }
            if (equivalentName(
                    owner,
                    "android/widget/AbsListView"
                ) && (
                    name == "CHOICE_MODE_NONE" ||
                        name == "CHOICE_MODE_MULTIPLE" ||
                        name == "CHOICE_MODE_SINGLE"
                    )
            ) {
                // android.widget.ListView#CHOICE_MODE_MULTIPLE and friends have API=1,
                // but in API 11 it was moved up to the parent class AbsListView.
                // Referencing AbsListView#CHOICE_MODE_MULTIPLE technically requires API 11,
                // but the constant is the same as the older version, so accept this without
                // warning.
                return true
            }

            // Gravity#START and Gravity#END are okay; these were specifically written to
            // be backwards compatible (by using the same lower bits for START as LEFT and
            // for END as RIGHT)
            if (equivalentName(
                    owner,
                    "android/view/Gravity"
                ) && ("START" == name || "END" == name)
            ) {
                return true
            }

            if (node == null) {
                return false
            }

            // It's okay to reference the constant as a case constant (since that
            // code path won't be taken) or in a condition of an if statement
            var curr = node.uastParent
            while (curr != null) {
                if (curr is USwitchClauseExpression) {
                    val caseValues = curr.caseValues
                    for (condition in caseValues) {
                        if (node.isUastChildOf(condition, false)) {
                            return true
                        }
                    }
                    return false
                } else if (curr is UIfExpression) {
                    val condition = curr.condition
                    return node.isUastChildOf(condition, false)
                } else if (curr is UMethod || curr is UClass) {
                    break
                }
                curr = curr.uastParent
            }

            return false
        }

        /**
         * Returns the first (in DFS order) inheritance chain connecting
         * the two given classes.
         *
         * @param derivedClass the derived class
         * @param baseClass the base class
         * @return The first found inheritance chain connecting the two
         *     classes, or `null` if the classes are not
         *     related by inheritance. The `baseClass` is not
         *     included in the returned inheritance chain, which
         *     will be empty if the two classes are the same.
         */
        private fun getInheritanceChain(
            derivedClass: PsiClassType,
            baseClass: PsiClassType?
        ): List<PsiClassType>? {
            if (derivedClass == baseClass) {
                return emptyList()
            }
            val chain = getInheritanceChain(derivedClass, baseClass, HashSet(), 0)
            chain?.reverse()
            return chain
        }

        private fun getInheritanceChain(
            derivedClass: PsiClassType,
            baseClass: PsiClassType?,
            visited: HashSet<PsiType>,
            depth: Int
        ): MutableList<PsiClassType>? {
            if (derivedClass == baseClass) {
                return ArrayList(depth)
            }

            for (type in derivedClass.superTypes) {
                if (visited.add(type) && type is PsiClassType) {
                    val chain = getInheritanceChain(type, baseClass, visited, depth + 1)
                    if (chain != null) {
                        chain.add(derivedClass)
                        return chain
                    }
                }
            }
            return null
        }

        private fun isSuppressed(
            context: JavaContext,
            api: Int,
            element: UElement,
            minSdk: Int
        ): Boolean {
            if (api <= minSdk) {
                return true
            }
            val target = getTargetApi(element)
            if (target != -1) {
                if (api <= target) {
                    return true
                }
            }

            val driver = context.driver
            return driver.isSuppressed(context, UNSUPPORTED, element) ||
                driver.isSuppressed(context, INLINED, element) ||
                isWithinVersionCheckConditional(context, element, api) ||
                isPrecededByVersionCheckExit(context, element, api)
        }

        fun getTargetApi(
            scope: UElement?,
            isApiLevelAnnotation: (String) -> Boolean = ::isTargetAnnotation
        ): Int {
            var current = scope
            while (current != null) {
                if (current is UAnnotated) {
                    val targetApi = getTargetApiForAnnotated(current, isApiLevelAnnotation)
                    if (targetApi != -1) {
                        return targetApi
                    }
                }
                if (current is UFile) {
                    break
                }
                current = current.uastParent
            }

            return -1
        }

        fun getApiLevel(context: JavaContext, annotation: UAnnotation): Int {
            var api = getLongAttribute(context, annotation, "version", -1).toInt()
            if (api == SdkVersionInfo.CUR_DEVELOPMENT) {
                val version = context.project.buildTarget?.version
                if (version != null && version.isPreview) {
                    return version.featureLevel
                }
                // Special value defined in the Android framework to indicate current development
                // version. This is different from the tools where we use current stable + 1 since
                // that's the anticipated version.
                @Suppress("KotlinConstantConditions")
                api = if (SdkVersionInfo.HIGHEST_KNOWN_API > SdkVersionInfo.HIGHEST_KNOWN_STABLE_API) {
                    SdkVersionInfo.HIGHEST_KNOWN_API
                } else {
                    SdkVersionInfo.HIGHEST_KNOWN_API + 1
                }

                // Try to match it up by codename
                val value = annotation.findDeclaredAttributeValue(ATTR_VALUE)
                    ?: annotation.findDeclaredAttributeValue("api")
                if (value is PsiReferenceExpression) {
                    val name = value.referenceName
                    if (name?.length == 1) {
                        api = max(api, SdkVersionInfo.getApiByBuildCode(name, true))
                    }
                }
            }
            return api
        }

        /**
         * Returns the API level for the given AST node if specified
         * with an `@TargetApi` annotation.
         *
         * @param annotated the annotated element to check
         * @return the target API level, or -1 if not specified
         */
        private fun getTargetApiForAnnotated(
            annotated: UAnnotated?,
            isApiLevelAnnotation: (String) -> Boolean
        ): Int {
            if (annotated == null) {
                return -1
            }

            //noinspection AndroidLintExternalAnnotations
            for (annotation in annotated.uAnnotations) {
                val target = getTargetApiForAnnotation(annotation, isApiLevelAnnotation)
                if (target != -1) {
                    return target
                }
            }

            return -1
        }
    }
}

/**
 * Attempts to find the [PsiMethod] for the operator overload of this
 * array access expression.
 *
 * Temporary workaround for
 * https://youtrack.jetbrains.com/issue/KTIJ-18765
 */
// TODO(kotlin-uast-cleanup): remove this when a fix for https://youtrack.jetbrains.com/issue/KTIJ-18765 arrives.
private fun UArrayAccessExpression.resolveOperator(skipOverloadedSetter: Boolean = true): PsiMethod? {
    if (skipOverloadedSetter && uastParent is UBinaryExpression) {
        val uastParentCapture = uastParent as UBinaryExpression
        if (uastParentCapture.leftOperand == this && uastParentCapture.operator == UastBinaryOperator.ASSIGN) {
            return null
        }
    }

    val receiver = this.receiver
    // for normal arrays this is typically PsiArrayType; a PsiClassType
    // is a sign that it's an operator overload
    if (receiver.getExpressionType() !is PsiClassType) return null

    // No UAST accessor method to find the corresponding get/set methods; see
    // https://youtrack.jetbrains.com/issue/KTIJ-18765
    // Instead we'll search ourselves.

    // First try Kotlin resolving service (base version, not FE1.0 variant)
    val ktElement = sourcePsi as? KtElement ?: return null
    val baseService = ApplicationManager.getApplication().getService(BaseKotlinUastResolveProviderService::class.java)
        ?: return null
    return baseService.resolveCall(ktElement)
}

// TODO(kotlin-uast-cleanup): remove this when a fix for https://youtrack.jetbrains.com/issue/KTIJ-17726 arrives.
// Excerpted/modified from IJ 189b439abec02e39db28fbfa12092b6ca014040a
private fun UBinaryExpression.resolveOperatorWorkaround(): PsiMethod? {
    // Try the existing logic first to resolve binary operator in general
    resolveOperator()?.let { return it }
    return when (operator) {
        UastBinaryOperator.ASSIGN -> {
            // Try to resolve overloaded indexed setter
            (leftOperand as? UArrayAccessExpression)?.resolveOperator(skipOverloadedSetter = false)
        }
        else -> null
    }
}
