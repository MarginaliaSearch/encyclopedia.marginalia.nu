package nu.marginalia.encyclopedia.svc;

import com.github.jknack.handlebars.Template;
import nu.marginalia.encyclopedia.model.Article;
import nu.marginalia.encyclopedia.model.LinkList;
import nu.marginalia.encyclopedia.model.ReferencedArticle;
import nu.marginalia.encyclopedia.store.ArticleIndex;
import nu.marginalia.encyclopedia.store.ArticleStoreReader;
import nu.marginalia.encyclopedia.RendererFactory;
import org.jetbrains.annotations.Nullable;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.*;

import static spark.Spark.halt;

public class ViewArticleService {
    private final Template template;
    private final ArticleStoreReader articleStore;
    private final ArticleIndex articleIndex;

    public ViewArticleService(RendererFactory rendererFactory, ArticleStoreReader articleStore, ArticleIndex articleIndex) throws IOException {
        template = rendererFactory.getTemplate("view");
        this.articleStore = articleStore;
        this.articleIndex = articleIndex;
    }

    public String handle(Request request, Response response) throws IOException {
        var url = request.splat()[0];

        var article = articleStore.get(url);
        var body = article
                .map(this::getArticleContext)
                .map(this::render);

        if (body.isEmpty()) {
            response.redirect("/find/" + url);
            return ""; // does nothing
        }

        response.header("Content-Encoding", "gzip");
        return body.get();
    }

    private String render(ArticleWithContext articleWithContext) {
        try {
            return template.apply(articleWithContext);
        } catch (IOException e) {
            halt(500, "Failed to render article");
            return null; // unreachable
        }
    }

    private ArticleWithContext getArticleContext(Article article) {
        return new ArticleWithContext(article,
                relatedArticles(article.urls()),
                relatedArticles(article.disambigs()),
                relatedArticles(articleIndex.previousArticles(article.url(), 3), Map.of()),
                relatedArticles(articleIndex.nextArticles(article.url(), 3), Map.of())
                );
    }

    private List<ReferencedArticle> relatedArticles(List<String> urls,
                                                    @Nullable
                                                    Map<String, List<String>> urlToTitles
                                                    ) {

        var refArticles = articleStore.getReferencedArticles(new HashSet<>(urls));

        List<ReferencedArticle> ret = new ArrayList<>();

        if (urlToTitles != null) {
            for (var ref : refArticles) {
                ret.add(ref.withAliases(urlToTitles.get(ref.url())));
            }
        }

        ret.sort(Comparator.naturalOrder());

        return ret;
    }
    private List<ReferencedArticle> relatedArticles(LinkList urls) {
        Map<String, List<String>> urlToTitles = new HashMap<>(urls.size());
        List<String> urlsList = new ArrayList<>(urls.size());

        for (var link : urls.links()) {
            final String url = link.url();
            final String title = link.text();

            urlsList.add(url);
            urlToTitles
                    .computeIfAbsent(url, k -> new ArrayList<>())
                    .add(title);
        }

        return relatedArticles(urlsList, urlToTitles);
    }

    public record ArticleWithContext(Article article,
                                            List<ReferencedArticle> linked,
                                            List<ReferencedArticle> disambiguated,
                                            List<ReferencedArticle> prevN,
                                            List<ReferencedArticle> nextN
                                     )
    {
    }
}
