package org.example.utils

import org.apache.lucene.document.DateTools
import org.apache.lucene.document.Document
import org.apache.lucene.document.NumericField
import org.apache.tools.ant.util.DateUtils
import spock.lang.Specification
import org.apache.lucene.document.Field
import static org.apache.lucene.document.Field.Store.*
import static org.apache.lucene.document.Field.Index.*
import static org.apache.lucene.document.Field.TermVector.*
import static org.example.utils.CreateTestIndex.*

class TestCreateTestIndex extends Specification {
    static int STORE = 1<<3
    static int ANALYZE = 1<<2
    static int NONORM = 1<<1
    static int TERM = 1<<0

    static Map<String,Boolean> getOptions(int flags) {
        Map<String,Boolean> options = [:]
        if (flags&STORE) options.store = true
        if (flags&ANALYZE) options.analyze = true
        if (flags&NONORM) options.nonorm = true
        if (flags&TERM) options.term = true
        return options
    }

    def "test getStore()"(Field.Store store, int flags) {
        expect:
        store == getStore(getOptions(flags))

        where:
        store | flags // store, analyze, nonorm, term
        NO | 0b0000
        NO | 0b0001
        NO | 0b0010
        NO | 0b0011
        NO | 0b0100
        NO | 0b0101
        NO | 0b0110
        NO | 0b0111

        YES | 0b1000
        YES | 0b1001
        YES | 0b1010
        YES | 0b1011
        YES | 0b1100
        YES | 0b1101
        YES | 0b1110
        YES | 0b1111
    }

    def "test getIndex()"(Field.Index index, int flags) {
        expect:
        index == getIndex(getOptions(flags))

        where:
        index | flags // store, analyze, nonorm, term
        NOT_ANALYZED | 0b0000 // 0
        NOT_ANALYZED | 0b0001 // 1
        NOT_ANALYZED_NO_NORMS | 0b0010 // 2
        NOT_ANALYZED_NO_NORMS | 0b0011 // 3
        ANALYZED | 0b0100 // 4
        ANALYZED | 0b0101 // 5
        ANALYZED | 0b0110 // 6
        ANALYZED | 0b0111 // 7

        NOT_ANALYZED | 0b1000 // 8
        NOT_ANALYZED | 0b1001 // 9
        NOT_ANALYZED_NO_NORMS | 0b1010 // 10
        NOT_ANALYZED_NO_NORMS | 0b1011 // 11
        ANALYZED | 0b1100 // 12
        ANALYZED | 0b1101 // 13
        ANALYZED | 0b1110 // 14
        ANALYZED | 0b1111 // 15
    }

    def "test getTerm()"(Field.TermVector term, int flags) {
        expect:
        term == getTerm(getOptions(flags))

        where:
        term | flags // store, analyze, nonorm, term
        null | 0b0000 // 0
        WITH_POSITIONS_OFFSETS | 0b0001 // 1
        null | 0b0010 // 2
        WITH_POSITIONS_OFFSETS | 0b0011 // 3
        null | 0b0100 // 4
        WITH_POSITIONS_OFFSETS | 0b0101 // 5
        null | 0b0110 // 6
        WITH_POSITIONS_OFFSETS | 0b0111 // 7

        null | 0b1000 // 8
        WITH_POSITIONS_OFFSETS | 0b1001 // 9
        null | 0b1010 // 10
        WITH_POSITIONS_OFFSETS | 0b1011 // 11
        null | 0b1100 // 12
        WITH_POSITIONS_OFFSETS | 0b1101 // 13
        null | 0b1110 // 14
        WITH_POSITIONS_OFFSETS | 0b1111 // 15
    }

    def "Test makeField(stored:true)"() {
//        Field("isbn",                     // 3
//                isbn,                       // 3
//                Field.Store.YES,            // 3
//                Field.Index.NOT_ANALYZED)); // 3
        given:
        String isbn = "293298329832983298398"
        Field field = makeField("isbn",isbn,store:true)

        expect:
        field!=null
        field.name()=="isbn"
        field.stringValue()==isbn
        field.stored
        field.indexed
        !field.tokenized  // analyze
        !field.omitNorms
        !field.termVectorStored
        !field.storeOffsetWithTermVector
        !field.storePositionWithTermVector
    }

    def "Test makeField(store:true,analyze:true,term:true)"() {
//        Field("title",                    // 3
//                title,                      // 3
//                Field.Store.YES,            // 3
//                Field.Index.ANALYZED,       // 3
//                Field.TermVector.WITH_POSITIONS_OFFSETS));
        given:
        String title = "Now is the time for all good men to come to the aid of their country"
        Field field = makeField("title",title,store:true,analyze:true,term:true)

        expect:
        field!=null
        field.name()=="title"
        field.stringValue()==title
        field.stored
        field.indexed
        field.tokenized  // analyze
        !field.omitNorms
        field.termVectorStored
        field.storeOffsetWithTermVector
        field.storePositionWithTermVector
    }

    def "Test makeField(store:true,analyze:true,term:true)"() {
//        Field("title",                    // 3
//                title,                      // 3
//                Field.Store.YES,            // 3
//                Field.Index.ANALYZED,       // 3
//                Field.TermVector.WITH_POSITIONS_OFFSETS));
        given:
        String title = "Now is the time for all good men to come to the aid of their country"
        Field field = makeField("title",title,store:true,analyze:true,term:true)

        expect:
        field!=null
        field.name()=="title"
        field.stringValue()==title
        field.stored
        field.indexed
        field.tokenized  // analyze
        !field.omitNorms
        field.termVectorStored
        field.storeOffsetWithTermVector
        field.storePositionWithTermVector
    }

    def "Test makeField(analyze:true,term:true)"() {
//        Field("contents", text,                             // 3 // 5
//                Field.Store.NO, Field.Index.ANALYZED,         // 3 // 5
//                Field.TermVector.WITH_POSITIONS_OFFSETS))
        given:
        String title = "Now is the time for all good men to come to the aid of their country"
        Field field = makeField("contents",title,analyze:true,term:true)

        expect:
        field!=null
        field.name()=="contents"
        field.stringValue()==title
        !field.stored
        field.indexed
        field.tokenized  // analyze
        !field.omitNorms
        field.termVectorStored
        field.storeOffsetWithTermVector
        field.storePositionWithTermVector
    }

    def "test makeIntField()"() {
        given:
        int testNum = 2_382_138
        NumericField field = makeIntField("pubmonthAsDay",testNum)

        expect:
        field!=null
        field.name()=="pubmonthAsDay"
        field.dataType==NumericField.DataType.INT
        field.numericValue==testNum
        field.numericValue.class==Integer
        !field.stored
        field.indexed
        field.tokenized  // analyze
        field.omitNorms
        !field.termVectorStored
        !field.storeOffsetWithTermVector
        !field.storePositionWithTermVector
    }

    def "test makeIntField(String)"() {
        given:
        int testNum = 2_382_138
        NumericField field = makeIntField("pubmonthAsDay",testNum.toString())

        expect:
        field!=null
        field.name()=="pubmonthAsDay"
        field.dataType==NumericField.DataType.INT
        field.numericValue==testNum
        field.numericValue.class==Integer
        !field.stored
        field.indexed
        field.tokenized  // analyze
        field.omitNorms
        !field.termVectorStored
        !field.storeOffsetWithTermVector
        !field.storePositionWithTermVector
    }

    def "test makeIntField(store:true)"() {
        given:
        int testNum = 2_382_138
        NumericField field = makeIntField("pubmonth", testNum, store:true)

        expect:
        field!=null
        field.name()=="pubmonth"
        field.dataType==NumericField.DataType.INT
        field.numericValue==testNum
        field.numericValue.class==Integer
        field.stored
        field.indexed
        field.tokenized  // analyze
        field.omitNorms
        !field.termVectorStored
        !field.storeOffsetWithTermVector
        !field.storePositionWithTermVector
    }

    def "test makeIntField(String,store:true)"() {
        given:
        int testNum = 2_382_138
        NumericField field = makeIntField("pubmonth", testNum.toString(), store:true)

        expect:
        field!=null
        field.name()=="pubmonth"
        field.dataType==NumericField.DataType.INT
        field.numericValue==testNum
        field.numericValue.class==Integer
        field.stored
        field.indexed
        field.tokenized  // analyze
        field.omitNorms
        !field.termVectorStored
        !field.storeOffsetWithTermVector
        !field.storePositionWithTermVector
    }

    def "Test Document.add()"() {
        given:
        Document doc = new Document()
        String isbn = "293298329832983298398"
        Field field1 = makeField("isbn",isbn,store:true)

        when:
        doc.add(field1)

        then:
        doc.fields.size()==1
        doc.getFieldable("isbn")===field1
        doc.get("isbn")==isbn
    }

    def "Test DocumentCategory .append(field1)"() {
        given:
        Document doc = new Document()
        String isbn = "293298329832983298398"
        Field field1 = makeField("isbn",isbn,store:true)

        when:
        use (DocumentCategory) {
            doc.append(field1)
        }

        then:
        doc.fields.size()==1
        doc.getFieldable("isbn")===field1
        doc.get("isbn")==isbn
    }

    def "Test DocumentCategory << field1"() {
        given:
        Document doc = new Document()
        String isbn = "293298329832983298398"
        Field field1 = makeField("isbn",isbn,store:true)

        when:
        use (DocumentCategory) {
            doc << field1
        }

        then:
        doc.fields.size()==1
        doc.getFieldable("isbn")===field1
        doc.get("isbn")==isbn
    }

    def "Test DocumentCategory << field1 << field2"() {
        given:
        Document doc = new Document()
        String isbn = "293298329832983298398"
        Field field1 = makeField("isbn",isbn,store:true)
        String title = "Now is the time for all good men to come to the aid of their country"
        Field field2 = makeField("contents",title,analyze:true,term:true)

        when:
        use (DocumentCategory) {
            doc << field1 << field2
        }

        then:
        doc.fields.size()==2
        doc.getFieldable("isbn")===field1
        doc.get("isbn")==isbn
        doc.getFieldable("contents")===field2
        doc.get("contents")==title
    }

    def "Test DocumentCategory << field1 << field2 << field3"() {
        given:
        Document doc = new Document()
        String isbn = "293298329832983298398"
        Field field1 = makeField("isbn",isbn,store:true)
        String title = "Now is the time for all good men to come to the aid of their country"
        Field field2 = makeField("contents",title,analyze:true,term:true)
        String topic = "Nuclear energy and radiation sickness"
        Field field3 = makeField("contents",topic,analyze:true,term:true)

        when:
        use (DocumentCategory) {
            doc
                    << field1
                    << field2
                    << field3
        }

        then:
        doc.fields.size()==3
        doc.getFieldables("isbn").length==1
        doc.getFieldables("isbn")[0]===field1
        doc.getValues("isbn").length==1
        doc.getValues("isbn")[0]==isbn
        doc.getFieldables("contents").length==2
        doc.getFieldables("contents")[0]==field2
        doc.getFieldables("contents")[1]==field3
        doc.getValues("contents").length==2
        doc.getValues("contents")[0]==title
        doc.getValues("contents")[1]==topic
    }

    static Map<String,String> test_geb_properties = [
            title:"G\u00F6del, Escher, Bach: an Eternal Golden Braid",
            isbn:"9780394756820",
            author:"Douglas Hofstadter",
            pubmonth:"199905",
            subject:"artificial intelligence number theory mathematics music",
            url:"http://www.amazon.com/Godel-Escher-Bach-Eternal-Golden/dp/0394756827",
            category:"technology.computers/ai",
    ]

    def "Test getDocument(Map)"() {
        given:
        Document doc = getDocument(test_geb_properties)
        Date pubDate = DateTools.stringToDate(test_geb_properties.pubmonth)
        int pubmonthAsDay = pubDate.getTime()/(1000*3600*24)

        expect:
        doc != null
        with(test_geb_properties) {
            doc.get("isbn") == isbn
            doc.get("title") == title
            doc.get("title2") == title.toLowerCase()
            doc.get("url") == url
            doc.get("subject") == subject
            doc.getValues("contents") as List == [title, subject, author, category]
            ((NumericField)doc.getFieldable("pubmonth")).numericValue==Integer.parseInt(pubmonth)
            ((NumericField)doc.getFieldable("pubmonthAsDay")).numericValue==pubmonthAsDay
        }
    }
}
