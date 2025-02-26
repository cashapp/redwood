/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.tooling.optimization.compose

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.WARNING
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrContainerExpression
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrInlinedFunctionBlock
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class ComposeOptimizationIrElementTransformer(
  private val messageCollector: MessageCollector,
  private val apis: ComposeRuntimeApi,
) : IrElementTransformerVoid() {
  private fun MutableList<IrStatement>.removeUnwanted() {
    removeIf { statement ->
      if (statement is IrWhen) {
        for (branch in statement.branches) {
          val condition = branch.condition
          if (condition is IrCall && condition.symbol == apis.isTraceInProgress) {
            // Remove calls like
            //
            //   if (isTraceInProgress()) {
            //     traceEventStart(1003431207, $changed, -1, 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>.<anonymous>.<anonymous>.<anonymous> (EmojiSearch.kt:171)');
            //   }
            //
            // and
            //
            //   if (isTraceInProgress()) {
            //     traceEventEnd();
            //   }
            messageCollector.report(WARNING, "Removing isTraceInProgress block $statement")
            return@removeIf true
          }
        }
      }
      false
    }
  }

  override fun visitContainerExpression(expression: IrContainerExpression): IrExpression {
    expression.statements.removeUnwanted()
    return super.visitContainerExpression(expression)
  }

  override fun visitExpressionBody(body: IrExpressionBody): IrBody {
    body.expression.acceptChildrenVoid(object : IrElementVisitorVoid {
      override fun visitCall(expression: IrCall) {
        messageCollector.report(WARNING, "222222222 ${expression.symbol}")
        super.visitCall(expression)
      }
    })
    return super.visitExpressionBody(body)
  }

  override fun visitBlockBody(body: IrBlockBody): IrBody {
    body.statements.removeUnwanted()
    return super.visitBlockBody(body)
  }

  override fun visitCall(expression: IrCall): IrExpression {
    messageCollector.report(WARNING, "supsupsup ${expression.symbol}")
    if (expression.symbol == apis.sourceInformationMarkerStart) {
    }
    return super.visitCall(expression)
  }

  override fun visitInlinedFunctionBlock(inlinedBlock: IrInlinedFunctionBlock): IrExpression {
    inlinedBlock.statements.removeUnwanted()
    return super.visitInlinedFunctionBlock(inlinedBlock)
  }
}
