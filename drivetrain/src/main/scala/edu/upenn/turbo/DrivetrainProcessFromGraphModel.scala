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

object DrivetrainProcessFromGraphModel extends ProjectwideGlobals
{
    def runProcess(gmCxn: RepositoryConnection, process: String, instantiation: String, globalUUID: String): String =
    {
        val localUUID = java.util.UUID.randomUUID().toString.replaceAll("-","")
        
        val inputs = getInputs(gmCxn, process)
        val outputs = getOutputs(gmCxn, process)
        val binds = getBind(gmCxn, process)
        
        val whereClause = createWhereClause(inputs)
        val bindClause = createBindClause(binds, localUUID, globalUUID, instantiation)
        val insertClause = createInsertClause(outputs, bindClause("variableList").asInstanceOf[ArrayBuffer[Value]])
        
        insertClause + whereClause+ bindClause("bindClause").asInstanceOf[String]
    }
    
    def createBindClause(binds: ArrayBuffer[ArrayBuffer[Value]], localUUID: String, globalUUID: String, instantiation: String): Map[String,Object] =
    {
        var bindClause = ""
        var varList = new ArrayBuffer[Value]
        for (rule <- binds)
        {
            var sparqlBind = rule(1).toString.replaceAll("replacement", convertTypeToVariable(rule(0)))
                                         .replaceAll("localUUID", localUUID)
                                         .replaceAll("globalUUID", globalUUID)
                                         .replaceAll("mainExpansionTypeVariableName", convertTypeToVariable(rule(4)))
                                         .replaceAll("instantiationPlaceholder", "\"" + instantiation + "\"")
            if (sparqlBind.contains("dependent")) sparqlBind = sparqlBind.replaceAll("dependent",convertTypeToVariable(rule(3)))
            if (sparqlBind.contains("original")) sparqlBind = sparqlBind.replaceAll("original",convertTypeToVariable(rule(2)))
            
            bindClause += sparqlBind.substring(1).split("\"\\^")(0) + "\n"
            varList += rule(0)
        }
        Map("bindClause" -> (bindClause + "}"), "variableList" -> varList)
    }
    
    def createInsertClause(outputs: ArrayBuffer[ArrayBuffer[Value]], boundList: ArrayBuffer[Value]): String =
    {
        if (outputs.size == 0) throw new RuntimeException("Received a list of 0 outputs")
        var insertClause = "INSERT { Graph <" + outputs(0)(5) + ">{\n"
        var typeSet = new HashSet[Value]
        for (triple <- outputs)
        {
            var subjectVariable = ""
            var objectVariable = ""
            if (boundList.contains(triple(0))) subjectVariable = "?" + convertTypeToVariable(triple(0))
            else subjectVariable = "<" + triple(0) + ">"
            if (boundList.contains(triple(2))) objectVariable = "?" + convertTypeToVariable(triple(2))
            else objectVariable = "<" + triple(2) + ">"
            insertClause += subjectVariable + " <" + triple(1).toString + "> " + objectVariable + " .\n"
            if (triple(3) != null && !typeSet.contains(triple(0)))
            {
                insertClause += subjectVariable + " a <" + triple(0) + "> .\n"
                typeSet += triple(0)
            }
            if (triple(4) != null && !typeSet.contains(triple(2)))
            {
                insertClause += objectVariable + " a <" + triple(2) + "> .\n"
                typeSet += triple(2)
            }
        }
        insertClause += "}}\n"
        insertClause
    }
    
    def createWhereClause(inputs: ArrayBuffer[ArrayBuffer[Value]]): String =
    {
        if (inputs.size == 0) throw new RuntimeException("Received a list of 0 inputs")
        var whereClause = "WHERE { GRAPH <" + inputs(0)(5) + "> {\n"
        
        var requiredTypeMap = new HashMap[String,Value]
        var optionalTypeMap = new HashMap[String, Value]
        
        var optionalGroups = new HashMap[Value, ArrayBuffer[ArrayBuffer[Value]]]
        for (triple <- inputs)
        {
            if (triple(7) != null) 
            {
                if (!optionalGroups.contains(triple(7))) optionalGroups += triple(7) -> ArrayBuffer(triple)
                else optionalGroups(triple(7)) += triple
                if (triple(3) != null && !optionalTypeMap.contains(convertTypeToVariable(triple(0)))) optionalTypeMap += convertTypeToVariable(triple(0)) -> triple(0)
                if (triple(4) != null && !optionalTypeMap.contains(convertTypeToVariable(triple(2)))) optionalTypeMap += convertTypeToVariable(triple(2)) -> triple(2)
            }
            else
            {
                whereClause += addTripleToWhereClause(triple)
                if (triple(3) != null && !requiredTypeMap.contains(convertTypeToVariable(triple(0)))) requiredTypeMap += convertTypeToVariable(triple(0)) -> triple(0)
                if (triple(4) != null && !requiredTypeMap.contains(convertTypeToVariable(triple(2)))) requiredTypeMap += convertTypeToVariable(triple(2)) -> triple(2)
            }
        }
        for ((k,v) <- optionalGroups)
        {
            var localTypeSet = new HashSet[String]
            whereClause += "OPTIONAL {\n"
            for (triple <- v)
            {
                whereClause += addTripleToWhereClause(triple)
                if (optionalTypeMap.contains(convertTypeToVariable(triple(0))) 
                    && !requiredTypeMap.contains(convertTypeToVariable(triple(0))) 
                    && !localTypeSet.contains(convertTypeToVariable(triple(0))))
                {
                    whereClause += "?" + convertTypeToVariable(triple(0)) + " a <" + triple(0) + "> .\n"
                    localTypeSet += convertTypeToVariable(triple(0))
                }
                if (optionalTypeMap.contains(convertTypeToVariable(triple(2))) 
                    && !requiredTypeMap.contains(convertTypeToVariable(triple(2))) 
                    && !localTypeSet.contains(convertTypeToVariable(triple(2)))) 
                {
                    whereClause += "?" + convertTypeToVariable(triple(2)) + " a <" + triple(2) + "> .\n"
                    localTypeSet += convertTypeToVariable(triple(2))
                }
            }
            whereClause += "}\n"
        }
        whereClause += "}\n"
        for ((k,v) <- requiredTypeMap) whereClause += "?" + k + " a <" + v + "> .\n"
        whereClause
    }
    
    def addTripleToWhereClause(triple: ArrayBuffer[Value]): String =
    {
        var whereClause = ""
        var required = true
        if (triple(6).toString == "\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>") required = false
        if (!required) whereClause += "OPTIONAL { "
        val subjectVariable = convertTypeToVariable(triple(0))
        val objectVariable = convertTypeToVariable(triple(2))
        whereClause += "?" + subjectVariable + " <" + triple(1).toString + "> ?" + objectVariable + " ."
        if (!required) whereClause += "}"
        whereClause += "\n"
        whereClause
    }
    
    def convertTypeToVariable(input: Value): String =
    {
       input.toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_")
    }
    
    def getInputs(cxn: RepositoryConnection, process: String): ArrayBuffer[ArrayBuffer[Value]] =
    {
       val query = s"""
         
         Select ?subject ?predicate ?object ?subjectType ?objectType ?graph ?required ?optionalGroup
         Where
         {
            ?connection turbo:inputTo <$process> .
            # ?connection a turbo:TurboGraphConnectionRecipe .
            <$process> turbo:inputNamedGraph ?graph .
            ?connection turbo:subject ?subject .
            ?connection turbo:predicate ?predicate .
            ?connection turbo:object ?object .
            ?connection turbo:required ?required .
            
            Optional
            {
                ?connection obo:BFO_0000050 ?optionalGroup .
            }
            
            Graph pmbb:ontology {
              Optional
              {
                  ?subject a owl:Class .
                  BIND (true AS ?subjectType)
              }
              Optional
              {
                  ?object a owl:Class .
                  BIND (true AS ?objectType)
              }
         }}
         
         """
       
       update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, Array("subject", "predicate", "object", "subjectType", "objectType", "graph", "required", "optionalGroup"))
    }
    
    def getOutputs(cxn: RepositoryConnection, process: String): ArrayBuffer[ArrayBuffer[Value]] =
    {
       val query = s"""
         
         Select ?subject ?predicate ?object ?subjectType ?objectType ?graph
         Where
         {
            ?connection turbo:outputOf <$process> .
            # ?connection a turbo:TurboGraphConnectionRecipe .
            <$process> turbo:outputNamedGraph ?graph .
            ?connection turbo:subject ?subject .
            ?connection turbo:predicate ?predicate .
            ?connection turbo:object ?object .
            
            Graph pmbb:ontology 
            {
              Optional
              {
                  ?subject a owl:Class .
                  BIND (true AS ?subjectType)
              }
              Optional
              {
                  ?object a owl:Class .
                  BIND (true AS ?objectType)
              }
            }
         }
         
         """
       
       update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, Array("subject", "predicate", "object", "subjectType", "objectType", "graph"))
    }
    
    def getBind(cxn: RepositoryConnection, process: String): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val query = s"""
          
          Select distinct ?expandedEntity ?sparqlString ?shortcutEntity ?dependee ?baseType
          Where
          {
    		      values ?manipulationRuleType {turbo:VariableManipulationForIntermediateNode turbo:VariableManipulationForLiteralValue}
              <$process> turbo:usesVariableManipulationRule ?variableManipulationRule .
              <$process> turbo:manipulatesBaseEntity ?baseType .
              
              ?variableManipulationRule a ?manipulationRuleType .
              ?variableManipulationRule turbo:manipulationCreates ?expandedEntity .
              ?variableManipulationRule turbo:usesSparqlLogic ?logic .
              ?logic turbo:usesSparql ?sparqlString .
              
              Optional
              {
                  ?variableManipulationRule turbo:hasOriginalVariable ?shortcutEntity .
              }
              Optional
              {
                  ?variableManipulationRule turbo:manipulationDependsOn ?dependee .
              }
          }
          
        """
        
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, Array("expandedEntity", "sparqlString", "shortcutEntity", "dependee", "baseType"))
    }
}