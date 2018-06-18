package wdl.transforms.base.ast2wdlom

import cats.syntax.apply._
import cats.syntax.either._
import common.validation.ErrorOr.ErrorOr
import wdl.model.draft3.elements.ExpressionElement
import wdl.model.draft3.elements.ExpressionElement.KvPair

object AstNodeToKvPair {
  def convert(astNode: GenericAstNode): ErrorOr[KvPair] = astNode match {
    case a: GenericAst if a.getName == "ObjectKV" || a.getName == "MapLiteralKv" || a.getName == "RuntimeAttribute" =>
      val keyValidation: ErrorOr[String] = a.getAttributeAs[String]("key").toValidated
      val valueValidation: ErrorOr[ExpressionElement] = a.getAttributeAs[ExpressionElement]("value").toValidated

      (keyValidation, valueValidation) mapN { (key, value) => KvPair(key, value) }
  }
}
