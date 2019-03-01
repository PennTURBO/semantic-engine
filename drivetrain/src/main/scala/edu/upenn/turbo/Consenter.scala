package edu.upenn.turbo

class Consenter extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = Consenter.pattern
    val baseVariableName = Consenter.baseVariableName
    val typeURI = Consenter.typeURI
    val variablesToSelect = Consenter.variablesToSelect
    override val optionalLinks = Consenter.optionalLinks
    override val mandatoryLinks = Consenter.mandatoryLinks
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object Consenter extends ExpandedGraphObjectSingletonFromDataset
{
    def create(optional: Boolean): Consenter =
    {
        new Consenter(optional)
    }
    
    val dataset = "consenterDataset"
    
    val baseVariableName = "part"
    val shortcutName = "shortcutPartName"
    
    val height = "consenterHeightSingleton"
    val weight = "consenterWeightSingleton"
    val adipose = "consenterAdiposeSingleton"
    
    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000502 .          
          ?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .
          
      """

    override val mandatoryLinks = Map(
        "Identifier" -> ConsenterIdentifier
    )

    override val optionalLinks = Map(
        "GenderIdentityDatum" -> GenderIdentityDatum, 
        "RaceIdentityDatum" -> RaceIdentityDatum, 
        "DateOfBirthDatum" -> DateOfBirthDatum
    )
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    val variablesToSelect = Array(baseVariableName)
}