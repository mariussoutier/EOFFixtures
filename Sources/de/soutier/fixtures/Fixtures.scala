package de.soutier.fixtures


import scalaj.collection.Imports._

import org.yaml.snakeyaml._

import org.apache.log4j.Logger

import com.webobjects.foundation.{NSDictionary, NSArray, NSMutableArray}
import com.webobjects.eocontrol.{EOEnterpriseObject, EOEditingContext}

import er.extensions.foundation.ERXProperties
import er.extensions.eof.{ERXEC, ERXEOControlUtilities}


object Fixtures {
	private lazy val logger = Logger.getLogger(this.getClass.getName)
	
	def load(): Unit = load(ERXEC.newEditingContext)
	
	def load(ec: EOEditingContext) {
		// TODO Localization via WOResourceManager
		val text = scala.io.Source.fromFile("Resources/" + ERXProperties.stringForKeyWithDefault("EOFFixtures.fileName", "EOFFixtures.fileName")) mkString
		
		val yaml = new Yaml()
		val yamlResult = yaml.load(text)
		val yamlMap = 
			if (yamlResult.isInstanceOf[java.util.Map[String, Any]])
				yamlResult.asInstanceOf[java.util.Map[String, Any]] asScala
			else Map[String, Any]()
		
		val objectCache = scala.collection.mutable.Map[String, EOEnterpriseObject]()
		
		val nestedContext = ERXEC.newEditingContext(ec)
		val entityExtractor = """([^(]+)\(([^)]+)\)""".r

		for (key <- yamlMap.keysIterator) {
			key match {
				case entityExtractor(entityName, id) => {
					logger.debug("Parsed: " + entityName + " with id: " + id)
					
					objectCache.get(key.toString) match {
						case Some(alreadyPresent) =>
						case None => {
							val insertedObject = ERXEOControlUtilities.createAndInsertObject(nestedContext, entityName)
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
							nestedContext.saveChanges
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
