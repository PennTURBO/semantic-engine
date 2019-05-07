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
    var globalUUID: String = null
    var instantiation: String = null
    var variableSet = new HashSet[Value]
    var inputSet = new HashSet[Value]
    var typeMap = new HashMap[String,Value]
    
    //define the SPARQL variables used in the retrieval methods
    val subject = "subject"
    val predicate = "predicate"
    val objectVar = "object"
    val subjectType = "subjectType"
    val objectType = "objectType"
    val graph = "graph"
    val requiredBool = "required"
    val optionalGroup = "optionalGroup"
    val expandedEntity = "expandedEntity"
    val sparqlString = "sparqlString"
    val dependee = "dependee"
    val baseType = "baseType"
    val shortcutEntity = "shortcutEntity"
    val connectionRecipeType = "connectionRecipeType"
    
    def setInstantiation(instantiation: String)
    {
        this.instantiation = instantiation
    }
    def setGlobalUUID(globalUUID: String)
    {
        this.globalUUID = globalUUID
    }
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    def setOutputRepositoryConnection(cxn: RepositoryConnection)
    {
        this.cxn = cxn
    }
        
    def runProcess(process: String)
    {
        logger.info("Starting process: " + process)
        val localUUID = java.util.UUID.randomUUID().toString.replaceAll("-","")
        
        val inputs = getInputs(process)
        val outputs = getOutputs(process)
        val binds = getBind(process)
        
        if (inputs.size == 0) throw new RuntimeException("Received a list of 0 inputs")
        if (outputs.size == 0) throw new RuntimeException("Received a list of 0 outputs")
        
        val inputNamedGraph = inputs(0)(graph).toString
        val outputNamedGraph = outputs(0)(graph).toString
        var inputNamedGraphsList = new ArrayBuffer[String]
        
        if (inputNamedGraph.charAt(inputNamedGraph.size-1) == '_') 
        {
            inputNamedGraphsList = helper.generateNamedGraphsListFromPrefix(cxn, inputNamedGraph)
        }
        else inputNamedGraphsList = ArrayBuffer(inputNamedGraph)
        
        for (a <- inputNamedGraphsList)
        {
            val whereClause = createWhereClause(inputs, a)
            val bindClause = createBindClause(binds, localUUID)
            val insertClause = createInsertClause(outputs, outputNamedGraph, a, process)
            
            val query = insertClause + whereClause + bindClause
            println(query)
            update.updateSparql(cxn, query)
        }
        
        variableSet = new HashSet[Value]
        typeMap = new HashMap[String,Value]
    }
    
    def createBindClause(binds: ArrayBuffer[HashMap[String, Value]], localUUID: String): String =
    {
        // example input and output of a single line
        var bindClause = ""
        var varList = new ArrayBuffer[Value]
        for (rule <- binds)
        {
            var sparqlBind = rule(sparqlString).toString.replaceAll("\\$\\{replacement\\}", convertTypeToVariable(rule(expandedEntity)))
                                         .replaceAll("\\$\\{localUUID\\}", localUUID)
                                         .replaceAll("\\$\\{globalUUID\\}", globalUUID)
                                         .replaceAll("\\$\\{mainExpansionTypeVariableName\\}", convertTypeToVariable(rule(baseType)))
                                         .replaceAll("\\$\\{instantiationPlaceholder\\}", "\"" + instantiation + "\"")
            if (sparqlBind.contains("${dependent}")) sparqlBind = sparqlBind.replaceAll("\\$\\{dependent\\}",convertTypeToVariable(rule(dependee)))
            if (sparqlBind.contains("${original}")) sparqlBind = sparqlBind.replaceAll("\\$\\{original\\}",convertTypeToVariable(rule(shortcutEntity)))
            if (sparqlBind.contains("${singletonType}")) sparqlBind = sparqlBind.replaceAll("\\$\\{singletonType\\}",rule(dependee).toString)
            
            bindClause += sparqlBind.substring(1).split("\"\\^")(0) + "\n"
            variableSet += rule(expandedEntity)
            variableSet += rule(shortcutEntity)
            variableSet += rule(dependee)
            variableSet += rule(baseType)
        }
        bindClause + "\n}"
    }
    
    def createInsertClause(outputs: ArrayBuffer[HashMap[String, Value]], outputNamedGraph: String, inputNamedGraph: String, process: String): String =
    {
        var insertClause = "INSERT { Graph <" + outputNamedGraph + ">{\n"
        //create process node
        insertClause += s"<$process> a turbo:TurboGraphProcess .\n"
        insertClause += s"<$process> turbo:addedTriplesTo <$outputNamedGraph> .\n"
        insertClause += s"<$process> turbo:sourcedInputFrom <$inputNamedGraph> .\n"
        var typeSet = new HashSet[Value]
        for (triple <- outputs)
        {
            var formattedSubjectVariable = ""
            var formattedObjectVariable = ""
            if (variableSet.contains(triple(subject)) || inputSet.contains(triple(subject))) formattedSubjectVariable = "?" + convertTypeToVariable(triple(subject))
            else formattedSubjectVariable = "<" + triple(subject) + ">"
            if (variableSet.contains(triple(objectVar)) || inputSet.contains(triple(objectVar))) formattedObjectVariable = "?" + convertTypeToVariable(triple(objectVar))
            else formattedObjectVariable = "<" + triple(objectVar) + ">"
            insertClause += formattedSubjectVariable + " <" + triple(predicate).toString + "> " + formattedObjectVariable + " .\n"
            if (triple(subjectType) != null && !typeSet.contains(triple(subject)))
            {
                insertClause += formattedSubjectVariable + " a <" + triple(subject) + "> .\n"
                typeSet += triple(subject)
            }
            if (triple(objectType) != null && !typeSet.contains(triple(objectVar)))
            {
                insertClause += formattedObjectVariable + " a <" + triple(objectVar) + "> .\n"
                typeSet += triple(objectVar)
            }
            if (triple(connectionRecipeType).toString() == "http://transformunify.org/ontologies/ObjectConnectionRecipe")
            {
                insertClause += s"<$process> obo:OBI_0000299 $formattedSubjectVariable .\n"
                if (!(triple(predicate).toString == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                {
                    insertClause += s"<$process> obo:OBI_0000299 $formattedObjectVariable .\n"
                } 
            }
        }
        for (input <- inputSet) insertClause += s"<$process> obo:OBI_0000293 ?" + convertTypeToVariable(input) + " .\n"
        
        insertClause += "}}\n"
        insertClause
    }
    
    def createWhereClause(inputs: ArrayBuffer[HashMap[String, Value]], namedGraph: String): String =
    {
        var whereClause = "WHERE { GRAPH <" + namedGraph + "> {\n"
        
        var optionalGroups = new HashMap[Value, ArrayBuffer[HashMap[String, Value]]]
        for (triple <- inputs)
        {
            inputSet += triple(subject)
            inputSet += triple(objectVar)
            if (triple(optionalGroup) != null) 
            {
                if (!optionalGroups.contains(triple(optionalGroup))) optionalGroups += triple(optionalGroup) -> ArrayBuffer(triple)
                else optionalGroups(triple(optionalGroup)) += triple
            }
            else
            {
                whereClause += addTripleToWhereClause(triple)
            }
        }
        for ((k,v) <- optionalGroups)
        {
            whereClause += "OPTIONAL {\n"
            for (triple <- v)
            {
                whereClause += addTripleToWhereClause(triple)
                
            }
            whereClause += "}\n"
        }
        whereClause += "}\n"
        whereClause
    }
    
    def addTripleToWhereClause(triple: HashMap[String, Value]): String =
    {
        var whereClause = ""
        var required = true
        if (triple(requiredBool).toString == "\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>") required = false
        if (!required) whereClause += "OPTIONAL { "
        val subjectVariable = convertTypeToVariable(triple(subject))
        val objectVariable = convertTypeToVariable(triple(objectVar))
        whereClause += "?" + subjectVariable + " <" + triple(predicate).toString + "> ?" + objectVariable + " .\n"
        if (triple(subjectType) != null && !typeMap.contains(subjectVariable))
        {
            typeMap += subjectVariable -> triple(subject)
            whereClause += "?" + subjectVariable + " a <" + triple(subject) + "> .\n"
        }
        if (triple(objectType) != null && !typeMap.contains(objectVariable))
        {
            typeMap += objectVariable -> triple(objectVar)
            whereClause += "?" + objectVariable + " a <" + triple(objectVar) + "> .\n"
        }
        if (!required) whereClause += "}"
        whereClause
    }
    
    def convertTypeToVariable(input: Value): String =
    {
       input.toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_")
    }
    
    def getInputs(process: String): ArrayBuffer[HashMap[String, Value]] =
    {
       val query = s"""
         
         Select ?$subject ?$predicate ?$objectVar ?$subjectType ?$objectType ?$graph ?$requiredBool ?$optionalGroup
         Where
         {
            ?connection turbo:inputTo <$process> .
            # ?connection a turbo:TurboGraphConnectionRecipe .
            <$process> turbo:inputNamedGraph ?$graph .
            ?connection turbo:subject ?$subject .
            ?connection turbo:predicate ?$predicate .
            ?connection turbo:object ?$objectVar .
            ?connection turbo:required ?$requiredBool .
            
            Optional
            {
                ?connection obo:BFO_0000050 ?$optionalGroup .
            }
            
            Graph pmbb:ontology {
              Optional
              {
                  ?$subject a owl:Class .
                  BIND (true AS ?$subjectType)
              }
              Optional
              {
                  ?$objectVar a owl:Class .
                  BIND (true AS ?$objectType)
              }
         }}
         
         """
       update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }
    
    def getOutputs(process: String): ArrayBuffer[HashMap[String, Value]] =
    {
       val query = s"""
         
         Select ?$subject ?$predicate ?$objectVar ?$subjectType ?$objectType ?$graph ?$connectionRecipeType
         Where
         {
            Values ?$connectionRecipeType {turbo:ObjectConnectionRecipe turbo:DatatypeConnectionRecipe}
            ?connection turbo:outputOf <$process> .
            ?connection a ?$connectionRecipeType .
            <$process> turbo:outputNamedGraph ?$graph .
            ?connection turbo:subject ?$subject .
            ?connection turbo:predicate ?$predicate .
            ?connection turbo:object ?$objectVar .
            
            Graph pmbb:ontology 
            {
              Optional
              {
                  ?$subject a owl:Class .
                  BIND (true AS ?$subjectType)
              }
              Optional
              {
                  ?$objectVar a owl:Class .
                  BIND (true AS ?$objectType)
              }
            }
         }
         
         """
       
       update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }
    
    def getBind(process: String): ArrayBuffer[HashMap[String, Value]] =
    {
        val query = s"""
          
          Select distinct ?$expandedEntity ?$sparqlString ?$shortcutEntity ?$dependee ?$baseType
          Where
          {
    		      values ?manipulationRuleType {turbo:VariableManipulationForIntermediateNode turbo:VariableManipulationForLiteralValue}
              <$process> turbo:usesVariableManipulationRule ?variableManipulationRule .
              <$process> turbo:manipulatesBaseEntity ?$baseType .
              
              ?variableManipulationRule a ?manipulationRuleType .
              ?variableManipulationRule turbo:manipulationCreates ?$expandedEntity .
              ?variableManipulationRule turbo:usesSparqlLogic ?logic .
              ?logic turbo:usesSparql ?$sparqlString .
              
              Optional
              {
                  ?variableManipulationRule turbo:hasOriginalVariable ?$shortcutEntity .
              }
              Optional
              {
                  ?variableManipulationRule turbo:manipulationDependsOn ?$dependee .
              }
          }
          
        """
        
        update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }
}