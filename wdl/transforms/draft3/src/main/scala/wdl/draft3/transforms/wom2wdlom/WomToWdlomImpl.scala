package wdl.draft3.transforms.wom2wdlom

import cats.data.Validated.Valid
import wdl.model.draft3.elements._
import wom.executable.WomBundle
import common.collections.EnhancedCollections.EnhancedTraversableLike
import wdl.draft2.model.command.{ParameterCommandPart, StringCommandPart}
import wdl.model.draft3.elements.CommandPartElement.{PlaceholderCommandPartElement, StringCommandPartElement}
import shapeless.{Inl, Inr}
import wdl.draft2.model.WdlWomExpression
import wdl.draft3.transforms.wdlom2wom.expression.WdlomWomExpression
import wom.callable.Callable._
import wdl.model.draft3.elements.ExpressionElement.{ExpressionLiteralElement, StringLiteral}
import wom.callable.MetaValueElement.MetaValueElementString
import wom.RuntimeAttributes
import wom.callable.Callable.OutputDefinition
import wom.callable.{CallableTaskDefinition, WorkflowDefinition}
import wom.expression.{ValueAsAnExpression, WomExpression}
import wom.graph.CallNode.InputDefinitionPointer
import wom.graph.GraphNodePort.GraphNodeOutputPort
import wom.graph._
import wom.graph.expression._
import wom.types._

case object UnrepresentableException extends Exception("Value has no representation in the destination format (WDL)")

object WomToWdlomImpl {

  import WomToWdlom.ops._

  implicit val graphOutputNodeToWorkflowGraphElement: WomToWdlom[GraphOutputNode, WorkflowGraphElement] =
    new WomToWdlom[GraphOutputNode, WorkflowGraphElement] {
      override def toWdlom(a: GraphOutputNode): WorkflowGraphElement = a match {
        case a: PortBasedGraphOutputNode =>
          OutputDeclarationElement(
            a.womType.toWdlom,
            a.identifier.localName.value,
            StringLiteral(a.toString) // TODO: debug more
          )
        case a: ExpressionBasedGraphOutputNode =>
          OutputDeclarationElement(
            a.womType.toWdlom,
            a.identifier.localName.value,
            a.womExpression.toWdlom
          )
      }
    }

  implicit val womBundleToFileElement: WomToWdlom[WomBundle, FileElement] = new WomToWdlom[WomBundle, FileElement] {
    override def toWdlom(a: WomBundle): FileElement = {
      val tasks: Iterable[CallableTaskDefinition] = a.allCallables.values.filterByType[CallableTaskDefinition]
      val workflows: Iterable[WorkflowDefinition] = a.allCallables.values.filterByType[WorkflowDefinition]

      FileElement(
        Seq(), // TODO: imports
        Seq(), // Structs do not exist in draft-2
        workflows.map(_.toWdlom).toSeq,
        tasks.map(_.toWdlom).toSeq
      )
    }
  }

  implicit val mapToMetaSectionElement: WomToWdlom[Map[String, String], Option[MetaSectionElement]] =
    new WomToWdlom[Map[String, String], Option[MetaSectionElement]] {
      override def toWdlom(a: Map[String, String]): Option[MetaSectionElement] = {
        if (a.nonEmpty)
          Some(MetaSectionElement(a map { case (key, value) =>
            key -> MetaValueElementString(value) // draft-2: strings only
          }))
        else
          None
      }
    }

  implicit val mapToParameterMetaSectionElement: WomToWdlom[Map[String, String], Option[ParameterMetaSectionElement]] =
    new WomToWdlom[Map[String, String], Option[ParameterMetaSectionElement]] {
      override def toWdlom(a: Map[String, String]): Option[ParameterMetaSectionElement] = {
        if (a.nonEmpty)
          Some(ParameterMetaSectionElement(a map { case (key, value) =>
            key -> MetaValueElementString(value) // draft-2: strings only
          }))
        else
          None
      }
    }

  implicit val runtimeAttributesToRuntimeAttributesSectionElement: WomToWdlom[RuntimeAttributes, Option[RuntimeAttributesSectionElement]] =
    new WomToWdlom[RuntimeAttributes, Option[RuntimeAttributesSectionElement]] {
      override def toWdlom(a: RuntimeAttributes): Option[RuntimeAttributesSectionElement] = {
        def tupleToKvPair(tuple: (String, WomExpression)): ExpressionElement.KvPair =
          ExpressionElement.KvPair(tuple._1, tuple._2.toWdlom)

        val kvPairs = (a.attributes map tupleToKvPair).toVector

        if (kvPairs.nonEmpty)
          Some(RuntimeAttributesSectionElement(kvPairs))
        else
          None
      }
    }

  implicit val outputDefinitionToOutputDeclarationElement: WomToWdlom[OutputDefinition, OutputDeclarationElement] =
    new WomToWdlom[OutputDefinition, OutputDeclarationElement] {
      override def toWdlom(a: OutputDefinition): OutputDeclarationElement =
        OutputDeclarationElement(a.womType.toWdlom, a.name, a.expression.toWdlom)
    }

  implicit val inputDefinitionToInputDeclarationElement: WomToWdlom[InputDefinition, InputDeclarationElement] =
    new WomToWdlom[InputDefinition, InputDeclarationElement] {
      override def toWdlom(a: InputDefinition): InputDeclarationElement = a match {
        case a: RequiredInputDefinition => InputDeclarationElement(a.womType.toWdlom, a.localName.value, None)
        case a: InputDefinitionWithDefault => InputDeclarationElement(a.womType.toWdlom, a.localName.value, Some(a.default.toWdlom))
        case a: FixedInputDefinition => InputDeclarationElement(a.womType.toWdlom, a.localName.value, Some(a.default.toWdlom))
        case a: OptionalInputDefinition => InputDeclarationElement(a.womType.toWdlom, a.localName.value, None)
      }
    }

  implicit val callableTaskDefinitionToTaskDefinitionElement: WomToWdlom[CallableTaskDefinition, TaskDefinitionElement] =
    new WomToWdlom[CallableTaskDefinition, TaskDefinitionElement] {
      override def toWdlom(a: CallableTaskDefinition): TaskDefinitionElement = {
        val inputs = a.inputs.map(inputDefinitionToInputDeclarationElement.toWdlom)
        val outputs = a.outputs.map(outputDefinitionToOutputDeclarationElement.toWdlom)

        val commands = a.commandTemplateBuilder(Map()) match {
          case Valid(cmd) => cmd
          case _ => throw UnrepresentableException
        }

        val commandLine = CommandSectionLine(commands map {
          case s: StringCommandPart =>
            StringCommandPartElement(s.literal)
          case p: ParameterCommandPart =>
            val attrs = PlaceholderAttributeSet(
              defaultAttribute = p.attributes.get("default"),
              trueAttribute = p.attributes.get("true"),
              falseAttribute = p.attributes.get("false"),
              sepAttribute = p.attributes.get("sep")
            )

            PlaceholderCommandPartElement(ExpressionLiteralElement(p.expression.toWomString), attrs)
        })

        TaskDefinitionElement(
          a.name,
          if (inputs.nonEmpty) Some(InputsSectionElement(inputs)) else None,
          Seq(), // No such thing in draft-2
          if (outputs.nonEmpty) Some(OutputsSectionElement(outputs)) else None,
          CommandSectionElement(Seq(commandLine)),
          a.runtimeAttributes.toWdlom,
          mapToMetaSectionElement.toWdlom(a.meta),
          mapToParameterMetaSectionElement.toWdlom(a.parameterMeta)
        )
      }
    }

  implicit val workflowDefinitionToWorkflowDefinitionElement: WomToWdlom[WorkflowDefinition, WorkflowDefinitionElement] =
    new WomToWdlom[WorkflowDefinition, WorkflowDefinitionElement] {
      override def toWdlom(a: WorkflowDefinition): WorkflowDefinitionElement = {
        // This is a bit odd, so let's explain. "Real" inputs/outputs that are specified by the WDL's author
        // cannot have periods in them - period. So any input/output that has a period in it
        // is an artifact of WOMification and should be dropped
        val inputs = a.inputs.filter(!_.localName.value.contains(".")).map(inputDefinitionToInputDeclarationElement.toWdlom)
        val outputs = a.outputs.filter(!_.localName.value.contains(".")).map(outputDefinitionToOutputDeclarationElement.toWdlom)

        WorkflowDefinitionElement(
          a.name,
          if (inputs.nonEmpty) Some(InputsSectionElement(inputs)) else None,
          selectWdlomRepresentableNodes(a.graph.nodes).map(_.toWdlom),
          if (outputs.nonEmpty) Some(OutputsSectionElement(outputs)) else None,
          mapToMetaSectionElement.toWdlom(a.meta),
          mapToParameterMetaSectionElement.toWdlom(a.parameterMeta)
        )
      }
    }

  implicit val expressionNodeLikeToWorkflowGraphElement: WomToWdlom[ExpressionNodeLike, WorkflowGraphElement] =
    new WomToWdlom[ExpressionNodeLike, WorkflowGraphElement] {
      override def toWdlom(a: ExpressionNodeLike): WorkflowGraphElement = a match {
        case a: ExpressionNode =>
          IntermediateValueDeclarationElement(
            typeElement = a.womType.toWdlom,
            name = a.identifier.localName.value,
            expression = a.toWdlom)
        case _: ExpressionCallNode => ???
      }
    }

  // Select only nodes that have a Wdlom representation (i.e. were not synthesized during WOMification)
  // WOM has some explicit representations that are implicit in WDL; they are necessary for execution,
  // but do not make sense (or are illegal) in WDL source.
  private def selectWdlomRepresentableNodes(allNodes: Set[GraphNode]): Set[GraphNode] = {
    val expressions: Set[GraphNode] = allNodes.filterByType[ExposedExpressionNode]
    val scatters: Set[GraphNode] = allNodes.filterByType[ScatterNode]
    val calls: Set[GraphNode] = allNodes.filterByType[CallNode]
    val conditionals: Set[GraphNode] = allNodes.filterByType[ConditionalNode]

    expressions ++ scatters ++ calls ++ conditionals
  }


  implicit val graphNodeToWorkflowGraphElement: WomToWdlom[GraphNode, WorkflowGraphElement] = new WomToWdlom[GraphNode, WorkflowGraphElement] {
    override def toWdlom(a: GraphNode): WorkflowGraphElement = {
      a match {
        case a: CallNode =>
          a.toWdlom
        case a: ConditionalNode =>
          IfElement(
            conditionExpression = a.conditionExpression.toWdlom,
            graphElements = selectWdlomRepresentableNodes(a.innerGraph.nodes).toList.map(graphNodeToWorkflowGraphElement.toWdlom)
          )
        case a: ExpressionNodeLike =>
          a.toWdlom
        case a: GraphNodeWithSingleOutputPort =>
          a.toWdlom
        case a: GraphOutputNode =>
          a.toWdlom
        case a: ScatterNode =>
          // Only CWL has multi-variable scatters
          if (a.scatterVariableNodes.size != 1) throw UnrepresentableException // TODO: upgrade from exceptions to typed errors

          ScatterElement(
            scatterName = a.identifier.localName.value,
            scatterExpression = a.scatterCollectionExpressionNodes.head.toWdlom,
            scatterVariableName = a.inputPorts.toList.head.name,
            graphElements = selectWdlomRepresentableNodes(a.innerGraph.nodes).toList.map(graphNodeToWorkflowGraphElement.toWdlom)
          )
      }
    }
  }

  implicit val graphNodeWithSingleOutputPortToWorkflowGraphElement: WomToWdlom[GraphNodeWithSingleOutputPort, WorkflowGraphElement] =
    new WomToWdlom[GraphNodeWithSingleOutputPort, WorkflowGraphElement] {
      override def toWdlom(a: GraphNodeWithSingleOutputPort): WorkflowGraphElement = a match {
        case a: GraphInputNode =>
          InputDeclarationElement(
            a.womType.toWdlom,
            a.identifier.localName.value,
            None
          )
        case a: ExpressionNode =>
          IntermediateValueDeclarationElement(
            a.womType.toWdlom,
            a.identifier.localName.value,
            a.womExpression.toWdlom
          )
      }
    }

  implicit val womTypeToTypeElement: WomToWdlom[WomType, TypeElement] = new WomToWdlom[WomType, TypeElement] {
    override def toWdlom(a: WomType): TypeElement = a match {
      case a: WomArrayType =>
        if (a.guaranteedNonEmpty)
          NonEmptyTypeElement(ArrayTypeElement(womTypeToTypeElement.toWdlom(a.memberType)))
        else
          ArrayTypeElement(womTypeToTypeElement.toWdlom(a.memberType))
      case _: WomCoproductType => throw UnrepresentableException
      case _: WomFileType => PrimitiveTypeElement(WomSingleFileType)
      case a: WomMapType => MapTypeElement(womTypeToTypeElement.toWdlom(a.keyType), womTypeToTypeElement.toWdlom(a.valueType))
      case _: WomNothingType.type => throw UnrepresentableException
      case _: WomObjectType.type => ObjectTypeElement
      case a: WomOptionalType => a.toWdlom
      case a: WomPairType => PairTypeElement(womTypeToTypeElement.toWdlom(a.leftType), womTypeToTypeElement.toWdlom(a.rightType))
      case a: WomPrimitiveType => a.toWdlom
    }
  }

  implicit val womOptionalTypeToOptionalTypeElement: WomToWdlom[WomOptionalType, OptionalTypeElement] = new WomToWdlom[WomOptionalType, OptionalTypeElement] {
    override def toWdlom(a: WomOptionalType): OptionalTypeElement = OptionalTypeElement(womTypeToTypeElement.toWdlom(a.memberType))
  }

  implicit val womPrimitiveTypeToPrimitiveTypeElement: WomToWdlom[WomPrimitiveType, PrimitiveTypeElement] = new WomToWdlom[WomPrimitiveType, PrimitiveTypeElement] {
    override def toWdlom(a: WomPrimitiveType): PrimitiveTypeElement = PrimitiveTypeElement(a)
  }

  implicit val expressionNodeToExpressionElement: WomToWdlom[ExpressionNode, ExpressionElement] = new WomToWdlom[ExpressionNode, ExpressionElement] {
    override def toWdlom(a: ExpressionNode): ExpressionElement = a.womExpression.toWdlom
  }

  implicit val womExpressionToExpressionElement: WomToWdlom[WomExpression, ExpressionElement] = new WomToWdlom[WomExpression, ExpressionElement] {
    override def toWdlom(a: WomExpression): ExpressionElement = a match {
      case _: WdlomWomExpression => throw UnrepresentableException
      case a: WdlWomExpression => ExpressionLiteralElement(a.sourceString)
      case _: ValueAsAnExpression => throw UnrepresentableException
      case _: PlainAnonymousExpressionNode => throw UnrepresentableException
      case _: TaskCallInputExpressionNode => throw UnrepresentableException
      case _: ExposedExpressionNode => throw UnrepresentableException
    }
  }

  implicit val inputDefinitionPointerToExpressionElement: WomToWdlom[InputDefinitionPointer, Option[ExpressionElement]] =
    new WomToWdlom[InputDefinitionPointer, Option[ExpressionElement]] {
      override def toWdlom(a: InputDefinitionPointer): Option[ExpressionElement] = a match {
        // If the input definition is a node containing an expression, it's been declared explicitly
        case Inl(a: GraphNodeOutputPort) => a.graphNode match {
          case _: OptionalGraphInputNode =>
            None
          case _: OptionalGraphInputNodeWithDefault =>
            None
          case a: PlainAnonymousExpressionNode => Some(a.womExpression.toWdlom)
          case a: TaskCallInputExpressionNode => Some(a.womExpression.toWdlom)
          case _: RequiredGraphInputNode => None // TODO: questionable
        }
        // Input definitions that directly contain expressions are the result of accepting a default input defined by the callable
        case Inr(Inl(_: WdlWomExpression)) => None
        case Inr(_) =>
          throw UnrepresentableException
        case _ =>
          throw UnrepresentableException
      }
    }

  implicit val callNodeToCallElement: WomToWdlom[CallNode, CallElement] = new WomToWdlom[CallNode, CallElement] {
    override def toWdlom(call: CallNode): CallElement = {
      def tupleToKvPair(tuple: (InputDefinition, InputDefinitionPointer)): Option[ExpressionElement.KvPair] = {
        tuple._2.toWdlom match {
          case Some(value) => Some(ExpressionElement.KvPair(tuple._1.name, value))
          case _ => None
        }
      }

      val inputs = (call.inputDefinitionMappings flatMap tupleToKvPair).toVector

      val callableName = call.callable.name
      val callAlias = call.identifier.localName.value // If no alias, this is just the name; we evaluate for that below

      val maybeAlias = if (callableName != callAlias)
        Some(callAlias)
      else
        None

      CallElement(
        call.callable.name,
        maybeAlias,
        if (inputs.nonEmpty) Some(CallBodyElement(inputs)) else None
      )
    }
  }
}