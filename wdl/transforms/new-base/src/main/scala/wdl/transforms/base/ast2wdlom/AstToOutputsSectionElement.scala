package wdl.transforms.base.ast2wdlom

import common.transforms.CheckedAtoB
import wdl.model.draft3.elements.{DeclarationContent, OutputDeclarationElement, OutputsSectionElement}

object AstToOutputsSectionElement {
  def astToMetaSectionElement(implicit astNodeToMetaKvPair: CheckedAtoB[GenericAstNode, DeclarationContent]
                             ): CheckedAtoB[GenericAst, OutputsSectionElement] = CheckedAtoB.fromCheck("convert AST to outputs section") { a =>

    a.getAttributeAsVector[DeclarationContent]("outputs").map(_.map(OutputDeclarationElement.fromContent)).map(OutputsSectionElement)
  }
}
