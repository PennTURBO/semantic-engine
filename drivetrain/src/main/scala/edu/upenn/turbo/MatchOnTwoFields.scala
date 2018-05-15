package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel

class MatchOnTwoFields extends ProjectwideGlobals
{
    def executeMatch(cxn: RepositoryConnection, joinResults: ArrayBuffer[ArrayBuffer[Value]], encResult: ArrayBuffer[ArrayBuffer[Value]], consResult: ArrayBuffer[ArrayBuffer[Value]])
    {
        var model: Model = new LinkedHashModel()
        val f: ValueFactory = cxn.getValueFactory
        
        val encResultMap: HashMap[String, ArrayBuffer[Value]] = new HashMap[String, ArrayBuffer[Value]]
        val consResultMap: HashMap[String, ArrayBuffer[Value]] = new HashMap[String, ArrayBuffer[Value]]
        
        logger.info("creating map of enc results")
        for (a <- encResult)
        {
            val stringToHash: String = a(1).toString + a(2).toString
            if (encResultMap.contains(stringToHash))
            {
                logger.info("This URI has the same symbol/reg combo as a newly submitted entry: " + encResultMap(stringToHash))
                throw new RuntimeException ("already found this encounter symbol/reg combo: " + a(1) + " " + a(2) + " " + a(0)) 
            }
            encResultMap += stringToHash -> a
        }
        logger.info("creating map of cons results")
        for (a <- consResult)
        {
            val stringToHash: String = a(1).toString + a(2).toString
            if (consResultMap.contains(stringToHash))
            {
                logger.info("This URI has the same symbol/reg combo as a newly submitted entry: " + consResultMap(stringToHash))
                throw new RuntimeException ("already found this consenter symbol/reg combo: " + a(1) + " " + a(2) + " " + a(0))
            }
            consResultMap += stringToHash -> a
        }
        logger.info("searching for joins")
        for (a <- joinResults)
        {
            val encStringToHash: String = a(0).toString + a(1).toString
            val consStringToHash: String = a(2).toString + a(3).toString
            //logger.info("searching for join on " + a(0).toString + " and " + a(2).toString)
            if (encResultMap.contains(encStringToHash) && consResultMap.contains(consStringToHash))
            {
                //logger.info("found a match: " + a(0) + " to " + a(2))
                val encToLink: IRI = f.createIRI(encResultMap(encStringToHash)(0).toString)
                val consToLink: IRI = f.createIRI(consResultMap(consStringToHash)(0).toString)
                model.add(consToLink, f.createIRI("http://purl.obolibrary.org/obo/RO_0000056"), encToLink)
            }
        }
        logger.info("joins processed")
        cxn.begin()
        cxn.add(model, f.createIRI("http://www.itmat.upenn.edu/biobank/expanded"))
        cxn.commit()
        logger.info("changes committed")
    }
}