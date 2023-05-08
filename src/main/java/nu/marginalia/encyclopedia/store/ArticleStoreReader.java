package nu.marginalia.encyclopedia.store;

import nu.marginalia.encyclopedia.cleaner.model.ArticleParts;
import nu.marginalia.encyclopedia.model.Article;
import nu.marginalia.encyclopedia.model.LinkList;
import nu.marginalia.encyclopedia.model.ReferencedArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ArticleStoreReader implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(ArticleStoreReader.class);
    private final Connection connection;

    public ArticleStoreReader(ArticleDbProvider dbProvider) {
        connection = dbProvider.getConnection();
    }

    public void close() throws SQLException {
        connection.close();
    }

    public Optional<Article> get(String title) {
        try (var stmt = connection.prepareStatement("""
                SELECT url, title, summary, html, urls, disambigs FROM articles WHERE url = ?
                """)) {

            stmt.setString(1, title);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Article(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        ArticleCodec.fromCompressedJson(rs.getBytes(4), ArticleParts.class),
                        ArticleCodec.fromCompressedJson(rs.getBytes(5), LinkList.class),
                        ArticleCodec.fromCompressedJson(rs.getBytes(6), LinkList.class)
                ));
            }
        } catch (IOException|SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /** Get the first section from each of the given articles.
     */
    public List<ReferencedArticle> getReferencedArticles(Collection<String> urls) {
        List<ReferencedArticle> ret = new ArrayList<>(urls.size());

        try (var stmt = connection.prepareStatement("""
                SELECT title, summary FROM articles WHERE url = ?
                """))
        {
            for (String url : urls) {
                stmt.setString(1, url);
                var rs = stmt.executeQuery();
                if (rs.next()) {

                    var ra = new ReferencedArticle(rs.getString(1), List.of(), url, rs.getString(2));

                    ret.add(ra);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ret;
    }

    public List<String> list(int skip, int count) {
        try (var stmt = connection.prepareStatement("SELECT url FROM articles LIMIT ? OFFSET ? ")) {
            stmt.setInt(1, count);
            stmt.setInt(2, skip);
            var rsp = stmt.executeQuery();
            var list = new ArrayList<String>();
            while (rsp.next()) {
                list.add(rsp.getString(1));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
