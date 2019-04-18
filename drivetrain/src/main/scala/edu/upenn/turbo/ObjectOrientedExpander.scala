package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class ObjectOrientedExpander extends ProjectwideGlobals
{   
  def runAllExpansionProcesses(cxn: RepositoryConnection, gmCxn: RepositoryConnection, globalUUID: String, instantiation: String = helper.genPmbbIRI())
  {
      DrivetrainProcessFromGraphModel.setInstantiation(instantiation)
      DrivetrainProcessFromGraphModel.setGlobalUUID(globalUUID)
      DrivetrainProcessFromGraphModel.setGraphModelConnection(gmCxn)
      DrivetrainProcessFromGraphModel.setConnection(cxn)
    
      DrivetrainProcessFromGraphModel.runProcess("http://transformunify.org/ontologies/homoSapiensExpansionProcess")
      DrivetrainProcessFromGraphModel.runProcess("http://transformunify.org/ontologies/healthcareEncounterExpansionProcess")
      DrivetrainProcessFromGraphModel.runProcess("http://transformunify.org/ontologies/biobankEncounterExpansionProcess")
      DrivetrainProcessFromGraphModel.runProcess("http://transformunify.org/ontologies/healthcareEncounterLinkingProcess")
      DrivetrainProcessFromGraphModel.runProcess("http://transformunify.org/ontologies/biobankEncounterLinkingProcess")
  }
}