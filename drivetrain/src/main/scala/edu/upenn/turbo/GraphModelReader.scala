package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.slf4j.LoggerFactory

class GraphModelReader
{   
    val logger = LoggerFactory.getLogger(getClass)
    
    def getInputs(process: String): ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]] =
    {
       var variablesToSelect = ""
       for (key <- Globals.requiredInputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select distinct $variablesToSelect
         
         Where
         {
              Values ?${Globals.INPUTTYPE} {drivetrain:hasRequiredInput drivetrain:hasOptionalInput}
              <$process> ?${Globals.INPUTTYPE} ?${Globals.CONNECTIONNAME} .
              ?${Globals.CONNECTIONNAME} a ?${Globals.CONNECTIONRECIPETYPE} .
              ?${Globals.CONNECTIONRECIPETYPE} rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              <$process> drivetrain:inputNamedGraph ?${Globals.GRAPH} .
              ?${Globals.CONNECTIONNAME} drivetrain:subject ?${Globals.SUBJECT} .
              ?${Globals.CONNECTIONNAME} drivetrain:predicate ?${Globals.PREDICATE} .
              ?${Globals.CONNECTIONNAME} drivetrain:object ?${Globals.OBJECT} .
              ?${Globals.CONNECTIONNAME} drivetrain:cardinality ?${Globals.MULTIPLICITY} .
              
              Optional
              {
                  ?${Globals.CONNECTIONNAME} drivetrain:subjectUsesContext ?${Globals.SUBJECTCONTEXT} .
                  ?${Globals.SUBJECT} drivetrain:hasPossibleContext ?${Globals.SUBJECTCONTEXT} .
                  ?${Globals.SUBJECTCONTEXT} a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?${Globals.CONNECTIONNAME} drivetrain:objectUsesContext ?${Globals.OBJECTCONTEXT} .
                  ?${Globals.OBJECT} drivetrain:hasPossibleContext ?${Globals.OBJECTCONTEXT} .
                  ?${Globals.OBJECTCONTEXT} a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?${Globals.CONNECTIONNAME} drivetrain:partOf ?${Globals.OPTIONALGROUP} .
                  ?${Globals.OPTIONALGROUP} a drivetrain:TurboGraphOptionalGroup .
                  <$process> drivetrain:buildsOptionalGroup ?${Globals.OPTIONALGROUP} .
              }
              Optional
              {
                  ?${Globals.CONNECTIONNAME} drivetrain:partOf ?${Globals.MINUSGROUP} .
                  ?${Globals.MINUSGROUP} a drivetrain:TurboGraphMinusGroup .
                  <$process> drivetrain:buildsMinusGroup ?${Globals.MINUSGROUP} .
              }
              Optional
              {
                  # this feature is a little sketcky. What if the creatingProcess is not queued? What if it is created by multiple processes?
                  ?creatingProcess drivetrain:hasOutput ?${Globals.CONNECTIONNAME} .
                  ?creatingProcess drivetrain:outputNamedGraph ?${Globals.GRAPHOFCREATINGPROCESS} .
              }
              Optional
              {
                  ?${Globals.CONNECTIONNAME} drivetrain:referencedInGraph ?${Globals.GRAPHOFORIGIN} .
              }
              Optional
              {
                  ?${Globals.OBJECT} a drivetrain:ClassResourceList .
                  BIND (true AS ?${Globals.OBJECTADESCRIBER})
              }
              Optional
              {
                  ?${Globals.SUBJECT} a drivetrain:ClassResourceList .
                  BIND (true AS ?${Globals.SUBJECTADESCRIBER})
              }
              Optional
              {
                  ?${Globals.SUBJECT} a drivetrain:UntypedInstance .
                  BIND (true as ?${Globals.SUBJECTUNTYPED})
              }
              Optional
              {
                  ?${Globals.OBJECT} a drivetrain:UntypedInstance .
                  BIND (true as ?${Globals.OBJECTUNTYPED})
              }
              Optional
              {
                  ?${Globals.CONNECTIONNAME} drivetrain:mustExecuteIf ?${Globals.REQUIREMENT} .
              }
              Optional
              {
                  ?${Globals.CONNECTIONNAME} drivetrain:predicateSuffix ?suffix .
                  ?suffix a drivetrain:PredicateSuffixSymbol .
                  ?suffix drivetrain:usesSparqlOperator ?${Globals.SUFFIXOPERATOR} .
              }
              Optional
              {
                  ?${Globals.OBJECT} a ?${Globals.GRAPHLITERALTYPE} .
                  ?${Globals.GRAPHLITERALTYPE} rdfs:subClassOf* drivetrain:LiteralResourceList .
                  minus
                  {
                      ?OBJECT a ?GRAPHLITERALTYPE2 .
                      ?GRAPHLITERALTYPE2 rdfs:subClassOf+ ?GRAPHLITERALTYPE .
                  }
              }
              BIND (isLiteral(?${Globals.OBJECT}) as ?${Globals.OBJECTALITERALVALUE})
         }
         
         """
       //println(query)          
       SparqlUpdater.querySparqlAndUnpackToListOfMap(Globals.gmCxn, query)
    }

    def getRemovals(process: String): ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]] =
    {
       var variablesToSelect = ""
       for (key <- Globals.requiredOutputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select distinct $variablesToSelect
         
         Where
         {
              <$process> drivetrain:removes ?${Globals.CONNECTIONNAME} .
              ?${Globals.CONNECTIONNAME} a ?${Globals.CONNECTIONRECIPETYPE} .
              ?${Globals.CONNECTIONRECIPETYPE} rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              <$process> drivetrain:outputNamedGraph ?${Globals.GRAPH} .
              ?${Globals.CONNECTIONNAME} drivetrain:subject ?${Globals.SUBJECT} .
              ?${Globals.CONNECTIONNAME} drivetrain:predicate ?${Globals.PREDICATE} .
              ?${Globals.CONNECTIONNAME} drivetrain:object ?${Globals.OBJECT} .
              ?${Globals.CONNECTIONNAME} drivetrain:cardinality ?${Globals.MULTIPLICITY} .
              Optional
              {
                  ?${Globals.OBJECT} a drivetrain:ClassResourceList .
                  BIND (true AS ?${Globals.OBJECTADESCRIBER})
              }
              Optional
              {
                  ?${Globals.SUBJECT} a drivetrain:ClassResourceList .
                  BIND (true AS ?${Globals.SUBJECTADESCRIBER})
              }
              BIND (isLiteral(?${Globals.OBJECT}) as ?${Globals.OBJECTALITERALVALUE})
         }
         
         """
       
       SparqlUpdater.querySparqlAndUnpackToListOfMap(Globals.gmCxn, query)
    }
    
    def getOutputs(process: String): ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]] =
    {
       var variablesToSelect = ""
       for (key <- Globals.requiredOutputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select distinct $variablesToSelect
         Where
         {
              <$process> drivetrain:hasOutput ?${Globals.CONNECTIONNAME} .
              ?${Globals.CONNECTIONNAME} a ?${Globals.CONNECTIONRECIPETYPE} .
              ?${Globals.CONNECTIONRECIPETYPE} rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              <$process> drivetrain:outputNamedGraph ?${Globals.GRAPH} .
              ?${Globals.CONNECTIONNAME} drivetrain:subject ?${Globals.SUBJECT} .
              ?${Globals.CONNECTIONNAME} drivetrain:predicate ?${Globals.PREDICATE} .
              ?${Globals.CONNECTIONNAME} drivetrain:object ?${Globals.OBJECT} .
              ?${Globals.CONNECTIONNAME} drivetrain:cardinality ?${Globals.MULTIPLICITY} .

              Optional
              {
                  ?${Globals.CONNECTIONNAME} drivetrain:subjectUsesContext ?${Globals.SUBJECTCONTEXT} .
                  ?${Globals.SUBJECT} drivetrain:hasPossibleContext ?${Globals.SUBJECTCONTEXT} .
                  ?${Globals.SUBJECTCONTEXT} a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?${Globals.CONNECTIONNAME} drivetrain:objectUsesContext ?${Globals.OBJECTCONTEXT} .
                  ?${Globals.OBJECT} drivetrain:hasPossibleContext ?${Globals.OBJECTCONTEXT} .
                  ?${Globals.OBJECTCONTEXT} a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?${Globals.SUBJECT} drivetrain:usesCustomVariableManipulationRule ?subjectRuleDenoter .
                  ?subjectRuleDenoter drivetrain:usesSparql ?${Globals.SUBJECTRULE} .
              }
              Optional
              {
                  ?${Globals.OBJECT} drivetrain:usesCustomVariableManipulationRule ?objectRuleDenoter .
                  ?objectRuleDenoter drivetrain:usesSparql ?${Globals.OBJECTRULE} .
              }
              Optional
              {
                  ?${Globals.SUBJECT} a drivetrain:ClassResourceList .
                  BIND (true as ?${Globals.SUBJECTADESCRIBER})
              }
              Optional
              {
                  ?${Globals.OBJECT} a drivetrain:ClassResourceList .
                  BIND (true as ?${Globals.OBJECTADESCRIBER})
              }
              Optional
              {
                  ?${Globals.SUBJECT} a drivetrain:UntypedInstance .
                  BIND (true as ?${Globals.SUBJECTUNTYPED})
              }
              Optional
              {
                  ?${Globals.OBJECT} a drivetrain:UntypedInstance .
                  BIND (true as ?${Globals.OBJECTUNTYPED})
              }
              Optional
              {
                  ?recipe drivetrain:objectRequiredToCreate ?${Globals.OBJECT} .
                  <$process> ?INPUTTO ?recipe .
                  ?recipe drivetrain:object ?OBJECTDEPENDEE1 .
                  FILTER(?INPUTTO IN(drivetrain:hasRequiredInput, drivetrain:hasOptionalInput))
              }
              Optional
              {
                  ?recipe drivetrain:subjectRequiredToCreate ?${Globals.OBJECT} .
                  <$process> ?INPUTTO ?recipe .
                  ?recipe drivetrain:subject ?OBJECTDEPENDEE2 .
                  FILTER(?INPUTTO IN(drivetrain:hasRequiredInput, drivetrain:hasOptionalInput))
              }
              Optional
              {
                  ?recipe drivetrain:objectRequiredToCreate ?${Globals.SUBJECT} .
                  <$process> ?INPUTTO ?recipe .
                  ?recipe drivetrain:object ?SUBJECTDEPENDEE1 .
                  FILTER(?INPUTTO IN(drivetrain:hasRequiredInput, drivetrain:hasOptionalInput))
              }
              Optional
              {
                  ?recipe drivetrain:subjectRequiredToCreate ?${Globals.SUBJECT} .
                  <$process> ?INPUTTO ?recipe .
                  ?recipe drivetrain:subject ?SUBJECTDEPENDEE2 .
                  FILTER(?INPUTTO IN(drivetrain:hasRequiredInput, drivetrain:hasOptionalInput))
              }
              BIND (isLiteral(?${Globals.OBJECT}) as ?${Globals.OBJECTALITERALVALUE})
              BIND(IF (BOUND (?SUBJECTDEPENDEE1), ?SUBJECTDEPENDEE1, ?SUBJECTDEPENDEE2) AS ?${Globals.SUBJECTDEPENDEE})
              BIND(IF (BOUND (?OBJECTDEPENDEE1), ?OBJECTDEPENDEE1, ?OBJECTDEPENDEE2) AS ?${Globals.OBJECTDEPENDEE})
         }
         
         """
       //println(query)
       SparqlUpdater.querySparqlAndUnpackToListOfMap(Globals.gmCxn, query)
    }
}