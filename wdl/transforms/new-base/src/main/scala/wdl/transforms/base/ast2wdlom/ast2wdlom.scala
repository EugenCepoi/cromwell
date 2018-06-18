package wdl.transforms.base

import better.files.File
import cats.instances.either._
import common.transforms.CheckedAtoB
import common.validation.Checked._
import wdl.model.draft3.elements.ExpressionElement.KvPair
import wdl.model.draft3.elements._
import wom.callable.{MetaKvPair, MetaValueElement}

package object ast2wdlom {

  implicit val astNodeToAst: CheckedAtoB[GenericAstNode, GenericAst] = CheckedAtoB.fromCheck {
    case ast: GenericAst => ast.validNelCheck
    case other => s"Cannot convert from AstNode type '${other.getClass.getSimpleName}' into Ast".invalidNelCheck
  }

  implicit val astNodeToAstList: CheckedAtoB[GenericAstNode, GenericAstList] = CheckedAtoB.fromCheck {
    case astList: GenericAstList => astList.validNelCheck
    case other => s"Cannot convert from AstNode type '${other.getClass.getSimpleName}' into AstList".invalidNelCheck
  }

  implicit val astNodeToTerminal: CheckedAtoB[GenericAstNode, GenericTerminal] = CheckedAtoB.fromCheck {
    case t: GenericTerminal => t.validNelCheck
    case other => s"Cannot convert from AstNode type '${other.getClass.getSimpleName}' into Terminal".invalidNelCheck
  }

  implicit val astNodeToString: CheckedAtoB[GenericAstNode, String] = CheckedAtoB.fromCheck { a: GenericAstNode => a match {
    case t: GenericTerminal => t.getSourceString.validNelCheck
    case a: GenericAst => s"Cannot convert Ast of type ${a.getName} into String. Did you want one of its attributes (${a.getAttributes.asScala.keys.mkString(", ")})?".invalidNelCheck
    case other: GenericAstNode => s"Cannot convert ${other.getClass.getSimpleName} into String".invalidNelCheck
  }}

}
