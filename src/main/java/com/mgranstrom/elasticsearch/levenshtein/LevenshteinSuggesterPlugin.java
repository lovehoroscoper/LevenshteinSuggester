package com.mgranstrom.elasticsearch.levenshtein;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;

/**
 * Created with IntelliJ IDEA.
 * User: magr
 * Date: 8/10/13
 * Time: 17:18
 * To change this template use File | Settings | File Templates.
 */
public class LevenshteinSuggesterPlugin extends AbstractPlugin {
    public String name() {
        return "Levenshtein-automata-suggester-plugin";
    }

    public String description() {
        return "Rest-plugin for lucene fuzzy suggester";
    }

    @Override public void processModule(Module module) {
        if (module instanceof RestModule) {
            ((RestModule) module).addRestAction(LevenshteinSuggesterRestAction.class);
        }
    }


}
