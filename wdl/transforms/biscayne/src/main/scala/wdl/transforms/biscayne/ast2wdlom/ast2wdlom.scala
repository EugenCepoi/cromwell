package wdl.transforms.biscayne

import common.transforms.CheckedAtoB
import common.validation.Checked._
import wdl.biscayne.parser.WdlParser.{Ast, AstNode}
import wdl.transforms.base.ast2wdlom._
import wom.callable.{MetaKvPair, MetaValueElement}

package object ast2wdlom {

  val wrapAst: CheckedAtoB[Ast, GenericAst] = CheckedAtoB.fromCheck { a => BiscayneGenericAst(a).validNelCheck }
  val wrapAstNode: CheckedAtoB[AstNode, GenericAstNode] = CheckedAtoB.fromCheck { a => BiscayneGenericAstNode(a).validNelCheck }

  //  // meta sections
  implicit lazy val astNodeToMetaValueElement: CheckedAtoB[GenericAstNode, MetaValueElement] = AstNodeToMetaValueElement.astNodeToMetaValueElement
  implicit lazy val astNodeToMetaKvPair: CheckedAtoB[GenericAstNode, MetaKvPair] = AstNodeToMetaKvPair.astNodeToMetaKvPair
//  implicit val astNodeToMetaSectionElement: CheckedAtoB[AstNode, MetaSectionElement] = astNodeToAst andThen CheckedAtoB.fromCheck(AstToMetaSectionElement.convert _)
//  implicit val astNodeToParameterMetaSectionElement: CheckedAtoB[AstNode, ParameterMetaSectionElement] = astNodeToAst andThen CheckedAtoB.fromCheck(AstToParameterMetaSectionElement.convert _)

//  implicit val astToFileElement: CheckedAtoB[Ast, FileElement] = CheckedAtoB.fromErrorOr(AstToFileElement.convert)
//  implicit val astToFileBodyElement: CheckedAtoB[AstNode, FileBodyElement] = astNodeToAst andThen CheckedAtoB.fromCheck(AstToFileBodyElement.convert _)
//  implicit val astNodeToStructEntry: CheckedAtoB[AstNode, StructElement] = astNodeToAst andThen CheckedAtoB.fromErrorOr(AstToStructElement.convert _)
//  implicit val astNodeToImportElement: CheckedAtoB[AstNode, ImportElement] = astNodeToAst andThen CheckedAtoB.fromErrorOr(AstToImportElement.convert _)
//  implicit val astNodeToTaskDefinitionElement: CheckedAtoB[AstNode, TaskDefinitionElement] = astNodeToAst andThen CheckedAtoB.fromErrorOr(AstToTaskDefinitionElement.convert _)
//  implicit val astNodeToWorkflowDefinitionElement: CheckedAtoB[AstNode, WorkflowDefinitionElement] = astNodeToAst andThen CheckedAtoB.fromErrorOr(AstToWorkflowDefinitionElement.convert _)
//  implicit val astNodeToInputsSectionElement: CheckedAtoB[AstNode, InputsSectionElement] = astNodeToAst andThen CheckedAtoB.fromCheck(AstToInputsSectionElement.convert _)
//  implicit val astNodeToInputDeclarationElement: CheckedAtoB[AstNode, InputDeclarationElement] = astNodeToAst andThen CheckedAtoB.fromErrorOr(AstToInputDeclarationElement.convert _)
//  implicit val astNodeToTypeElement: CheckedAtoB[AstNode, TypeElement] = CheckedAtoB.fromErrorOr(AstNodeToTypeElement.convert)
//  implicit val astNodeToExpressionElement: CheckedAtoB[AstNode, ExpressionElement] = CheckedAtoB.fromErrorOr(AstNodeToExpressionElement.convert)
//  implicit val astNodeToKvPair: CheckedAtoB[AstNode, KvPair] = CheckedAtoB.fromErrorOr(AstNodeToKvPair.convert)
//  implicit val astNodeToTaskSectionElement: CheckedAtoB[AstNode, TaskSectionElement] = astNodeToAst andThen CheckedAtoB.fromCheck(AstToTaskSectionElement.convert _)
//  implicit val astNodeToWorkflowBodyElement: CheckedAtoB[AstNode, WorkflowBodyElement] = astNodeToAst andThen CheckedAtoB.fromCheck(AstToWorkflowBodyElement.convert _)
//  implicit val astNodeToOutputsSectionElement: CheckedAtoB[AstNode, OutputsSectionElement] = astNodeToAst andThen CheckedAtoB.fromCheck(AstToOutputsSectionElement.convert _)
//  implicit val astNodeToDeclarationContent: CheckedAtoB[AstNode, DeclarationContent] = astNodeToAst andThen CheckedAtoB.fromErrorOr(AstToDeclarationContent.convert _)
//  implicit val astNodeToRuntimeAttributesSectionElement: CheckedAtoB[AstNode, RuntimeAttributesSectionElement] = astNodeToAst andThen CheckedAtoB.fromCheck(AstToRuntimeAttributesSectionElement.convert _)
//  implicit val astNodeToCommandSectionElement: CheckedAtoB[AstNode, CommandSectionElement] = astNodeToAst andThen CheckedAtoB.fromCheck(AstToCommandSectionElement.convert _)
//  implicit val astNodeToCommandPartElement: CheckedAtoB[AstNode, CommandPartElement] = CheckedAtoB.fromErrorOr(AstNodeToCommandPartElement.convert)
//  implicit val astNodeToPlaceholderAttributeSet: CheckedAtoB[AstNode, PlaceholderAttributeSet] = astNodeToAstList andThen AstNodeToPlaceholderAttributeSet.attributeKvpConverter
//  implicit val astNodeToCallElement: CheckedAtoB[AstNode, CallElement] = astNodeToAst andThen CheckedAtoB.fromErrorOr(AstToCallElement.convert _)
//  implicit val astNodeToScatterElement: CheckedAtoB[AstNode, ScatterElement] = astNodeToAst andThen CheckedAtoB.fromErrorOr(AstToScatterElement.convert _)
//  implicit val astNodeToIfElement: CheckedAtoB[AstNode, IfElement] = astNodeToAst andThen CheckedAtoB.fromErrorOr(AstToIfElement.convert _)
//  implicit val astNodeToGraphElement: CheckedAtoB[AstNode, WorkflowGraphElement] = astNodeToAst andThen CheckedAtoB.fromCheck(AstToWorkflowGraphNodeElement.convert _)
//  implicit val fileToFileElement: CheckedAtoB[File, FileElement] = fileToAst andThen astToFileElement
//
//  implicit val astNodeToString: CheckedAtoB[AstNode, String] = CheckedAtoB.fromCheck { a: AstNode => a match {
//    case t: Terminal => t.getSourceString.validNelCheck
//    case a: Ast => s"Cannot convert Ast of type ${a.getName} into String. Did you want one of its attributes (${a.getAttributes.asScala.keys.mkString(", ")})?".invalidNelCheck
//    case other: AstNode => s"Cannot convert ${other.getClass.getSimpleName} into String".invalidNelCheck
//  }}
//
}
