package nu.marginalia.encyclopedia.store;

import nu.marginalia.encyclopedia.model.Article;
import nu.marginalia.encyclopedia.cleaner.model.ArticleParts;
import nu.marginalia.encyclopedia.model.LinkList;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

class ArticleStoreTest {
    Path dbName;

    @BeforeEach
    public void setup() throws Exception {
        dbName = Files.createTempFile("articles", ".db");
    }

    @AfterEach
    public void teardown() throws Exception {
        Files.deleteIfExists(dbName);
    }

    @Test
    public void codec() throws SQLException {
        try (var asw = new ArticleStoreWriter(new ArticleDbProvider(dbName))) {
            asw.add(new Article("_title", "title", "sum", new ArticleParts("foo", "bar", "baz"),
                    new LinkList(),
                    new LinkList())
                    .asData());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        var asr = new ArticleStoreReader(new ArticleDbProvider(dbName));
        var article = asr.get("title");
        System.out.println(article);
        asr.close();
    }
}