package io.github.bralax.shotput;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bralax.shotput.code.JavaGenerator;
import io.github.bralax.shotput.code.SampleCodeGenerator;
import io.github.bralax.shotput.endpoint.Endpoint;
import io.github.bralax.shotput.endpoint.Parameter;
import io.github.bralax.shotput.excel.ExcelGenerator;
import io.github.bralax.shotput.markdown.Scribe;
import io.github.bralax.shotput.openapi.OpenApiGenerator;
import io.github.bralax.shotput.parser.CodeParser;

import io.swagger.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Central class responsible for the actual documentation generation.
 * @author Brandon Lax
 */
public class Shotput {
    /** The logging tool to use. */
    private static Logger logger = LoggerFactory.getLogger(Shotput.class);

    /** The list of interpretted endpoints. */
    private List<Endpoint> endpoints;

    /** The source directory/file of the code to interpret. */
    private File file;

    /** Boolean of whether to generate an excel file. */
    private boolean excel;

    /** Boolean of whether to generate html documentation. */
    private boolean html;

    /** The  directory to put the generated files into. */
    private File out;

    /** Code generators to use for generating sample code. */
    private List<SampleCodeGenerator> generators;

    /** The interpretted config to used when generating documentation. */
    private Config config;

    /** Boolean of whether to generate a swagger/open-api spec. */
    private boolean swagger;


    /**
     * Constructor of shotput.
     * 
     * All fields are required.
     * @param config The config to base the docs on.
     * @param src The soure directory or file
     * @param excel Whether to generate an excel file
     * @param html Whether to generate html docs
     * @param swagger Whether to generate swagger/open-api docus
     * @param out The path to the output directory
     */
    public Shotput(Config config, File src, boolean excel, boolean html, boolean swagger, File out) {
        this.endpoints = new ArrayList<>();
        this.generators = new ArrayList<>();
        registerGenerator(new JavaGenerator());
        this.file = src;
        this.out = out;
        this.config = config;
        this.swagger = swagger;
        if (!excel && !html) {
            this.excel = true;
            this.html = true;
        } else {
            this.excel = excel;
            this.html = html;
        }
    }

    /**
     * Access the logger attached to the system.
     * @return The current logger.
     */
    public static Logger getLogger() {
        return  Shotput.logger;
    }

    /**
     * Set the current SLF4J logger.
     * Can be used to integrate with other tools that provide a logger.
     * Can be chained.
     * @param logger the logger to use.
     */
    public static void setLogger(Logger logger) {
        Shotput.logger = logger;
    }

    /**
     * Adds a new code generator to the system.
     * Can be chained.
     * @param generator The sample code generator to add.
     * @return this
     */
    public Shotput registerGenerator(SampleCodeGenerator generator) {
        this.generators.add(generator);
        return this;
    }

    /**
     * Method that triggers the action documentation generation.
     * @throws IOException If src or out can not be found
     */
    public void start() throws IOException {
        CombinedTypeSolver solver = new CombinedTypeSolver();
        solver.add(new ReflectionTypeSolver());
        solver.add(new JarTypeSolver(getClass().getClassLoader().getResource("javalin-3.9.1.jar").openStream()));
        this.addFolderSymbolSolvers(solver, this.file);
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(solver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        this.parse(this.file);
        if (this.excel) {
            ExcelGenerator.generateExcel(this.endpoints, this.out);
        }
        if (this.html) {
            this.generateHTML();
        }
        if (this.swagger) {
            this.generateOpenApi();
        }
    }

    /** Small helper used to register all base paths in the symbol solver.
     * 
     * @param solver The symbol solver to add the file to
     * @param file The base file/src dir
     */
    private void addFolderSymbolSolvers(CombinedTypeSolver solver, File file) {
        if (file != null) {
            if (file.isDirectory()) {
                boolean isEmpty = true;
                for (File f: file.listFiles()) {
                    if (f.isDirectory()) {
                        Shotput.logger.info("File Path", f.getAbsolutePath());
                        solver.add(new JavaParserTypeSolver(f));
                        isEmpty = false;
                    }
                }
                if (isEmpty) {
                    solver.add(new JavaParserTypeSolver(file));
                }
            }
        }
    }

    /**
     * The method that triggers a parse of a source file.
     * @param f The file or directory to parse (performed recursively)
     * @throws IOException If a file can not be found
     */
    private void parse(File f) throws IOException{
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                this.parse(file);
            }
        } else if (f.getName().endsWith(".java")) {
            CompilationUnit unit =  StaticJavaParser.parse(f.toPath());
            CodeParser parser = new CodeParser();
            parser.visitPreOrder(unit);
            List<Endpoint> newEndpoints = parser.getEndpoints();
            this.endpoints.addAll(newEndpoints);
        }
    }

    /**
     * Small helper responsible for triggering the generation of html docs.
     * @throws IOException If the out directory does not exist
     */
    private void generateHTML() throws IOException{
        new Scribe(out.getAbsolutePath(), this.config, this.generators).writeDocs(this.groupEndpoints(endpoints));
    }


    /**
     * Small helper for generating open api specs.
     */
    private void generateOpenApi() {
       OpenAPI api = new OpenApiGenerator(config).generate(endpoints);
       try {
        PrintWriter printWriter = new PrintWriter(this.out.getAbsolutePath() + "/openapi.json");
        printWriter.print(Json.pretty().writeValueAsString(api));
        printWriter.flush();
        printWriter.close();
       } catch (IOException ex) {
            Shotput.logger.error("Failed to generate OpenApi Spec!", ex);
       }
    }

    /**
     * Small helper used to group endpoints by their group property.
     * @param endpoints The list of all endpoints
     * @return The list of endpoints grouped by the result of .getGroup()
     */
    private Map<String, List<Endpoint>> groupEndpoints(List<Endpoint> endpoints) {
        Map<String, List<Endpoint>> grouped = new HashMap<>();
        for (Endpoint endpoint: endpoints) {
            String group = endpoint.getGroup();
            if (grouped.containsKey(group)) {
                grouped.get(group).add(endpoint);
            } else {
                List<Endpoint> groupList = new ArrayList<>();
                groupList.add(endpoint);
                grouped.put(group, groupList);
            }
        }
        return grouped;
    }


    
}
