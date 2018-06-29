package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.rio.RDFFormat
import java.nio.file.Path
import java.nio.file.Paths
import java.io.File
import java.io.Reader
import java.io.FileReader
import java.io.BufferedReader
import scala.collection.mutable.HashMap
import java.io.PrintWriter
import org.ddahl.rscala
import scala.collection.mutable.ArrayBuffer
import java.util.Arrays
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.ValueFactory

class DrivetrainVisualizations extends ProjectwideGlobals
{
    def createDrivetrainVisualizations(cxn: RepositoryConnection)
    {
        val timestamp: String = helper.getCurrentTimestamp()
        new File("..//HTMLTables//" + timestamp).mkdirs()
        createLofExpansionInfoTable(cxn, timestamp)
        createReferentTrackingInfoTable(cxn, timestamp)
        createDiagnosisInfoTable(cxn, timestamp)
        createConclusionationInfoTable(cxn, timestamp)
    }
    
    def createConclusionationInfoTable(cxn: RepositoryConnection, timestamp: String)
    {
        val concGraphs: String = helper.generateShortcutNamedGraphsString(cxn, true, "http://www.itmat.upenn.edu/biobank/Conclusionations")
        
        val getConclusionatedBiosex: String =
        """
        Select (count (?statement) as ?biosexConcCount) """ + concGraphs + """ FROM <http://www.itmat.upenn.edu/biobank/expanded> Where
        {
            ?statement a rdf:Statement .
            ?statement rdf:subject ?biosex .
            ?biosex a obo:PATO_0000047 .
        }  
        """
        
        val getMissingKnowledgeBiosex: String =
        """
        Select (count (?missingKnowledge) as ?mkCount) """ + concGraphs + """ FROM <http://www.itmat.upenn.edu/biobank/expanded> Where
        {
            ?missingKnowledge a obo:OBI_0000852 ;
                  obo:IAO_0000136 ?biosex .
            ?biosex a obo:PATO_0000047 .
        }  
        """
        val getConclusionatedBirths: String =
        """
        Select (count (?statement) as ?birthConcCount) """ + concGraphs + """ Where
        {
            ?statement a rdf:Statement .
            ?statement rdf:subject ?birth .
            ?birth a efo:EFO_0004950 .
        }  
        """
        val getMissingKnowledgeBirths: String =
        """
        Select (count (?missingKnowledge) as ?mkCount) """ + concGraphs + """ Where
        {
            ?missingKnowledge a obo:OBI_0000852 ;
                  obo:IAO_0000136 ?birth .
            ?birth a efo:EFO_0004950 .
        }  
        """
        val getConclusionatedBMI: String =
        """
        Select (count (?statement) as ?BmiConcCount) """ + concGraphs + """ Where
        {
            ?statement a rdf:Statement .
            ?statement rdf:subject ?BMI .
            ?BMI a efo:EFO_0004340 .
        }  
        """
        val getMissingKnowledgeBMI: String =
        """
        Select (count (?missingKnowledge) as ?mkCount) """ + concGraphs + """ FROM <http://www.itmat.upenn.edu/biobank/expanded> Where
        {
            ?missingKnowledge a obo:OBI_0000852 ;
                  obo:IAO_0000136 ?bbEnc .
            ?bbEnc a turbo:TURBO_0000527 .
        }  
        """
        
        val concBiosexCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getConclusionatedBiosex, "biosexConcCount")(0).toString.split("\"")(1)
        val mkBiosexCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getMissingKnowledgeBiosex, "mkCount")(0).toString.split("\"")(1)
        val concBirthCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getConclusionatedBirths, "birthConcCount")(0).toString.split("\"")(1)
        val mkBirthCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getMissingKnowledgeBirths, "mkCount")(0).toString.split("\"")(1)
        val concBmiCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getConclusionatedBMI, "BmiConcCount")(0).toString.split("\"")(1)
        val mkBmiCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getMissingKnowledgeBMI, "mkCount")(0).toString.split("\"")(1)
        
        val dataString: String = "\""+concBiosexCount+"\",\""+mkBiosexCount+"\",\""+concBirthCount+"\",\""+mkBirthCount+"\",\""+concBmiCount+"\",\""+mkBmiCount+"\""
        val colHeaders: String = "\"Biological Sex\",\"Date Of Birth\",\"BMI\""
        val rowHeaders: String = "\"Conclusions\",\"Records of Missing Knowledge\""
        
        writeMultiRowHTMLFile(dataString, colHeaders, rowHeaders, "2", "3", timestamp + "//ConclusionationInfoTable.html")
    }
    
    def createDiagnosisInfoTable(cxn: RepositoryConnection, timestamp: String)
    {
        val getDiagRegistryCount: String = """
          SELECT (count(?ICD9) as ?ICD9Count) (count(?ICD10) as ?ICD10Count)  WHERE
          {
              Graph pmbb:expanded {
              ?diagCodeRegID a turbo:TURBO_0000555 .
        		  ?diagCodeRegID obo:IAO_0000219 ?diagCodeRegURI .
              ?diagCodeRegURI a turbo:TURBO_0000556 .
              BIND ( IF (?diagCodeRegURI = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892>, ?diagCodeRegURI, ?unbound) AS ?ICD10)
              BIND ( IF (?diagCodeRegURI = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890>, ?diagCodeRegURI, ?unbound) AS ?ICD9)
          }}
          """
        val countDiagnoses: String =
        """
        Select (count (?diagnosis) as ?diagCount) Where
        {
            Graph pmbb:expanded
            {
                ?diagnosis a obo:OGMS_0000073 .
            }
        }  
        """
        val diagnosisCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countDiagnoses, "diagCount")(0).toString.split("\"")(1)
        val diagRegCountResult: ArrayBuffer[ArrayBuffer[Value]] = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getDiagRegistryCount, Array("ICD9Count", "ICD10Count"))

        val ICD9Count: String = diagRegCountResult(0)(0).toString.split("\"")(1)
        val ICD10Count: String = diagRegCountResult(0)(1).toString.split("\"")(1)
        
        val dataString: String = "\""+diagnosisCount+"\",\""+ICD9Count+"\",\""+ICD10Count+"\""
        val colHeaders: String = "\"Total Diagnoses\",\"ICD9 Diagnoses\",\"ICD10 Diagnoses\""
        
        writeSingleRowHTMLFile(dataString, colHeaders, timestamp + "//DiagnosisInfoTable.html")
    }
    
    def createReferentTrackingInfoTable(cxn: RepositoryConnection, timestamp: String)
    {
        val countReftrackedBbEncs: String =
        """
        Select (count (?enc) as ?encCount) Where
        {
            Graph pmbb:expanded {
            ?enc a turbo:TURBO_0000527 .
            ?enc turbo:TURBO_0006500 'true'^^xsd:boolean .
        }}  
        """
        val countPreReftrackedBbEncs: String =
        """
        Select (count (?retiredEnc) as ?retiredEncCount) Where
        {
            Graph pmbb:expanded {
            ?retiredEnc a turbo:TURBO_0000927 .
        }}
        """
        val countReftrackedHcEncs: String =
        """
        Select (count (?enc) as ?encCount) Where
        {
            Graph pmbb:expanded {
            ?enc a obo:OGMS_0000097 .
            ?enc turbo:TURBO_0006500 'true'^^xsd:boolean .
        }}
        """
        val countPreReftrackedHcEncs: String =
        """
        Select (count (?retiredEnc) as ?retiredEncCount) Where
        {
            Graph pmbb:expanded {
            ?retiredEnc a turbo:TURBO_0000907 .
        }}
        """
        val countReftrackedConsenters: String =
        """
        Select (count (?consenter) as ?consenterCount) Where
        {
            Graph pmbb:expanded {
            ?consenter a turbo:TURBO_0000502 .
        }}
        """
        val countPreReftrackedConsenters: String =
        """
        Select (count (?retiredConsenter) as ?retiredConsenterCount) Where
        {
            Graph pmbb:expanded {
            ?retiredConsenter a turbo:TURBO_0000902 .
        }}
        """
        
        val reftrackedBbEncs: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countReftrackedBbEncs, "encCount")(0).toString.split("\"")(1)
        val preReftrackedBbEncs: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countPreReftrackedBbEncs, "retiredEncCount")(0).toString.split("\"")(1)
        val reftrackedHcEncs: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countReftrackedHcEncs, "encCount")(0).toString.split("\"")(1)
        val preReftrackedHcEncs: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countPreReftrackedHcEncs, "retiredEncCount")(0).toString.split("\"")(1)
        val reftrackedConsenterCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countReftrackedConsenters, "consenterCount")(0).toString.split("\"")(1)
        val preReftrackedConsterCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countPreReftrackedConsenters, "retiredConsenterCount")(0).toString.split("\"")(1)
    
        val dataString: String = "\""+reftrackedBbEncs+"\",\""+preReftrackedBbEncs+"\",\""+reftrackedHcEncs+"\",\""+preReftrackedHcEncs+"\",\""+reftrackedConsenterCount+"\",\""+preReftrackedConsterCount+"\""
        val colHeaders: String = "\"Biobank Encounters\",\"Healthcare Encounters\",\"Biobank Consenters\""
        val rowHeaders: String = "\"Pre-Referent Tracking\",\"Post-Referent Tracking\""
        
        writeMultiRowHTMLFile(dataString, colHeaders, rowHeaders, "2", "3", timestamp + "//ReferentTrackingInfoTable.html")
    }
    
    def createLofExpansionInfoTable(cxn: RepositoryConnection, timestamp: String)
    {
        val getTotalLof: String = 
        """
        Select (count (?allele) as ?alleleCount) Where
        {
            ?allele a obo:OBI_0001352 .
        }
        """
        val getExpandedLof: String =
        """
        Select (count (?allele) as ?alleleCount) Where
        {
            Graph pmbb:expanded
            {
                ?allele a obo:OBI_0001352 .
            }
        }  
        """
        val getUnexpandedLof: String =
        """
        Select (count (?allele) as ?alleleCount) Where
        {
            Graph ?g
            {
                ?allele a obo:OBI_0001352 .
            }
            Filter (?g != pmbb:expanded)
        }
        """
        val getUnexpandedDueToNoMatch: String =
        """
        Select (count (?allele) as ?alleleCount) Where
        {
            ?allele a obo:OBI_0001352 .
            Graph pmbb:errorLogging
            {
                ?allele graphBuilder:reasonNotExpanded graphBuilder:noMatchFound .
            }
        }
        """
        val getUnexpandedDueToDataFormat: String =
        """
        Select (count (?allele) as ?alleleCount) Where
        {
            ?allele a obo:OBI_0001352 .
            Graph pmbb:errorLogging
            {
                ?allele graphBuilder:reasonNotExpanded graphBuilder:dataFormatError .
            }
        }
        """
        val totalCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getTotalLof, "alleleCount")(0).toString.split("\"")(1)
        val expandedCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getExpandedLof, "alleleCount")(0).toString.split("\"")(1)
        val unexpandedCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getUnexpandedLof, "alleleCount")(0).toString.split("\"")(1)
        val noMatchFoundCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getUnexpandedDueToNoMatch, "alleleCount")(0).toString.split("\"")(1)
        val dataFormatCount: String = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getUnexpandedDueToDataFormat, "alleleCount")(0).toString.split("\"")(1)
        
        val dataString: String = "\""+totalCount+"\",\""+expandedCount+"\",\""+unexpandedCount+"\",\""+noMatchFoundCount+"\",\""+dataFormatCount+"\""
        val headers: String = "\"Total LOF rows received\",\"Total expanded\",\"Total unexpanded\",\"Unexpanded due to No Match\",\"Unexpanded due to Data Format Error\""
        
        writeSingleRowHTMLFile(dataString, headers, timestamp + "//LofExpansionInfoTable.html")
    }
    
    def writeSingleRowHTMLFile(rowData: String, columnNames: String, filename: String)
    {
        val R = org.ddahl.rscala.RClient()
        R.eval(
        """
            library(tableHTML)

            matr <- rbind.data.frame(cbind("""+rowData+"""))
            
            names(matr) <-
             c("""+columnNames+""")
            
            tabtoprint = tableHTML(matr, rownames = FALSE)
            write_tableHTML(tabtoprint, file = "..//HTMLtables//"""+filename+"""")
        """)
    }
    
    def writeMultiRowHTMLFile(rowData: String, columnNames: String, rowNames: String, numberOfRows: String, numberOfColumns: String, filename: String)
    {
        val R = org.ddahl.rscala.RClient()
        R.eval(
        """
            library(tableHTML)

            matr <- matrix(c(
              """ + rowData + """), nrow = """ + numberOfRows + """, """ + numberOfColumns + """
            )
            
            df <- rbind.data.frame(matr)
            
            names(df) <-
             c("""+columnNames+""")
               
             rownames(df) <- c("""+ rowNames +""")
            
            tabtoprint = tableHTML(df)
            write_tableHTML(tabtoprint, file = "..//HTMLtables//"""+filename+"""")
        """)
    }
}