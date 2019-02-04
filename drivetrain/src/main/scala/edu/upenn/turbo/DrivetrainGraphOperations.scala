package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Literal
import org.eclipse.rdf4j.repository.RepositoryConnection
import java.util.UUID
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.XMLSchema
import java.util.Date
import java.text.DateFormat
import org.eclipse.rdf4j.model.util.ModelBuilder

class DrivetrainGraphOperations extends ProjectwideGlobals
{
    val builder = new ModelBuilder() 
    val factory = SimpleValueFactory.getInstance()

    def addDateOrderingToHealthcareEncounters(cxn: RepositoryConnection)
    {
        val retrieveData = """
            Select distinct ?cons ?hcEnc ?encounterDate ?dateLit
            Where
            {
                ?cons a turbo:TURBO_0000502 .
                ?cons obo:RO_0000056 ?hcEnc .
                ?encStart a turbo:TURBO_0000511 .
                ?encStart obo:RO_0002223 ?hcEnc .    
                ?encounterDate a turbo:TURBO_0000512 .
                ?encounterDate turbo:TURBO_0006511 ?dateLit .
                ?encounterDate obo:IAO_0000136 ?encStart .

                ?hcEnc obo:RO_0002234 ?diag .
                ?diag a obo:OGMS_0000073 .

                #filter (?cons = pmbb:98c3481f980b72aadece474d4b96eaf0)
            }
            Order By ?cons
        """
        val res = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + retrieveData, ArrayBuffer("cons", "hcEnc", "encounterDate", "dateLit"))
        println("retrieved results for " + res.size + " healthcare encounters")

        var currentConsenter = res(0)(0)
        var dateToEncMap = new HashMap[Literal, ArrayBuffer[IRI]]()
        var dateList = new ArrayBuffer[Literal]
        for (record <- res)
        {
            if (record(0) != currentConsenter)
            {
                addDateRankings(cxn, dateList, dateToEncMap, currentConsenter)
                currentConsenter = record(0)
                dateToEncMap = new HashMap[Literal, ArrayBuffer[IRI]]()
                dateList = new ArrayBuffer[Literal]
            }

            val dateAsLiteral = record(3).asInstanceOf[Literal]
            if (dateToEncMap.contains(dateAsLiteral)) dateToEncMap(dateAsLiteral) += record(1).asInstanceOf[IRI]
            else dateToEncMap += dateAsLiteral -> ArrayBuffer(record(1).asInstanceOf[IRI])
            dateList += record(3).asInstanceOf[Literal]
        }

        addDateRankings(cxn, dateList, dateToEncMap, currentConsenter)
        def model = builder.build()
        cxn.add(model)
    }

    def addDateRankings(cxn: RepositoryConnection, dateList: ArrayBuffer[Literal], dateToEncMap: HashMap[Literal, ArrayBuffer[IRI]], currentConsenter: Value)
    {
        println("ordering dates in list of size: " + dateList.size)
        val orderedDates = orderDatesInList(dateList)/*.toSet.toList*/
        var count = 1
        for (a <- orderedDates)
        {
            println(a)
            for (encounter <- dateToEncMap(a))
            {
                //println("encounter: " + count)
                //println("date: " + a.toString)
                builder.namedGraph("http://www.itmat.upenn.edu/biobank/healthcareEncounterDiagsDateRankings")
                  .subject(encounter).add("http://graphBuilder.org/hasDateRanking", 
                    factory.createLiteral((count).toString, XMLSchema.INTEGER))
                builder.namedGraph("http://www.itmat.upenn.edu/biobank/healthcareEncounterDiagsDateRankings")
                  .subject(encounter).add("http://graphBuilder.org/hasDate", a)
                builder.namedGraph("http://www.itmat.upenn.edu/biobank/healthcareEncounterDiagsDateRankings")
                  .subject(encounter).add("http://graphBuilder.org/pointsToConsenter", 
                    currentConsenter)
                count = count + 1
            }
        }
    }

    def orderDatesInList(dateList: ArrayBuffer[Literal]): ArrayBuffer[Literal] =
    {
        var formattedDateList = new ArrayBuffer[Date]
        var litToDateMap = new HashMap[Date, Literal]
        var listToReturn = new ArrayBuffer[Literal]

        val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
        for (date <- dateList)
        {
            val removeTag = date.toString.split("\\^")(0)
            val formattedDateString = removeTag.substring(1, removeTag.size-1)
            val parsedDate = dateFormat.parse(formattedDateString)
            formattedDateList += parsedDate
            litToDateMap += parsedDate -> date
        }

        val dedupedDateList = formattedDateList.toSet.toList
        dedupedDateList.sortBy(_.getTime)

        var dedupMap = new HashSet[Date]
        for (a <- formattedDateList)
        {
            if (!dedupMap.contains(a))
            {
                listToReturn += litToDateMap(a)
                dedupMap += a
            }
        }
        listToReturn
    }
}