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
    def executeMatchWithThreeTables(cxn: RepositoryConnection, joinResults: ArrayBuffer[ArrayBuffer[Value]], table1: ArrayBuffer[ArrayBuffer[Value]], table2: ArrayBuffer[ArrayBuffer[Value]])
    {
        var model: Model = new LinkedHashModel()
        val f: ValueFactory = cxn.getValueFactory
        
        logger.info("creating map of results")
        val table1map: HashMap[String, ArrayBuffer[Value]] = createHashMapFromTable(table1)
        val table2map: HashMap[String, ArrayBuffer[Value]] = createHashMapFromTable(table2)
        
        logger.info("searching for joins")
        for (a <- joinResults)
        {
            val encStringToHash: String = a(0).toString + a(1).toString
            val consStringToHash: String = a(2).toString + a(3).toString
            //logger.info("searching for join on " + a(0).toString + " and " + a(2).toString)
            if (table1map.contains(encStringToHash) && table2map.contains(consStringToHash))
            {
                //logger.info("found a match: " + a(0) + " to " + a(2))
                val encToLink: IRI = f.createIRI(table1map(encStringToHash)(0).toString)
                val consToLink: IRI = f.createIRI(table2map(consStringToHash)(0).toString)
                model.add(consToLink, f.createIRI("http://purl.obolibrary.org/obo/RO_0000056"), encToLink)
                //model.add(a(4).asInstanceOf[IRI], f.createIRI("http://graphBuilder.org/deleteWith"), a(5).asInstanceOf[IRI])
            }
        }
        logger.info("joins processed")
        cxn.begin()
        cxn.add(model, f.createIRI("http://www.itmat.upenn.edu/biobank/expanded"))
        cxn.commit()
        logger.info("changes committed")
    }
    
    def executeMatchWithTwoTables(cxn: RepositoryConnection, table1: ArrayBuffer[ArrayBuffer[Value]], table2: ArrayBuffer[ArrayBuffer[Value]])
    {
        var model: Model = new LinkedHashModel()
        val f: ValueFactory = cxn.getValueFactory
        val pred: IRI = f.createIRI("http://graphBuilder.org/willBeLinkedWith")
        logger.info("Searching for matches")
        val table1map: HashMap[String, ArrayBuffer[Value]] = createHashMapFromTable(table1)
        for (a <- table2)
        {
            val checkString: String = a(1).toString + a(2).toString
            if (table1map.contains(checkString)) 
            {
                model.add(a(0).asInstanceOf[IRI], pred, table1map(checkString)(0))
            }
        }
        logger.info("joins processed")
        cxn.begin()
        cxn.add(model, f.createIRI("http://www.itmat.upenn.edu/biobank/expanded"))
        cxn.commit()
        logger.info("changes committed")
    }
    
    def createHashMapFromTable(table: ArrayBuffer[ArrayBuffer[Value]]): HashMap[String, ArrayBuffer[Value]] =
    {
        var resultMap: HashMap[String, ArrayBuffer[Value]] = new HashMap[String, ArrayBuffer[Value]]
        for (a <- table)
        {
            val stringToHash: String = a(1).toString + a(2).toString
            if (resultMap.contains(stringToHash))
            {
                logger.info("This URI has the same symbol/reg combo as a newly submitted entry: " + resultMap(stringToHash))
                throw new RuntimeException ("already found this symbol/reg combo: " + a(1) + " " + a(2) + " " + a(0)) 
            }
            resultMap += stringToHash -> a
        }
        resultMap
    }
}