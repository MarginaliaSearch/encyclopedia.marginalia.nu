package nu.marginalia.encyclopedia.svc;

import com.github.jknack.handlebars.Template;
import nu.marginalia.encyclopedia.RendererFactory;
import nu.marginalia.encyclopedia.model.ReferencedArticle;
import nu.marginalia.encyclopedia.model.SearchResult;
import nu.marginalia.encyclopedia.store.ArticleIndex;
import nu.marginalia.encyclopedia.store.ArticleStoreReader;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;
import java.util.*;

public class FindArticleService {
    private final Template template;
    private final ArticleStoreReader articleStore;
    private final ArticleIndex articleIndex;

    public FindArticleService(RendererFactory rendererFactory,
                              ArticleStoreReader articleStore,
                              ArticleIndex articleIndex) throws IOException {
        template = rendererFactory.getTemplate("find");
        this.articleStore = articleStore;
        this.articleIndex = articleIndex;
    }

    public String handle(Request request, Response response) throws IOException {
        var url = request.params(":url");
        var searchKey = request.queryParamOrDefault("start", url);

        response.header("Content-Encoding", "gzip");

        SearchResult results = articleIndex.search(url, searchKey);

        if (results.isEmpty()) {
            Spark.halt(404);
        }

        return template.apply(new SearchResultWithContext(results));
    }

    public class SearchResultWithContext
    {
        public final List<ReferencedArticle> articles;
        public final String searchTerm;
        public final String nextPrefix;

        public SearchResultWithContext(List<ReferencedArticle> articles, String searchTerm, String nextPrefix) {
            this.articles = articles;
            this.searchTerm = searchTerm;
            this.nextPrefix = nextPrefix;
        }

        public SearchResultWithContext(SearchResult result) {
            this(articleStore.getReferencedArticles(result.results()), result.searchTerm(), result.nextPrefix());
        }


        public List<ReferencedArticle> getArticles() {
            return articles;
        }
        public String getNextPrefix() {
            return nextPrefix;
        }
        public String getSearchTerm() { return searchTerm; }
    }
}
