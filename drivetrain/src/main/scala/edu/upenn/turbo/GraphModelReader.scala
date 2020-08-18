package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import org.eclipse.rdf4j.repository.RepositoryConnection

class GraphModelReader(cxn: RepositoryConnection) extends ProjectwideGlobals
{   
    this.gmCxn = cxn
    
    def getInputs(process: String): ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]] =
    {
       var variablesToSelect = ""
       for (key <- requiredInputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select distinct $variablesToSelect
         
         Where
         {
              Values ?$INPUTTYPE {drivetrain:hasRequiredInput drivetrain:hasOptionalInput}
              <$process> ?$INPUTTYPE ?$CONNECTIONNAME .
              ?$CONNECTIONNAME a ?$CONNECTIONRECIPETYPE .
              ?$CONNECTIONRECIPETYPE rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              <$process> drivetrain:inputNamedGraph ?$GRAPH .
              ?$CONNECTIONNAME drivetrain:subject ?$SUBJECT .
              ?$CONNECTIONNAME drivetrain:predicate ?$PREDICATE .
              ?$CONNECTIONNAME drivetrain:object ?$OBJECT .
              ?$CONNECTIONNAME drivetrain:cardinality ?$MULTIPLICITY .
              
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:subjectUsesContext ?$SUBJECTCONTEXT .
                  ?$SUBJECT drivetrain:hasPossibleContext ?$SUBJECTCONTEXT .
                  ?$SUBJECTCONTEXT a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:objectUsesContext ?$OBJECTCONTEXT .
                  ?$OBJECT drivetrain:hasPossibleContext ?$OBJECTCONTEXT .
                  ?$OBJECTCONTEXT a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:partOf ?$OPTIONALGROUP .
                  ?$OPTIONALGROUP a drivetrain:TurboGraphOptionalGroup .
                  <$process> drivetrain:buildsOptionalGroup ?$OPTIONALGROUP .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:partOf ?$MINUSGROUP .
                  ?$MINUSGROUP a drivetrain:TurboGraphMinusGroup .
                  <$process> drivetrain:buildsMinusGroup ?$MINUSGROUP .
              }
              Optional
              {
                  # this feature is a little sketcky. What if the creatingProcess is not queued? What if it is created by multiple processes?
                  ?creatingProcess drivetrain:hasOutput ?$CONNECTIONNAME .
                  ?creatingProcess drivetrain:outputNamedGraph ?$GRAPHOFCREATINGPROCESS .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:referencedInGraph ?$GRAPHOFORIGIN .
              }
              Optional
              {
                  ?$OBJECT a drivetrain:ClassResourceList .
                  BIND (true AS ?$OBJECTADESCRIBER)
              }
              Optional
              {
                  ?$SUBJECT a drivetrain:ClassResourceList .
                  BIND (true AS ?$SUBJECTADESCRIBER)
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:mustExecuteIf ?$REQUIREMENT .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:predicateSuffix ?suffix .
                  ?suffix a drivetrain:PredicateSuffixSymbol .
                  ?suffix drivetrain:usesSparqlOperator ?$SUFFIXOPERATOR .
              }
              Optional
              {
                  ?$OBJECT a ?$GRAPHLITERALTYPE .
                  ?$GRAPHLITERALTYPE rdfs:subClassOf* drivetrain:LiteralResourceList .
                  minus
                  {
                      ?OBJECT a ?GRAPHLITERALTYPE2 .
                      ?GRAPHLITERALTYPE2 rdfs:subClassOf+ ?GRAPHLITERALTYPE .
                  }
              }
              BIND (isLiteral(?$OBJECT) as ?$OBJECTALITERALVALUE)
         }
         
         """
       //println(query)          
       update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }

    def getRemovals(process: String): ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]] =
    {
       var variablesToSelect = ""
       for (key <- requiredOutputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select distinct $variablesToSelect
         
         Where
         {
              <$process> drivetrain:removes ?$CONNECTIONNAME .
              ?$CONNECTIONNAME a ?$CONNECTIONRECIPETYPE .
              ?$CONNECTIONRECIPETYPE rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              <$process> drivetrain:outputNamedGraph ?$GRAPH .
              ?$CONNECTIONNAME drivetrain:subject ?$SUBJECT .
              ?$CONNECTIONNAME drivetrain:predicate ?$PREDICATE .
              ?$CONNECTIONNAME drivetrain:object ?$OBJECT .
              Optional
              {
                  ?$OBJECT a drivetrain:ClassResourceList .
                  BIND (true AS ?$OBJECTADESCRIBER)
              }
              Optional
              {
                  ?$SUBJECT a drivetrain:ClassResourceList .
                  BIND (true AS ?$SUBJECTADESCRIBER)
              }
              BIND (isLiteral(?$OBJECT) as ?$OBJECTALITERALVALUE)
         }
         
         """
       
       update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }
    
    def getOutputs(process: String): ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]] =
    {
       var variablesToSelect = ""
       for (key <- requiredOutputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select distinct $variablesToSelect
         Where
         {
              <http://www.itmat.upenn.edu/biobank/EncounterExpansionProcess> drivetrain:hasOutput ?CONNECTIONNAME .
              ?CONNECTIONNAME a ?CONNECTIONRECIPETYPE .
              ?CONNECTIONRECIPETYPE rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              <http://www.itmat.upenn.edu/biobank/EncounterExpansionProcess> drivetrain:outputNamedGraph ?GRAPH .
              ?CONNECTIONNAME drivetrain:subject ?SUBJECT .
              ?CONNECTIONNAME drivetrain:predicate ?PREDICATE .
              ?CONNECTIONNAME drivetrain:object ?OBJECT .
              ?CONNECTIONNAME drivetrain:cardinality ?MULTIPLICITY .

              Optional
              {
                  ?CONNECTIONNAME drivetrain:subjectUsesContext ?SUBJECTCONTEXT .
                  ?SUBJECT drivetrain:hasPossibleContext ?SUBJECTCONTEXT .
                  ?SUBJECTCONTEXT a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?CONNECTIONNAME drivetrain:objectUsesContext ?OBJECTCONTEXT .
                  ?OBJECT drivetrain:hasPossibleContext ?OBJECTCONTEXT .
                  ?OBJECTCONTEXT a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?SUBJECT drivetrain:usesCustomVariableManipulationRule ?subjectRuleDenoter .
                  ?subjectRuleDenoter drivetrain:usesSparql ?SUBJECTRULE .
              }
              Optional
              {
                  ?OBJECT drivetrain:usesCustomVariableManipulationRule ?objectRuleDenoter .
                  ?objectRuleDenoter drivetrain:usesSparql ?OBJECTRULE .
              }
              Optional
              {
                  ?SUBJECT a drivetrain:ClassResourceList .
                  BIND (true as ?SUBJECTADESCRIBER)
              }
              Optional
              {
                  ?OBJECT a drivetrain:ClassResourceList .
                  BIND (true as ?OBJECTADESCRIBER)
              }
              Optional
              {
                  ?recipe drivetrain:objectRequiredToCreate ?OBJECT .
                  <http://www.itmat.upenn.edu/biobank/EncounterExpansionProcess> ?INPUTTO ?recipe .
                  ?recipe drivetrain:object ?OBJECTDEPENDEE1 .
                FILTER(?INPUTTO IN(drivetrain:hasRequiredInput, drivetrain:hasOptionalInput))
              }
              Optional
              {
                  ?recipe drivetrain:subjectRequiredToCreate ?OBJECT .
                  <http://www.itmat.upenn.edu/biobank/EncounterExpansionProcess> ?INPUTTO ?recipe .
                  ?recipe drivetrain:subject ?OBJECTDEPENDEE2 .
                FILTER(?INPUTTO IN(drivetrain:hasRequiredInput, drivetrain:hasOptionalInput))
              }
              Optional
              {
                  ?recipe drivetrain:objectRequiredToCreate ?SUBJECT .
                  <http://www.itmat.upenn.edu/biobank/EncounterExpansionProcess> ?INPUTTO ?recipe .
                  ?recipe drivetrain:object ?SUBJECTDEPENDEE1 .
              FILTER(?INPUTTO IN(drivetrain:hasRequiredInput, drivetrain:hasOptionalInput))
              }
              Optional
              {
                  ?recipe drivetrain:subjectRequiredToCreate ?SUBJECT .
                  <http://www.itmat.upenn.edu/biobank/EncounterExpansionProcess> ?INPUTTO ?recipe .
                  ?recipe drivetrain:subject ?SUBJECTDEPENDEE2 .
              FILTER(?INPUTTO IN(drivetrain:hasRequiredInput, drivetrain:hasOptionalInput))
              }
              BIND (isLiteral(?OBJECT) as ?OBJECTALITERALVALUE)
              BIND(IF (BOUND (?SUBJECTDEPENDEE1), ?SUBJECTDEPENDEE1, ?SUBJECTDEPENDEE2) AS ?SUBJECTDEPENDEE)
              BIND(IF (BOUND (?OBJECTDEPENDEE1), ?OBJECTDEPENDEE1, ?OBJECTDEPENDEE2) AS ?OBJECTDEPENDEE)
         }
         
         """
       println(query)
       update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }
}