package org.example.chapter02

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version

class Searcher {

    static void search(String indexDir, String q) {
        Directory dir = FSDirectory.open(new File(indexDir))
        IndexSearcher is = new IndexSearcher(dir)
        QueryParser parser = new QueryParser(
                Version.LUCENE_30,
                'contents',
                new StandardAnalyzer(Version.LUCENE_30)
        )
        Query query = parser.parse(q)
        long start = System.currentTimeMillis()
        TopDocs hits = is.search(query, 10)
        long end = System.currentTimeMillis()
        println "Found ${hits.totalHits} document(s) in ${end-start} milliseconds that matched query >$q<:"
        for (ScoreDoc scoreDoc: hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc)
            println "\t${doc.get('fullpath')}"
        }
        is.close()
    }

    static void main(String[] args) {
        assert args.length==2, "Usage <indexDir> <query>"
        String indexDir = args[0]
        String q = args[1]
        search(indexDir, q)
    }
}
