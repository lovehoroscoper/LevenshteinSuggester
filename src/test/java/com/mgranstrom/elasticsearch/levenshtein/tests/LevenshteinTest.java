package com.mgranstrom.elasticsearch.levenshtein.tests;

import com.mgranstrom.elasticsearch.levenshtein.tests.base.AbstractTestBase;
import com.mgranstrom.elasticsearch.levenshtein.tests.base.HttpClient;
import com.mgranstrom.elasticsearch.levenshtein.tests.base.HttpClientResponse;
import org.junit.*;

import java.io.IOException;

public class LevenshteinTest extends AbstractTestBase {
    @Override
    public long waitingTime() throws Exception {
        return 2;
    }

    @Test
    public void SimpleIntegrationTest() throws IOException {
        HttpClient client = getHttpClient();
        HttpClientResponse response = client.request("GET", indexName()+"/_lucenesuggest?q=Whi");
        Assert.assertTrue(response.response().contains("\"Whiplash\",\"Whirlwind\",\"Whistler\""));
    }

    @Test
    public void NotProvideQParam() throws IOException {
        HttpClient client = getHttpClient();
        HttpClientResponse response = client.request("GET", indexName()+"/_lucenesuggest");
        Assert.assertTrue(response.response().contains("Must provide q param to levenshtein suggester"));
    }
}
