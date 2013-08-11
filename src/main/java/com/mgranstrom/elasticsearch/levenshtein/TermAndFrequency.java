package com.mgranstrom.elasticsearch.levenshtein;

import org.apache.lucene.util.BytesRef;

public final class TermAndFrequency {
	  public final BytesRef term;
	  public final long freq;
	  
	  private TermAndFrequency(BytesRef term, long v) {
	    this.term = term;
	    this.freq = v;
	  }
	  
	  public static TermAndFrequency GetTermAndFreq(String term, long freq){
		  return new TermAndFrequency(new BytesRef(term), freq);
	  }
	}
