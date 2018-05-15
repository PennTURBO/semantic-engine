package edu.upenn.turbo

import org.eclipse.rdf4j.model._
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.impl._
import org.eclipse.rdf4j.model.vocabulary._
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import org.scalatest._
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

// what do I have to do to control what log statements I see?
// aren't we are using org.slf4j.LoggerFactory 
// on top of logback??

class Helper4SparqlTests {

  val logger = LoggerFactory.getLogger(getClass)

  def CountTriples(cxn: RepositoryConnection, PrintTrips: Boolean): Int = {

    var tripleCount = 0

    try {
      val tupleQuery = cxn.prepareTupleQuery(QueryLanguage.SPARQL,
        "select ?s ?p ?o where { ?s ?p ?o . }")
      val result = tupleQuery.evaluate()

      try {
        while (result.hasNext()) {
          tripleCount = tripleCount + 1;
          val bindingSet = result.next();
          if (PrintTrips) {
            logger.warn("bindingSet = " + bindingSet)

          }
        }
      } finally {
        result.close()
        logger.warn(tripleCount + " rows counted")
      }
    } finally {
      // close the repository connection
      //      cxn.close()
    }

    return (tripleCount)

  }

  def ModelFromSparqlConstruct(cxn: RepositoryConnection, ConstructString: String): LinkedHashModel = {

    // string based generic container for triples, inbound and outbound
    var resultStream = new java.io.ByteArrayOutputStream
    var resultWriter = Rio.createWriter(RDFFormat.TURTLE, resultStream)

    cxn.prepareGraphQuery(QueryLanguage.SPARQL, ConstructString).evaluate(resultWriter)

    var resString = resultStream.toString()

    logger.warn("when asking for")
    logger.warn(ConstructString)
    logger.warn("I got this: " + resString)

    var streamBack = new java.io.StringReader(resString)
    var resultParser = Rio.createParser(RDFFormat.TURTLE)
    var resultModel = new LinkedHashModel()
    resultParser.setRDFHandler(new StatementCollector(resultModel))

    resultParser.parse(streamBack, "http://base.url")

    // implicit return
    resultModel

  }

  def ModelFromTurtleString(ExpectedString: String): LinkedHashModel = {

    var expectedStream = new java.io.StringReader(ExpectedString)
    var expectedParser = Rio.createParser(RDFFormat.TURTLE)
    var expectedMod = new LinkedHashModel
    expectedParser.setRDFHandler(new StatementCollector(expectedMod))
    expectedParser.parse(expectedStream, "http://base.url")

    // implicit return
    expectedMod

  }

}