package wdl.transforms.base.ast2wdlom

import common.Checked
import wdl.model.draft3.elements.ParameterMetaSectionElement
import wom.callable.MetaKvPair

object AstToParameterMetaSectionElement {
  def convert(ast: GenericAst): Checked[ParameterMetaSectionElement] =  {
    ast.getAttributeAsVector[MetaKvPair]("map") map { attributes =>
      val asMap = attributes.map(kv => kv.key -> kv.value).toMap
      ParameterMetaSectionElement(asMap)
    }
  }
}
