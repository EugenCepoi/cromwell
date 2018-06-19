package wdl.transforms.base.ast2wdlom

import common.Checked
import wdl.model.draft3.elements.{InputDeclarationElement, InputsSectionElement}

object AstToInputsSectionElement {

  def convert(a: GenericAst): Checked[InputsSectionElement] = {

    a.getAttributeAsVector[InputDeclarationElement]("inputs") map { declarations =>
      InputsSectionElement(declarations)
    }

  }
}
