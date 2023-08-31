package nu.marginalia.encyclopedia;

import nu.marginalia.encyclopedia.store.ArticleDbProvider;
import nu.marginalia.encyclopedia.store.ArticleIndex;
import nu.marginalia.encyclopedia.store.ArticleStoreReader;
import nu.marginalia.encyclopedia.svc.FindArticleService;
import nu.marginalia.encyclopedia.svc.ListArticleService;
import nu.marginalia.encyclopedia.svc.ViewArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

public class EncyclopediaServiceMain {
    ViewArticleService viewArticleService;
    FindArticleService findArticleService;
    ListArticleService listArticleService;

    public static void main(String... args) throws IOException, SQLException {
        if (args.length != 2) throw new IllegalArgumentException("Usage: EncyclopediaServiceMain /path/to/file.db");

        var main = new EncyclopediaServiceMain(Path.of(args[1]));

        int port = Integer.parseInt(args[0]);
        main.serve(port);
    }

    private final static Logger logger = LoggerFactory.getLogger(EncyclopediaServiceMain.class);

    public EncyclopediaServiceMain(Path dbPath) throws SQLException, IOException {

        if (!Files.exists(dbPath)) {
            logger.error("Database file does not exist exists: {}", dbPath);
            System.exit(255);
        }

        var dbProvider = new ArticleDbProvider(dbPath);

        var articleStore = new ArticleStoreReader(dbProvider);
        var articleIndex = new ArticleIndex(dbProvider);
        var rendererFactory = new RendererFactory();

        viewArticleService = new ViewArticleService(rendererFactory, articleStore, articleIndex);
        listArticleService = new ListArticleService(rendererFactory, articleStore);
        findArticleService = new FindArticleService(rendererFactory, articleStore, articleIndex);
    }

    private void serve(int port) {
        Spark.port(port);
        Spark.staticFileLocation("/static");
        Spark.init();

        Spark.get("/list/", listArticleService::handle);
        Spark.get("/article/:url", viewArticleService::handle);
        Spark.get("/find/:url", findArticleService::handle);

        Spark.get("/search", (rq, rsp) -> {
            rsp.redirect("/find/" + URLEncoder.encode(rq.queryParams("q"), StandardCharsets.UTF_8));
            return "";
        });
    }

}

