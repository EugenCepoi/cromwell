package wdl.transforms.base.ast2wdlom

import common.Checked
import wdl.model.draft3.elements.ExpressionElement.KvPair
import wdl.model.draft3.elements.RuntimeAttributesSectionElement

object AstToRuntimeAttributesSectionElement {
  def convert(ast: GenericAst): Checked[RuntimeAttributesSectionElement] =  {
    ast.getAttributeAsVector[KvPair]("map") map { attributes =>
      RuntimeAttributesSectionElement(attributes)
    }
  }
}
