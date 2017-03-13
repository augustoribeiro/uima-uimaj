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

package org.apache.uima.jcas.cas;

import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.SelectFSs;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.SelectFSs_impl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;

public abstract class FSList extends TOP implements CommonList, Iterable<TOP> {
 
	// Never called.
	protected FSList() {// Disable default constructor
	}

	public FSList(JCas jcas) {
		super(jcas);
	}

  /**
  * used by generator
  * Make a new AnnotationBase
  * @param c -
  * @param t -
  */

   public FSList(TypeImpl t, CASImpl c) {
     super(t, c);
   }
	
   public TOP getNthElement(int i) {
     CommonList node = getNthNode(i);
     if (node instanceof EmptyFSList) {
       throw new CASRuntimeException(CASRuntimeException.JCAS_GET_NTH_PAST_END, Integer.toString(i));
     }
     return ((NonEmptyFSList)node).getHead();
   } 
   
  public NonEmptyFSList createNonEmptyNode() {
   return new NonEmptyFSList(this._casView.getJCasImpl());
  }
  
  public NonEmptyFSList pushNode() {
    NonEmptyFSList n = createNonEmptyNode();
    n.setTail(this);
    return n;
  }
    
  /**
   * Treat an FSArray as a source for SelectFSs. 
   * @param <T> generic type being selected
   * @return a new instance of SelectFSs
   */
  public <T extends FeatureStructure> SelectFSs<T> select() {
    return new SelectFSs_impl<>(this);
  }

  /**
   * Treat an FSArray as a source for SelectFSs. 
   * @param filterByType only includes elements of this type
   * @param <T> generic type being selected
   * @return a new instance of SelectFSs
   */
  public <T extends FeatureStructure> SelectFSs<T> select(Type filterByType) {
    return new SelectFSs_impl<>(this).type(filterByType);
  }

  /**
   * Treat an FSArray as a source for SelectFSs.  
   * @param filterByType only includes elements of this JCas class
   * @param <T> generic type being selected
   * @return a new instance of SelectFSs
   */
  public <T extends FeatureStructure> SelectFSs<T> select(Class<T> filterByType) {
    return new SelectFSs_impl<>(this).type(filterByType);
  }
  
  /**
   * Treat an FSArray as a source for SelectFSs. 
   * @param filterByType only includes elements of this JCas class's type
   * @param <T> generic type being selected
   * @return a new instance of SelectFSs
   */
  public <T extends FeatureStructure> SelectFSs<T> select(int filterByType) {
    return new SelectFSs_impl<>(this).type(filterByType);
  }
  
  /**
   * Treat an FSArray as a source for SelectFSs. 
   * @param filterByType only includes elements of this type (fully qualifined type name)
   * @param <T> generic type being selected
   * @return a new instance of SelectFSs
   */
  public <T extends FeatureStructure> SelectFSs<T> select(String filterByType) {
    return new SelectFSs_impl<>(this).type(filterByType);
  }
  
  /**
   * Create an FSList from an existing array of Feature Structures
   * @param jcas the JCas to use
   * @param a the array of Feature Structures to populate the list with
   * @return an FSList, with the elements from the array
   */
  public static FSList createFromArray(JCas jcas, FeatureStructure[] a) {
    FSList fsl = jcas.getCasImpl().getEmptyFSList();   
    for (int i = a.length - 1; i >= 0; i--) {
      fsl = fsl.push((TOP) a[i]);
    }   
    return fsl;
  }

  /* (non-Javadoc)
   * @see java.lang.Iterable#iterator()
   */
  @Override
  public Iterator<TOP> iterator() {
    return Collections.emptyIterator();  // overridden by NonEmptyFSList
  }
    
  /**
   * pushes item onto front of this list
   * @param item the item to push onto the list
   * @return the new list, with this item as the head value of the first element
   */
  public NonEmptyFSList push(TOP item) {
    return new NonEmptyFSList(_casView.getJCasImpl(), item, this);
  }

  /**
   * @param <T> generic type being returned
   * @return a stream over this FSList
   */
  public <T extends TOP> Stream<T> stream() {
    return (Stream<T>) StreamSupport.stream(spliterator(), false);
  }
  
  @Override
  public EmptyFSList getEmptyList() {
    return this._casView.getEmptyFSList();
  }
}