/*
 * Copyright (C) 2023 Square, Inc.
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
package app.cash.redwood.yoga

public expect class Node {
  // Inputs
  public val children: MutableList<Node>

  public var flexDirection: FlexDirection
  public var justifyContent: JustifyContent
  public var alignItems: AlignItems
  public var alignSelf: AlignSelf

  public var flexGrow: Float
  public var flexShrink: Float

  public var marginStart: Float
  public var marginEnd: Float
  public var marginTop: Float
  public var marginBottom: Float

  public var measureCallback: MeasureCallback
  public var dirty: Boolean

  // Outputs
  public val left: Float
  public val top: Float
  public val width: Float
  public val height: Float
}
