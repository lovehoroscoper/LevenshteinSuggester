package com.mgranstrom.elasticsearch.levenshtein;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.lucene.search.spell.TermFreqIterator;
import org.apache.lucene.util.BytesRef;

public final class TermAndFrequencyIterator implements TermFreqIterator {
	  private final Iterator<TermAndFrequency> i;
	  private TermAndFrequency current;
	  private final BytesRef spare = new BytesRef();

	  public TermAndFrequencyIterator(Iterator<TermAndFrequency> i) {
	    this.i = i;
	  }

	  public TermAndFrequencyIterator(TermAndFrequency[] i) {
	    this(Arrays.asList(i));
	  }

	  public TermAndFrequencyIterator(Iterable<TermAndFrequency> i) {
	    this(i.iterator());
	  }
	  
	  @Override
	  public long weight() {
	    return current.freq;
	  }

	  @Override
	  public BytesRef next() {
	    if (i.hasNext()) {
	      current = i.next();
	      spare.copyBytes(current.term);
	      return spare;
	    }
	    return null;
	  }

	  @Override
	  public Comparator<BytesRef> getComparator() {
	    return null;
	  }
	}
