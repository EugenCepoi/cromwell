package wdl.transforms.base.ast2wdlom

import cats.syntax.apply._
import cats.syntax.either._
import common.validation.ErrorOr.ErrorOr
import wdl.draft3.parser.WdlParser.{Ast, Terminal}
import wdl.draft3.transforms.ast2wdlom.EnhancedDraft3Ast._
import wdl.model.draft3.elements._

object AstToScatterElement {
  def convert(ast: GenericAst): ErrorOr[ScatterElement] = {

    val scatterVariableValidation: ErrorOr[GenericTerminal] = ast.getAttributeAs[GenericTerminal]("item").toValidated

    val scatterCollectionExpressionValidation: ErrorOr[ExpressionElement] = ast.getAttributeAs[ExpressionElement]("collection").toValidated
    val bodyValidation: ErrorOr[Vector[WorkflowGraphElement]] = ast.getAttributeAsVector[WorkflowGraphElement]("body").toValidated

    (scatterVariableValidation, scatterCollectionExpressionValidation, bodyValidation) mapN { (variable, collection, body) =>
      val scatterName = s"ScatterAt${variable.getLine}_${variable.getColumn}"
      ScatterElement(scatterName, collection, variable.getSourceString, body)
    }
  }
}
