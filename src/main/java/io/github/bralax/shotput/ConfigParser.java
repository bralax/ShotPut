package io.github.bralax.shotput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.commonmark.Extension;
import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

/**
 * Class responsible for parsing a config file.
 * @author Brandon Lax
 */
public class ConfigParser {

    /**
     * Method responsible for parsing a config file.
     * @param path the path to the config file.
     * @return The resulting config file. If the file is not found, it returns the default config.
     */
    public static Config parseConfig(String path) {
        try {
            String content = Files.readString(Path.of(path));
            List<Extension> extensions = Arrays.asList(YamlFrontMatterExtension.create());
            Parser parser = Parser.builder().extensions(extensions).build();
            Node document = parser.parse(content);
            Node child = document.getFirstChild();
            if (child instanceof YamlFrontMatterBlock) {
                YamlFrontMatterBlock frontmatter = (YamlFrontMatterBlock) child;
                //handle the frontmatter
                YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
                frontmatter.accept(visitor);
                Map<String, List<String>> frontData = visitor.getData();
                Config config = new Config();
                if (frontData.containsKey("title") && !frontData.get("title").isEmpty()) {
                    config.title = frontData.get("title").get(0);
                }
                if (frontData.containsKey("intro") && !frontData.get("intro").isEmpty()) {
                    config.intro = frontData.get("intro").get(0);
                }
                if (frontData.containsKey("baseURL") && !frontData.get("baseURL").isEmpty()) {
                    config.baseUrl = frontData.get("baseURL").get(0);
                }
                if (frontData.containsKey("description") && !frontData.get("description").isEmpty()) {
                    config.description = frontData.get("description").get(0);
                }
                if (frontData.containsKey("languages") && !frontData.get("languages").isEmpty()) {
                    config.languages.addAll(frontData.get("languages"));
                }
                if (frontData.containsKey("interactive") && !frontData.get("interactive").isEmpty()) {
                    config.interactive = frontData.get("interactive").get(0).equals("true");
                }
                if (frontData.containsKey("disableMethodParsing") && !frontData.get("disableMethodParsing").isEmpty()) {
                    config.disableMethodParsing = frontData.get("disableMethodParsing").get(0).equals("true");
                }
                return config;
            } else {
                return Config.defaultValue();
            }
        } catch (IOException ex) {
            return Config.defaultValue();
        }
    }
}
