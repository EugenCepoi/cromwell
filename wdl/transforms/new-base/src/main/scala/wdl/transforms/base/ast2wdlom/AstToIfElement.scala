package wdl.transforms.base.ast2wdlom

import cats.syntax.apply._
import cats.syntax.either._
import common.validation.ErrorOr.ErrorOr
import wdl.model.draft3.elements._

object AstToIfElement {
  def convert(ast: GenericAst): ErrorOr[IfElement] = {

    val conditionCollectionExpressionValidation: ErrorOr[ExpressionElement] = ast.getAttributeAs[ExpressionElement]("expression").toValidated
    val bodyValidation: ErrorOr[Vector[WorkflowGraphElement]] = ast.getAttributeAsVector[WorkflowGraphElement]("body").toValidated

    (conditionCollectionExpressionValidation, bodyValidation) mapN { (condition, body) =>
      IfElement(condition, body)
    }
  }
}
