package org.example.chapter03

import spock.lang.Specification
import org.apache.lucene.store.Directory
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TopDocs
import org.apache.lucene.document.Document
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.SimpleAnalyzer
import org.example.utils.TestUtil

class SearchTest extends Specification {
    static Directory dir
    static IndexSearcher searcher

    def setupSpec() {
        dir = TestUtil.getBookIndexDirectory()
        searcher = new IndexSearcher(dir)
    }

    def cleanupSpec() {
        searcher?.close()
        dir?.close()
    }

    def "test query parser results - Part 1"() {
        given: "A directory and an IndexSearcher"
        QueryParser parser = new QueryParser(Version.LUCENE_30, "contents", new SimpleAnalyzer())

        when: "A query for documents containing both JUNIT and ANT but not MOCK"
        Query query1 = parser.parse("+JUNIT +ANT -MOCK")

        and: "Search is performed"
        TopDocs docs1 = searcher.search(query1, 10)

        then: "One document is found"
        docs1.totalHits == 1

        and: "The title of the document matches 'Ant in Action'"
        Document d1 = searcher.doc(docs1.scoreDocs[0].doc)
        d1.get("title") == "Ant in Action"
    }

    def "test query parser results - Part 2"() {
        given: "A directory and an IndexSearcher"
        QueryParser parser = new QueryParser(Version.LUCENE_30, "contents", new SimpleAnalyzer())

        when: "A query for documents containing either mock or junit"
        Query query2 = parser.parse("mock OR junit")

        and: "Search is performed"
        TopDocs docs2 = searcher.search(query2, 10)

        then: "Two documents are found"
        docs2.totalHits == 2

        when: "Get the two documents"
        Document d2 = searcher.doc(docs2.scoreDocs[0].doc)
        Document d3 = searcher.doc(docs2.scoreDocs[1].doc)

        then: "The titles of the documents match the expected values"
        new HashSet([d2.get("title"), d3.get("title")])
                ==
                new HashSet(["Ant in Action", "JUnit in Action, Second Edition"])
    }
}

