package org.grobid.core.engines;

import com.googlecode.clearnlp.engine.EngineGetter
import com.googlecode.clearnlp.tokenization.AbstractTokenizer
import org.grobid.core.data.SentenceParse
import org.grobid.core.utilities.GrobidConfig
import org.grobid.core.utilities.GrobidProperties
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test


class DefaultQuantifiedObjectParserTest {

    private var target: DefaultQuantifiedObjectParser? = null

    @BeforeEach
    fun setUp() {
        target = DefaultQuantifiedObjectParser()
//        val tokenizer: AbstractTokenizer? = EngineGetter.getTokenizer("en", );
    }

    @Test
    fun addTokenIndex() {
        val result: List<String> = ArrayList()

        val tree = """1	However	however	RB	_	12	advmod	12:AM-DIS
2	,	,	,	_	12	punct	_
3	upon	upon	IN	_	12	prep	12:AM-TMP
4	closer	close	JJR	_	5	amod	_
5	analysis	analysis	NN	_	3	pobj	_
6	,	,	,	_	12	punct	_
7	none	none	NN	_	12	nsubj	12:A1;15:A1
8	of	of	IN	_	7	prep	_
9	the	the	DT	_	11	det	_
10	five	#crd#	CD	_	11	num	_
11	assumptions	assumption	NNS	_	8	pobj	_
12	are	be	VBP	pb=be.01	0	root	_
13	ful	ful	JJ	_	15	hmod	15:A2
14	-	-	HYPH	_	15	hyph	_
15	filled	fill	VBN	pb=fill.01	12	acomp	_
16	a	a	DT	_	29	det	_
17	priori	priori	JJ	_	12	acomp	12:A2
18	and	and	CC	_	17	cc	_
19	,	,	,	_	17	punct	_
20	thus	thus	RB	_	37	advmod	37:AM-ADV
21	,	,	,	_	37	punct	_
22	significant	significant	JJ	_	23	amod	_
23	errors	error	NNS	_	37	nsubj	37:A1
24	or	or	CC	_	23	cc	_
25	misinterpretations	misinterpretation	NNS	_	23	conj	_
26	,	,	,	_	25	punct	_
27	e.g.	e.g.	FW	_	29	advmod	_
28	,	,	,	_	29	punct	_
29	overestimation	overestimation	NN	_	25	appos	_
30	of	of	IN	_	29	prep	_
31	the	the	DT	_	34	det	_
32	average	average	JJ	_	34	amod	_
33	field	field	NN	_	34	nn	_
34	strength	strength	NN	_	30	pobj	_
35	,	,	,	_	37	punct	_
36	can	can	MD	_	37	aux	37:AM-MOD
37	occur	occur	VB	pb=occur.01	17	appos	_
38	when	when	WRB	_	41	advmod	41:R-AM-TMP
39	apply	apply	VB	pb=apply.02	41	hmod	_
40	-	-	HYPH	_	41	hyph	_
41	ing	ing	VBG	pb=ing.01	37	advcl	37:AM-TMP;39:AM-TMP
42	the	the	DT	_	45	det	_
43	simple	simple	JJ	_	45	amod	_
44	signal	signal	NN	_	45	nn	_
45	model	model	NN	_	41	dobj	41:A1
46	,	,	,	_	45	punct	_
47	i.e.	i.e.	FW	_	50	advmod	_
48	,	,	,	_	50	punct	_
49	(	(	-LRB-	_	50	punct	_
50	1	0	LS	_	54	meta	_
51	)	)	-RRB-	_	50	punct	_
52	to	to	TO	_	17	prep	_
53	(	(	-LRB-	_	54	punct	_
54	3	0	CD	_	52	parataxis	_
55	)	)	-RRB-	_	54	punct	_
56	.	.	.	_	12	punct	_"""

        var sentence =
            "However, upon closer analysis, none of the five assumptions are ful- filled a priori and, thus, significant errors or misinterpretations, e.g., overestimation of the average field strength, can occur when apply- ing the simple signal model, i.e., (1) to (3).";

        val sentenceParse = SentenceParse()
        sentenceParse.parseRepresentation = tree
        sentenceParse.createMap(sentence)

        var indexes = target?.addTokenIndex(0, 1, sentenceParse, ArrayList())

        assertThat(indexes, hasSize(1))
        assertThat(indexes!!.get(0), `is`("1"))
        assertThat(sentenceParse.getTokenStructureByIndex(indexes.get(0)), startsWith("1\tHowever"))

        indexes = target?.addTokenIndex(0, 7, sentenceParse, ArrayList())

        assertThat(indexes, hasSize(1))
        assertThat(indexes!!.get(0), `is`("1"))
        assertThat(sentenceParse.getTokenStructureByIndex(indexes.get(0)), startsWith("1\tHowever"))

        indexes = target?.addTokenIndex(9, 4, sentenceParse, ArrayList())
        assertThat(indexes, hasSize(1))
        assertThat(indexes!!.get(0), `is`("3"))

        indexes = target?.addTokenIndex(9, 6, sentenceParse, ArrayList())
        assertThat(indexes, hasSize(2))
        assertThat(indexes!!.get(0), `is`("3"))
        assertThat(indexes.get(1), `is`("4"))
    }

    companion object {
        @JvmStatic
        @BeforeAll
        @Throws(Exception::class)
        fun before() {
            val modelParameters = GrobidConfig.ModelParameters()
            modelParameters.name = "bao"
            GrobidProperties.addModel(modelParameters)
        }
    }
}