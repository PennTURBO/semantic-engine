package edu.upenn.turbo

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer

import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.query.TupleQuery
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.query.BindingSet

class Futures extends ProjectwideGlobals
{
    /*def testFutures(cxn: RepositoryConnection)
    {
        val triplesIterator: TupleQueryResult = getTriplesToTransfer(cxn)
        val bindingSet: BindingSet = triplesIterator.next
        var model: Model = new LinkedHashModel()
        model.add(bindingSet.getValue("s"), bindingSet.getValue("p"), bindingSet.getValue("o"))
        /*var triplesArry: ArrayBuffer[ArrayBuffer[String]] = new ArrayBuffer[ArrayBuffer[String]]
        while(triplesIterator.hasNext)
        {
            val bindingset: BindingSet = triplesIterator.next()
            var result: String = helper.removeQuotesFromString(bindingset.getValue(variableToUnpack).toString)
            triplesArr += result
        }
        
        val anomFuture = (model: Model ) => Future{ cxn.add(model) }

        f1 onComplete
        {
            case Success(result) => println("result: " + result.next)
            case Failure(t)  => t.printStackTrace()
        }
        
        
        Await.result(f1, scala.concurrent.duration.Duration.Inf)*/
    }
    
    def getTriplesToTransfer(cxn: RepositoryConnection): TupleQueryResult = 
    {
        val query: String = 
            """
            select * where {
                      Graph pmbb:expanded {
			                ?s ?p ?o .
			            }}
            """
          logger.info("running query")
          helper.querySparql(cxn, sparqlPrefixes + query).get
    }*/
}