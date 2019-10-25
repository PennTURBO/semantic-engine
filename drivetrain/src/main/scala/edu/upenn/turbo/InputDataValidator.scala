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
    var stopRun = false
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    def setOutputRepositoryConnection(cxn: RepositoryConnection)
    {
        this.cxn = cxn
    }

    def validateInputData(graphs: ArrayBuffer[String], inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], dataValidationMode: String = dataValidationMode)
    {
        if (dataValidationMode == "stop") stopRun = true
        
        assert (graphs.size != 0, "Input Validator received a list of 0 named graphs")
        for (input <- inputs)
        {   
            if (input(REQUIREMENT.toString) != null && 
                (input(REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/eitherSubjectOrObjectExists" ||
                input(REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/objectExists" ||
                input(REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/subjectExists"
                ))
            {
                val graphsFromNamedClause = helper.buildFromNamedGraphsClauseFromList(graphs)
                if (input(CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/ObjectConnectionToTermRecipe")
                {
                    validateTermAgainstSubject(graphsFromNamedClause, input)
                }
                else if (input(CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/ObjectConnectionFromTermRecipe")
                {
                    validateTermAgainstObject(graphsFromNamedClause, input)
                }
                else if (input(CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/DatatypeConnectionRecipe")
                {
                    validateLiteralAgainstSubject(graphsFromNamedClause, input)
                }
                else if (input(CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/ObjectConnectionToInstanceRecipe")
                {
                    if (input(REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/eitherSubjectOrObjectExists")
                    {
                        validateSubjectAgainstObject(graphsFromNamedClause, input)
                        validateObjectAgainstSubject(graphsFromNamedClause, input)
                    }
                    else if (input(REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/objectExists")
                    {
                        validateObjectAgainstSubject(graphsFromNamedClause, input)
                    }
                    else if (input(REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/subjectExists")
                    {
                        validateSubjectAgainstObject(graphsFromNamedClause, input)
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
    
    def validateObjectAgainstSubject(graphsFromNamedClause: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val subjectAsType = input(SUBJECT.toString).toString
        val objectAsType = input(OBJECT.toString).toString
        val subjectAsVar = helper.convertTypeToSparqlVariable(subjectAsType, true)
        val objectAsVar = helper.convertTypeToSparqlVariable(objectAsType, true)
        val predicate = input(PREDICATE.toString).toString
        
        val query = s"""
          Select * 
          $graphsFromNamedClause
          Where
          {
              $objectAsVar rdf:type <$objectAsType> .
              Minus
              {
                  $subjectAsVar <$predicate> $objectAsVar .
                  $subjectAsVar rdf:type <$subjectAsType> .
              }
          }
          """
          
        //println(query)
        val errorMsg = s"Input data error: instance {res} of type $objectAsType does not have the required connection to an instance of type $subjectAsType in one of the following graphs:\n $graphsFromNamedClause"
        val res = update.querySparqlAndUnpackTuple(cxn, query, objectAsVar.substring(1))
        
        handleErrorReporting(errorMsg, res)
    }
    
    def validateSubjectAgainstObject(graphsFromNamedClause: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val subjectAsType = input(SUBJECT.toString).toString
        val objectAsType = input(OBJECT.toString).toString
        val subjectAsVar = helper.convertTypeToSparqlVariable(subjectAsType, true)
        val objectAsVar = helper.convertTypeToSparqlVariable(objectAsType, true)
        val predicate = input(PREDICATE.toString).toString
        
        val query = s"""
          Select * 
          $graphsFromNamedClause
          Where
          {
              $subjectAsVar rdf:type <$subjectAsType> .
              Minus
              {
                  $subjectAsVar <$predicate> $objectAsVar .
                  $objectAsVar rdf:type <$objectAsType> .
              }
          }
          """
          
        //println(query)
        val errorMsg = s"Input data error: instance {res} of type $subjectAsType does not have the required connection to an instance of type $objectAsType in one of the following graphs:\n $graphsFromNamedClause"
        val res = update.querySparqlAndUnpackTuple(cxn, query, subjectAsVar.substring(1))

        handleErrorReporting(errorMsg, res)
    }
    
    def validateTermAgainstSubject(graphsFromNamedClause: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
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
          Select * 
          $graphsFromNamedClause
          Where
          {
              $subjectAsVar rdf:type <$subject> .
              Minus
              {
                  $minusBlock
              }
          }
          """
        //println(sparql)
        val errorMsg = s"Input data error: instance {res} of type $subject does not have the required connection to term $describer in one of the following graphs:\n $graphsFromNamedClause"
        val res = update.querySparqlAndUnpackTuple(cxn, sparql, subjectAsVar.substring(1))

        handleErrorReporting(errorMsg, res)
    }
    
    def validateTermAgainstObject(graphsFromNamedClause: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
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
          Select * 
          $graphsFromNamedClause
          Where
          {
              $objectAsVar rdf:type <$objectAsType> .
              Minus
              {
                  $minusBlock
              }
          }
          """
        //println(sparql)
        val errorMsg = s"Input data error: instance {res} of type $objectAsType does not have the required connection to term $describer in one of the following graphs:\n $graphsFromNamedClause"
        val res = update.querySparqlAndUnpackTuple(cxn, sparql, objectAsVar.substring(1))

        handleErrorReporting(errorMsg, res)
    }
    
    def validateLiteralAgainstSubject(graphsFromNamedClause: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val subjectAsType = input(SUBJECT.toString).toString
        val objectLiteral = input(OBJECT.toString).toString
        val subjectAsVar = helper.convertTypeToSparqlVariable(subjectAsType, true)
        val objectAsVar = helper.convertTypeToSparqlVariable(objectLiteral, true)
        val predicate = input(PREDICATE.toString).toString
        
        val query = s"""
          Select * 
          $graphsFromNamedClause
          Where
          {
              $subjectAsVar rdf:type <$subjectAsType> .
              Minus
              {
                  $subjectAsVar <$predicate> $objectAsVar .
                  Filter isLiteral($objectAsVar)
              }
          }
          """
          
        //println(query)
        val errorMsg = s"Input data error: instance {res} of type $subjectAsType does not have the required connection to literal value $objectLiteral in one of the following graphs:\n $graphsFromNamedClause"
        val res = update.querySparqlAndUnpackTuple(cxn, query, subjectAsVar.substring(1))
        
        handleErrorReporting(errorMsg, res)
    }
    
    def handleErrorReporting(errorMsg: String, res: ArrayBuffer[String])
    {
        if (stopRun)
        {
            var firstResult = ""
            if (res.size != 0) firstResult = res(0)
            assert (firstResult == "", errorMsg.replaceAll("\\{res\\}", firstResult)) 
        }
        else
        {
            for (element <- res)
            {
                val errorMsgWithReplacement = errorMsg.replaceAll("\\{res\\}", element)
                logger.info(errorMsgWithReplacement)
                helper.writeErrorLog("Input Data Validation", errorMsgWithReplacement)   
            }
        }
    }
}