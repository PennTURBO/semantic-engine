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
    
    var gmCxn: RepositoryConnection = null
    var gmRepo: Repository = null
    var gmRepoManager: RemoteRepositoryManager = null
    
    var testCxn: RepositoryConnection = null
    var testRepo: Repository = null
    var testRepoManager: RemoteRepositoryManager = null

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
    
    def setGmConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    
    def getGmConnection(): RepositoryConnection = gmCxn
    
    def setGmRepository(gmRepo: Repository)
    {
        this.gmRepo = gmRepo
    }
    
    def getGmRepository(): Repository = gmRepo
    
    def setGmRepoManager(gmRepoManager: RemoteRepositoryManager)
    {
        this.gmRepoManager = gmRepoManager
    }
    
    def getGmRepoManager(): RemoteRepositoryManager = gmRepoManager
    
    def setTestConnection(testCxn: RepositoryConnection)
    {
        this.testCxn = testCxn
    }
    
    def getTestConnection(): RepositoryConnection = testCxn
    
    def setTestRepository(testRepo: Repository)
    {
        this.testRepo = testRepo
    }
    
    def getTestRepository(): Repository = testRepo
    
    def setTestRepoManager(testRepoManager: RemoteRepositoryManager)
    {
        this.testRepoManager = testRepoManager
    }
    
    def getTestRepoManager(): RemoteRepositoryManager = testRepoManager
}