package wdl.transforms.base.ast2wdlom

import common.Checked
import wdl.model.draft3.elements.{DeclarationContent, OutputDeclarationElement, OutputsSectionElement}

object AstToOutputsSectionElement {
  def convert(a: GenericAst): Checked[OutputsSectionElement] =
      a.getAttributeAsVector[DeclarationContent]("outputs").map(_.map(OutputDeclarationElement.fromContent)).map(OutputsSectionElement)
}
