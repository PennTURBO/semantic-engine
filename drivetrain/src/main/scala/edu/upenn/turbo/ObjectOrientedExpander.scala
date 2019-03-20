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
    /*def runAllExpansionProcesses(cxn: RepositoryConnection, globalUUID: String, instantiation: String = helper.genPmbbIRI())
    {
        val namedGraphsList = helper.generateNamedGraphsListFromPrefix(cxn)
        
        for (namedGraph <- namedGraphsList)
        {
          val shortcutHomoSapiensInstance = ShortcutHomoSapiens.create(instantiation, namedGraph, globalUUID, false)
          val participantExpansion = shortcutHomoSapiensInstance.expand()
          
          val shortcutBiobankEncounterInstance = ShortcutBiobankEncounter.create(instantiation, namedGraph, globalUUID)
          val biobankEncounterExpansion = shortcutBiobankEncounterInstance.expand()
          
          val shortcutHealthcareEncounterInstance = ShortcutHealthcareEncounter.create(instantiation, namedGraph, globalUUID)
          val healthcareEncounterExpansion = shortcutHealthcareEncounterInstance.expand()
          
          val shortcutHomoSapiensToBiobankEncounterInstance = ShortcutHomoSapiensToBiobankEncounterJoin.create(instantiation, namedGraph, globalUUID)
          val homoSapiensToBbEncLinkExpansion = shortcutHomoSapiensToBiobankEncounterInstance.expand()
          
          val shortcutHomoSapiensToHealthcareEncounterInstance = ShortcutHomoSapiensToHealthcareEncounterJoin.create(instantiation, namedGraph, globalUUID)
          val homoSapiensToHcEncLinkExpansion = shortcutHomoSapiensToHealthcareEncounterInstance.expand()
        println(homoSapiensToBbEncLinkExpansion)
          update.updateSparql(cxn, participantExpansion)
          update.updateSparql(cxn, biobankEncounterExpansion)
          update.updateSparql(cxn, healthcareEncounterExpansion)
          update.updateSparql(cxn, homoSapiensToBbEncLinkExpansion)
          update.updateSparql(cxn, homoSapiensToHcEncLinkExpansion)
        }
    }*/
  def runAllExpansionProcesses(cxn: RepositoryConnection, globalUUID: String, instantiation: String = helper.genPmbbIRI())
  {
      DrivetrainProcessFromGraphModel.runProcess(cxn, "http://haydensgraph.org/homoSapiensExpansionProcess")
  }
}