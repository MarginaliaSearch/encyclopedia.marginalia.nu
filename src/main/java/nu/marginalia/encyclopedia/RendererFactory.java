package nu.marginalia.encyclopedia;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import java.io.IOException;

public class RendererFactory {
    Handlebars handlebars;

    public RendererFactory() {
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".hdb");
        handlebars = new Handlebars(loader);
    }

    public Template getTemplate(String templateFile) throws IOException {
        return handlebars.compile(templateFile);
    }
}
