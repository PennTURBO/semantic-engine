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
import org.slf4j.LoggerFactory

class InputDataValidator
{
    var stopRun = false
    val logger = LoggerFactory.getLogger(getClass)

    def validateInputData(graphs: ArrayBuffer[String], inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], dataValidationMode: String = Globals.dataValidationMode)
    {
        if (dataValidationMode == "stop") stopRun = true
        
        assert (graphs.size != 0, "Input Validator received a list of 0 named graphs")
        for (input <- inputs)
        {   
            if (input(Globals.REQUIREMENT.toString) != null && 
                (input(Globals.REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/eitherSubjectOrObjectExists" ||
                input(Globals.REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/objectExists" ||
                input(Globals.REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/subjectExists"
                ))
            {
                val graphsFromNamedClause = Utilities.buildFromNamedGraphsClauseFromList(graphs)
                if (input(Globals.CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/InstanceToTermRecipe")
                {
                    validateTermAgainstSubject(graphsFromNamedClause, input)
                }
                else if (input(Globals.CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/TermToInstanceRecipe")
                {
                    validateTermAgainstObject(graphsFromNamedClause, input)
                }
                else if (input(Globals.CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/InstanceToLiteralRecipe")
                {
                    validateLiteralAgainstSubject(graphsFromNamedClause, input)
                }
                else if (input(Globals.CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/InstanceToInstanceRecipe")
                {
                    if (input(Globals.REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/eitherSubjectOrObjectExists")
                    {
                        validateSubjectAgainstObject(graphsFromNamedClause, input)
                        validateObjectAgainstSubject(graphsFromNamedClause, input)
                    }
                    else if (input(Globals.REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/objectExists")
                    {
                        validateObjectAgainstSubject(graphsFromNamedClause, input)
                    }
                    else if (input(Globals.REQUIREMENT.toString).toString == "https://github.com/PennTURBO/Drivetrain/subjectExists")
                    {
                        validateSubjectAgainstObject(graphsFromNamedClause, input)
                    }   
                }
                else
                {
                    val connectionType = input(Globals.CONNECTIONRECIPETYPE.toString).toString
                    if (connectionType != "https://github.com/PennTURBO/Drivetrain/TermToTermRecipe" && connectionType != "https://github.com/PennTURBO/Drivetrain/TermToLiteralRecipe") throw new RuntimeException(s"Unrecognized connection recipe type $connectionType")
                }
            }
        }
    }
    
    def validateObjectAgainstSubject(graphsFromNamedClause: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val subjectAsType = input(Globals.SUBJECT.toString).toString
        val objectAsType = input(Globals.OBJECT.toString).toString
        val subjectAsVar = Utilities.convertTypeToSparqlVariable(subjectAsType, true)
        val objectAsVar = Utilities.convertTypeToSparqlVariable(objectAsType, true)
        val predicate = input(Globals.PREDICATE.toString).toString
        
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
        val res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, query, objectAsVar.substring(1))
        
        handleErrorReporting(errorMsg, res)
    }
    
    def validateSubjectAgainstObject(graphsFromNamedClause: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val subjectAsType = input(Globals.SUBJECT.toString).toString
        val objectAsType = input(Globals.OBJECT.toString).toString
        val subjectAsVar = Utilities.convertTypeToSparqlVariable(subjectAsType, true)
        val objectAsVar = Utilities.convertTypeToSparqlVariable(objectAsType, true)
        val predicate = input(Globals.PREDICATE.toString).toString
        
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
        val res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, query, subjectAsVar.substring(1))

        handleErrorReporting(errorMsg, res)
    }
    
    def validateTermAgainstSubject(graphsFromNamedClause: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val describer = input(Globals.OBJECT.toString).toString
        val subject = input(Globals.SUBJECT.toString).toString
        val subjectAsVar = Utilities.convertTypeToSparqlVariable(subject, true)
        val predicate = input(Globals.PREDICATE.toString).toString
        val describerRanges = Utilities.getDescriberRangesAsList(Globals.gmCxn, describer)
        
        var objectADescriber = false
        if (input(Globals.OBJECTADESCRIBER.toString) != null) objectADescriber = true
        
        var minusBlock = ""
        if (describerRanges == None) 
        {
            var describerInQuery = s"<$describer>"
            if (objectADescriber) describerInQuery = Utilities.convertTypeToSparqlVariable(describer)
            minusBlock = s"$subjectAsVar <$predicate> $describerInQuery ."
            if (objectADescriber) minusBlock += s"\nFilter isUri($describerInQuery)\n"
        }
        else
        {
            val ranges = describerRanges.get
            minusBlock += "{\n"
            for (termIndex <- 0 to ranges.size - 1) 
            {
              val term = ranges(termIndex)
              minusBlock += s"""
                  {$subjectAsVar <$predicate> <$term> .}\n
              """
              if (termIndex != ranges.size - 1) minusBlock += "UNION\n"
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
        val res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, sparql, subjectAsVar.substring(1))

        handleErrorReporting(errorMsg, res)
    }
    
    def validateTermAgainstObject(graphsFromNamedClause: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val describer = input(Globals.SUBJECT.toString).toString
        val objectAsType = input(Globals.OBJECT.toString).toString
        val objectAsVar = Utilities.convertTypeToSparqlVariable(objectAsType, true)
        val predicate = input(Globals.PREDICATE.toString).toString
        val describerRanges = Utilities.getDescriberRangesAsList(Globals.gmCxn, describer)
        
        var subjectADescriber = false
        if (input(Globals.SUBJECTADESCRIBER.toString) != null) subjectADescriber = true
        
        var minusBlock = ""
        if (describerRanges == None) 
        {
            var describerInQuery = s"<$describer>"
            if (subjectADescriber) describerInQuery = Utilities.convertTypeToSparqlVariable(describer)
            minusBlock = s"$describerInQuery <$predicate> $objectAsVar ."
            if (subjectADescriber) minusBlock += s"\nFilter isUri($describerInQuery)\n"
        }
        else
        {
            val ranges = describerRanges.get
            minusBlock += "{\n"
            for (termIndex <- 0 to ranges.size - 1) 
            {
              val term = ranges(termIndex)
              minusBlock += s"""
                  {<$term> <$predicate> $objectAsVar .}\n
              """
              if (termIndex != ranges.size - 1) minusBlock += "UNION\n"
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
        val res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, sparql, objectAsVar.substring(1))

        handleErrorReporting(errorMsg, res)
    }
    
    def validateLiteralAgainstSubject(graphsFromNamedClause: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val subjectAsType = input(Globals.SUBJECT.toString).toString
        val objectLiteral = input(Globals.OBJECT.toString).toString
        val subjectAsVar = Utilities.convertTypeToSparqlVariable(subjectAsType, true)
        val objectAsVar = Utilities.convertTypeToSparqlVariable(objectLiteral, true)
        val predicate = input(Globals.PREDICATE.toString).toString
        
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
        val res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, query, subjectAsVar.substring(1))
        
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
                logger.info("\t"+errorMsgWithReplacement)
                Utilities.writeErrorLog("Input Data Validation", errorMsgWithReplacement)   
            }
        }
    }
}