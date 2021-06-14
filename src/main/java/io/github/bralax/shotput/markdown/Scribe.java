package io.github.bralax.shotput.markdown;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.slugify.Slugify;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import io.github.bralax.shotput.Config;
import io.github.bralax.shotput.code.SampleCodeGenerator;
import io.github.bralax.shotput.endpoint.Endpoint;
import io.github.bralax.shotput.html.Pastel;

/** Class resposible for generating the markdown.
 * @author Brandon Lax
 */
public class Scribe {
    private Pastel pastel;
    private Path outputPath;
    private Path sourceOutputPath;
    private VelocityEngine engine;
    private Slugify slugify;
    private String baseUrl;
    private List<SampleCodeGenerator> generators;
    private Config config;


    public Scribe(String path, Config config, List<SampleCodeGenerator> generators) {
        // If no config is injected, pull from global. Makes testing easier.
        //$this->config = $config ?: new DocumentationConfig(config('scribe'));
        //$this->baseUrl = $this->config->get('base_url') ?? config('app.url');
        //$this->shouldOverwrite = $shouldOverwrite;
        //$this->shouldGeneratePostmanCollection = $this->config->get('postman.enabled', false);
        //$this->shouldGenerateOpenAPISpec = $this->config->get('openapi.enabled', false);
        this.pastel = new Pastel();
        this.outputPath = Path.of(path);
        this.sourceOutputPath = this.outputPath.resolve("markdown/");
        this.engine = new VelocityEngine();
        this.engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
        this.engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        this.engine.setProperty("velocimacro.library", "views/macros/velocimacros.vtl");
        this.engine.init();
        this.slugify = new Slugify();
        
        this.baseUrl = config.baseUrl;
        this.generators = generators;
        this.config = config;
        //$this->isStatic = $this->config->get('type') === 'static';
        //$this->staticTypeOutputPath = rtrim($this->config->get('static.output_path', 'public/docs'), '/');

        //$this->fileModificationTimesFile = $this->sourceOutputPath . '/.filemtimes';
        //$this->lastTimesWeModifiedTheseFiles = [];
    }


    public void writeDocs(Map<String, List<Endpoint>> routes) {
        // The source Markdown files always go in resources/docs.
        // The static assets (js/, css/, and images/) always go in public/docs/.
        // For 'static' docs, the output files (index.html, collection.json) go in public/docs/.
        // For 'laravel' docs, the output files (index.blade.php, collection.json)
        // go in resources/views/scribe/ and storage/app/scribe/ respectively.

        // When running with --no-extraction, $routes will be null.
        // In that case, we only want to write HTMl docs again, hence the conditionals below
        if (routes != null) {
            this.writeMarkdownAndSourceFiles(routes);

            this.writeHtmlDocs();

            this.writePostmanCollection(routes);

            this.writeOpenAPISpec(routes);
        }
    }

    public void writeMarkdownAndSourceFiles(Map<String, List<Endpoint>> parsedRoutes) {

        System.out.println("Writing source Markdown files to: " + this.sourceOutputPath.toString());

        if (!this.sourceOutputPath.toFile().exists()) {
            this.sourceOutputPath.toFile().mkdirs();
        }

        //this.fetchLastTimeWeModifiedFilesFromTrackingFile();

        this.writeEndpointsMarkdownFile(parsedRoutes);
        this.writeIndexMarkdownFile();
        this.writeAuthMarkdownFile();

        //this.writeModificationTimesTrackingFile();

        System.out.println("Wrote source Markdown files to: " + this.sourceOutputPath.toString());
    }


    protected void writeEndpointsMarkdownFile(Map<String, List<Endpoint>> parsedRoutes)
    {
        if (!this.sourceOutputPath.resolve("groups/").toFile().exists()) {
            this.sourceOutputPath.resolve("groups").toFile().mkdirs();
        }

        Map<String, List<EndpointWithRender>> parsedRoutesWithOutput = this.generateMarkdownOutputForEachRoute(parsedRoutes);
        for (Map.Entry<String, List<EndpointWithRender>> group: parsedRoutesWithOutput.entrySet()) {
            String groupId = slugify.slugify(group.getKey());
            String filename = this.sourceOutputPath + "/groups/"+groupId+ ".md";
            Template template = engine.getTemplate("views/partial/group.vtl");
            VelocityContext context = new VelocityContext();
            context.put("groupName", group.getKey());
            context.put("groupDescription", "");
            context.put("routes", group.getValue());
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            try {
            BufferedWriter indexWriter = new BufferedWriter(new FileWriter(filename));
            indexWriter.write(writer.toString());
            
            indexWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Map<String, List<EndpointWithRender>> generateMarkdownOutputForEachRoute(Map<String, List<Endpoint>> parsedRoutes)
    {
        Template template = engine.getTemplate("views/partial/endpoint.vtl");
        Map<String, List<EndpointWithRender>> routesWithOutput = new HashMap<>();
        for (Map.Entry<String, List<Endpoint>> routeGroup: parsedRoutes.entrySet() ) {
            routesWithOutput.put(routeGroup.getKey(), new ArrayList<>());
            for (Endpoint route: routeGroup.getValue()) {
                boolean hasRequestOptions = route.responseHeaderLength() > 0 
                    || route.queryParamLength() > 0 
                    || route.formParamLength() > 0;
                VelocityContext context = new VelocityContext();
                context.put("hasRequestOptions", hasRequestOptions);
                context.put("util", new MarkdownWriterUtils(this.baseUrl, this.generators));
                context.put("baseUrl", this.baseUrl);
                context.put("settings", config);
                context.put("endpointId", route.getType() + route.getEndpoint().replaceAll("[/?{}:]", "-"));
                context.put("route", route);
                StringWriter writer = new StringWriter();
                template.merge(context, writer);
                EndpointWithRender rendered = new EndpointWithRender(route, writer.toString());
                routesWithOutput.get(routeGroup.getKey()).add(rendered);
            }
        }

        return routesWithOutput;
    }

    protected void writeIndexMarkdownFile()
    {
        Path indexMarkdownFile = this.sourceOutputPath.resolve("index.md");
        Template front = engine.getTemplate("views/partial/frontmatter.vtl");
        VelocityContext frontCTX = new VelocityContext();
        frontCTX.put("showPostmanCollectionButton", false);//$this->shouldGeneratePostmanCollection)
        frontCTX.put("showOpenAPISpecButton", false);//$this->shouldGenerateOpenAPISpec)
        frontCTX.put("postmanCollectionLink", "./collection.json");
        frontCTX.put("openAPISpecLink", "./openapi.yaml");
        frontCTX.put("outputPath", "docs");
        frontCTX.put("settings", config);
        StringWriter fwriter = new StringWriter();
        front.merge(frontCTX, fwriter);

        Template index = engine.getTemplate("views/markdownIndex.vtl");
        VelocityContext indexCTX = new VelocityContext();
        indexCTX.put("frontmatter", fwriter.toString());
        indexCTX.put("description", config.description);
        indexCTX.put("introText", config.intro);
        indexCTX.put("baseUrl", this.baseUrl);
        indexCTX.put("isInteractive", config.interactive);
        StringWriter iwriter = new StringWriter();
        index.merge(indexCTX, iwriter);
        try {
        BufferedWriter indexWriter = new BufferedWriter(new FileWriter(indexMarkdownFile.toAbsolutePath().toFile()));
        indexWriter.write(iwriter.toString());
        indexWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // TODO: Support auth settings
    protected void writeAuthMarkdownFile()
    {
        /*$authMarkdownFile = $this->sourceOutputPath . '/authentication.md';
        if ($this->hasFileBeenModified($authMarkdownFile)) {
            if ($this->shouldOverwrite) {
                ConsoleOutputUtils::warn("Discarding manual changes for file $authMarkdownFile because you specified --force");
            } else {
                ConsoleOutputUtils::warn("Skipping modified file $authMarkdownFile");
                return;
            }
        }

        $isAuthed = $this->config->get('auth.enabled', false);
        $authDescription = '';
        $extraInfo = '';

        if ($isAuthed) {
            $strategy = $this->config->get('auth.in');
            $parameterName = $this->config->get('auth.name');
            $authDescription = Arr::random([
                "This API is authenticated by sending ",
                "To authenticate requests, include ",
                "Authenticate requests to this API's endpoints by sending ",
            ]);
            switch ($strategy) {
                case 'query':
                    $authDescription .= "a query parameter **`$parameterName`** in the request.";
                    break;
                case 'body':
                    $authDescription .= "a parameter **`$parameterName`** in the body of the request.";
                    break;
                case 'query_or_body':
                    $authDescription .= "a parameter **`$parameterName`** either in the query string or in the request body.";
                    break;
                case 'bearer':
                    $authDescription .= sprintf('an **`Authorization`** header with the value **`"Bearer %s"`**.', $this->config->get('auth.placeholder') ?: 'your-token');;
                    break;
                case 'basic':
                    $authDescription .= "an **`Authorization`** header in the form **`\"Basic {credentials}\"`**. The value of `{credentials}` should be your username/id and your password, joined with a colon (:), and then base64-encoded.";
                    break;
                case 'header':
                    $authDescription .= sprintf('a **`%s`** header with the value **`"%s"`**.', $parameterName, $this->config->get('auth.placeholder') ?: 'your-token');
                    break;
            }
            $authDescription .= "\n\nAll authenticated endpoints are marked with a `requires authentication` badge in the documentation below.";
            $extraInfo = $this->config->get('auth.extra_info', '');
        }

        $authMarkdown = view('scribe::authentication', [
            'isAuthed' => $isAuthed,
            'authDescription' => $authDescription,
            'extraAuthInfo' => $extraInfo,
        ]);
        $this->writeFile($authMarkdownFile, $authMarkdown);*/
    }

    public void writeHtmlDocs()
    {
        System.out.println("Transforming Markdown docs to HTML...");
        this.pastel.generate( this.sourceOutputPath.resolve("index.md").toString(),
                             this.outputPath.resolve("html").toAbsolutePath().toString(), null);
        
        System.out.println("Wrote HTML documentation to: " + this.sourceOutputPath.toString());
    }


    protected void writePostmanCollection(Map<String, List<Endpoint>> parsedRoutes)
    {
        /*if ($this->shouldGeneratePostmanCollection) {
            ConsoleOutputUtils::info('Generating Postman collection');

            $collection = $this->generatePostmanCollection($parsedRoutes);
            if ($this->isStatic) {
                $collectionPath = "{$this->staticTypeOutputPath}/collection.json";
                file_put_contents($collectionPath, $collection);
            } else {
                Storage::disk('local')->put('scribe/collection.json', $collection);
                $collectionPath = 'storage/app/scribe/collection.json';
            }

            ConsoleOutputUtils::success("Wrote Postman collection to: {$collectionPath}");
        }*/
    }

    protected void writeOpenAPISpec(Map<String, List<Endpoint>> parsedRoutes)
    {
        /*if ($this->shouldGenerateOpenAPISpec) {
            ConsoleOutputUtils::info('Generating OpenAPI specification');

            $spec = $this->generateOpenAPISpec($parsedRoutes);
            if ($this->isStatic) {
                $specPath = "{$this->staticTypeOutputPath}/openapi.yaml";
                file_put_contents($specPath, $spec);
            } else {
                Storage::disk('local')->put('scribe/openapi.yaml', $spec);
                $specPath = 'storage/app/scribe/openapi.yaml';
            }

            ConsoleOutputUtils::success("Wrote OpenAPI specification to: {$specPath}");
        }*/
    }
    /**
     * Generate Postman collection JSON file.
     *
     * @param groupedEndpoints All the endpoints grouped
     */
    public void generatePostmanCollection(Map<String, List<Endpoint>> groupedEndpoints)
    {
        /** @var PostmanCollectionWriter $writer */
        /*$writer = app()->makeWith(
            PostmanCollectionWriter::class,
            ['config' => $this->config]
        );

        $collection = $writer->generatePostmanCollection($groupedEndpoints);
        $overrides = $this->config->get('postman.overrides', []);
        if (count($overrides)) {
            foreach ($overrides as $key => $value) {
                data_set($collection, $key, $value);
            }
        }
        return json_encode($collection, JSON_PRETTY_PRINT);*/
    }

    public void generateOpenAPISpec(Map<String, List<Endpoint>> groupedEndpoints)
    {
        
        /*$writer = app()->makeWith(
            OpenAPISpecWriter::class,
            ['config' => $this->config]
        );

        $spec = $writer->generateSpecContent($groupedEndpoints);
        $overrides = $this->config->get('openapi.overrides', []);
        if (count($overrides)) {
            foreach ($overrides as $key => $value) {
                data_set($spec, $key, $value);
            }
        }
        return Yaml::dump($spec, 10, 4, Yaml::DUMP_EMPTY_ARRAY_AS_SEQUENCE | Yaml::DUMP_OBJECT_AS_MAP);*/
    }

    public class EndpointWithRender {
        public Endpoint endpoint;
        public String render;
        public EndpointWithRender(Endpoint end, String res) {
            this.endpoint = end;
            this.render = res;
        }

        public String getRender() {
            return render;
        }
    }
}