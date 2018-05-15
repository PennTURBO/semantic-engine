package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.repository.RepositoryConnection

/**
 * This is the driver class for the Conclusionation processes.
 */

class Conclusionator extends ProjectwideGlobals
{
    val dobConc: BirthdateConclusionator = new BirthdateConclusionator()
    val biosexConc: BiosexConclusionator = new BiosexConclusionator()
    val bmiConc: BMIConclusionator = new BMIConclusionator()
   
   /**
    * Receives a threshold for biosex and date of birth conclusionation. Generates the IRI for the Conclusionated Named Graph which will be used
    * for all dependencies of this conclusionation process, as well as the IRIs for the master conclusionation and master plan. Calls all 
    * dependent conclusionation processes and returns the IRI for the named graph. Also enforces that the thresholds are appropriate, and defaults
    * to using a threshold of 1 if threshold is not appropriate.
    */
   def runConclusionationProcess(cxn: RepositoryConnection, biosexThreshold: Double, dateofbirthThreshold: Double): IRI =
    {
        val f: ValueFactory = cxn.getValueFactory()
        val conclusionationNamedGraph: IRI = f.createIRI("http://www.itmat.upenn.edu/biobank/Conclusionations" + helper.getCurrentTimestamp(""))
        val masterConclusionation: IRI = helper.genPmbbIRI(cxn)
        val masterPlanspec: IRI = helper.genPmbbIRI(cxn)
        val masterPlan: IRI = helper.genPmbbIRI(cxn)
        logger.info("conclusionated named graph : " + conclusionationNamedGraph)
        var biosexThreshold_mut = biosexThreshold
        var dateofbirthThreshold_mut = dateofbirthThreshold
        if (biosexThreshold <= .5 ||biosexThreshold > 1)
        {
            biosexThreshold_mut = 1
            logger.info("Invalid biosex threshold supplied. Defaulting to 1")
        }
        if (dateofbirthThreshold <= .5 || biosexThreshold > 1)
        {
            dateofbirthThreshold_mut = 1
            logger.info("Invalid biosex threshold supplied. Defaulting to 1")
        }
        dobConc.conclusionateBirthdate(cxn, conclusionationNamedGraph, dateofbirthThreshold_mut, masterConclusionation, masterPlanspec, masterPlan)
        biosexConc.conclusionateBiosex(cxn, conclusionationNamedGraph, biosexThreshold_mut, masterConclusionation, masterPlanspec, masterPlan)
        bmiConc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        conclusionationNamedGraph
    }
}
