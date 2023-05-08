package nu.marginalia.encyclopedia.svc;

import com.github.jknack.handlebars.Template;
import nu.marginalia.encyclopedia.store.ArticleStoreReader;
import nu.marginalia.encyclopedia.RendererFactory;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ListArticleService {
    private final Template template;
    private final ArticleStoreReader articleStore;

    public ListArticleService(RendererFactory rendererFactory, ArticleStoreReader articleStore) throws IOException {
        template = rendererFactory.getTemplate("list");
        this.articleStore = articleStore;
    }

    public String handle(Request request, Response response) throws IOException {
        String startParam = request.queryParamOrDefault("start", "0");
        String countParam = request.queryParamOrDefault("count", "20");

        int start = Integer.parseInt(startParam);
        int count = Integer.parseInt(countParam);

        return render(start, count);
    }

    private String render(int start, int count) throws IOException {
        var links = articleStore.list(start, count);
        var data = new HashMap<String, Object>();

        data.put("start", start);
        data.put("count", count);
        data.put("links", links);

        data.put("next", start + count);
        if (count > start) {
            data.put("prev", start - count);
        }

        return template.apply(data);
    }

    record ListData(int start, int count, List<String> links) {

    }
}
