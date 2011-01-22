package de.soutier.fixtures


import scalaj.collection.Imports._

import org.yaml.snakeyaml._

import org.apache.log4j.Logger

import com.webobjects.foundation.{NSDictionary, NSArray, NSMutableArray}
import com.webobjects.eocontrol.{EOEnterpriseObject, EOEditingContext}

import er.extensions.foundation.ERXProperties
import er.extensions.eof.{ERXEC, ERXEOControlUtilities}


sealed class Fixtures

object Fixtures {
	private lazy val logger = Logger.getLogger(classOf[Fixtures])
	
	def load(): Unit = load(ERXEC.newEditingContext)
	
	def load(ec: EOEditingContext) {
		logger.debug("Parsing file: " + ERXProperties.stringForKeyWithDefault("EOFFixtures.fileName", "fixtures.yaml"))
		val text =
			try {
				scala.io.Source.fromFile("Resources/" + ERXProperties.stringForKeyWithDefault("EOFFixtures.fileName", "fixtures.yaml")) mkString
			} catch {
				case fnfe: java.io.FileNotFoundException => logger.warn("Fixtures file not found."); ""
				case _ => ""
			}
		
		// TODO Localization via WOResourceManager
		//val content = new String(this.resourceManager().bytesForResourceNamed(ERXProperties.stringForKeyWithDefault("EOFFixtures.fileName", "fixtures.yaml"), "app", languages));
		
		val yaml = new Yaml()
		val yamlResult = yaml.load(text)
		val yamlMap = 
			if (yamlResult.isInstanceOf[java.util.Map[String, Any]])
				yamlResult.asInstanceOf[java.util.Map[String, Any]] asScala
			else Map[String, Any]()
		
		val objectCache = scala.collection.mutable.Map[String, EOEnterpriseObject]()
		val entityExtractor = """([^(]+)\(([^)]+)\)""".r

		for (key <- yamlMap.keysIterator) {
			key match {
				case entityExtractor(entityName, id) => {
					logger.debug("Parsed: " + entityName + " with id: " + id)
					
					objectCache.get(key.toString) match {
						case Some(alreadyPresent) =>
						case None => {
							val insertedObject = ERXEOControlUtilities.createAndInsertObject(ec, entityName)
							val attributes = yamlMap.get(key).get.asInstanceOf[java.util.Map[String, Any]] asScala
							
							for (attributeKey <- attributes.keysIterator) {
								if (insertedObject.attributeKeys.contains(attributeKey)) {
									insertedObject.takeValueForKey(attributes.get(attributeKey).get, attributeKey)
								} else if (insertedObject.toOneRelationshipKeys.contains(attributeKey)) {
									val relationshipObject = objectCache.get(attributes.get(attributeKey).get.toString).get
									insertedObject.addObjectToBothSidesOfRelationshipWithKey(relationshipObject, attributeKey)
								} else if (insertedObject.toManyRelationshipKeys().contains(attributeKey))
									for (obj <- attributes.get(attributeKey).get.asInstanceOf[java.util.List[Any]])
										insertedObject.addObjectToBothSidesOfRelationshipWithKey(objectCache.get(obj.toString).get, attributeKey)
							}
							objectCache.put(id.toString, insertedObject)
							if (ERXProperties.booleanForKey("EOFFixtures.saveOnEachInsert"))
								ec.saveChanges
						}
					}
				}
				case unknownItem => println("Could not parse " + unknownItem)
			}
		}
		
		ec.saveChanges
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
