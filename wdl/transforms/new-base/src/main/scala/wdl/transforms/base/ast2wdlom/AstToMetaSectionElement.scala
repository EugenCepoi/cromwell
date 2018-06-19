package wdl.transforms.base.ast2wdlom

import common.Checked
import wdl.model.draft3.elements.MetaSectionElement
import wom.callable.MetaKvPair

object AstToMetaSectionElement {
  def convert(ast: GenericAst): Checked[MetaSectionElement] =  {
    ast.getAttributeAsVector[MetaKvPair]("map") map { attributes =>
      val asMap = attributes.map(kv => kv.key -> kv.value).toMap
      MetaSectionElement(asMap)
    }
  }
}
