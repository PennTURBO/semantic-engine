package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import java.util.UUID

object InputDataValidator extends ProjectwideGlobals
{
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    def setOutputRepositoryConnection(cxn: RepositoryConnection)
    {
        this.cxn = cxn
    }
    def validateInputData(graph: String, inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        for (input <- inputs)
        {   
            if (input(REQUIREMENT.toString) != null && 
                (input(REQUIREMENT.toString).toString == "http://transformunify.org/ontologies/bothRequired" ||
                input(REQUIREMENT.toString).toString == "http://transformunify.org/ontologies/subjectRequired" ||
                input(REQUIREMENT.toString).toString == "http://transformunify.org/ontologies/objectRequired"
                ))
            {
                if (input(CONNECTIONRECIPETYPE.toString).toString == "http://transformunify.org/ontologies/ObjectConnectionToTermRecipe")
                {
                    validateTermAgainstSubject(graph, input)
                }
                else if (input(CONNECTIONRECIPETYPE.toString).toString == "http://transformunify.org/ontologies/ObjectConnectionFromTermRecipe")
                {
                    validateTermAgainstObject(graph, input)
                }
                else if (input(CONNECTIONRECIPETYPE.toString).toString == "http://transformunify.org/ontologies/DatatypeConnectionRecipe")
                {
                    validateLiteralAgainstSubject(graph, input)
                }
                else if (input(CONNECTIONRECIPETYPE.toString).toString == "http://transformunify.org/ontologies/ObjectConnectionToInstanceRecipe")
                {
                    if (input(REQUIREMENT.toString).toString == "http://transformunify.org/ontologies/bothRequired")
                    {
                        validateSubjectAgainstObject(graph, input)
                        validateObjectAgainstSubject(graph, input)
                    }
                    else if (input(REQUIREMENT.toString).toString == "http://transformunify.org/ontologies/subjectRequired")
                    {
                        validateObjectAgainstSubject(graph, input)
                    }
                    else if (input(REQUIREMENT.toString).toString == "http://transformunify.org/ontologies/objectRequired")
                    {
                        validateSubjectAgainstObject(graph, input)
                    }   
                }
                else
                {
                    val connectionType = input(CONNECTIONRECIPETYPE.toString).toString
                    throw new RuntimeException(s"Unrecognized connection recipe type $connectionType")
                }
            }
        }
    }
    
    def validateObjectAgainstSubject(graph: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        // make a copy so we don't affect the master result list
        val inputCopy = new HashMap[String, org.eclipse.rdf4j.model.Value]
        for ((key, value) <- input) inputCopy += key -> value
        
        val f = cxn.getValueFactory()
        val subjectAsType = inputCopy(SUBJECT.toString)
        val objectAsType = inputCopy(OBJECT.toString)
        val objectAsVar = helper.convertTypeToSparqlVariable(objectAsType, false)
        val multiplicity = inputCopy(MULTIPLICITY.toString).toString
        
        inputCopy(MINUSGROUP.toString) = f.createIRI("http://www.itmat.upenn.edu/biobank/validatorMinusGroup")
        inputCopy(OBJECTTYPE.toString) = null
        inputCopy(OBJECTADESCRIBER.toString) = f.createLiteral(true)
        
        val objectTypeInput = helper.makeGenericTypeInput(f, objectAsType, graph)
        
        val query = new PatternMatchQuery()
        query.setGraphModelConnection(gmCxn)
        query.setInputGraph(graph)
        
        query.createWhereClause(ArrayBuffer(inputCopy, objectTypeInput))
        val whereBlock = query.whereClause
        val checkRequired = s"SELECT * $whereBlock }"
        println(checkRequired)
        var firstResult = ""
        val res = update.querySparqlAndUnpackTuple(cxn, checkRequired, objectAsVar)
        if (res.size != 0) firstResult = res(0)
        assert (firstResult == "", s"Input data error: instance $firstResult of type $objectAsType does not have the required connection to an instance of type $subjectAsType in graph $graph") 
    }
    
    def validateSubjectAgainstObject(graph: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        // make a copy so we don't affect the master result list
        val inputCopy = new HashMap[String, org.eclipse.rdf4j.model.Value]
        for ((key, value) <- input) inputCopy += key -> value
        
        val f = cxn.getValueFactory()
        val subjectAsType = inputCopy(SUBJECT.toString)
        val objectAsType = inputCopy(OBJECT.toString)
        val subjectAsVar = helper.convertTypeToSparqlVariable(subjectAsType, false)
        val multiplicity = inputCopy(MULTIPLICITY.toString).toString
        
        inputCopy(MINUSGROUP.toString) = f.createIRI("http://www.itmat.upenn.edu/biobank/validatorMinusGroup")
        inputCopy(SUBJECTTYPE.toString) = null
        inputCopy(SUBJECTADESCRIBER.toString) = f.createLiteral(true)
        
        val subjectTypeInput = helper.makeGenericTypeInput(f, subjectAsType, graph)
        
        val query = new PatternMatchQuery()
        query.setGraphModelConnection(gmCxn)
        query.setInputGraph(graph)
        
        query.createWhereClause(ArrayBuffer(inputCopy, subjectTypeInput))
        val whereBlock = query.whereClause
        val checkRequired = s"SELECT * $whereBlock }"
        println(checkRequired)
        var firstResult = ""
        val res = update.querySparqlAndUnpackTuple(cxn, checkRequired, subjectAsVar)
        if (res.size != 0) firstResult = res(0)
        assert (firstResult == "", s"Input data error: instance $firstResult of type $subjectAsType does not have the required connection to an instance of type $objectAsType in graph $graph") 
    }
    
    def validateTermAgainstSubject(graph: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val describer = input(OBJECT.toString).toString
        val subject = input(SUBJECT.toString).toString
        val subjectAsVar = helper.convertTypeToSparqlVariable(subject, true)
        val predicate = input(PREDICATE.toString).toString
        val describerRanges = helper.getDescriberRangesAsList(gmCxn, describer)
        
        var objectADescriber = false
        if (input(OBJECTADESCRIBER.toString) != null) objectADescriber = true
        
        var minusBlock = ""
        if (describerRanges.size == 0) 
        {
            var describerInQuery = s"<$describer>"
            if (objectADescriber) describerInQuery = helper.convertTypeToSparqlVariable(describer)
            minusBlock = s"$subjectAsVar <$predicate> $describerInQuery ."
            if (objectADescriber) minusBlock += s"\nFilter isUri($describerInQuery)\n"
        }
        else
        {
            minusBlock += "{\n"
            for (termIndex <- 0 to describerRanges.size - 1) 
            {
              val term = describerRanges(termIndex)
              minusBlock += s"""
                  {$subjectAsVar <$predicate> <$term> .}\n
              """
              if (termIndex != describerRanges.size - 1) minusBlock += "UNION\n"
            }
            minusBlock += "}\n"  
        }
        
        val sparql: String = s"""
          Select * Where
          {
              Graph <$graph>
              {
                  $subjectAsVar rdf:type <$subject> .
                  Minus
                  {
                      $minusBlock
                  }
              }
          }
          """
        println(sparql)
        var firstResult = ""
        val res = update.querySparqlAndUnpackTuple(cxn, sparql, subjectAsVar.substring(1))
        if (res.size != 0) firstResult = res(0)
        assert (firstResult == "", s"Input data error: instance $firstResult of type $subject does not have the required connection to term $describer in graph $graph") 
    }
    
    def validateTermAgainstObject(graph: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val describer = input(SUBJECT.toString).toString
        val objectAsType = input(OBJECT.toString).toString
        val objectAsVar = helper.convertTypeToSparqlVariable(objectAsType, true)
        val predicate = input(PREDICATE.toString).toString
        val describerRanges = helper.getDescriberRangesAsList(gmCxn, describer)
        
        var subjectADescriber = false
        if (input(SUBJECTADESCRIBER.toString) != null) subjectADescriber = true
        
        var minusBlock = ""
        if (describerRanges.size == 0) 
        {
            var describerInQuery = s"<$describer>"
            if (subjectADescriber) describerInQuery = helper.convertTypeToSparqlVariable(describer)
            minusBlock = s"$describerInQuery <$predicate> $objectAsVar ."
            if (subjectADescriber) minusBlock += s"\nFilter isUri($describerInQuery)\n"
        }
        else
        {
            minusBlock += "{\n"
            for (termIndex <- 0 to describerRanges.size - 1) 
            {
              val term = describerRanges(termIndex)
              minusBlock += s"""
                  {<$term> <$predicate> $objectAsVar .}\n
              """
              if (termIndex != describerRanges.size - 1) minusBlock += "UNION\n"
            }
            minusBlock += "}\n"  
        }
        
        val sparql: String = s"""
          Select * Where
          {
              Graph <$graph>
              {
                  $objectAsVar rdf:type <$objectAsType> .
                  Minus
                  {
                      $minusBlock
                  }
              }
          }
          """
        println(sparql)
        var firstResult = ""
        val res = update.querySparqlAndUnpackTuple(cxn, sparql, objectAsVar.substring(1))
        if (res.size != 0) firstResult = res(0)
        assert (firstResult == "", s"Input data error: instance $firstResult of type $objectAsType does not have the required connection to term $describer in graph $graph") 
    }
    
    def validateLiteralAgainstSubject(graph: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        // make a copy so we don't affect the master result list
        val inputCopy = new HashMap[String, org.eclipse.rdf4j.model.Value]
        for ((key, value) <- input) inputCopy += key -> value
        
        val f = cxn.getValueFactory()
        val subjectAsType = inputCopy(SUBJECT.toString)
        val objectLiteral = inputCopy(OBJECT.toString)
        val subjectAsVar = helper.convertTypeToSparqlVariable(subjectAsType, false)
        val objectAsVar = helper.convertTypeToSparqlVariable(objectLiteral, true)
        val multiplicity = inputCopy(MULTIPLICITY.toString).toString
        
        inputCopy(MINUSGROUP.toString) = f.createIRI("http://www.itmat.upenn.edu/biobank/validatorMinusGroup")
        inputCopy(SUBJECTTYPE.toString) = null
        inputCopy(SUBJECTADESCRIBER.toString) = f.createLiteral(true)
        
        val subjectTypeInput = helper.makeGenericTypeInput(f, subjectAsType, graph)
        
        val query = new PatternMatchQuery()
        query.setGraphModelConnection(gmCxn)
        query.setInputGraph(graph)
        
        query.createWhereClause(ArrayBuffer(inputCopy, subjectTypeInput))
        val whereBlock = query.whereClause.split("\\}")(0)
        val checkRequired = s"SELECT * $whereBlock Filter isLiteral($objectAsVar)\n }}}"
        println(checkRequired)
        var firstResult = ""
        val res = update.querySparqlAndUnpackTuple(cxn, checkRequired, subjectAsVar)
        if (res.size != 0) firstResult = res(0)
        assert (firstResult == "", s"Input data error: instance $firstResult of type $subjectAsType does not have the required connection to literal value $objectLiteral in graph $graph") 
    }
}