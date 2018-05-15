package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection

/**
 * This class contains methods relating to the process of generic Referent Tracking. 
 */
class ReferentTracker extends ProjectwideGlobals
{
    val partReftrackInst: ParticipantReferentTracker = new ParticipantReferentTracker()
    val encReftrackInst: EncounterReferentTracker = new EncounterReferentTracker()
    
    /**
     * This is the driver method for the Referent Tracking process, which calls methods to referent track
     * each type of entity to be referent tracked.
     */
    def runAllReftrackProcesses(cxn: RepositoryConnection)
    {
        reftrackEncountersAndDependents(cxn)
        reftrackParticipantsAndDependents(cxn)
        logger.info("Finished Referent Tracking Process")
    }
    
    /**
     * This is the driver method for the Consenter Referent Tracking process, which calls methods to referent
     * track each non-referent tracked Consenter and its dependents.
     */
    def reftrackParticipantsAndDependents(cxn: RepositoryConnection)
    {
        logger.info("Starting Participant Referent Tracking")
        partReftrackInst.reftrackParticipants(cxn)
        helper.completeReftrackProcess(cxn)
        logger.info("Participants have been Referent Tracked.")
        logger.info("Starting Participant Dependent Referent Tracking")
        partReftrackInst.reftrackParticipantDependents(cxn)
        helper.completeReftrackProcess(cxn)
        partReftrackInst.reftrackSecondaryParticipantDependents(cxn)
        helper.completeReftrackProcess(cxn)
        logger.info("Participant Dependents have been Referent Tracked.")
    }
    
    /**
     * This is the driver method for the Encounter Referent Tracking process, which calls methods to referent
     * track each non-referent tracked Encounter and its dependents.
     */
    def reftrackEncountersAndDependents(cxn: RepositoryConnection)
    {
        logger.info("Starting Encounter Referent Tracking")
        encReftrackInst.reftrackEncounters(cxn)
        helper.completeReftrackProcess(cxn)
        logger.info("Encounters have been Referent Tracked")
        logger.info("Starting Encounter Dependent Referent Tracking")
        encReftrackInst.reftrackEncounterDependents(cxn)
        logger.info("Encounter Dependents have been Referent Tracked")
    }
}