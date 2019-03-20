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
    def runProcess(cxn: RepositoryConnection, process: String)
    {
        val inputs = getInputs(cxn, process)
        val outputs = getOutputs(cxn, process)
        println(createInsertClause(outputs) + createWhereClause(inputs))
    }
    
    def createInsertClause(outputs: ArrayBuffer[ArrayBuffer[Value]]): String =
    {
        if (outputs.size == 0) throw new RuntimeException("Received a list of 0 outputs")
        var insertClause = "INSERT { Graph <" + outputs(0)(5) + ">{\n"
        var typeSet = new HashSet[Value]
        for (triple <- outputs)
        {
            val subjectVariable = triple(0).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_") + "_OUTPUT"
            val objectVariable = triple(2).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_") + "_OUTPUT"
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
            val subjectVariable = triple(0).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_") + "_OUTPUT"
            val objectVariable = triple(2).toString.replaceAll("\\/","_").replaceAll("\\:","").replaceAll("\\.","_") + "_OUTPUT"
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
        whereClause += "}}\n"
        whereClause
    }
    
    def getInputs(cxn: RepositoryConnection, process: String): ArrayBuffer[ArrayBuffer[Value]] =
    {
       val query = s"""
         
         Select ?subject ?predicate ?object ?subjectType ?objectType ?graph ?required
         Where
         {
            ?s graph:inputTo <$process> .
            <$process> graph:inputNamedGraph ?graph .
            ?s graph:subject ?subject .
            ?s graph:predicate ?predicate .
            ?s graph:object ?object .
            ?s graph:required ?required .
            
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
            ?s graph:outputOf <$process> .
            <$process> graph:outputNamedGraph ?graph .
            ?s graph:subject ?subject .
            ?s graph:predicate ?predicate .
            ?s graph:object ?object .
            
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
}