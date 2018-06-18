package wdl.transforms.base.ast2wdlom

import cats.instances.vector._
import cats.syntax.either._
import cats.syntax.traverse._

import common.validation.Validation._
import common.validation.Checked._
import common.Checked
import common.transforms.CheckedAtoB

trait GenericAstNode {
  def astListAsVector: Checked[Vector[GenericAstNode]] = this match {
    case list: GenericAstList => list.astNodeList.toVector.validNelCheck
    case _ => s"Invalid target for astListAsVector: ${getClass.getSimpleName}".invalidNelCheck
  }
}

trait GenericAst extends GenericAstNode {
  def getAttribute(attr: String): GenericAstNode
  def getAttributes: Map[String, GenericAstNode]
  def getName: String

  private def getAttributeAsAstNodeVector(attr: String): Checked[Vector[GenericAstNode]] = for {
    attributeNode <- Option(getAttribute(attr)).toChecked(s"No attribute '$attr' found on Ast of type $getName. Did you mean: ${getAttributes.keys.mkString(", ")}")
    asVector <- attributeNode.astListAsVector
  } yield asVector

  /**
    * Will get an attribute on this Ast as an AstNode and then convert that into a single element of
    * the required type.
    */
  def getAttributeAs[A](attr: String)(implicit toA: CheckedAtoB[GenericAstNode, A]): Checked[A] = {
    val attribute = Option(getAttribute(attr))
    attribute.map(toA.run).getOrElse(s"No attribute '$attr' found on Ast '$getName'. Did you mean: ${getAttributes.keys.mkString(", ")}".invalidNelCheck)
  }

  /**
    * Will get an attribute on this Ast as an AstList and then convert that into a vector of Ast
    * @param attr The attribute to read from this Ast
    */
  def getAttributeAsVector[A](attr: String)(implicit toA: CheckedAtoB[GenericAstNode, A]): Checked[Vector[A]] = {
    for {
      asVector <- getAttributeAsAstNodeVector(attr)
      // This toValidated/toEither dance is necessary to
      // (1) collect all errors from the traverse as an ErrorOr, then
      // (2) convert back into a Checked for the flatMap
      result <- asVector.traverse(item => toA.run(item).toValidated).toEither
    } yield result
  }

  /**
    * Gets an attribute on this Ast as an Optional Ast, returns an empty Option if the attribute is empty.
    */
  def getAttributeAsOptional[A](attr: String)(implicit toA: CheckedAtoB[GenericAstNode, A]): Checked[Option[A]] =  {
    Option(getAttribute(attr)) match {
      case None => None.validNelCheck
      case Some(attribute) => toA.run(attribute).map(Option.apply)
    }
  }
}

trait GenericAstList extends GenericAstNode {
  def astNodeList: Seq[GenericAstNode]
}

trait GenericTerminal extends GenericAstNode {
  def getSourceString: String
  def getTerminalStr: String
}
