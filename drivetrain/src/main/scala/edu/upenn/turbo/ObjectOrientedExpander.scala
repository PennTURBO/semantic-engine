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
      DrivetrainProcessFromGraphModel.runProcess(cxn, gmCxn, "http://transformunify.org/ontologies/homoSapiensExpansionProcess", instantiation, globalUUID)
      DrivetrainProcessFromGraphModel.runProcess(cxn, gmCxn, "http://transformunify.org/ontologies/healthcareEncounterExpansionProcess", instantiation, globalUUID)
  }
}