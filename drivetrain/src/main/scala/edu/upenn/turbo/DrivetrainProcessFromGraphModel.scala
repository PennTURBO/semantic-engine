package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet
import java.util.UUID

object DrivetrainProcessFromGraphModel extends ProjectwideGlobals
{
    def runProcess(gmCxn: RepositoryConnection, process: String, instantiation: String, globalUUID: String): String =
    {
        val localUUID = java.util.UUID.randomUUID().toString.replaceAll("-","")
        
        val inputs = getInputs(gmCxn, process)
        val outputs = getOutputs(gmCxn, process)
        val binds = getBind(gmCxn, process)
        
        createInsertClause(outputs) + createWhereClause(inputs) + createBindClause(binds, localUUID, globalUUID, instantiation)
    }
    
    def createBindClause(binds: ArrayBuffer[ArrayBuffer[Value]], localUUID: String, globalUUID: String, instantiation: String): String =
    {
        var bindClause = ""
        for (rule <- binds)
        {
            var sparqlBind = rule(1).toString.replaceAll("replacement", rule(0).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_"))
                                         .replaceAll("localUUID", localUUID)
                                         .replaceAll("globalUUID", globalUUID)
                                         .replaceAll("mainExpansionTypeVariableName", rule(4).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_"))
                                         .replaceAll("instantiationPlaceholder", "\"" + instantiation + "\"")
            if (sparqlBind.contains("dependent")) sparqlBind = sparqlBind.replaceAll("dependent",rule(3).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_"))
            if (sparqlBind.contains("original")) sparqlBind = sparqlBind.replaceAll("original",rule(2).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_"))
            
            bindClause += sparqlBind.substring(1).split("\"\\^")(0) + "\n"
        }
        bindClause + "}"
    }
    
    def createInsertClause(outputs: ArrayBuffer[ArrayBuffer[Value]]): String =
    {
        if (outputs.size == 0) throw new RuntimeException("Received a list of 0 outputs")
        var insertClause = "INSERT { Graph <" + outputs(0)(5) + ">{\n"
        var typeSet = new HashSet[Value]
        for (triple <- outputs)
        {
            val subjectVariable = triple(0).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_")
            val objectVariable = triple(2).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_")
            insertClause += "?" + subjectVariable + " <" + triple(1).toString + "> ?" + objectVariable + " .\n"
            if (triple(3) != null && !typeSet.contains(triple(0)))
            {
                insertClause += "?" + subjectVariable + " a <" + triple(0) + "> .\n"
                typeSet += triple(0)
            }
            if (triple(4) != null && !typeSet.contains(triple(2)))
            {
                insertClause += "?" + objectVariable + " a <" + triple(2) + "> .\n"
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
        var typeSet = new HashSet[Value]
        for (triple <- inputs)
        {
            var required = true
            if (triple(6).toString == "\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>") required = false
            if (!required) whereClause += "OPTIONAL { "
            val subjectVariable = triple(0).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_")
            val objectVariable = triple(2).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_")
            whereClause += "?" + subjectVariable + " <" + triple(1).toString + "> ?" + objectVariable + " ."
            if (!required) whereClause += "}"
            whereClause += "\n"
            if (triple(3) != null && !typeSet.contains(triple(0)))
            {
                whereClause += "?" + subjectVariable + " a <" + triple(0) + "> .\n"
                typeSet += triple(0)
            }
            if (triple(4) != null && !typeSet.contains(triple(2)))
            {
                whereClause += "?" + objectVariable + " a <" + triple(2) + "> .\n"
                typeSet += triple(2)
            }
        }
        whereClause += "}\n"
        whereClause
    }
    
    def getInputs(cxn: RepositoryConnection, process: String): ArrayBuffer[ArrayBuffer[Value]] =
    {
       val query = s"""
         
         Select ?subject ?predicate ?object ?subjectType ?objectType ?graph ?required
         Where
         {
            ?connection graph:inputTo <$process> .
            # ?connection a graph:Connection .
            <$process> graph:inputNamedGraph ?graph .
            ?connection graph:subject ?subject .
            ?connection graph:predicate ?predicate .
            ?connection graph:object ?object .
            ?connection graph:required ?required .
            
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
       
       update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, Array("subject", "predicate", "object", "subjectType", "objectType", "graph", "required"))
    }
    
    def getOutputs(cxn: RepositoryConnection, process: String): ArrayBuffer[ArrayBuffer[Value]] =
    {
       val query = s"""
         
         Select ?subject ?predicate ?object ?subjectType ?objectType ?graph
         Where
         {
            ?connection graph:outputOf <$process> .
            # ?connection a graph:Connection .
            <$process> graph:outputNamedGraph ?graph .
            ?connection graph:subject ?subject .
            ?connection graph:predicate ?predicate .
            ?connection graph:object ?object .
            
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
          
          Select distinct ?expandedEntity ?sparqlString ?shortcutEntity ?dependee ?baseExpansionType
          Where
          {
              ?connection graph:outputOf <$process> .
              # ?connection a graph:Connection .
              
              {
                  {
                      ?connection graph:subject ?expandedEntity .
                  }
                  Union
                  {
                      ?connection graph:object ?expandedEntity .
                  }
              }
              
              ?expansionRule a graph:ExpansionRule .
              ?expansionRule graph:creates ?expandedEntity .
              ?expansionRule graph:usesLogic ?logic .
              ?expansionRule graph:basedOn ?baseExpansionType .
              ?logic graph:usesSparql ?sparqlString .
              
              Optional
              {
                  ?expansionRule graph:hasShortcutSource ?shortcutEntity .
              }
              Optional
              {
                  ?expansionRule graph:dependsOn ?dependee .
              }
          }
          
        """
        
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, Array("expandedEntity", "sparqlString", "shortcutEntity", "dependee", "baseExpansionType"))
    }
}