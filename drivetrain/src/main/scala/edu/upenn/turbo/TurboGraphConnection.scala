package edu.upenn.turbo

import org.eclipse.rdf4j.rio._
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager

class TurboGraphConnection 
{
    var cxn: RepositoryConnection = null
    var repo: Repository = null
    var repoManager: RemoteRepositoryManager = null

    def setConnection(cxn: RepositoryConnection)
    {
        this.cxn = cxn
    }
    
    def getConnection(): RepositoryConnection = cxn
    
    def setRepository(repo: Repository)
    {
        this.repo = repo
    }
    
    def getRepository(): Repository = repo
    
    def setRepoManager(repoManager: RemoteRepositoryManager)
    {
        this.repoManager = repoManager
    }
    
    def getRepoManager(): RemoteRepositoryManager = repoManager
}