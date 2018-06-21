package wdl.transforms.base.ast2wdlom

import cats.syntax.validated._
import cats.syntax.apply._
import cats.syntax.either._
import common.transforms.CheckedAtoB
import common.validation.ErrorOr.ErrorOr
import wom.callable.{MetaKvPair, MetaValueElement}

object AstNodeToMetaKvPair {
  def astNodeToKvPair(implicit astNodeToMetaKvPair: CheckedAtoB[GenericAstNode, MetaValueElement]): CheckedAtoB[GenericAstNode, MetaKvPair] = CheckedAtoB.fromErrorOr("convert AstNodeToMetaKvPair") {
    case a: GenericAst if a.getName == "MetaKvPair" =>
      val keyValidation: ErrorOr[String] = a.getAttributeAs[String]("key").toValidated
      val valueValidation: ErrorOr[MetaValueElement] = a.getAttributeAs[MetaValueElement]("value").toValidated

      (keyValidation, valueValidation) mapN { (key, value) => MetaKvPair(key, value) }
    case other => s"Expected Ast of type 'MetaKvPair' but got $other".invalidNel
  }
}
