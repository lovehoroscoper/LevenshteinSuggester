package com.mgranstrom.elasticsearch.levenshtein;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.util.Version;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class LevenshteinSuggesterRestAction extends BaseRestHandler {

    private final FuzzySuggester suggester;

    @Inject
    public LevenshteinSuggesterRestAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(GET, "/{index}/_lucenesuggest", this);
        controller.registerHandler(POST, "/{index}/_lucenesuggest", this);
        suggester = new FuzzySuggester(new StandardAnalyzer(Version.LUCENE_43));
        List<TermAndFrequency> list = new ArrayList<TermAndFrequency>();

        try {
            //read file from disk.
            BufferedReader br = new BufferedReader(new FileReader(new File("names.txt")));
            String line;
            while((line = br.readLine()) != null) {
                list.add(TermAndFrequency.GetTermAndFreq(line,1));
            }
            br.close();
            suggester.build(new TermAndFrequencyIterator(list));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleRequest(RestRequest request, RestChannel channel) {
        long start = System.currentTimeMillis();
        String lookup = "";
        if(request.hasParam("q")){
            lookup = request.param("q");
        }
        else{
            XContentBuilder errorBuilder;
            try{
                errorBuilder = jsonBuilder()
                        .startObject()
                            .field("Error", "Must provide q param to levenshtein suggester")
                        .endObject();
                RestResponse errorResponse = new XContentRestResponse(request,RestStatus.BAD_REQUEST, errorBuilder);
                channel.sendResponse(errorResponse);
                return;
            }
            catch(IOException e){

            }
        }
        List<Lookup.LookupResult> result = suggester.lookup(lookup,false,3);
        long elapsedTime = System.currentTimeMillis() - start;
        XContentBuilder builder;
        try {
            builder = jsonBuilder();
            builder.startObject();
            builder.field("took", elapsedTime);
            builder.startArray("suggestions");
            for(Lookup.LookupResult res : result){
                builder.value(res.key);
            }
            builder.endArray();
            builder.endObject();
            RestResponse response = new XContentRestResponse(request,RestStatus.OK , builder);
            channel.sendResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
