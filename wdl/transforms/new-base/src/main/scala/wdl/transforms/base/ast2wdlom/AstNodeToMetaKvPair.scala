package wdl.transforms.base.ast2wdlom

import cats.syntax.validated._
import cats.syntax.apply._
import cats.syntax.either._
import common.validation.ErrorOr._
import common.validation.ErrorOr.ErrorOr
import wom.callable.{MetaKvPair, MetaValueElement}

object AstNodeToMetaKvPair {
  def convert(astNode: GenericAstNode): ErrorOr[MetaKvPair] = (astNode match {
    case a: GenericAst if a.getName == "MetaKvPair" =>
      val keyValidation: ErrorOr[String] = a.getAttributeAs[String]("key").toValidated
      val valueValidation: ErrorOr[MetaValueElement] = a.getAttributeAs[MetaValueElement]("value").toValidated

      (keyValidation, valueValidation) mapN { (key, value) => MetaKvPair(key, value) }
    case other => s"Expected Ast of type 'MetaKvPair' but got $other".invalidNel
  }).contextualizeErrors("convert AstNodeToMetaKvPair")
}
