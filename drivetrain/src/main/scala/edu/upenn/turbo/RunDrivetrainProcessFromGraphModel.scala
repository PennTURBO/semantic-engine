package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import java.util.UUID

class RunDrivetrainProcessFromGraphModel extends ProjectwideGlobals
{   
    def runAllDrivetrainProcesses(cxn: RepositoryConnection, gmCxn: RepositoryConnection, globalUUID: String, instantiation: String = helper.genPmbbIRI())
    {
        DrivetrainProcessFromGraphModel.setInstantiation(instantiation)
        DrivetrainProcessFromGraphModel.setGlobalUUID(globalUUID)
        DrivetrainProcessFromGraphModel.setGraphModelConnection(gmCxn)
        DrivetrainProcessFromGraphModel.setOutputRepositoryConnection(cxn)
        
        //load the TURBO ontology
        OntologyLoader.addOntologyFromUrl(cxn)
      
        val orderedProcessList: ArrayBuffer[String] = getAllProcessesInOrder(gmCxn)
        
        logger.info("Drivetrain will now run the following processes in this order:")
        for (a <- orderedProcessList) logger.info(a)
        
        for (process <- orderedProcessList) DrivetrainProcessFromGraphModel.runProcess(process)
    }

    def getAllProcessesInOrder(gmCxn: RepositoryConnection): ArrayBuffer[String] =
    {
        val getFirstProcess: String = """
          select ?firstProcess where
          {
              ?firstProcess a turbo:TurboGraphProcess .
              Minus
              {
                  ?something turbo:precedes ?firstProcess .
              }
          }
        """
        
        val getProcesses: String = """
          select ?precedingProcess ?succeedingProcess where
          {
              ?precedingProcess a turbo:TurboGraphProcess .
              ?succeedingProcess a turbo:TurboGraphProcess .
              ?precedingProcess turbo:precedes ?succeedingProcess .
          }
        """
        
        val firstProcessRes = update.querySparqlAndUnpackTuple(gmCxn, getFirstProcess, "firstProcess")
        if (firstProcessRes.size > 1) throw new RuntimeException ("Multiple starting processes discovered in graph model")
        val res = update.querySparqlAndUnpackTuple(gmCxn, getProcesses, Array("precedingProcess", "succeedingProcess"))
        var currProcess: String = firstProcessRes(0)
        var processesInOrder: ArrayBuffer[String] = new ArrayBuffer[String]
        var processMap: HashMap[String, String] = new HashMap[String, String]
        
        for (a <- res) processMap += a(0).toString -> a(1).toString
        
        while (currProcess != null)
        {
            processesInOrder += currProcess
            if (processMap.contains(currProcess)) currProcess = processMap(currProcess)
            else currProcess = null
        }
        processesInOrder
    }
}