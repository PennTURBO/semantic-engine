# r2gdb_keep_punct_do_urlencode.R

what portion of the labels retrieved from DRON contain digits?

look in http://localhost:7200/repositories/dron_no_ndc

only consider subclasses of particular types of drug products

|                     URI                      |     label      |
|----------------------------------------------|--------------------|
| http://purl.obolibrary.org/obo/DRON_00000026 | drug capsule       |
| http://purl.obolibrary.org/obo/DRON_00000017 | drug cream         |
| http://purl.obolibrary.org/obo/DRON_00000016 | drug emulsion      |
| http://purl.obolibrary.org/obo/DRON_00000024 | drug foam          |
| http://purl.obolibrary.org/obo/DRON_00000015 | drug gel           |
| http://purl.obolibrary.org/obo/DRON_00000018 | drug lotion        |
| http://purl.obolibrary.org/obo/DRON_00000019 | drug ointment      |
| http://purl.obolibrary.org/obo/DRON_00000005 | drug product       |
| http://purl.obolibrary.org/obo/DRON_00000020 | drug solution      |
| http://purl.obolibrary.org/obo/DRON_00000021 | drug suspension    |
| http://purl.obolibrary.org/obo/DRON_00000022 | drug tablet        |
| http://purl.obolibrary.org/obo/CHEBI_60004   | mixture            |
| http://purl.obolibrary.org/obo/CHEBI_23367   | molecular entity   |
| http://purl.obolibrary.org/obo/OBI_0000047   | processed material |


```
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
select *  where {
    values ?top {
        obo:DRON_00000017 obo:DRON_00000019 obo:DRON_00000020
        obo:DRON_00000021 obo:DRON_00000022 obo:DRON_00000026
        obo:DRON_00000015 obo:DRON_00000018 obo:DRON_00000024
        obo:DRON_00000016 obo:DRON_00000005 obo:CHEBI_60004 obo:CHEBI_23367 obo:OBI_0000047
    }
    ?top rdfs:label ?toplab .
    ?d rdfs:subClassOf+ ?top .
    optional {
        ?d rdfs:label ?dl
    }
    optional {
        ?d obo:DRON_00010000 ?rxn
    }
}
```

| No Digits | With Digits   |
|-------|--------|
| 21273 | 60595  |

follow that up with some tidying, and for every row that contains a brand name in square brackets (500 mg acetaminophen tablets [Tylenol]) create a rows with the same URI and the brand name alone as the label

-> `unique4solr` data.frame

Also get active ingredient relationships.  Sometimes a product (500 mg acetaminophen tablets) doesn't have any ingredient listed, but the more general parent does (acetaminophen tablets)... so consider the ingredients for any class to hold for any subClass

```
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
select
?child ?cl ?cr ?ing ?il
where {
    optional {
        ?subj rdfs:label ?sl
    }  .
    ?subj obo:DRON_00010000 ?rxn .
    ?subj  rdfs:subClassOf   [ owl:onProperty <http://www.obofoundry.org/ro/ro.owl#has_proper_part> ;
                               owl:someValuesFrom [ owl:intersectionOf [ ?fr1
            obo:OBI_0000576 ;
        ?fr4 [ ?fr2 [ owl:onProperty obo:BFO_0000053 ;
        owl:someValuesFrom ?role  ] ;
        ?fr5 [ ?fr3 [ owl:onProperty
            obo:BFO_0000071 ;
        owl:someValuesFrom ?ing ] ] ] ] ] ] .
    optional {
        ?ing rdfs:label ?il
    }  .
    optional {
        ?role rdfs:label ?rl
    }  .
    optional {
        ?child rdfs:subClassOf* ?subj .
        optional {
            ?child rdfs:label ?cl
        }
        optional {
            ?child obo:DRON_00010000 ?cr
        }
    }
}

```

That query doesn't complete in real time with GraphDB on my laptop, so run it from the web interface, export and read into R 


```
recursive_ingredients <-
  read.csv("recursive_ingredients.csv",
           stringsAsFactors = FALSE)
```

get some counts


> length(unique(recursive_ingredients$child))
[1] 74170
> length(unique(unique4solr$d.full))
[1] 80051
> length(setdiff(recursive_ingredients$child, unique4solr$d.full))
[1] 0
> length(setdiff(unique4solr$d.full, recursive_ingredients$child))
[1] 5881

... there are 5881 labeled things from 5881 for which we still haven't found any active ingredients

What classes do they come from?

|       class        | FALSE | TRUE |
|--------------------|-------|------|
| drug capsule       | 12300 |   45 |
| drug cream         |  3275 |    5 |
| drug foam          |   387 |    2 |
| drug gel           |  2135 |    4 |
| drug lotion        |  1374 |    2 |
| drug ointment      |  2599 |    2 |
| drug product       | 11989 |  108 |
| drug solution      | 25057 |   17 |
| drug suspension    |  4706 |    5 |
| drug tablet        | 31373 |  116 |
| mixture            |     0 |   14 |
| molecular entity   |     0 | 1444 |
| processed material |     0 | 4792 |

let's address that eventually

but first, how many active ingredients are in a typical drug or drug product? 

![](act_ing_count.png)

now import and *rbind r_medication.csv* from the "5000" patient set and *wes_pds__med_standard.csv* into `r_medication` and do some minimal tidying (collapsing multiple spaces, removing leading and training spaces and punctuation, lower-casing (these steps are done to most strings in this workflow)

|   r_medication.field   | fraction empty |fraction populated |
|------------------------|-----------|---------------|
| PK_MEDICATION_ID       | 0.0000000 | 1.00000000    |
| FULL_NAME              | 0.0000000 | 1.00000000    |
| PHARMACY_CLASS         | 0.1846377 | 0.81536234    |
| SIMPLE_GENERIC_NAME    | 0.2773185 | 0.72268147    |
| GENERIC_NAME           | 0.2440717 | 0.75592832    |
| THERAPEUTIC_CLASS      | 0.2765945 | 0.72340554    |
| PHARMACY_SUBCLASS      | 0.2806372 | 0.71936282    |
| AMOUNT                 | 0.2835335 | 0.71646654    |
| FORM                   | 0.2450371 | 0.75496289    |
| ROUTE_DESCRIPTION      | 0.1970072 | 0.80299282    |
| ROUTE_TYPE             | 0.1970072 | 0.80299282    |
| CONTROLLED_MED_YN      | 0.3298739 | 0.67012611    |
| DEA_CLASS              | 0.9249382 | 0.07506185    |
| RECORD_STATE           | 0.9093707 | 0.09062934    |
| FK_3M_NCID_ID          | 0.3062813 | 0.69371870    |
| RXNORM                 | 0.5971761 | 0.40282387    |
| RXNORM_DEFINITION      | 0.5971761 | 0.40282387    |
| MDM_LAST_UPDATE_DATE   | 0.0000000 | 1.00000000    |
| MDM_INSERT_UPDATE_FLAG | 0.0000000 | 1.00000000    |
| FULL_NAME.tidy         | 0.0000000 | 1.00000000    |

how many orders with non-NA rxnorm values?
> nrow(r_medication.subset)
[1] 6676

how many unique rxnorm values from orders
> length(r_medication.RXNORM)
[1] 1295

for how many rxnorm-labeled medication orders do we not have any label/rxnorm look-up information?
(different from knowing whether any ingredients can be reached via the rxnorm values)
> length(setdiff(r_medication.RXNORM, rxnorm.by.reason.rxn))
[1] 36

for what fraction of  rxnorm-labeled medication orders do we not have any label/rxnorm look-up information?
> length(setdiff(r_medication.RXNORM, rxnorm.by.reason.rxn)) / length(r_medication.RXNORM)
[1] 0.02779923

create a dataframe called `rxn2ing` that maps from rxnorm values to active ingredients.  include members of classes "molecular entity" and "processed material" as active ingredients even if they haven't been claimed by and drug product yet

how does that effect the availability of active ingredients for drugs/drug products?

|       class        | FALSE | TRUE |
|--------------------|-------|------|
| drug capsule       | 12340 |    5 |
| drug cream         |  3276 |    4 |
| drug foam          |   387 |    2 |
| drug gel           |  2135 |    4 |
| drug lotion        |  1374 |    2 |
| drug ointment      |  2599 |    2 |
| drug product       | 12034 |   63 |
| drug solution      | 25064 |   10 |
| drug suspension    |  4707 |    4 |
| drug tablet        | 31472 |   17 |
| mixture            |     0 |   14 |
| molecular entity   |  1444 |    0 |
| processed material |  4792 |    0 |

save *unique4solr.csv*, which contains URIs, parent classes, labels for both, and an indicator of whether digits were present in the drug/drug product label, or whether the row was created by parsing a brand name out of another longer label.  this file can be the Solr search dictionary... but adding some synonyms and stop words make a significant improvement...

then merge `r_medication` with `rxn2ing` to get a data.frame with known relationships from medication order strings to active ingredients, expressed as DRON URIs.  save that as *svmable.csv*.

then create a list-of-lists structure where the indices/names are DRON URIs and the values are 

save the current environment as *ing_list.Rdata*

----

# r2gdb_svmprep_with_punct_urlencoded.R

run the sorted, unique, tidied FULL_NAMEs from *svmable.csv* through Solr, using *unique4solr.csv* as the document collection

creates `forward.results` and saves as *r_meds_solr_res.csv*

run the Solr results through the following string similarity assessments:  

- osa
- lv
- dl
- lcs
- qgram
- cosine
- jaccard
- jw

merge the string similarity metrics back into `forward.results`

calculate the difference in number of characters and words between the Solr queries and results, make sure numbers are typed as numbers, and convert the sources of the DRON URI/label relationships into factors... although that isn't being used yet as the SVM is a regressor, not a classifier

also calculate `matchfreq`... do some labels/URIs get returned by Solr disproportionately from the others?  Might be noise.

write it out as *forward_with_dist.csv*

what is the distribution of Solr scores?

![](training_solr_scores.png)


how about the frequency of each terms ever returned by Solr?

![](match_freq.png)

These terms are each returned more than 200 times... one is probably a legitimate match for a common drug, one is probably noise

- bayer aspirin
- rescon jr reformulated feb 2010


Reminiscent of the building the list-of-lists with active ingredients per drug or drug product identified by DRON URI, a list-of-lists `pds.training` is now build for all of the ingredients known  to be in each medication order, by way of the mapped RXNORM value, and labeled with its full name and 

`bottom.line` is then created with all unique pairings of Solr search terms (medication order full names) vs the URIs of the drugs they hit (via the string that they hit)

`bottom.line$inter.div.union` contains the calculated jaccard indices between the ingredients in the drugs mentioned in the orders, again by way of the pre-mapped RXNORM values, and the ingredients in the matched DRON URIs

KINDA SLOW

![](calc_jaccards.png)

MIGHT NEED MORE NEGATIVE TRAINERS... so ask for more Solr results... will make training slower

`curated.backmerge`  is a merge of `forward.with.dist` and `bottom.line`

A 70/30 training/validation split is performed on `curated.backmerge`

The quantitative columns are split out into `quantcols`.  Boolean values become 0 or 1.  The "sources" of the DRON URI/label relationships are strings and can be cast to factors, but I'm not modeling them in the SVM regressor... switch to a classifier?

The split, quantitative data-frames are `modelable` and `checkable`

A SVM regression can be run on `modelable`, using the jaccard index as the target and everything else as a predictor.

70% of the 5000 = wes set can be trained in 13.25 minutes

Settings of cost = 10 and gamma = 0.5 were previously set with a tuner running over 

`cost = 10 ^ (-2:2)` and `gamma = c(0.01, 0.1, .5, 1, 2)`

the tuner hasn't been rerun in a while, because it runs the SVM though |cost| * |gamma| iterations (or whatever other parameters are varied), and doesn't have a built-in multithreading approach

next, jaccard coefficients are **predicted** for all of the Solr search/result pairings

The correlation between the known jaccard indices and the predicted jaccard indices is 0.925461.  The SVM training takes ~ 30 minutes

In the following visualization, the color of the hexagon indicates the number of Solr pairings that fall at the intersection of a give known and predicated jaccard index for active ingredients.

![](jac_cor_hexbin.png)

The distribution of predicted jaccard indices can be modeled as a mixture of Guassians.  The lower bound of the peak centered at 1.0 (defined as mean - 2 * SD) could be used as an acceptance cutoff and has he value 0.927


![](pred_jac_mix.png)


`spotcheck` is created from the rows in `curated.backmerge` that are relevant to the training set, along with a column of the difference between the calculated and predicted jaccard indices.  `spotcheck.top` limit the data frame to the top predicted score for each query, and is then saved as *spotcheck.csv*.

----

## svmdiff diagnostics

A low svmdiff (roughly -1 for the two following examples) means the SVM thinks there's a good match but the RXNORM annotation from the medication orders disagrees.  In other words, the SVM predicted that the URI returned by Solr correctly predicts the active ingredients, but the calculated jaccard index is low, indicating that the URI returned by Solr doesn't indicate the correct active ingredients.

The "grape seed 50 mg po caps" example is clear:  "grape" in the query matched the (rare word?) "grape" from "wine grape allergenic extract 50 MG/ML Injectable Solution" (http://purl.obolibrary.org/obo/DRON_00060006, RXNORM 892320) in the Solr document collection, which contains only one ingredient, "wine grape allergenic extract" http://purl.obolibrary.org/obo/DRON_00019544, RXNORM 892317.  The medication orders row for "grape seed 50 mg po caps" is annotated with RXNORM 237116 for the active ingredient "Grape Seed Extract" (http://purl.obolibrary.org/obo/DRON_00016788.  Therefore, the calculated jaccard is 0.0.

Would adding caps, capsule etc to the synonyms help?

See "Grape Seed Extract 50 MG Oral Capsule", http://purl.obolibrary.org/obo/DRON_00037231

Doing so brought http://purl.obolibrary.org/obo/DRON_00037231 up top the top Solr result.  (String similarity wouldn't improve.)

```
grape seed 50 mg po caps
http://purl.obolibrary.org/obo/DRON_00060006
wine grape allergenic extract 50 MG/ML Injectable Solution
892320
0.0000000
1.1594112
-1.1594112
```

One of the Solr hits for query "doxycycline -" is "Doxycycline Disintegrating Oral Tablet" (http://purl.obolibrary.org/obo/DRON_00731332, RXNORM 597519), which doesn't have any active ingredients, so its inclusion in the Solr document collection is really an error... or at least, it should reverse-inherit the active ingredients from "Doxycycline 100 MG Disintegrating Tablet", http://purl.obolibrary.org/obo/DRON_00054441.  Currently, only forward inheritance is applied.

```
doxycycline -
http://purl.obolibrary.org/obo/DRON_00731332
Doxycycline Disintegrating Oral Tablet
597519
0.0000000
1.1709084
-1.1709084
```

```
> pds.training['doxycycline -']
$`doxycycline -`
[1] "http://purl.obolibrary.org/obo/CHEBI_50845"
```

Actually, 'http://purl.obolibrary.org/obo/DRON_00731332' isn't in the ingredient list... 
```
> ing.list['http://purl.obolibrary.org/obo/DRON_00731332']
$<NA>
NULL
```

```
> ing.list['http://purl.obolibrary.org/obo/DRON_00054441']
$`http://purl.obolibrary.org/obo/DRON_00054441`
[1] "http://purl.obolibrary.org/obo/CHEBI_50845"
```

A high svmdiff (roughly 1 for the two following examples) means the SVM thinks there's a poor match but the RXNORM annotation from the medication orders disagrees.  In other words, the SVM predicted that the URI returned by Solr fails to predict the active ingredients, but the calculated jaccard index is high, indicating that the URI returned by Solr really does indicate the correct active ingredients.


k-10 oral tablet, extended release
http://purl.obolibrary.org/obo/DRON_00073160
rum k
208941
1.0000000
-0.08550927
1.0855093

bisacodyl 5 mg oral delayed release tablet
http://purl.obolibrary.org/obo/DRON_00020976
Bisacodyl Rectal Suppository
371086
1.0000000
-0.05130112
1.0513011

----

## applying the SVM to the entire *wes_pds_enc__med_order.csv* dataset

The distribution of Solr scores from searching all of the full names from the entire *wes_pds_enc__med_order.csv* is similar to the results form just searching the verifiable/pre-mapped full names

![](applied_solr_scores.png)

But the distrubtion of SVM-predicted jaccard indices is different... the peak at 1.0 isn't as sharp, adn there is a new razor-sharp peak ~ 0.4

![](applied_svm_preds.png)

Immediately after the predication are done, `forward.with.dist` is saved as *predicted_meds.csv*

Then the best predictions for each query, over the threshold (0.9271507), become `for.karma`, **which has lots of blank order names**

I filter them out

Then I create `unmatched`... the subset of medication orders that don't have predicted DRON URI

About 25% of the orders don't get a prediction above the threshold!

dl_t:insulin aspart 100 unit/ml sc sopn

auto_not_tidy.csv#14237
c95871396b964d698adc367472f3c8cb
Score: 25.466858
d.full_s
http://purl.obolibrary.org/obo/DRON_00045186
dl_s
Insulin, Aspart, Human 100 UNT/ML Injectable Solution
id
auto_not_tidy.csv#14237
rxn_s
311040

do some of them already have an rxnorm mapping in wes_pds__med_standard.csv ?
maybe 10%
have been going through the mapping failures and updating stop-words and synonyms, and preparing a curated training list


