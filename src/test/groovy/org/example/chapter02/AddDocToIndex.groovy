package org.example.chapter02

import org.apache.lucene.analysis.WhitespaceAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.Term
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.store.Directory
import org.apache.lucene.store.RAMDirectory
import spock.lang.Specification
import org.apache.lucene.document.Field

class AddDocToIndex extends Specification {
    def ids = ['1','2']
    def unindexed = ['Netherlands','Italy']
    def unstored = ['Amsterdam has lots of bridges',
    'Venice has lots of canals']
    def text = ['Amsterdam','Venice']
    def Directory directory

    private IndexWriter getWriter() {
        new IndexWriter(
                directory,
                new WhitespaceAnalyzer(),
                IndexWriter.MaxFieldLength.UNLIMITED
        )
    }

    def setup() {
       directory = new RAMDirectory()
        IndexWriter writer = this.writer
        ids.eachWithIndex {
            String id, int i
            ->
                Document doc = new Document()
                doc.add(new Field('id',       id,           Field.Store.YES, Field.Index.NOT_ANALYZED))
                doc.add(new Field('country',  unindexed[i], Field.Store.YES, Field.Index.NO))
                doc.add(new Field('contents', unstored[i],  Field.Store.NO,  Field.Index.ANALYZED))
                doc.add(new Field('city',     text[i],      Field.Store.YES, Field.Index.ANALYZED))
                writer.addDocument(doc)
        }
        writer.close()
    }

    protected int getHitCount(String fieldName, String searchString) {
        IndexSearcher searcher = new IndexSearcher(directory)
        Term t = new Term(fieldName, searchString)
        Query query = new TermQuery(t)
        int hitCount = TestUtil.hitCount(searcher,query)
        searcher.close()
        return hitCount
    }

    def "Test IndexWriter"() {
        when:
        IndexWriter writer = getWriter()

        then:
        ids.size() == writer.numDocs()

        cleanup:
        writer.close()
    }

    def "Test IndexReader"() {
        when:
        IndexReader reader = IndexReader.open(directory)

        then:
        ids.size() == reader.maxDoc()
        ids.size() == reader.numDocs()

        cleanup:
        reader.close()
    }

    def "Test Delete before Optimize"() {
        when:
        IndexWriter writer = getWriter()

        then:
        writer.numDocs() == 2

        when:
        writer.deleteDocuments(new Term('id','1'))
        writer.commit()

        then:
        writer.hasDeletions()
        writer.maxDoc() == 2
        writer.numDocs() == 1

        cleanup:
        writer.close()
    }

    def "Test Delete After Optimize"() {
        when:
        IndexWriter writer = getWriter()

        then:
        writer.numDocs() == 2

        when:
        writer.deleteDocuments(new Term('id','1'))
        writer.optimize()
        writer.commit()

        then:
        !writer.hasDeletions()
        writer.maxDoc() == 1
        writer.numDocs() == 1

        cleanup:
        writer.close()
    }
}
