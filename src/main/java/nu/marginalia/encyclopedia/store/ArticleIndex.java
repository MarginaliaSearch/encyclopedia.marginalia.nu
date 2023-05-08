package nu.marginalia.encyclopedia.store;

import nu.marginalia.encyclopedia.model.SearchResult;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArticleIndex {

    private final Logger logger = LoggerFactory.getLogger(ArticleIndex.class);
    private final PatriciaTrie<String> trie = new PatriciaTrie<>();

    public ArticleIndex(ArticleDbProvider dbProvider) {
        logger.info("Loading trie");

        var connection = dbProvider.getConnection();

        try (var stmt = connection.createStatement()) {
            var rsp = stmt.executeQuery("SELECT url from articles");
            while (rsp.next()) {
                String url = rsp.getString(1);
                trie.put(trimKey(url), url);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        logger.info("Done!");
    }

    /** Prefix based search. The search is case-insensitive.
     *
     * @param prefix The prefix to search for.
     * @param startPrefix Start from this prefix.
     */
    public SearchResult search(String prefix, String startPrefix) {
        List<String> ret = new ArrayList<>();

        final String lcPrefix = findSearchStart(trimKey(prefix));
        String startPrefixLc;

        if (Objects.equals(prefix, startPrefix)) {
            startPrefixLc = lcPrefix;
        }
        else {
            startPrefixLc = findSearchStart(startPrefix.toLowerCase());
        }

        String key;

        for (key = findSearchStart(startPrefixLc);
             key != null && key.startsWith(lcPrefix);
             key = trie.nextKey(key))
        {
            ret.add(trie.get(key));

            if (ret.size() > 100)
                break;
        }

        return new SearchResult(ret, prefix, nextSearchKey(key, lcPrefix));
    }

    private String findSearchStart(String prefix) {

        if (trie.containsKey(prefix)) {
            return prefix;
        }

        int maxChomp = Math.min(prefix.length() - 2, prefix.length() / 2);

        for (int i = 0; i < maxChomp; i++) {
            String trialPrefix = prefix.substring(0, prefix.length() - i);
            var pmap = trie.prefixMap(trialPrefix);
            if (!pmap.isEmpty()) {
                return pmap.firstKey();
            }
        }

        return prefix;
    }

    private String nextSearchKey(String lastKey, String lcPrefix) {

        if (lastKey == null || !lastKey.startsWith(lcPrefix)) {
            return null;
        }

        String nextKey = trie.nextKey(lastKey);

        if (!nextKey.startsWith(lcPrefix)) {
            return null;
        }

        return nextKey;
    }

    public List<String> previousArticles(String url, int n) {

        List<String> links = new ArrayList<>(n);

        for (String key = trie.previousKey(trimKey(url));
             key != null;
             key = trie.previousKey(key))
        {
            links.add(0, trie.get(key));

            if (links.size() >= n)
                break;
        }

        return links;
    }

    public List<String> nextArticles(String url, int n) {

        List<String> links = new ArrayList<>(n);

        for (String key = trie.nextKey(trimKey(url));
             key != null;
             key = trie.nextKey(key))
        {
            links.add(trie.get(key));

            if (links.size() >= n)
                break;
        }

        return links;
    }
    private String trimKey(String searchKey) {
        StringBuilder sb =  new StringBuilder();
        char[] chars = searchKey.trim().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            }
            else if (c == ':' || c == '(' || c == ')') {
                //
            }
            else if (c == ' ' ) {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
