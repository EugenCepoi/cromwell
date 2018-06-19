package wdl.transforms.base.ast2wdlom

import common.transforms.CheckedAtoB
import common.validation.Checked._
import wdl.model.draft3.elements._

object AstToWorkflowGraphNodeElement {
  def astToWorkflowGraphNodeElement(astNodeToDeclarationContent: CheckedAtoB[GenericAstNode, DeclarationContent],
                                    astNodeToCallElement: CheckedAtoB[GenericAstNode, CallElement],
                                    astNodeToScatterElement: CheckedAtoB[GenericAstNode, ScatterElement],
                                    astNodeToIfElement: CheckedAtoB[GenericAstNode, IfElement]
                                   ): CheckedAtoB[GenericAst, WorkflowGraphElement] = CheckedAtoB.fromCheck { a: GenericAst => a.getName match {
    case "Declaration" => astNodeToDeclarationContent(a).map(IntermediateValueDeclarationElement.fromContent)
    case "Call" => astNodeToCallElement(a)
    case "Scatter" => astNodeToScatterElement(a)
    case "If" => astNodeToIfElement(a)
    case other => s"No conversion defined for Ast with name $other to WorkflowGraphElement".invalidNelCheck
  }}
}
