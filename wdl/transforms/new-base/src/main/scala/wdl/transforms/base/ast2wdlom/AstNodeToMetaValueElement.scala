//package wdl.transforms.base.ast2wdlom
//
//import cats.syntax.either._
//import cats.syntax.validated._
//import common.transforms.CheckedAtoB
//import common.validation.ErrorOr.ErrorOr
//import common.validation.Validation._
//import wom.callable.{MetaKvPair, MetaValueElement}
//import wom.callable.MetaValueElement._
//
//import scala.util.{Failure, Try}
//
//object AstNodeToMetaValueElement {
//
//  def astNodeToMetaValueElement(implicit astNodeToMetaKvPair: CheckedAtoB[GenericAstNode, MetaKvPair]
//                               ): CheckedAtoB[GenericAstNode, MetaValueElement] = {
//    CheckedAtoB.fromErrorOr("convert AstNode to MetaValueElement")(convert)
//  }
//
//  private def convert(astNode: GenericAstNode): ErrorOr[MetaValueElement] = {
//    implicit val recursiveConversion = CheckedAtoB.fromErrorOr(convert _)
//
//    astNode match {
//      // This is a primitive type, one of {null, boolean, float, int, string}.
//      case t: GenericTerminal =>
//        (t.getTerminalStr, t.getSourceString) match {
//          case ("integer", i) => Try(MetaValueElementInteger(i.toInt)).toErrorOr
//          case ("float", f) => Try(MetaValueElementFloat(f.toDouble)).toErrorOr
//          case ("boolean", b) => Try(MetaValueElementBoolean(b.toBoolean)).toErrorOr
//          case ("string", s) => MetaValueElementString(s).validNel
//          case ("null", _) => MetaValueElementNull.validNel
//          case (name, other) => s"No conversion defined for Ast ($name, $other) to MetaValueElement".invalidNel
//        }
//
//      case a: GenericAst if a.getName == "MetaArray" =>
//        a.getAttributeAsVector[MetaValueElement]("values").toValidated.map(MetaValueElementArray)
//
//      case a: GenericAst if a.getName == "MetaObject" =>
//        (for {
//          mapKvs <- a.getAttributeAsVector[MetaKvPair]("map")
//          asMap = mapKvs.map(kv => kv.key -> kv.value).toMap
//        } yield MetaValueElementObject(asMap)).toValidated
//
//      case other =>
//        Failure(new Exception(s"No conversion defined for AstNode $other to MetaValueElement")).toErrorOr
//    }
//  }
//
//  private def convertKvPair(astNode: GenericAstNode): CheckedAtoB[GenericAstNode, MetaKvPair] = CheckedAtoB.fromErrorOr("convert AstNodeToMetaKvPair") {
//    case a: GenericAst if a.getName == "MetaKvPair" =>
//      val keyValidation: ErrorOr[String] = a.getAttributeAs[String]("key").toValidated
//      val valueValidation: ErrorOr[MetaValueElement] = a.getAttributeAs[MetaValueElement]("value").toValidated
//
//      (keyValidation, valueValidation) mapN { (key, value) => MetaKvPair(key, value) }
//    case other => s"Expected Ast of type 'MetaKvPair' but got $other".invalidNel
//  }
//}
