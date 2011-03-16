package de.soutier.fixtures

import scalaj.collection.Imports._

import org.yaml.snakeyaml._

import org.apache.log4j.Logger

import com.webobjects.foundation.{ NSArray }
import com.webobjects.appserver.WOApplication
import com.webobjects.eocontrol.{ EOEnterpriseObject, EOEditingContext }

import er.extensions.foundation.ERXProperties
import er.extensions.eof.{ ERXEC, ERXEOControlUtilities }

sealed class Fixtures

object Fixtures {
  private lazy val logger = Logger.getLogger(classOf[Fixtures])

  def load(): Unit = load(ERXEC.newEditingContext)

  def load(ec: EOEditingContext) {
    val fileName = ERXProperties.stringForKeyWithDefault("EOFFixtures.fileName", "fixtures.yaml")
    logger.debug("Parsing file: " + fileName)

    val languages = ERXProperties.stringForKey("EOFFixtures.language") match {
      case language: String => new NSArray[String](language)
      case _ => null
    }

    val text = WOApplication.application.resourceManager.bytesForResourceNamed(fileName, "app", languages) match {
      case null => logger.warn("File " + fileName + " not found."); null
      case bytes => new String(bytes)
    }

    val entitiesMap = entityMapFromText(text).getOrElse(scala.collection.Map[String, Any]())
    val objectCache = scala.collection.mutable.Map[String, EOEnterpriseObject]()
    val entityExtractor = """([^(]+)\(([^)]+)?\)""".r

    for (key <- entitiesMap.keysIterator) {
      key match {
        case entityExtractor(entityName, id) => {
          logger.debug("Parsed: " + entityName + " with id: " + id)
          objectCache.get(key.toString) match {
            case Some(alreadyPresent) =>
            case None => {
              val insertedObject = insertEnterpriseObject(ec, entityName, parsedAttributes(entitiesMap.get(key)), objectCache)
              objectCache.put(id.toString, insertedObject)
            }
          }
          if (ERXProperties.booleanForKey("EOFFixtures.saveOnEachInsert"))
            ec.saveChanges
        }

        case unknownItem => println("Could not parse " + unknownItem)
      }
    }

    ec.saveChanges
  }

  private def entityMapFromText(text: String) = {
    // Currently only YAML supported
    val yaml = new Yaml()
    val yamlResult = yaml.load(text)
    yamlResult match {
      case map: java.util.Map[String, Any] => Some(map asScala)
      case _ => None
    }
  }

  private def parsedAttributes(parsedObject: Any) = {
    parsedObject match {
      case Some(x: java.util.Map[String, Any]) => x asScala
      case _ => Map[String, Any]()
    }
  }

  private def insertEnterpriseObject(ec: EOEditingContext, entityName: String, entityAttributes: scala.collection.Map[String, Any], relationships: scala.collection.Map[String, EOEnterpriseObject]) = {
    val insertedObject = ERXEOControlUtilities.createAndInsertObject(ec, entityName)

    for (attributeKey <- entityAttributes.keysIterator) {
      if (insertedObject.attributeKeys.contains(attributeKey)) {
        insertedObject.takeValueForKey(entityAttributes.get(attributeKey).get, attributeKey)
      } else if (insertedObject.toOneRelationshipKeys.contains(attributeKey)) {
        val relationshipObject = relationships.get(entityAttributes.get(attributeKey).get.toString).get
        insertedObject.addObjectToBothSidesOfRelationshipWithKey(relationshipObject, attributeKey)
      } else if (insertedObject.toManyRelationshipKeys().contains(attributeKey)) {
        for (obj <- entityAttributes.get(attributeKey).get.asInstanceOf[java.util.List[Any]])
          insertedObject.addObjectToBothSidesOfRelationshipWithKey(relationships.get(obj.toString).get, attributeKey)
      } else { // This can be (ab)used to allow calls to methods or properties that do not exist in the class description
        entityAttributes.get(attributeKey) match {
          case Some(x) if (x == null) => insertedObject.valueForKey(attributeKey)
          case Some(x) => insertedObject.takeValueForKey(x, attributeKey)
          case None =>
        }
      }

    }

    insertedObject
  }

  def dump(objects: NSArray[_ <: EOEnterpriseObject]) = {
    if (!ERXProperties.booleanForKeyWithDefault("EOFFixtures.allowDump", false))
      throw new IllegalAccessException()

    val yaml = new Yaml()

    // TODO Needs own mapper so it finds WO-like getters
    //yaml.dumpAll(objects)
    "Not yet implemented"
  }
}
