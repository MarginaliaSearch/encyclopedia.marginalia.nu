package nu.marginalia.encyclopedia.cleaner;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WikiCleanerTest {

    @Test
    void getDocumentParts() {
        String html = """
                <div>
                    <h1>title</h1>
                    <p>Lorem</p>
                    <p>Ipsum</p>
                    <div>
                        <h2>Test</h2>
                        <p>foo</p>
                        <p>bar</p>
                        <p>baz</p>
                    </div>
                    <h3>Foo</h3>
                    <ul>bar</ul>
                    <div>
                        <p>qux</p>
                        <p>quux</p>
                        <p>quuz</p>
                    </div>
                </div>
                """;
        var parts = WikiCleaner.getDocumentParts(Jsoup.parseBodyFragment(html));
        parts.parts().forEach(part -> {
            assertTrue(part.startsWith("<div>"));
        });
        System.out.println(parts);
    }
}