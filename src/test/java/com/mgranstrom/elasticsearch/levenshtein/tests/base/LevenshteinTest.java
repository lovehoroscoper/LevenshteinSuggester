package com.mgranstrom.elasticsearch.levenshtein.tests.base;

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
}
