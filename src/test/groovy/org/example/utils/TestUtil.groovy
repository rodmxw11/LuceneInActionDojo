package org.example.utils

import org.apache.lucene.document.Field.Index
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopDocs
import org.apache.lucene.search.Query
import org.apache.lucene.search.Filter
import org.apache.lucene.document.Document
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.store.Directory

class TestUtil {
    static String BOOK_INDEX_PATH = 'C:/Projects/BookLuceneInAction/lia2e/build/index'

    static boolean hitsIncludeTitle(IndexSearcher searcher, TopDocs hits, String title) {
        for (ScoreDoc match : hits.scoreDocs) {
            Document doc = searcher.doc(match.doc)
            if (title.equals(doc.get('title'))) {
                return true
            }
        }
        println "title $title not found"
        return false
    }

    static int hitCount(IndexSearcher searcher, Query query) {
        searcher.search(query,1).totalHits
    }

    static int hitCount(IndexSearcher searcher, Query query, Filter filter) {
        searcher.search(query, filter, 1).totalHits
    }

    static void dumpHits(IndexSearcher searcher, TopDocs hits) {
        if (hits.totalHits==0) {
            println "NO HITS!"
        }
        for (ScoreDoc match : hits.scoreDocs) {
            Document doc = searcher.doc(match.doc)
            println "${match.score}: ${doc.get('title')}"
        }
    }

    static Directory getBookIndexDirectory() {
        FSDirectory.open(new File(BOOK_INDEX_PATH))
    }

    static void rmDir(File dir) {
        if (!dir.exists()) {
            return
        }
        File[] files = dir.listFiles()
        files.each {
            File file
            ->
                assert file.delete(), "Unable to delete this file: ${file.canonicalPath}"
        }
        assert dir.delete(), "Unable to delete this directory: ${files.canonicalPath}"
    }
}
