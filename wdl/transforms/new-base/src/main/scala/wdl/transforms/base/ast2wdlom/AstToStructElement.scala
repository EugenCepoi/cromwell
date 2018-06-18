package wdl.transforms.base.ast2wdlom

import cats.syntax.apply._
import common.transforms.CheckedAtoB
import common.validation.ErrorOr.ErrorOr
import wdl.model.draft3.elements.{StructElement, StructEntryElement, TypeElement}

object AstToStructElement {
  def convert(a: Ast): ErrorOr[StructElement] = {
    implicit val astNodeToStructEntry: CheckedAtoB[AstNode, StructEntryElement] = astNodeToAst andThen CheckedAtoB.fromErrorOr(convertAstToStructEntry)
    val nameValidation: ErrorOr[String] = a.getAttributeAs[String]("name").toValidated
    val entriesValidation: ErrorOr[Vector[StructEntryElement]] = a.getAttributeAsVector[StructEntryElement]("entries").toValidated

    (nameValidation, entriesValidation) mapN { (name, entries) => StructElement(name, entries) }
  }

  private def convertAstToStructEntry(a: Ast): ErrorOr[StructEntryElement] = {
    val name: ErrorOr[String] = a.getAttributeAs[String]("name").toValidated
    val typeElement: ErrorOr[TypeElement] = a.getAttributeAs[TypeElement]("type").toValidated

    (name, typeElement).mapN(StructEntryElement.apply)
  }
}
