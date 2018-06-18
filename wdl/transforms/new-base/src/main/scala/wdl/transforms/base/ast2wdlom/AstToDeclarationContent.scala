package wdl.transforms.base.ast2wdlom

import common.validation.ErrorOr.ErrorOr
import wdl.model.draft3.elements.{DeclarationContent, ExpressionElement, TypeElement}
import cats.syntax.apply._
import cats.syntax.either._

object AstToDeclarationContent {
  def convert(a: GenericAst): ErrorOr[DeclarationContent] = {

    val nameValidation: ErrorOr[String] = astNodeToString(a.getAttribute("name")).toValidated
    val outputTypeValidation: ErrorOr[TypeElement] = a.getAttributeAs[TypeElement]("type").toValidated
    val expressionElementValidation: ErrorOr[ExpressionElement] = a.getAttributeAs[ExpressionElement]("expression").toValidated

    (nameValidation, outputTypeValidation, expressionElementValidation) mapN {
      (name, outputType, expression) => DeclarationContent(outputType, name, expression)
    }
  }
}
