package wdl.transforms.base.ast2wdlom

import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.traverse._
import cats.instances.either._
import cats.instances.list._
import common.Checked
import common.transforms.CheckedAtoB
import common.validation.ErrorOr.ErrorOr
import wdl.model.draft3.elements.{ImportElement, LanguageElement}

object AstToImportElement {



  def convert(a: GenericAst): ErrorOr[ImportElement] = {
    val importPath: ErrorOr[String] = a.getAttributeAs[String]("uri").toValidated
    val alias: ErrorOr[Option[String]] = a.getAttributeAsOptional[String]("namespace").toValidated

    val aliasElementMaker: CheckedAtoB[GenericAstNode, ImportStructRenameElement] = astNodeToAst andThen CheckedAtoB.fromErrorOr(convertAliasElement _)
    val structRenames: ErrorOr[Vector[ImportStructRenameElement]] = a.getAttributeAsVector[ImportStructRenameElement]("aliases")(aliasElementMaker).toValidated
    val structRenameMap: ErrorOr[Map[String, String]] = structRenames.map(_.map(rename => rename.oldName -> rename.newName).toMap)

    val x: List[Checked[Int]] = ???

    val y: Checked[List[Int]] = x.sequence[Checked, Int]


    (importPath, alias, structRenameMap) mapN { ImportElement }
  }

  private def convertAliasElement(a: GenericAst): ErrorOr[ImportStructRenameElement] = {
    val oldName: ErrorOr[String] = a.getAttributeAs[String]("old_name").toValidated
    val newName: ErrorOr[String] = a.getAttributeAs[String]("new_name").toValidated

    (oldName, newName) mapN ImportStructRenameElement
  }

  private final case class ImportStructRenameElement(oldName: String, newName: String) extends LanguageElement
}
