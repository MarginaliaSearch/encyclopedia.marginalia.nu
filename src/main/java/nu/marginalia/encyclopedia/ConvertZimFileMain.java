package nu.marginalia.encyclopedia;

import nu.marginalia.encyclopedia.cleaner.WikiCleaner;
import nu.marginalia.encyclopedia.store.ArticleDbProvider;
import nu.marginalia.encyclopedia.store.ArticleStoreWriter;
import nu.marginalia.encyclopedia.util.BlockingFixedThreadPool;
import org.openzim.ZIMTypes.ZIMFile;
import org.openzim.ZIMTypes.ZIMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ConvertZimFileMain {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String... args) throws IOException, InterruptedException {
        var main = new ConvertZimFileMain();
        main.load(Path.of(args[0]), Path.of(args[1]));
    }

    public ConvertZimFileMain() {

    }

    public void load(Path zimFile, Path dbFilePath) throws IOException, InterruptedException {
        var wc = new WikiCleaner();
        var pool = new BlockingFixedThreadPool(16);
        var size = new AtomicInteger();

        if (!Files.exists(zimFile)) {
            logger.error("ZIM file not found: {}", zimFile);
            System.exit(255);
        }
        if (Files.exists(dbFilePath)) {
            logger.error("Database file already exists: {}", dbFilePath);
            System.exit(255);
        }

        try (var asw = new ArticleStoreWriter(new ArticleDbProvider(dbFilePath))) {

            Predicate<Integer> keepGoing = (s) -> true;

            BiConsumer<String, String> handleArticle = (url, html) -> {
                if (pool.isShutdown())
                    return;

                pool.execute(() -> {
                    int sz = size.incrementAndGet();
                    if (sz % 1000 == 0) {
                        System.out.printf("\u001b[2K\r%d", sz);
                    }
                    asw.add(wc.cleanWikiJunk(url, html));
                });

                size.incrementAndGet();
            };

            new ZIMReader(new ZIMFile(zimFile.toString())).forEachArticles(handleArticle, keepGoing);

            pool.shutdown();
            logger.info("Waiting for pool to finish");

            while (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
                // ...
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

