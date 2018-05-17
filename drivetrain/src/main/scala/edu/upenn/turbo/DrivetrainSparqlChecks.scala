package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection

/**
 * Addresses validation of the data within a Graph DB repository in accordance with axioms specific to the TURBO project's ontologized models. Containing methods may be run at various 
 * stages in the Drivetrain stack to ensure that errors do not exist within the dataset. 
 */
class DrivetrainSparqlChecks extends ProjectwideGlobals
{
    val precheck: SparqlPreExpansionChecks = new SparqlPreExpansionChecks
    val postcheck: SparqlPostExpansionChecks = new SparqlPostExpansionChecks
    
    /**
     * Runs a suite of checks specific to the pre-expansion stage of the Drivetrain stack. 
     * 
     * @return a Boolean, true if all checks passed, false if any check failed
     */
    def preExpansionChecks (cxn: RepositoryConnection): Boolean =
    {
        val graphsString: String = helper.generateShortcutNamedGraphsString(cxn)
        val graphsStringAsFrom: String = helper.generateShortcutNamedGraphsString(cxn, true)
        logger.info("graph string: " + graphsString)
        logger.info("graph string with from: " + graphsStringAsFrom)
        
        logger.info("starting pre-expansion checks")
        var proceed: Boolean = precheck.checkAllSubjectsHaveAType(cxn, graphsString)
        if (proceed) proceed = precheck.checkForUnexpectedClasses(cxn, graphsString)
        if (proceed) proceed = precheck.checkAllObjectsAreLiterals(cxn, graphsString)
        //if (proceed) proceed = precheck.checkForPropertiesOutOfRange(cxn, graphsString)
        if (proceed) proceed = precheck.checkForUnexpectedPredicates(cxn, graphsString)
        if (proceed) proceed = precheck.checkBiosexURIsAreValid(cxn, graphsStringAsFrom)
        if (proceed) proceed = precheck.checkEncounterRegistryURIsAreValid(cxn, graphsString)
        if (proceed) proceed = precheck.checkForRequiredParticipantShortcuts(cxn, graphsString)
        if (proceed) proceed = precheck.checkForRequiredHealthcareEncounterShortcuts(cxn, graphsStringAsFrom)
        if (proceed) proceed = precheck.checkForRequiredBiobankEncounterShortcuts(cxn, graphsString)
        if (proceed) proceed = precheck.checkForValidParticipantBirthShortcuts(cxn, graphsStringAsFrom)
        if (proceed) proceed = precheck.checkForValidParticipantBiosexShortcuts(cxn, graphsStringAsFrom)
        if (proceed) proceed = precheck.checkForValidHealthcareEncounterDateShortcuts(cxn, graphsStringAsFrom)
        if (proceed) proceed = precheck.checkForValidBiobankEncounterDateShortcuts(cxn, graphsStringAsFrom)
        if (proceed) proceed = precheck.checkForValidEncounterDiagnosisShortcuts(cxn, graphsString)
        if (proceed) proceed = precheck.checkForValidEncounterPrescriptionShortcuts(cxn, graphsString)
        if (proceed) proceed = precheck.checkForMultipleParticipantDependentNodes(cxn, graphsStringAsFrom)
        if (proceed) proceed = precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, graphsStringAsFrom)
        if (proceed) proceed = precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, graphsStringAsFrom)
        if (proceed) proceed = precheck.checkForMultiplePrescriptionDependentNodes(cxn, graphsStringAsFrom)
        if (proceed) proceed = precheck.checkForMultipleDiagnosisDependentNodes(cxn, graphsStringAsFrom)
        logger.info("pre-expansion checks returning " + proceed)
        proceed
    }
    
    /**
     * Runs a suite of checks specific to the post-expansion stage of the Drivetrain stack. A named graph is passed in by the calling
     * method which becomes the named graph which this method operates over. Checks 1 - 15 run no matter the named graph. Check 16 
     * runs only if the named graph receives as input is pmbb:expanded. Checks 17 - 19 run only if the graph received as input is a
     * Conclusionated named graph.
     * 
     * @return a Boolean, true if all checks passed, false if any check failed
     */
    def postExpansionChecks (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        /** 
         *  add checks to:
         *  1. check that all typed literals are the correct type according to their associated predicate
         *  2. check that referent-tracked encounter dates don't have multiple and different date values (could happen bc we are no longer tracking on enc date)
         *  3. No  Graph Builder post-expansion!
         *  4. Make sure everything is linked to its appropriate dataset in the correct way
         *  5. Check for reftracked encounter starts with multiple reftracked dates attached (indicates errors in the data)
         */
        logger.info("starting post-expansion checks")
        logger.info("starting check 1")
        var proceed: Boolean = postcheck.checkForInvalidClasses(cxn, namedGraph, stage)
        logger.info("starting check 2")
        if (proceed) proceed = postcheck.checkParticipantsForRequiredDependents(cxn, namedGraph, stage)
        logger.info("starting check 3")
        if (proceed) proceed = postcheck.checkHealthcareEncountersForRequiredDependents(cxn, namedGraph, stage)
        logger.info("starting check 4")
        if (proceed) proceed = postcheck.checkBiobankEncountersForRequiredDependents(cxn, namedGraph, stage)
        logger.info("starting check 5")
        if (proceed) proceed = postcheck.checkForInvalidPredicates(cxn, namedGraph, stage)
        logger.info("starting check 6")
        if (proceed) proceed = postcheck.checkForUnidentifiedRegistryIDs(cxn, namedGraph, stage)
        logger.info("starting check 7")
        if (proceed) proceed = postcheck.checkForUnparseableOrUntaggedDates(cxn, namedGraph, stage)
        logger.info("starting check 8")
        if (proceed) proceed = postcheck.checkThatDateLiteralsHaveValidDatePredicates(cxn, namedGraph, stage)
        logger.info("starting check 9")
        if (proceed) proceed = postcheck.checkAllDatesAreReasonable(cxn, namedGraph, stage)
        logger.info("starting check 10")
        if (proceed) proceed = postcheck.checkForSingleInstantiationProcess(cxn, namedGraph, stage)
        logger.info("starting check 11")
        if (proceed) proceed = postcheck.checkAllInstantiationProcessesAreAttachedToDatasets(cxn, namedGraph, stage)
        logger.info("starting check 12")
        if (proceed) proceed = postcheck.checkForSubclassRelationships(cxn, namedGraph, stage)
        logger.info("starting check 13")
        if (proceed) proceed = postcheck.checkObjectPropertiesDoNotHaveLiteralObjects(cxn, namedGraph, stage)
        logger.info("starting check 14")
        if (proceed) proceed = postcheck.checkDatatypePropertiesDoNotHaveUriObjects(cxn, namedGraph, stage)
        logger.info("starting check 15")
        if (proceed) proceed = postcheck.noShortcutRelationsInGraph(cxn, namedGraph, stage)
        //checks specific to named graphs should be called inside the following blocks
        if (namedGraph == "http://www.itmat.upenn.edu/biobank/expanded")
        {
            logger.info("starting check 16 - specific to expanded graph")
            if (proceed) proceed = postcheck.allEntitiesAreReftracked(cxn, namedGraph, stage)
        }
        //this limits to the scope to just the conclusionation graph
        if (namedGraph.startsWith("http://www.itmat.upenn.edu/biobank/Conclusionations"))
        {
            logger.info("starting check 17 - 19 - specific to conclusionated named graph")
            if (proceed) proceed = postcheck.allBMIsAreConclusionated(cxn, namedGraph, stage)
            if (proceed) proceed = postcheck.allBiosexAreConclusionated(cxn, namedGraph, stage)
            if (proceed) proceed = postcheck.allBirthsAreConclusionated(cxn, namedGraph, stage)
            if (proceed) proceed = postcheck.noHealthcareEncountersWithMultipleDates(cxn, namedGraph, stage)
            if (proceed) proceed = postcheck.noBiobankEncountersWithMultipleDates(cxn, namedGraph, stage)
        }
        logger.info(stage + " checks returning " + proceed + " for " + namedGraph.toString)
        proceed
    }
}