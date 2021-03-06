/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.core.api

import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit

import org.joda.time.DateTime
import reactivemongo.bson._
import scrupal.core.api.Html.Template.BSONHandlerForHtmlTemplate
import scrupal.core.api.Layout.BSONHandlerForLayout
import scrupal.core.api.Type.BSONHandlerForType
import scrupal.core.types.BundleType

import scrupal.utils._
import spray.http.{MediaType, MediaTypes}

import scala.concurrent.duration.Duration
import scala.util.matching.Regex

object BSONHandlers {

  implicit val IdentifierConverter = (id: Identifier) ⇒ reactivemongo.bson.BSONString(id.name)
  implicit val BSONObjectIDConverter = (id: BSONObjectID) ⇒ id

  implicit val IdentifierBSONHandler = new BSONHandler[BSONString,Identifier] {
    override def write(t: Identifier): BSONString = BSONString(t.name)
    override def read(bson: BSONString): Identifier = Symbol(bson.value)
  }

  implicit val ShortBSONHandler = new BSONHandler[BSONInteger,Short] {
    override def write(t: Short): BSONInteger = BSONInteger(t.toInt)
    override def read(bson: BSONInteger): Short = bson.value.toShort
  }

  implicit val URLHandler = new BSONHandler[BSONString,URL] {
    override def write(t: URL): BSONString = BSONString(t.toString)
    override def read(bson: BSONString): URL = new URL(bson.value)
  }

  implicit val DateTimeBSONHandler = new BSONHandler[BSONDateTime,DateTime] {
    override def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)
    override def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }

  implicit val AlertKindHandler = new BSONHandler[BSONString,AlertKind.Kind] {
    override def write(t: AlertKind.Kind): BSONString = BSONString(t.toString)
    override def read(bson: BSONString): AlertKind.Kind = AlertKind.withName(bson.value)
  }

  implicit val IconsHandler = new BSONHandler[BSONString,Icons.Kind] {
    override def write(t: Icons.Kind): BSONString = BSONString(t.toString)
    override def read(bson: BSONString): Icons.Kind = Icons.withName(bson.value)
  }

  implicit val RegexHandler: BSONHandler[BSONString,Regex] = new BSONHandler[BSONString,Regex] {
    override def write(t: Regex): BSONString = BSONString(t.pattern.pattern())
    override def read(bson: BSONString): Regex = new Regex(bson.value)
  }

  implicit val DurationHandler: BSONHandler[BSONLong,Duration] = new BSONHandler[BSONLong,Duration] {
    override def write(t: Duration): BSONLong = BSONLong(t.toNanos)
    override def read(bson: BSONLong): Duration = Duration(bson.value, TimeUnit.NANOSECONDS)
  }

  implicit val TypeHandler : BSONHandler[BSONString,Type] = new BSONHandlerForType[Type]
  implicit val IndexableTypeHandler : BSONHandler[BSONString,IndexableType] = new BSONHandlerForType[IndexableType]
  implicit val StructuredTypeHandler : BSONHandler[BSONString,StructuredType] = new BSONHandlerForType[StructuredType]
  implicit val BundleTypeHandler: BSONHandler[BSONString,BundleType] = new BSONHandlerForType[BundleType]


  implicit val EntityHandler : BSONHandler[BSONString,Entity] = new BSONHandler[BSONString,Entity] {
    override def write(m: Entity): BSONString = BSONString(m.id.name)
    override def read(bson: BSONString): Entity = Entity(Symbol(bson.value)).get
  }

  implicit val ModuleHandler : BSONHandler[BSONString,Module] = new BSONHandler[BSONString,Module] {
    override def write(m: Module): BSONString = BSONString(m.id.name)
    override def read(bson: BSONString): Module = Module(Symbol(bson.value)).get
  }

  implicit val AppSeqHandler : BSONHandler[BSONArray,Seq[Application]] = new BSONHandler[BSONArray, Seq[Application]] {
    override def write(sa: Seq[Application]): BSONArray = BSONArray(sa map { app ⇒ app._id.name })
    override def read(array: BSONArray): Seq[Application] =
      Application.find(array.values.toSeq.map{ v ⇒ Symbol(v.asInstanceOf[BSONString].value) })
  }

  implicit val MediaTypeHandler : BSONHandler[BSONArray,MediaType] = new BSONHandler[BSONArray,MediaType] {
    override def write(mt: MediaType): BSONArray = BSONArray(mt.mainType, mt.subType)
    override def read(bson: BSONArray): MediaType = {
      val key = bson.values(0).asInstanceOf[BSONString].value → bson.values(1).asInstanceOf[BSONString].value
      MediaTypes.getForKey(key) match {
        case Some(mt) ⇒ mt
        case None ⇒ toss(s"MediaType `$key` is unknown.")
      }
    }
  }

  implicit val ArrayByteHandler : BSONHandler[BSONBinary,Array[Byte]] = new BSONHandler[BSONBinary,Array[Byte]] {
    override def write(bytes: Array[Byte]) : BSONBinary = BSONBinary(bytes, Subtype.GenericBinarySubtype)
    override def read(bson: BSONBinary) : Array[Byte] = bson.value.readArray(bson.value.size)
  }

  implicit val HtmlTemplateHandler = new BSONHandlerForHtmlTemplate[Html.Template]

  implicit val LayoutHandler : BSONHandler[BSONString,Layout] = new BSONHandlerForLayout[Layout]

  implicit val FileHandler : BSONHandler[BSONString,File] = new BSONHandler[BSONString,File] {
    override def write(f: File): BSONString = BSONString(f.getPath)
    override def read(bson: BSONString): File = new File(bson.value)
  }

  implicit val MapOfNamedHtml : BSONHandler[BSONDocument,Map[String,Html.Template]] =
    new BSONHandler[BSONDocument,Map[String,Html.Template]] {
      override def write(elements: Map[String,Html.Template]): BSONDocument = {
        BSONDocument( elements.map { case (name,html) ⇒ name → HtmlTemplateHandler.write(html) } )
      }
      override def read(doc: BSONDocument): Map[String,Html.Template] = {
        doc.elements.map { case(name, value) ⇒ name -> HtmlTemplateHandler.read(value.asInstanceOf[BSONString])  }
      }.toMap
    }

  implicit val MapOfNamedDocumentsHandler = new BSONHandler[BSONDocument,Map[String,BSONValue]] {
    override def write(elements: Map[String,BSONValue]): BSONDocument = BSONDocument(elements)
    override def read(doc: BSONDocument): Map[String,BSONValue] = doc.elements.toMap
  }

  implicit val MapOfNamedObjectIDHandler = new BSONHandler[BSONDocument,Map[String,BSONObjectID]] {
    override def write(elements: Map[String,BSONObjectID]): BSONDocument = MapOfNamedDocumentsHandler.write(elements)
    override def read(doc: BSONDocument): Map[String,BSONObjectID] =
      MapOfNamedDocumentsHandler.read(doc).map { case (k,v) ⇒ k -> v.asInstanceOf[BSONObjectID] }
  }

  implicit val EitherNodeRefOrNodeHandler = new BSONHandler[BSONDocument, Either[NodeRef,Node]] {
    def write(e: Either[NodeRef,Node]) : BSONDocument = {
      BSONDocument(
        "either" → (if (e.isLeft) BSONString("left") else BSONString("right")),
        "value"  → (
          if (e.isLeft)
            NodeRef.nodeRefHandler.write(e.left.get)
          else
            Node.NodeWriter.write(e.right.get) )
      )
    }
    def read(doc: BSONDocument) : Either[NodeRef,Node] = {
      val value = doc.get("value").get.asInstanceOf[BSONDocument]
      doc.getAs[String]("either").get match {
        case "left" ⇒  Left[NodeRef,Node](NodeRef.nodeRefHandler.read(value))
        case "right" ⇒ Right[NodeRef,Node](Node.NodeReader.read(value))
      }
    }
  }

  implicit val MapOfNamedNodeRefHandler = new BSONHandler[BSONDocument,Map[String,Either[NodeRef,Node]]]{
    override def write(elements: Map[String,Either[NodeRef,Node]]): BSONDocument = {
      BSONDocument( elements.map { case (key,e) ⇒ key → EitherNodeRefOrNodeHandler.write(e) } )
    }
    override def read(doc: BSONDocument): Map[String,Either[NodeRef,Node]] = {
      val elems = doc.elements.map { case (k,d) ⇒ k → EitherNodeRefOrNodeHandler.read(d.asInstanceOf[BSONDocument]) }
      elems.toMap
    }
  }

  implicit val EnablementHandler = new BSONHandler[BSONDocument,Enablement[_]] {
    override def write(e: Enablement[_]): BSONDocument =
      Reference.ReferenceHandler.write(new Reference[IdentifiedWithRegistry](e.id,e.registryName))
    override def read(doc: BSONDocument): Enablement[_] =
      Reference.ReferenceHandler.read(doc)().asInstanceOf[Enablement[_]]
  }

}
