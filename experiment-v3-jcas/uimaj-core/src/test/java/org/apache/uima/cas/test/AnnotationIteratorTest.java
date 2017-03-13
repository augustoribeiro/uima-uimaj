/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.uima.cas.test;

import static org.apache.uima.cas.SelectFSs.select;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIndexRepository;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.SelectFSs;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.tcas.Annotation;

import junit.framework.TestCase;

/**
 * Class comment for FilteredIteratorTest.java goes here.
 * 
 */
public class AnnotationIteratorTest extends TestCase {
  
  private static final boolean showFSs = false;

  private CAS cas;

  private TypeSystem ts;

  private Type stringType;

  private Type tokenType;

  private Type intType;

  private Type tokenTypeType;

  private Type wordType;

  private Type sepType;

  private Type eosType;

  private Feature tokenTypeFeat;

  private Feature lemmaFeat;

  private Feature sentLenFeat;

  private Feature tokenFloatFeat;

  private Feature startFeature;

  private Feature endFeature;

  private Type sentenceType;
  
  private Type phraseType;

  private boolean isSave;

  private List<AnnotationFS> fss;

  private List<Integer> fssStarts = new ArrayList<Integer>();

  private int callCount = -1;

  /**
   * Constructor for FilteredIteratorTest.
   * 
   * @param arg0
   */
  public AnnotationIteratorTest(String arg0) {
    super(arg0);
  }

  @Override
  public void setUp() {
    try {
      // make a cas with various types, fairly complex -- see CASTestSetup class
      this.cas = CASInitializer.initCas(new CASTestSetup(), null);
      assertTrue(this.cas != null);
      this.ts = this.cas.getTypeSystem();
      assertTrue(this.ts != null);
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
    this.stringType = this.ts.getType(CAS.TYPE_NAME_STRING);
    assertTrue(this.stringType != null);
    this.tokenType = this.ts.getType(CASTestSetup.TOKEN_TYPE);
    assertTrue(this.stringType != null);
    this.intType = this.ts.getType(CAS.TYPE_NAME_INTEGER);
    assertTrue(this.intType != null);
    this.tokenTypeType = this.ts.getType(CASTestSetup.TOKEN_TYPE_TYPE);
    assertTrue(this.tokenTypeType != null);
    this.wordType = this.ts.getType(CASTestSetup.WORD_TYPE);
    assertTrue(this.wordType != null);
    this.sepType = this.ts.getType(CASTestSetup.SEP_TYPE);
    assertTrue(this.sepType != null);
    this.eosType = this.ts.getType(CASTestSetup.EOS_TYPE);
    assertTrue(this.eosType != null);
    this.tokenTypeFeat = this.ts.getFeatureByFullName(CASTestSetup.TOKEN_TYPE_FEAT_Q);
    assertTrue(this.tokenTypeFeat != null);
    this.lemmaFeat = this.ts.getFeatureByFullName(CASTestSetup.LEMMA_FEAT_Q);
    assertTrue(this.lemmaFeat != null);
    this.sentLenFeat = this.ts.getFeatureByFullName(CASTestSetup.SENT_LEN_FEAT_Q);
    assertTrue(this.sentLenFeat != null);
    this.tokenFloatFeat = this.ts.getFeatureByFullName(CASTestSetup.TOKEN_FLOAT_FEAT_Q);
    assertTrue(this.tokenFloatFeat != null);
    this.startFeature = this.ts.getFeatureByFullName(CAS.FEATURE_FULL_NAME_BEGIN);
    assertTrue(this.startFeature != null);
    this.endFeature = this.ts.getFeatureByFullName(CAS.FEATURE_FULL_NAME_END);
    assertTrue(this.endFeature != null);
    this.sentenceType = this.ts.getType(CASTestSetup.SENT_TYPE);
    assertTrue(this.sentenceType != null);
    this.phraseType = this.ts.getType(CASTestSetup.PHRASE_TYPE);
    assertTrue(this.phraseType != null);
  }

  @Override
  public void tearDown() {
    this.cas = null;
    this.ts = null;
    this.tokenType = null;
    this.intType = null;
    this.tokenTypeType = null;
    this.wordType = null;
    this.sepType = null;
    this.eosType = null;
    this.tokenTypeFeat = null;
    this.lemmaFeat = null;
    this.sentLenFeat = null;
    this.tokenFloatFeat = null;
    this.startFeature = null;
    this.endFeature = null;
    this.sentenceType = null;
  }

  public void testIterator1() {
    
    
//   Tokens                +---+
//                        +---+
//                       +---+
//   BigBound                      +-----------------------------+
    final String text = "aaaa bbbb cccc dddd aaaa bbbb cccc dddd aaaa bbbb cccc dddd ";
//                       +--------+
//   Sentences                +--------+
//                                 +----------+
//
//   bound4strict                   +------------------+            
//   sentence4strict                 +-----------------------------+
    
    
    try {
      this.cas.setDocumentText(text);
    } catch (CASRuntimeException e) {
      fail();
    }

    /***************************************************
     * Create and index tokens and sentences 
     ***************************************************/
    FSIndexRepository ir = this.cas.getIndexRepository();
    int annotCount = 1; // Init with document annotation.
    // create token and sentence annotations
    AnnotationFS fs;
    for (int i = 0; i < text.length() - 5; i++) {
      ++annotCount;
      ir.addFS(fs = this.cas.createAnnotation(this.tokenType, i, i + 5));
      if (showFSs) {
        System.out.format("creating: %d begin: %d end: %d type: %s%n", annotCount, fs.getBegin(), fs.getEnd(), fs.getType().getName() );
      }
    }
    // for (int i = 0; i < text.length() - 5; i++) {
    // cas.getIndexRepository().addFS(cas.createAnnotation(tokenType, i, i+5));
    // }
    
    // create overlapping sentences for unambigious testing
    //   begin =  0,  5, 10, ...
    //   end   = 10, 15, 20, ...
    // non-overlapping:  0-10, 10-20, etc.
    for (int i = 0; i < text.length() - 10; i += 5) {
      ++annotCount;
      ir.addFS(fs = this.cas.createAnnotation(this.sentenceType, i, i + 10));
      if (showFSs) {
        System.out.format("creating: %d begin: %d end: %d type: %s%n", annotCount, fs.getBegin(), fs.getEnd(), fs.getType().getName() );
      }
    }
    
    // create overlapping phrases
    // begin =  0,  6,  10, 14, ...
    // end   =  5,  9,  16, 19, ...
    
    int beginAlt = 0, endAlt = 0;
    for (int i = 0; i < text.length() - 10; i += 5) {
      ++annotCount;
      ir.addFS(fs = this.cas.createAnnotation(this.phraseType,  i + beginAlt, i + 5 + endAlt));
      beginAlt = (beginAlt == 1) ? -1 : beginAlt + 1;
      endAlt = (endAlt == -1) ? 1 : endAlt - 1;
      if (showFSs) {
        System.out.format("creating: %d begin: %d end: %d type: %s%n", annotCount, fs.getBegin(), fs.getEnd(), fs.getType().getName() );
      }
    }
    
    ++annotCount;
    ir.addFS(fs = this.cas.createAnnotation(this.sentenceType,  12, 31));
    if (showFSs) {
      System.out.format("creating: %d begin: %d end: %d type: %s%n", annotCount, fs.getBegin(), fs.getEnd(), fs.getType().getName() );
    }


    /***************************************************
     * iterate over them
     ***************************************************/
    List fss = new ArrayList();
    callCount = -1;
    iterateOverAnnotations(annotCount, fss); // annotCount is the total number of sentences and tokens
    
    callCount = -1;
    iterateOverAnnotations(annotCount, fss);  // should be using flattened version
    
    /***************************************************
     * test skipping over multiple equal items at front
     ***************************************************/
    callCount = -1;
    fss.clear();
    isSave = true;
    AnnotationFS a1, a2;
    ir.addFS(a1 = this.cas.createAnnotation(this.tokenType, 1, 6));
    a1.setStringValue(lemmaFeat, "lemma1");
    ir.addFS(a2 = this.cas.createAnnotation(this.tokenType, 1, 6));
    a2.setStringValue(lemmaFeat, "lemma2");
    
    FSIterator<AnnotationFS> it;
    AnnotationIndex<AnnotationFS> tokenIndex = cas.getAnnotationIndex(tokenType);
    it = tokenIndex.subiterator(a1);
    assertCount("multi equal", 0, it);
    it = tokenIndex.subiterator(a1);
    // make a new iterator that hasn't been converted to a list form internally
    it.moveTo(cas.getDocumentAnnotation());
    assertFalse(it.isValid()); 
        
  }
  
  /**
   * The tests include:
   *   a) running with / w/o "flattened" indexes
   *   b) running forwards and backwards (testing moveToLast, isValid)
   *   c) testing strict and unambiguous variants
   *   d) running over all annotations and restricting to just a particular subtype
   *   
   *   new tests:  
   *     verifying bounding FS < all returned, including multiples of it
   *     strict at 1st element, at last element
   *     (not done yet) ConcurrentModificationException testing
   *     
   *     (not done yet) Testing with different bound styles
   *     
   * @param annotCount
   * @param fss
   */
  // called twice, the 2nd time should be with flattened indexes (List fss non empty the 2nd time)
  private void iterateOverAnnotations(int annotCount, List<AnnotationFS> fss) {
    this.fss = fss;
    isSave = fss.size() == 0;
    int count;
    AnnotationIndex<AnnotationFS> annotIndex = this.cas.getAnnotationIndex();
    AnnotationIndex<AnnotationFS> sentIndex = this.cas.getAnnotationIndex(sentenceType);
    FSIterator<AnnotationFS> it = annotIndex.iterator(true);  // a normal "ambiguous" iterator
    FSIterator<AnnotationFS> select_it = cas.<AnnotationFS>select().fsIterator();
//    assertTrue((isSave) ? it instanceof FSIteratorWrapper : 
//      FSIndexFlat.enabled ? it instanceof FSIndexFlat.FSIteratorFlat : it instanceof FSIteratorWrapper);   
    assertCount("Normal ambiguous annot iterator", annotCount, it);
    assertCount("Normal ambiguous select annot iterator", annotCount, select_it);
    assertEquals(annotCount, select(annotIndex).toArray().length);  // stream op
    assertEquals(annotCount, select(annotIndex).asArray(Annotation.class).length);  // select op
    
    AnnotationFS[] tokensAndSentences = select(annotIndex).asArray(AnnotationFS.class);
    JCas jcas = null;
    try {
      jcas = cas.getJCas();
    } catch (CASException e) {
      assertTrue(false);
    }
    FSArray fsa = FSArray.create(jcas, tokensAndSentences);
    NonEmptyFSList fslhead = (NonEmptyFSList) FSList.createFromArray(jcas,  tokensAndSentences);
    
    select_it = fsa.<AnnotationFS>select().fsIterator();
    assertCount("fsa ambiguous select annot iterator", annotCount, select_it);

    select_it = fslhead.<AnnotationFS>select().fsIterator();
    assertCount("fslhead ambiguous select annot iterator", annotCount, select_it);
    
    // backwards
    select_it = select(annotIndex).backwards().fsIterator();
    assertCount("Normal select backwards ambiguous annot iterator", annotCount, select_it);
    
    it = annotIndex.iterator(false);  // false means create an unambiguous iterator
    assertCount("Unambiguous annot iterator", 1, it);  // because of document Annotation - spans the whole range
    select_it = cas.<AnnotationFS>select().nonOverlapping().fsIterator();
    assertCount("Unambiguous select annot iterator", 1, select_it);  // because of document Annotation - spans the whole range
    select_it = cas.<AnnotationFS>select().nonOverlapping().backwards(true).fsIterator();
    assertCount("Unambiguous select backwards annot iterator", 1, select_it);  // because of document Annotation - spans the whole range
    
    it = sentIndex.iterator(false);  //  false means create an unambiguous iterator
    assertCount("Unambigous sentence iterator", 5, it);
    select_it = cas.<AnnotationFS>select(sentenceType).nonOverlapping(true).fsIterator();
    assertCount("Unambigous select sentence iterator", 5, select_it);
    select_it = sentIndex.<AnnotationFS>select().nonOverlapping().fsIterator();
    assertCount("Unambigous select sentence iterator", 5, select_it);
    select_it = select(sentIndex).nonOverlapping().fsIterator();
    assertCount("Unambigous select sentence iterator", 5, select_it);
    
    
    AnnotationFS bigBound = this.cas.createAnnotation(this.sentenceType, 10, 41);
    it = annotIndex.subiterator(bigBound, true, true);  // ambiguous, and strict
    assertCount("Subiterator over annot with big bound, strict", 38, it);
    select_it = cas.<AnnotationFS>select().coveredBy((Annotation) bigBound).includeAnnotationsWithEndBeyondBounds(false).fsIterator();
    assertCount("Subiterator select over annot with big bound, strict", 38, select_it);

    select_it = cas.<AnnotationFS>select().coveredBy(bigBound).limit(7).includeAnnotationsWithEndBeyondBounds().fsIterator();
    assertCountLimit("Subiterator select limit 7 over annot with big bound, strict", 7, select_it);
    
    FSIndex<Token> token_index = null;
    // uncomment these to check compile-time generic arguments OK
    // comment these out for running, because Token not a type
//    FSIterator<Token> token_it = token_index.<Token>select().fsIterator();
//    token_it = select(token_index).fsIterator();
//    token_it = select(annotIndex, Token.class).fsIterator();
//    FSIterator<Token> token_it = cas.select(Token.class).fsIterator();
//    FSIterator<Token> token_it = token_index.select(Token.class).fsIterator();
    
    Object[] o = cas.<AnnotationFS>select().coveredBy(bigBound).skip(3).toArray();
    assertEquals(35, o.length);
    
    Object[] o1 = cas.<AnnotationFS>select().coveredBy(bigBound).toArray();
    List<AnnotationFS> l2 = cas.<AnnotationFS>select().coveredBy(bigBound).backwards().asList();
    Deque<AnnotationFS> l2r = new ArrayDeque<>();
    for (AnnotationFS fs : l2) {
      l2r.push(fs);
    }
    
    assertTrue(Arrays.equals(o1, l2r.toArray()));
    
    it = annotIndex.subiterator(bigBound, false, true);  // unambiguous, strict
    assertCount("Subiterator over annot unambiguous strict", 3, it);
    select_it = cas.<AnnotationFS>select().coveredBy((Annotation) bigBound).includeAnnotationsWithEndBeyondBounds(false).nonOverlapping().fsIterator();
    assertCount("Subiterator select over annot unambiguous strict", 3, select_it);
    select_it = cas.<AnnotationFS>select().backwards().coveredBy((Annotation) bigBound).includeAnnotationsWithEndBeyondBounds(false).nonOverlapping().fsIterator();
    assertCount("Subiterator select over annot unambiguous strict", 3, select_it);

//    it = annotIndex.subiterator(bigBound, true, false);
//    while (it.hasNext()) {
//      Annotation a = (Annotation) it.next();
//      System.out.format("debug %s:%d   b:%d e:%d%n", a.getType().getShortName(), a._id(), a.getBegin(), a.getEnd());
//    }

    it = annotIndex.subiterator(bigBound, true, false);
    assertCount("Subiterator over annot ambiguous not-strict", 46, it);
    
    // covered by implies endWithinBounds
    select_it = select(annotIndex).coveredBy(bigBound).fsIterator();
    assertCount("Subiterator select over annot ambiguous strict", 38, select_it);
    select_it = annotIndex.<AnnotationFS>select().coveredBy(bigBound).includeAnnotationsWithEndBeyondBounds(false).fsIterator();
    assertCount("Subiterator select over annot ambiguous strict", 38, select_it);
    select_it = select(annotIndex).coveredBy(bigBound).includeAnnotationsWithEndBeyondBounds(true).fsIterator();
    assertCount("Subiterator select over annot ambiguous not-strict", 46, select_it);
    
    it = annotIndex.subiterator(bigBound, false, false);  // unambiguous, not strict
    assertCount("Subiterator over annot, unambiguous, not-strict", 4, it);
    select_it = select(annotIndex).nonOverlapping().coveredBy(bigBound).includeAnnotationsWithEndBeyondBounds(true).fsIterator();
    assertCount("Subiterator select over annot unambiguous not-strict", 4, select_it);
    
    AnnotationFS sent = this.cas.getAnnotationIndex(this.sentenceType).iterator().get();
    it = annotIndex.subiterator(sent, false, true);
    assertCount("Subiterator over annot unambiguous strict", 2, it);
    select_it = annotIndex.<AnnotationFS>select().nonOverlapping().coveredBy(sent).fsIterator();
    assertCount("Subiterator select over annot unambiguous strict", 2, select_it);
    
    // strict skips first item
    bigBound = this.cas.createAnnotation(this.sentenceType,  11, 30);
    it = sentIndex.subiterator(bigBound, true, true);
    assertCount("Subiteratover over sent ambiguous strict", 4, it);
    it = sentIndex.subiterator(bigBound, true, false);
    assertCount("Subiteratover over sent ambiguous", 9, it);
    it = sentIndex.subiterator(bigBound, false, false);
    assertCount("Subiteratover over sent unambiguous", 1, it); 
    
    // single, get, nullOK
         
    assertTrue(cas.<AnnotationFS>select().nonOverlapping().get().getType().getShortName().equals("DocumentAnnotation"));
    boolean x = false;
    try {
      assertNull(cas.<AnnotationFS>select().nullOK(false).coveredBy(3, 3).get());
    } catch (CASRuntimeException e) {
      if (e.hasMessageKey(CASRuntimeException.SELECT_GET_NO_INSTANCES)) {
        x= true;
      }
    }
    assertTrue(x);
    assertNull(cas.<AnnotationFS>select().coveredBy(3, 3).get());
    assertNotNull(cas.<AnnotationFS>select().get(3));
    assertNull(cas.<AnnotationFS>select().nullOK().coveredBy(3, 5).get(3));
    x = false;
    try {
      cas.<AnnotationFS>select().coveredBy(3, 5). get(3);
    } catch (CASRuntimeException e) {
      if (e.hasMessageKey(CASRuntimeException.SELECT_GET_NO_INSTANCES)) {
        x= true;
      }
    }
    assertTrue(x);
    assertTrue(cas.<AnnotationFS>select().nonOverlapping().get().getType().getShortName().equals("DocumentAnnotation"));
    
    select_it = cas.<AnnotationFS>select().nonOverlapping().fsIterator(); 
    assertCount("Unambiguous select annot iterator", 1, select_it);  // because of document Annotation - spans the whole range
    select_it = cas.<AnnotationFS>select().nonOverlapping().backwards(true).fsIterator();
    assertCount("Unambiguous select backwards annot iterator", 1, select_it);  // because of document Annotation - spans the whole range
    assertNotNull(cas.<AnnotationFS>select().nonOverlapping().single());

    x = false;
    try {
      cas.<AnnotationFS>select().coveredBy(3, 10).single();
    } catch (CASRuntimeException e) {
      if (e.hasMessageKey(CASRuntimeException.SELECT_GET_TOO_MANY_INSTANCES)) {
        x= true;
      }
    }
    assertTrue(x);
    x = false;
    try {
      cas.<AnnotationFS>select().coveredBy(3, 10).singleOrNull();
    } catch (CASRuntimeException e) {
      if (e.hasMessageKey(CASRuntimeException.SELECT_GET_TOO_MANY_INSTANCES)) {
        x= true;
      }
    }
    assertTrue(x);
    x = false;
    try {
      cas.<AnnotationFS>select().coveredBy(3, 5).single();
    } catch (CASRuntimeException e) {
      if (e.hasMessageKey(CASRuntimeException.SELECT_GET_NO_INSTANCES)) {
        x= true;
      }
    }
    assertTrue(x);
    cas.<AnnotationFS>select().coveredBy(3, 5).singleOrNull();
    
    select_it = select(annotIndex).following(2, 39).limit(2).fsIterator();
    assertCountLimit("Following", 2, select_it);
    select_it = select(annotIndex).following(2, 39).backwards().limit(2).fsIterator();
    assertCountLimit("Following", 2, select_it);
    
    select_it = fsa.<AnnotationFS>select(sentenceType).fsIterator();
    assertCount("select source array", 21, select_it);
    select_it = fslhead.<AnnotationFS>select(sentenceType).fsIterator();
    assertCount("select source array", 21, select_it);
    
    /** covering **/
    cas.<AnnotationFS>select(sentenceType).covering(20, 30).forEachOrdered(f ->  
        System.out.format("found fs start at %d end %d%n", Integer.valueOf(f.getBegin()), Integer.valueOf(f.getEnd())));
    
    cas.<AnnotationFS>select(sentenceType).covering(15, 19).forEachOrdered(f ->  
        System.out.format("covering 15, 19:   %s:%d   %d -  %d%n", f.getType().getShortName(), Integer.valueOf(f._id()), Integer.valueOf(f.getBegin()), Integer.valueOf(f.getEnd())));

    cas.<AnnotationFS>select(sentenceType).covering(37, 39).forEachOrdered(f ->  
        System.out.format("covering sentences 37, 39:   %s:%d   %d -  %d%n", f.getType().getShortName(), Integer.valueOf(f._id()), Integer.valueOf(f.getBegin()), Integer.valueOf(f.getEnd())));

    cas.<AnnotationFS>select(phraseType).covering(15, 19).forEachOrdered(f ->  
       System.out.format("covering phrases 15, 19:   %s:%d   %d -  %d%n", f.getType().getShortName(), Integer.valueOf(f._id()), Integer.valueOf(f.getBegin()), Integer.valueOf(f.getEnd())));

    cas.<AnnotationFS>select(phraseType).covering(37, 39).forEachOrdered(f ->  
       System.out.format("covering phrases 37, 39:   %s:%d   %d -  %d%n", f.getType().getShortName(), Integer.valueOf(f._id()), Integer.valueOf(f.getBegin()), Integer.valueOf(f.getEnd())));

  }
  
  private String flatStateMsg(String s) {
    return s + (isSave ? "" : " with flattened index");
  }
  private void assertCount(String msg, int expected,  FSIterator<? extends AnnotationFS> it) {
    int fssStart = assertCountCmn(msg, expected, it);
    msg = flatStateMsg(msg);
    int count = expected;
    if (count > 0) {
      // test moveTo(fs) in middle, first, and last
      AnnotationFS posFs = fss.get(fssStart + (count >> 1));
//      //debug
//      System.out.println(posFs.toString());
       it.moveTo(posFs);
       assertEquals(msg, it.get().hashCode(), posFs.hashCode());
      
      posFs = fss.get(fssStart);
      it.moveTo(posFs);
      assertEquals(msg, it.get().hashCode(), posFs.hashCode());
      it.moveToFirst();
      assertEquals(msg, it.get().hashCode(), posFs.hashCode());
      
      posFs = fss.get(fssStart + count - 1);
      it.moveTo(posFs);
      assertEquals(msg, it.get().hashCode(), posFs.hashCode());
      it.moveToLast();
      assertEquals(msg, it.get().hashCode(), posFs.hashCode());
    } else {
      // count is 0
      it.moveToFirst();
      assertFalse(it.isValid());
      it.moveToLast();
      assertFalse(it.isValid());
      it.moveTo(cas.getDocumentAnnotation());
      assertFalse(it.isValid());    
    }
    
    // test movetoLast, moving backwards
    count = 0;
    for (it.moveToLast(); it.isValid(); it.moveToPrevious()) {
      ++count;
    }
    assertEquals(msg, expected, count);
  }
  
  private int assertCountCmn(String msg, int expected, FSIterator<? extends AnnotationFS> it) {
    msg = flatStateMsg(msg);
    int count = 0;
    callCount  ++;
    int fssStart;
    if (isSave) {
      fssStarts.add(fssStart = fss.size());
    } else {
      fssStart = fssStarts.get(callCount);
    }
    while (it.isValid()) {
      ++count;
      AnnotationFS fs = it.next();
      if (showFSs) {
        System.out.format("%d " + msg + " fs begin: %d end: %d type: %s%n", count, fs.getBegin(), fs.getEnd(), fs.getType().getName() );
      }
      if (isSave) {
        fss.add(fs);
      } else {
        assertEquals(msg, fss.get(fssStart + count -1).hashCode(), fs.hashCode());
      }
    }
    assertEquals(msg, expected, count);
    return fssStart;
  }
  
    
  private void assertCountLimit(String msg, int expected,  FSIterator<? extends AnnotationFS> it) {
    assertCountCmn(msg, expected, it);
    it.moveToFirst();
    assertFalse(it.isValid());
  }
  
  
  
  public void testIncorrectIndexTypeException() {
    boolean caughtException = false;
    try {
      this.cas.getAnnotationIndex(this.stringType);
    } catch (CASRuntimeException e) {
//      e.printStackTrace();
      caughtException = true;
    }
    assertTrue(caughtException);
    
    caughtException = false;
    try {
    	AnnotationIndex<AnnotationFS> ai = this.cas.getAnnotationIndex(ts.getType(CASTestSetup.TOKEN_TYPE_TYPE));
    } catch (CASRuntimeException e) {
    	caughtException = true;
    }
    assertTrue(caughtException);
    try {
      this.cas.getAnnotationIndex(this.tokenType);
    } catch (CASRuntimeException e) {
      assertTrue(false);
    }
  }
  
  /**
   * UIMA-2808 - There was a bug in Subiterator causing the first annotation of the type of the
   * index the subiterator was applied to always to be returned, even if outside the boundary
   * annotation.
   */
  public void testUnambiguousSubiteratorOnIndex() {
    try {
      //                        0    0    1    1    2    2    3    3    4    4    5
      //                        0    5    0    5    0    5    0    5    0    5    0
      this.cas.setDocumentText("Sentence A with no value. Sentence B with value 377.");
    } catch (CASRuntimeException e) {
      assertTrue(false);
    }      
          
    cas.addFsToIndexes(cas.createAnnotation(this.sentenceType, 0, 25));
    cas.addFsToIndexes(cas.createAnnotation(this.sentenceType, 26, 52));
    cas.addFsToIndexes(cas.createAnnotation(this.tokenType, 48, 51));
    AnnotationIndex<AnnotationFS> tokenIdx = cas.getAnnotationIndex(this.tokenType);
    
    AnnotationIndex<AnnotationFS> si = cas.getAnnotationIndex(this.sentenceType);
    for (AnnotationFS sa : si) {
      FSIterator<AnnotationFS> ti2 = tokenIdx.subiterator(sa, false, false);
      
      while (ti2.hasNext()) {
        AnnotationFS t = ti2.next();
        assertTrue("Subiterator returned annotation outside boundaries", t.getBegin() < sa.getEnd());
      }
    }

    SelectFSs<AnnotationFS> ssi = cas.getAnnotationIndex(this.sentenceType).select();
    
    for (AnnotationFS sa : ssi) {
    
      FSIterator<AnnotationFS> ti2 = tokenIdx.<AnnotationFS>select()
          .coveredBy(sa).includeAnnotationsWithEndBeyondBounds(false).nonOverlapping().fsIterator();
      
      while (ti2.hasNext()) {
        AnnotationFS t = ti2.next();
        assertTrue("Subiterator returned annotation outside boundaries", t.getBegin() < sa.getEnd());
      }
    }

  }

  public static void main(String[] args) {
    AnnotationIteratorTest test = new AnnotationIteratorTest(null);
    test.run();
  }

}