package org.infinispan.lucene.testutils;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * Utilities to read and write Lucene indexes
 *
 * @author gustavonalle
 * @since 7.0
 */
public class LuceneUtils {

   private LuceneUtils() {
   }

   /**
    * Read all terms from a field
    *
    * @param field the field in the document to load terms from
    * @param directory Any directory implementation
    * @return Unique terms represented as UTF-8
    * @throws IOException
    */
   public static Set<String> readTerms(String field, Directory directory) throws IOException {
      IndexReader reader = null;
      Set<String> termStrings = new TreeSet<String>();
      try {
         reader = IndexReader.open(directory);
         TermEnum terms = reader.terms();
         while (terms.next()) {
            Term term = terms.term();
            if (term.field().equals(field)) {
               termStrings.add(term.text());
            }
         }
      } finally {
         if (reader != null) {
            reader.close();
         }
      }
      return termStrings;
   }

   /**
    * Counts the documents
    * @param directory Directory
    * @return the number of docs,including all segments
    * @throws IOException
    */
   public static int numDocs(Directory directory) throws IOException {
      IndexReader reader = null;
      try {
         reader = IndexReader.open(directory);
         return reader.numDocs();
      } finally {
         if (reader != null) {
            reader.close();
         }
      }
   }

   /**
    * Collect all documents from an index
    * @param directory Directory
    * @param limit maximum number of documents to collect
    * @return List of Documents
    * @throws IOException
    */
   public static List<Document> collect(Directory directory, int limit) throws IOException {
      IndexSearcher indexSearcher = null;
      try {
         indexSearcher = new IndexSearcher(IndexReader.open(directory));
         MatchAllDocsQuery allDocsQuery = new MatchAllDocsQuery();
         List<Document> docs = new ArrayList<Document>(limit);
         TopDocs topDocs = indexSearcher.search(allDocsQuery, limit);
         for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            docs.add(indexSearcher.doc(scoreDoc.doc));
         }
         return docs;
      } finally {
         assert indexSearcher != null;
         indexSearcher.close();
      }
   }


}
