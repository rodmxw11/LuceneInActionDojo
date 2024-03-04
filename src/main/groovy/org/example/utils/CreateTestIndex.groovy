package org.example.utils

import org.apache.lucene.document.Document

import org.apache.lucene.document.Field
import org.apache.lucene.document.Fieldable

import static org.apache.lucene.document.Field.Store.*
import static org.apache.lucene.document.Field.Index.*
import static org.apache.lucene.document.Field.TermVector.*

import org.apache.lucene.document.NumericField
import org.apache.lucene.document.DateTools
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.util.Version
import java.text.ParseException

class CreateTestIndex {

    static Field.Store getStore(Map<String,Boolean> options=[:]) {
        options.store?YES:NO
    }

    static Field.Index getIndex(Map<String,Boolean> options=[:]) {
        options.analyze?ANALYZED:
                options.nonorm?NOT_ANALYZED_NO_NORMS:NOT_ANALYZED
    }

    static Field.TermVector getTerm(Map<String,Boolean> options=[:]) {
        options.term?WITH_POSITIONS_OFFSETS:null
    }

    static class DocumentCategory {
        static Document leftShift(Document self, Fieldable field) {
            self.add(field)
            return self
        }
        static Document append(Document self, Fieldable field) {
            self.add(field)
            return self
        }
    }

    static Field makeField( Map<String,Boolean> options=[:], String name, String value) {
        Field.TermVector term = getTerm(options)
        if (term) {
            return new Field(name,(String)value,getStore(options),getIndex(options),term)
        } else {
            return new Field(name,(String)value,getStore(options),getIndex(options))
        }
    }

    static NumericField makeIntField(Map<String,Boolean> options=[:], String name, Integer value) {
        Field.Store store = getStore(options)
        if (store==YES) {
            return new NumericField(name, YES, true).setIntValue(value)
        } else {
            return new NumericField(name).setIntValue(value)
        }
    }

    static NumericField makeIntField(Map<String,Boolean> options=[:], String name, String value) {
        return makeIntField(options,name,Integer.parseInt(value))
    }

    static Document getDocument(File file) {
        Map<String, String> props = new Properties().with {
            load(file)
            return it
        } as Map<String, String>

        // category comes from relative path below the base directory
        String category = file.canonicalPath.replace('\\', '/').split("/data/")[1]
        props["category"] = category

        return getDocument(props)
    }

    static Document getDocument(Map<String,String> props) {
        Document doc = new Document()

        def makePropField = {
            Map<String, Boolean> options, String propName
                ->
                makeField(options, propName, props[propName])
        }

        def makeContentsField = {
            String propsName
            ->
            makeField("contents",props[propsName],analyze:true,term:true)
        }

        println "${props.title}\n${props.author}\n${props.subject}\n${props.pubmonth}\n${props.category}\n---------"

        use (DocumentCategory) {
            Date pubDate = DateTools.stringToDate(props.pubmonth)
            int pubmonthAsDay = pubDate.getTime()/(1000*3600*24)
            doc
                    << makePropField("isbn",store:true)
                    << makePropField("title",store:true,analyze:true,term:true)
                    << makeField("title2",props.title.toLowerCase(),store:true,nonorm:true,term:true)
                    << makePropField("url",store:true)
                    << makePropField("subject",store:true,analyze:true,term:true)
                    << makeIntField("pubmonth", props.pubmonth, store:true)
                    << makeIntField("pubmonthAsDay",pubmonthAsDay)
                    << makeContentsField("title")
                    << makeContentsField("subject")
                    << makeContentsField("author")
                    << makeContentsField("category")
        }

        // split multiple authors into unique field instances
        props.author.split(',').each {
            String authorName
                ->
                doc.add(makeField("author",authorName.trim(),store:true,term:true))
        }

        return doc;
    }
}
