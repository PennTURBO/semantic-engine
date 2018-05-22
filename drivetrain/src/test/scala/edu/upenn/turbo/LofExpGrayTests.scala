package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest._

class LofExpGrayTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals {
  val lofExp = new Expander

  test("placeholder / stub test") {
    val toyInput = 2
    //expander.expandLossOfFunctionShortcuts(cxn, graphslist)
    //val toyOutput = lofExp.addOne(toyInput)

    //assert(toyOutput == 3)
  }

}