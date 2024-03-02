package org.example.chapter01

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version

class Indexer implements Closeable {
    static final String FILE_SUFFIX = '.txt'

    private IndexWriter writer;
    Indexer(String indexDir) {
        Directory dir = FSDirectory.open(new File(indexDir))
        writer = new IndexWriter(
                dir,
                new StandardAnalyzer(Version.LUCENE_30),
                true,
                IndexWriter.MaxFieldLength.UNLIMITED
        )
    }

    void close() {
        writer?.close()
    }

    private static class TextFilesFilter implements FileFilter {
        boolean accept(File path) {
            return path.name.toLowerCase().endsWith(FILE_SUFFIX)
        }
    }

    protected Document getDocument(File f) {
        Document doc = new Document()
        doc.add(new Field('contents', new FileReader(f)))
        doc.add(new Field('filename', f.name, Field.Store.YES, Field.Index.NOT_ANALYZED))
        doc.add(new Field('fullpath', f.canonicalPath, Field.Store.YES, Field.Index.NOT_ANALYZED))
        return doc
    }

    private void indexFile(File f) {
        println "Indexing: ${f.canonicalPath}"
        Document doc = getDocument(f)
        writer.addDocument(doc)
    }

    int index(String dataDir, FileFilter filter) {
        File[] files = new File(dataDir).listFiles()
        for (File f : files) {
            if (!f.directory && !f.hidden
            && f.exists() && f.canRead() && (filter?.accept(f))) {
                indexFile(f)
            }
        }
        return writer.numDocs()
    }


    static void main(String[] args) {
        assert args.length==2, "Usage: <index dir> <data dir>"
        String indexDir = args[0]
        String dataDir = args[1]
        long start = System.currentTimeMillis()

        Indexer indexer = new Indexer(indexDir)
        try {
            int numIndexed = indexer.index(dataDir, new TextFilesFilter())
            long end = System.currentTimeMillis()
            println ">>>>>>>> Indexing $numIndexed files took ${end-start} milliseconds"
        } finally {
            indexer.close()
        }
    }
}
