package wdl.transforms.base.ast2wdlom

import cats.syntax.either._
import cats.syntax.validated._
import common.Checked
import common.validation.Checked._
import common.validation.ErrorOr.ErrorOr
import wdl.model.draft3.elements._
import wom.types._

object AstNodeToTypeElement {
  def convert(astNode: GenericAstNode): ErrorOr[TypeElement] = astNode match {
    case a: GenericAst if a.getName == "OptionalType" => convert(a.getAttribute("innerType")) map OptionalTypeElement
    case a: GenericAst if a.getName == "NonEmptyType" => convert(a.getAttribute("innerType")) map NonEmptyTypeElement
    case a: GenericAst if a.getName == "Type" => compoundType(a).toValidated
    case unknownAst: GenericAst => s"No rule available to create TypeElement from Ast: '${unknownAst.getName}'".invalidNel
    case t: GenericTerminal if typeMap.contains(t.getSourceString) => PrimitiveTypeElement(typeMap(t.getSourceString)).validNel
    case t: GenericTerminal if t.getSourceString == "Object" => ObjectTypeElement.validNel
    case t: GenericTerminal if t.getTerminalStr == "identifier" => TypeAliasElement(t.getSourceString).validNel
    case t: GenericTerminal => s"No rule available to create TypeElement from '${t.getTerminalStr}' Terminal with value '${t.getSourceString}'".invalidNel
    case _ => s"No rule available to create TypeElement from AstNode: ${astNode.getClass.getSimpleName}".invalidNel
  }

  private def compoundType(typeAst: GenericAst): Checked[TypeElement] = typeAst.getAttributeAs[String]("name") flatMap {
    case "Array" => typeAst.getAttributeAsVector[TypeElement]("subtype") flatMap {
      case one if one.size == 1 => ArrayTypeElement(one.head).validNelCheck
      case other => s"Arrays must have exactly one type parameter, but got ${other.size}".invalidNelCheck
    }
    case "Pair" => typeAst.getAttributeAsVector[TypeElement]("subtype") flatMap {
      case two if two.size == 2 => PairTypeElement(two.head, two(1)).validNelCheck
      case other => s"Pairs must have exactly two type parameters, but got ${other.size}".invalidNelCheck
    }
    case "Map" => typeAst.getAttributeAsVector[TypeElement]("subtype") flatMap {
      case two if two.size == 2 => MapTypeElement(two.head, two(1)).validNelCheck
      case other => s"Maps must have exactly two type parameters, but got ${other.size}".invalidNelCheck
    }
    case unknown => s"No rule available to create TypeElement from compound type: $unknown".invalidNelCheck
  }



  private val typeMap: Map[String, WomPrimitiveType] = Map(
    "Int" -> WomIntegerType,
    "String" -> WomStringType,
    "Float" -> WomFloatType,
    "Boolean" -> WomBooleanType,
    "File" -> WomSingleFileType
  )



}
