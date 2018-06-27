package wdl.transforms.biscayne

import cats.instances.either._
import common.Checked
import common.transforms.CheckedAtoB
import common.assertion.ErrorOrAssertions._
import org.scalatest.{FlatSpec, Matchers}
import wdl.biscayne.parser.WdlParser
import wdl.model.draft3.elements.ExpressionElement
import wdl.model.draft3.elements.ExpressionElement.{Add, PrimitiveLiteralExpressionElement}
import wdl.transforms.base.ast2wdlom.GenericAstNode
import wdl.transforms.biscayne.parsing.WdlBiscayneSyntaxErrorFormatter
import wdl.transforms.biscayne.ast2wdlom._
import wom.values.WomInteger

import scala.collection.JavaConverters._

class Ast2WdlomSpec extends FlatSpec with Matchers {

  def fromString[A](expression: String)(implicit converter: CheckedAtoB[GenericAstNode, A]): Checked[A] = {
    val parser = new WdlParser()
    val tokens = parser.lex(expression, "string")
    val terminalMap = (tokens.asScala.toVector map {(_, expression)}).toMap
    val parseTree = parser.parse_e(tokens, WdlBiscayneSyntaxErrorFormatter(terminalMap))
    (wrapAstNode andThen converter).run(parseTree.toAst)
  }

  it should "parse a simple expression" in {
    val str = "3 + 3"
    val expr = fromString[ExpressionElement](str)
    expr shouldBeValid Add(PrimitiveLiteralExpressionElement(WomInteger(3)), PrimitiveLiteralExpressionElement(WomInteger(3)))
  }

}
