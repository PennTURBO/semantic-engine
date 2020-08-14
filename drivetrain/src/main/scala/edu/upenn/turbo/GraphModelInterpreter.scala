package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import org.eclipse.rdf4j.repository.RepositoryConnection

class GraphModelInterpreter(cxn: RepositoryConnection) extends ProjectwideGlobals
{
    this.gmCxn = cxn
    
    def handleAcornData(inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], removals: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]]) =
    {
        var discoveredInstances = new HashMap[String, Instance]
        var discoveredTerms = new HashMap[String, Term]
        var discoveredLiterals = new HashMap[String, Literal]
        
        val (inputRecipeList, inpDisInst, inpDisTerm, inpDisLit) = processAcornRowResults("input", inputs, discoveredInstances, discoveredTerms, discoveredLiterals)
        discoveredInstances = inpDisInst; discoveredTerms = inpDisTerm; discoveredLiterals = inpDisLit
        val (outputRecipeList, outDisInst, outDisTerm, outDisLit) = processAcornRowResults("output", outputs, discoveredInstances, discoveredTerms, discoveredLiterals)
        discoveredInstances = outDisInst; discoveredTerms = outDisTerm; discoveredLiterals = outDisLit
        val (removalsRecipeList, remDisInst, remDisTerm, remDisLit) = processAcornRowResults("removal", removals, discoveredInstances, discoveredTerms, discoveredLiterals)
        (inputRecipeList, outputRecipeList, removalsRecipeList)
    }
    
    def processAcornRowResults(typeOfData: String, data: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], disInst: HashMap[String, Instance], disTerm: HashMap[String, Term], disLit: HashMap[String, Literal]) =
    {        
        val recipesList = new HashSet[ConnectionRecipe]
        for (row <- data)
        {
            val (subjectString, objectString, predicate, connectionName, thisMultiplicity, recipeType, optional, subjectAResourceList, 
                objectAResourceList, subjectIsSingleton, subjectIsSuper, objectIsSingleton, objectIsSuper, graphForThisRow, suffixOperator,
                minusGroup, optionalGroup, subjectCustomRule, objectCustomRule, subjectDependee, objectDependee) 
                = interpretRowData(row)
                
            var subjectWithContext = subjectString
            var objectWithContext = objectString
            if (row(SUBJECTCONTEXT.toString) != null) subjectWithContext += "_"+helper.convertTypeToSparqlVariable(row(SUBJECTCONTEXT.toString).toString).substring(1)
            if (row(OBJECTCONTEXT.toString) != null) objectWithContext += "_"+helper.convertTypeToSparqlVariable(row(OBJECTCONTEXT.toString).toString).substring(1)
            if (recipeType == instToInstRecipe)
            {
                val subjInst = findOrCreateNewInstance(typeOfData, disInst, subjectString, subjectWithContext, subjectAResourceList, subjectIsSingleton, subjectIsSuper, subjectCustomRule)
                disInst += subjInst.value -> subjInst
                val obInst = findOrCreateNewInstance(typeOfData, disInst, objectString, objectWithContext, objectAResourceList, objectIsSingleton, objectIsSuper, objectCustomRule)
                disInst += obInst.value -> obInst
                
                // right now only updating connection lists for instance-to-instance relations
                //it would be possible to enforce cardinality based on a LiteralResourceList or maybe even a ClassResourceList...but leaving that alone for now
                updateConnectionLists(subjInst, obInst, thisMultiplicity)
                                                
                val recipe = new InstToInstConnRecipe(subjInst, predicate, obInst)
                subjInst.referencedByRecipes += recipe
                obInst.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjInst, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, obInst, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else if (recipeType == instToTermRecipe)
            {
                val subjInst = findOrCreateNewInstance(typeOfData, disInst, subjectString, subjectWithContext, subjectAResourceList, subjectIsSingleton, subjectIsSuper, subjectCustomRule)
                disInst += subjInst.value -> subjInst
                val objTerm = findOrCreateNewTerm(typeOfData, disTerm, objectWithContext, objectAResourceList, objectCustomRule)
                disTerm += objTerm.value -> objTerm
                
                val recipe = new InstToTermConnRecipe(subjInst, predicate, objTerm)
                subjInst.referencedByRecipes += recipe
                objTerm.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjInst, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, objTerm, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else if (recipeType == termToInstRecipe)
            {
                val subjTerm = findOrCreateNewTerm(typeOfData, disTerm, subjectWithContext, subjectAResourceList, subjectCustomRule)
                disTerm += subjTerm.value -> subjTerm
                val objInst = findOrCreateNewInstance(typeOfData, disInst, objectString, objectWithContext, objectAResourceList, objectIsSingleton, objectIsSuper, objectCustomRule)
                disInst += objInst.value -> objInst
                
                val recipe = new TermToInstConnRecipe(subjTerm, predicate, objInst)
                subjTerm.referencedByRecipes += recipe
                objInst.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjTerm, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, objInst, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else if (recipeType == instToLiteralRecipe)
            {
                val subjInst = findOrCreateNewInstance(typeOfData, disInst, subjectString, subjectWithContext, subjectAResourceList, subjectIsSingleton, subjectIsSuper, subjectCustomRule)
                disInst += subjInst.value -> subjInst
                val objLit = findOrCreateNewLiteral(typeOfData, disLit, objectWithContext, objectAResourceList, objectCustomRule)
                disLit += objLit.value -> objLit
                
                val recipe = new InstToLitConnRecipe(subjInst, predicate, objLit)
                subjInst.referencedByRecipes += recipe
                objLit.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjInst, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, objLit, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else if (recipeType == termToLiteralRecipe)
            {
                val subjTerm = findOrCreateNewTerm(typeOfData, disTerm, subjectWithContext, subjectAResourceList, subjectCustomRule)
                disTerm += subjTerm.value -> subjTerm
                val objLit = findOrCreateNewLiteral(typeOfData, disLit, objectWithContext, objectAResourceList, objectCustomRule)
                disLit += objLit.value -> objLit
                
                val recipe = new TermToLitConnRecipe(subjTerm, predicate, objLit)
                subjTerm.referencedByRecipes += recipe
                objLit.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjTerm, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, objLit, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else if (recipeType == termToTermRecipe)
            {
                val subjTerm = findOrCreateNewTerm(typeOfData, disTerm, subjectWithContext, subjectAResourceList, subjectCustomRule)
                disTerm += subjTerm.value -> subjTerm
                val objTerm = findOrCreateNewTerm(typeOfData, disTerm, objectWithContext, objectAResourceList, objectCustomRule)
                disTerm += objTerm.value -> objTerm
                
                val recipe = new TermToTermConnRecipe(subjTerm, predicate, objTerm)
                subjTerm.referencedByRecipes += recipe
                objTerm.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjTerm, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, objTerm, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else throw new RuntimeException(s"Unrecognized input cardinality setting: $thisMultiplicity")
        }
        (recipesList, disInst, disTerm, disLit)
    }
    
    def addDependent(dependee: String, element: GraphPatternElement, disInst: HashMap[String,Instance], disTerm: HashMap[String,Term], disLit:HashMap[String,Literal])
    {
        var dependeeElement: GraphPatternElement = null
        if (disInst.contains(dependee)) dependeeElement = disInst(dependee)
        if (disTerm.contains(dependee)) dependeeElement = disTerm(dependee)
        if (disLit.contains(dependee)) dependeeElement = disLit(dependee)
        if (dependeeElement == null) 
        {
            val dependentElement = element.value
            throw new RuntimeException("Element $dependentElement was declared dependent on $dependee, but $dependee was not found as an input")
        }
        element.dependentOn = Some(dependeeElement)
    }
    
    def updateRecipeWithNonTypeData(recipe: ConnectionRecipe, connectionName: String, thisMultiplicity: String, predicate: String, optional: Boolean, 
        graphForThisRow: String, suffixOperator: String, minusGroup: String, optionalGroup: String)
    {
        recipe.name = connectionName
        recipe.cardinality = thisMultiplicity
        recipe.isOptional = Some(optional)
        if (graphForThisRow != null) recipe.foundInGraph = Some(graphForThisRow)
        if (suffixOperator != null) recipe.predicateSuffixOperator = Some(suffixOperator)
        if (minusGroup != null) recipe.minusGroup = Some(minusGroup)
        if (optionalGroup != null) recipe.optionalGroup = Some(optionalGroup)
    }
    
    def updateConnectionLists(subj: Instance, obj: Instance, cardinality: String)
    {
        if (cardinality == oneToOneMultiplicity)
        {
            val subjList = subj.oneToOneConnections
            val objList = obj.oneToOneConnections
            
            subjList += obj
            objList += subj
            
            for (instance <- subjList)
            {
                if (obj != instance && subj != instance) 
                {
                    instance.oneToOneConnections += obj   
                    obj.oneToOneConnections += instance
                }
            }
            for (instance <- objList) 
            {
                if (subj != instance && obj != instance) 
                {
                    instance.oneToOneConnections += subj
                    subj.oneToOneConnections += instance
                }
            }
            
            //for (element <- subjList) logger.info(subj.value + " is connected with " + element.value)
            //for (element <- objList) logger.info(obj.value + " is connected with " + element.value)
        }
        else if (cardinality == oneToManyMultiplicity)
        {
            val subjList = subj.oneToManyConnections
            val objList = subj.oneToManyConnections
            
            subjList += obj
            objList += subj
        }
        else if (cardinality == manyToOneMultiplicity)
        {
            val subjList = subj.manyToOneConnections
            val objList = subj.manyToOneConnections
            
            subjList += obj
            objList += subj
        }
    }
    
    def interpretRowData(row: HashMap[String, org.eclipse.rdf4j.model.Value]) =
    {
        var subjectString = row(SUBJECT.toString).toString
        var objectString = row(OBJECT.toString).toString
        
        val predicate = row(PREDICATE.toString).toString
        
        var suffixOperator: String = null
        if (row.contains(SUFFIXOPERATOR.toString) && row(SUFFIXOPERATOR.toString) != null) suffixOperator = row(SUFFIXOPERATOR.toString).toString
        
        val connectionName = row(CONNECTIONNAME.toString).toString
        val thisMultiplicity = row(MULTIPLICITY.toString).toString
        val recipeType = row(CONNECTIONRECIPETYPE.toString).toString
        
        var optional: Boolean = false
        if (row.contains(INPUTTYPE.toString) && row(INPUTTYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/hasOptionalInput") optional = true
        
        var subjectAResourceList = false
        if (row(SUBJECTADESCRIBER.toString) != null) subjectAResourceList = true
        var objectAResourceList = false
        if (row(OBJECTADESCRIBER.toString) != null) objectAResourceList = true
        if (row.contains(GRAPHLITERALTYPE.toString) && row(GRAPHLITERALTYPE.toString) != null) objectAResourceList = true
        
        // SPARQL searches process that created this input as an output and returns their graph (GRAPHOFCREATINGPROCESS). We override that if the user provided a graph explicitly (GRAPHOFORIGIN)
        var graphForThisRow: String = null
        if (row.contains(GRAPHOFCREATINGPROCESS.toString) && row(GRAPHOFCREATINGPROCESS.toString) != null) graphForThisRow = row(GRAPHOFCREATINGPROCESS.toString).toString
        if (row.contains(GRAPHOFORIGIN.toString) && row(GRAPHOFORIGIN.toString) != null) graphForThisRow = row(GRAPHOFORIGIN.toString).toString
        
        var subjectIsSingleton = false
        var subjectIsSuper = false
        var objectIsSingleton = false
        var objectIsSuper = false
        if (subjectSingleton.contains(thisMultiplicity)) subjectIsSingleton = true
        else if (subjectSuperSingleton.contains(thisMultiplicity)) subjectIsSuper = true
        if (objectSingleton.contains(thisMultiplicity)) objectIsSingleton = true
        else if (objectSuperSingleton.contains(thisMultiplicity)) objectIsSuper = true
        
        var minusGroup: String = null
        var optionalGroup: String = null
        if (row.contains(MINUSGROUP.toString) && row(MINUSGROUP.toString) != null) minusGroup = row(MINUSGROUP.toString).toString
        if (row.contains(OPTIONALGROUP.toString) && row(OPTIONALGROUP.toString) != null) optionalGroup = row(OPTIONALGROUP.toString).toString
        
        var subjectDependee: String = null
        var objectDependee: String = null
        var subjectCustomRule: String = null
        var objectCustomRule: String = null
        if (row.contains(SUBJECTRULE.toString) && row(SUBJECTRULE.toString) != null) subjectCustomRule = row(SUBJECTRULE.toString).toString
        if (row.contains(OBJECTRULE.toString) && row(OBJECTRULE.toString) != null) objectCustomRule = row(OBJECTRULE.toString).toString
        if (row.contains(SUBJECTDEPENDEE.toString) && row(SUBJECTDEPENDEE.toString) != null) subjectDependee = row(SUBJECTDEPENDEE.toString).toString
        if (row.contains(OBJECTDEPENDEE.toString) && row(OBJECTDEPENDEE.toString) != null) objectDependee = row(OBJECTDEPENDEE.toString).toString
        
        // null check
        val nonNulls = Array(subjectString, objectString, predicate, connectionName, thisMultiplicity, recipeType, optional, subjectAResourceList,
            objectAResourceList, subjectIsSingleton, subjectIsSuper, objectIsSingleton, objectIsSuper)
        for (nonNull <- nonNulls) assert(nonNull != null, "Found null object")
        
        (subjectString, objectString, predicate, connectionName, thisMultiplicity, recipeType, optional, subjectAResourceList, 
            objectAResourceList, subjectIsSingleton, subjectIsSuper, objectIsSingleton, objectIsSuper, graphForThisRow, suffixOperator,
            minusGroup, optionalGroup, subjectCustomRule, objectCustomRule, subjectDependee, objectDependee)
    }
    
    def findOrCreateNewInstance(typeOfData: String, disInst: HashMap[String, Instance], stringVal: String, valWithContext: String, isResourceList: Boolean, singleton: Boolean, superSingleton: Boolean, customRule: String): Instance =
    {
        var newInst: Instance = null
        if (disInst.contains(valWithContext)) 
        {
            // might be a good place to validate the discovered element with the informations gathered about it that are parameters to this method
            newInst = disInst(valWithContext)
        }
        else
        {
          newInst = new Instance(valWithContext)
          newInst.isUntyped = Some(isResourceList)
          newInst.isSingleton = Some(singleton)
          newInst.isSuperSingleton = Some(superSingleton)
          newInst.instanceType = stringVal
          newInst.buildInstanceType(newInst)
          if (customRule != null) newInst.createdWithRule = Some(customRule)
        }
        if (typeOfData == "input") newInst.existsInInput = Some(true)
        else if (newInst.existsInInput == None) newInst.existsInInput = Some(false)
        if (typeOfData == "output") newInst.existsInOutput = Some(true)
        else if (newInst.existsInOutput == None) newInst.existsInOutput = Some(false)
        newInst
    }
    
    def findOrCreateNewTerm(typeOfData: String, disTerm: HashMap[String, Term], stringVal: String, isResourceList: Boolean, customRule: String): Term =
    {
        var newTerm: Term = null
        if (disTerm.contains(stringVal)) 
        {
            // might be a good place to validate the discovered element with the informations gathered about it that are parameters to this method
            newTerm = disTerm(stringVal)
        }
        else
        {
          newTerm = new Term(stringVal)
          newTerm.isResourceList = Some(isResourceList)
          if (isResourceList) newTerm.ranges = helper.getDescriberRangesAsList(gmCxn, stringVal)
          if (customRule != null) newTerm.createdWithRule = Some(customRule)
          newTerm.buildValuesBlock()
        }
        if (typeOfData == "input") newTerm.existsInInput = Some(true)
        else if (newTerm.existsInInput == None) newTerm.existsInInput = Some(false)
        if (typeOfData == "output") newTerm.existsInOutput = Some(true)
        else if (newTerm.existsInOutput == None) newTerm.existsInOutput = Some(false)
        newTerm
    }
    
    def findOrCreateNewLiteral(typeOfData: String, disLit: HashMap[String, Literal], stringVal: String, isResourceList: Boolean, customRule: String): Literal =
    {
        var newLit: Literal = null
        if (disLit.contains(stringVal)) 
        {
            // might be a good place to validate the discovered element with the informations gathered about it that are parameters to this method
            newLit = disLit(stringVal)
        }
        else
        {
          newLit = new Literal(stringVal)
          newLit.isResourceList = Some(isResourceList)
          if (customRule != null) newLit.createdWithRule = Some(customRule)
        }
        if (typeOfData == "input") newLit.existsInInput = Some(true)
        else if (newLit.existsInInput == None) newLit.existsInInput = Some(false)
        if (typeOfData == "output") newLit.existsInOutput = Some(true)
        else if (newLit.existsInOutput == None) newLit.existsInOutput = Some(false)
        newLit
    }
}