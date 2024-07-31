package nu.marginalia.encyclopedia.svc;

import com.github.jknack.handlebars.Template;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nu.marginalia.encyclopedia.RendererFactory;
import nu.marginalia.encyclopedia.model.ReferencedArticle;
import nu.marginalia.encyclopedia.model.SearchResult;
import nu.marginalia.encyclopedia.store.ArticleIndex;
import nu.marginalia.encyclopedia.store.ArticleStoreReader;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class FindArticleService {
    private final Template template;
    private final ArticleStoreReader articleStore;
    private final ArticleIndex articleIndex;

    private final String MARGINALIA_API_KEY = System.getProperty("MARGINALIA_API_KEY");
    private final HttpClient client;

    private final Gson gson = new GsonBuilder().create();

    public FindArticleService(RendererFactory rendererFactory,
                              ArticleStoreReader articleStore,
                              ArticleIndex articleIndex) throws IOException {
        template = rendererFactory.getTemplate("find");
        this.articleStore = articleStore;
        this.articleIndex = articleIndex;

        this.client = HttpClient.newHttpClient();
    }

    public String handle(Request request, Response response) throws IOException {
        var url = request.splat()[0].replace('+', ' ');
        var searchKey = request.queryParamOrDefault("start", url);

        response.header("Content-Encoding", "gzip");

        SearchResult searchResults;
        SearchResult indexResults;
        if (null != MARGINALIA_API_KEY && !Objects.equals(url, searchKey)) {
            searchResults = searchMarginalia(url);
        }
        else {
            searchResults = new SearchResult(List.of(), url);
        }

        indexResults = articleIndex.search(url, searchKey);

        if (searchResults.isEmpty() && indexResults.isEmpty()) {
            Spark.halt(404);
        }

        if (!searchResults.isEmpty()) {
            // Remove search results from index results to prevent duplicates,
            // a bit of a cursed pattern but
            List<String> cleanedResults = new ArrayList<>(indexResults.results());
            cleanedResults.removeAll(searchResults.results());
            indexResults = new SearchResult(cleanedResults, indexResults.searchTerm(), indexResults.nextPrefix());
        }

        return template.apply(new SearchResultWithContext(searchResults, indexResults));
    }

    public static class MarginaliaSearchResult {
        public String url;
        public String title;
    }

    public static class MarginaliaSearchResponse {
        public final List<MarginaliaSearchResult> results = new ArrayList<>();
    }

    private SearchResult searchMarginalia(String query) {
        try {
            var req = HttpRequest.newBuilder(new URI("https://api.marginalia.nu/%s/search/site:en.wikipedia.org%%20%s".formatted(MARGINALIA_API_KEY, query))).method("GET", HttpRequest.BodyPublishers.noBody()).build();
            var rsp = client.send(req, HttpResponse.BodyHandlers.ofString());

            var response = gson.fromJson(rsp.body(), MarginaliaSearchResponse.class);
            var urls = new ArrayList<String>();

            for (var result : response.results) {
                var url = result.url;
                if (url.startsWith("https://en.wikipedia.org/wiki/")) {
                    urls.add(url.substring(30));
                }
            }

            return new SearchResult(urls, query);
        }
        catch (Exception e) {
            return new SearchResult(query);
        }
    }

    public class SearchResultWithContext
    {
        public final List<ReferencedArticle> searchArticles;
        public final List<ReferencedArticle> indexArticles;
        public final String searchTerm;
        public final String nextPrefix;

        public SearchResultWithContext(List<ReferencedArticle> searchArticles, List<ReferencedArticle> indexArticles, String searchTerm, String nextPrefix) {
            this.searchArticles = searchArticles;
            this.indexArticles = indexArticles;
            this.searchTerm = searchTerm;
            this.nextPrefix = nextPrefix;
        }

        public SearchResultWithContext(SearchResult searchResult, SearchResult indexResults) {
            this(articleStore.getReferencedArticles(searchResult.results()),
                    articleStore.getReferencedArticles(indexResults.results()),
                    indexResults.searchTerm(),
                    indexResults.nextPrefix());
        }


        public List<ReferencedArticle> getSearchArticles() {
            return searchArticles;
        }
        public List<ReferencedArticle> getIndexArticles() { return indexArticles; }
        public String getNextPrefix() {
            return nextPrefix;
        }
        public String getSearchTerm() { return searchTerm; }
    }


}
