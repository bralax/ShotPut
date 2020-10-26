package org.bralax;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.TreeVisitor;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import io.javalin.Javalin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class JavalinDoc {
    private List<Endpoint> endpoints;
    private File file;
    private File css;
    private boolean excel;
    private boolean html;
    private File out;

    private JavalinDoc(File file, String css, boolean excel, boolean html, File out) {
        this.endpoints = new ArrayList<>();
        this.file = file;
        this.css = new File(css);
        this.out = out;
        if (!excel && !html) {
            this.excel = true;
            this.html = true;
        } else {
            this.excel = excel;
            this.html = html;
        }
    }

    private void start() throws IOException {
        CombinedTypeSolver solver = new CombinedTypeSolver();
        solver.add(new ReflectionTypeSolver());
        solver.add(new JarTypeSolver("src/main/resources/javalin-3.9.1.jar"));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(solver);
        JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);
        this.parse(this.file);
        /*CompilationUnit unit =  JavaParser.parse(this.file.toPath());
        new VisitAll().visitPreOrder(unit);
        System.out.println(endpoints);*/
        if (this.excel) {
            this.generateExcel();
        }
        if (this.html) {
            this.generateHTML();
        }
    }

    private void parse(File f) throws IOException{
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                this.parse(file);
            }
        } else if (f.getName().endsWith(".java")) {
            CompilationUnit unit =  JavaParser.parse(f.toPath());
            new VisitAll().visitPreOrder(unit);
        }
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder("s").longOpt("css").desc("The css file to be used on the html").hasArg().required(false).build());
        options.addOption(Option.builder("cp").longOpt("classpath").desc("The files to parse (Required)").hasArg().required(true).build());
        options.addOption(Option.builder("o").longOpt("outdir").desc("The place to put the generated files (Required)").hasArg().required(true).build());
        options.addOption(Option.builder("x").longOpt("excel").desc("Flag to generate excel file. If this flag and -h are not set both will be generated").required(false).build());
        options.addOption(Option.builder("h").longOpt("html").desc("Flag to generate html file. If this flag and -h are not set both will be generated").required(false).build());
        options.addOption(Option.builder("?").longOpt("help").desc("Print the Help Menu").required(false).build());
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("javalin-doc",options);
            } else {
                String outDir = commandLine.getOptionValue("o");
                String classPath = commandLine.getOptionValue("cp");
                String css = commandLine.getOptionValue("s", "test.css");
                boolean excel = commandLine.hasOption("x");
                boolean html = commandLine.hasOption("h");
                File file = new File(classPath);
                File out = new File(outDir);
                if (file.exists() && out.exists()) {
                    if (out.isDirectory()) {
                        new JavalinDoc(file, css, excel, html, out).start();
                    } else {
                        System.out.println("The output directory must be a folder");
                    }
                }
                else {
                    System.out.println("The classpath and output directory have to be valid locations");
                }
            }
        } catch (org.apache.commons.cli.ParseException exception) {
            
            System.err.println(exception.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("javalin-doc",options);
        }
        
        
    }

    private void generateExcel() {
        //System.out.println(this.out.getAbsolutePath());
        try {
        PrintWriter printWriter = new PrintWriter(this.out.getAbsolutePath() + "/endpoints.csv");
        printWriter.print("Index,Type,Endpoint,Response Type,Description,Path Parameter, Path Parameter Description, Query Parameter, Query Parameter Description,");
        printWriter.print("Form Parameter, Form Parameter Description, Request Header, Request Header Description, Response Header, Response Header Description,");
        printWriter.println("Response Status,Response Status Description");
        for (int i = 0; i < this.endpoints.size(); i++) {
            Endpoint endpoint = endpoints.get(i);
            int max = maxLength(endpoint.pathParamLength(), endpoint.formParamLength(), endpoint.headerParamLength(), endpoint.queryParamLength(), endpoint.responseHeaderLength(), endpoint.responseStatusLength());
            if (max > 0) {
                for (int j = 0; j < max; j++) {
                    if (j == 0) {
                        printWriter.print((i + 1) + "," + prepForCSV(endpoint.getType()) + "," + prepForCSV(endpoint.getEndpoint()) + "," +  prepForCSV(endpoint.getResponseType()) + "," + prepForCSV(endpoint.getDescription()) + ",");
                    } else {
                        printWriter.print(",,,,,");
                    }
                    if (j < endpoint.pathParamLength()) {
                        Parameter param = endpoint.pathParam(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }

                    if (j < endpoint.queryParamLength()) {
                        Parameter param = endpoint.queryParam(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }

                    if (j < endpoint.formParamLength()) {
                        Parameter param = endpoint.formParam(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }

                    if (j < endpoint.headerParamLength()) {
                        Parameter param = endpoint.headerParam(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }

                    if (j < endpoint.responseHeaderLength()) {
                        Parameter param = endpoint.responseHeader(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }

                    if (j < endpoint.responseStatusLength()) {
                        Parameter param = endpoint.responseStatus(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }
                    printWriter.println("");
                }
            } else {
                printWriter.println((i + 1) + "," + prepForCSV(endpoint.getType()) + "," + prepForCSV(endpoint.getEndpoint()) + "," +  prepForCSV(endpoint.getResponseType()) + "," + prepForCSV(endpoint.getDescription()) + ",");
            }
        }
        printWriter.flush();
        printWriter.close();
        } catch (IOException exception) {
            System.err.println("Failed to generate CSV!");
        }
    }

    private int maxLength(int... vals) {
        int max = 0; // the minimum possible length is 0
        for (int val: vals) {
            if (val > max) {
                max = val;
            }
        }
        return max;
    }

    private String prepForCSV(String original) {
        original = original.strip();
        original = original.replace("\n", " ");
        original = original.replace(",", " ");
        return original;
    }

    private void generateHTML() throws IOException{
        HTMLGenerator.generateHTML(css, out, endpoints);
    }

    private class VisitAll extends TreeVisitor {
        @Override
        public void process(Node node) {
            if (node instanceof ExpressionStmt) {
                ExpressionStmt stmt = (ExpressionStmt) node;
                if (stmt.getExpression() instanceof MethodCallExpr) {
                    
                    MethodCallExpr call = (MethodCallExpr) stmt.getExpression();
                    Optional<Expression> exp = call.getScope();
                    if (exp.isPresent() && exp.get() instanceof NameExpr) {
                        if (getSymbol((NameExpr) exp.get()).equals("io.javalin.Javalin") && isValidMethod(call.getNameAsString()) ) {
                            //Is an endpoint
                            Endpoint endpoint = new Endpoint();
                            if (stmt.getComment().isPresent() && stmt.getComment().get() instanceof JavadocComment) {
                                JavadocComment comment = (JavadocComment) stmt.getComment().get();
                                Javadoc javadoc = parseJavadoc(comment);
                                List<JavadocBlockTag> tags = new ArrayList<>();
                                tags.addAll(javadoc.getBlockTags());
                                endpoint.setDescription(javadoc.getDescription().toText());
                                parseEndpoint(endpoint, tags, call);
                            } else {
                                parseEndpoint(endpoint, new ArrayList<>(), call);
                            }
                            endpoints.add(endpoint);
                        }
    
                        
                    }
                    //NOTE:
                    //call.getNameAsString() -> name of the method
                    //call.getArguments() -> the parameters passed to the method
                    //call.getScope() -> the caller
                    //getSymbol(((NameExpr) exp.get())).equals('io.javalin.Javalin') -> is a endpoint
                }
            }
        } 
    }

    private void parseEndpoint(Endpoint endpoint, List<JavadocBlockTag> tags, MethodCallExpr call) {
        endpoint.setType(Endpoint.Type.valueOf(call.getNameAsString().toUpperCase()));
        getCommentTag(tags, "type");
        if (call.getArgument(0) instanceof StringLiteralExpr) {
            endpoint.setEndpoint(((StringLiteralExpr)call.getArgument(0)).asString());
            getCommentTag(tags, "endpoint");
        } else {
            JavadocBlockTag tag = getCommentTag(tags, "endpoint");
            if (tag != null) {
                endpoint.setEndpoint(tag.getContent().toText().strip());
            }
        }
        if (call.getArgument(1) instanceof LambdaExpr) {
            parseLambdaExpression((LambdaExpr)call.getArgument(1), endpoint, tags);
        }
        for (JavadocBlockTag tag: tags) {
            String content = tag.getContent().toText().strip();
            switch (tag.getTagName()) {
                case "endpoint":
                    endpoint.setEndpoint(content);
                    break;
                case "type":
                    endpoint.setType(Endpoint.Type.valueOf(content));
                    break;
                case "queryParam":
                    endpoint.addQueryParam(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                    break;
                case "pathParam":
                    endpoint.addPathParam(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                    break;
                case "formParam":
                    endpoint.addFormParam(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                    break;
                case "requestHeader":
                    endpoint.addHeaderParam(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                    break;
                case "responseHeader":
                    endpoint.addResponseHeader(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                    break;
                case "responseStatus":
                    endpoint.addResponseStatus(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                    break;
                case "responseType":
                    endpoint.setResponseType(content);
                    break;
            }
        }
    }

    private void parseLambdaExpression(LambdaExpr expr, Endpoint endpoint, List<JavadocBlockTag> tags) {
        String ctx = expr.getParameter(0).getNameAsString();
        Statement e = expr.getBody();
        if (e instanceof BlockStmt) {
            NodeList<Statement> stmts = ((BlockStmt) e).getStatements();
            parseLambdaStatements(stmts, endpoint, tags, ctx);
        }

    }

    private void parseLambdaStatements(NodeList<Statement> stmts, Endpoint endpoint, List<JavadocBlockTag> tags, String ctx) {
        for(Statement stmt: stmts) {
            if (stmt instanceof ExpressionStmt) {
                ExpressionStmt stm = (ExpressionStmt) stmt;
                parseLambdaExpressionStmt(endpoint, stm, tags, ctx);
            } else if (stmt instanceof NodeWithBody) {
                Statement bod = ((NodeWithBody)stmt).getBody();
                if (bod instanceof BlockStmt) {
                    NodeList<Statement> newStmts = ((BlockStmt) bod).getStatements();
                    parseLambdaStatements(newStmts, endpoint, tags, ctx);
                }
            } else if (stmt instanceof IfStmt) {
                IfStmt stm = (IfStmt) stmt;
                if (stm.getThenStmt() instanceof BlockStmt) {
                    NodeList<Statement> newStmts = ((BlockStmt) stm.getThenStmt()).getStatements();
                    parseLambdaStatements(newStmts, endpoint, tags, ctx);
                }
                if (stm.getElseStmt().isPresent() && stm.getElseStmt().get() instanceof BlockStmt) {
                    NodeList<Statement> newStmts = ((BlockStmt) stm.getElseStmt().get()).getStatements();
                    parseLambdaStatements(newStmts, endpoint, tags, ctx);
                }
            }
        }
    }

    private void parseLambdaExpressionStmt(Endpoint endpoint, ExpressionStmt stm, List<JavadocBlockTag> tags, String ctx ){
                    //System.out.println(stm + "\t" + stm.getExpression().getClass().getSimpleName());
                    if (stm.getExpression() instanceof MethodCallExpr) {
                        MethodCallExpr call = (MethodCallExpr) stm.getExpression();
                        parseMethodLambdaCall(endpoint, call, tags, ctx);
                    } else if (stm.getExpression() instanceof VariableDeclarationExpr) {
                        VariableDeclarationExpr declaration = (VariableDeclarationExpr) stm.getExpression();
                        try {
                            Optional<Expression> init = declaration.getVariable(0).getInitializer();
                            if (init.isPresent() && init.get() instanceof MethodCallExpr) {
                                MethodCallExpr call = (MethodCallExpr) init.get();
                                parseMethodLambdaCall(endpoint, call, tags, ctx);
                            }
                        } catch (Exception except) {}
                    }
    }

    private void parseMethodLambdaCall(Endpoint endpoint, MethodCallExpr call, List<JavadocBlockTag> tags, String ctx) {
        Optional<Expression> exp = call.getScope();
        if (exp.isPresent() && exp.get() instanceof NameExpr) {
            if ((getSymbol((NameExpr) exp.get()).equals("? super io.javalin.http.Context")) || ((NameExpr) exp.get()).getNameAsString().equals(ctx)) {
                switch(call.getNameAsString()) {
                    case "json":
                        endpoint.setResponseType("json");
                        break;
                    case "formParam":
                        if (call.getArgument(0) instanceof StringLiteralExpr) {
                            JavadocBlockTag tag = getCommentTag(tags,"formParam",((StringLiteralExpr) call.getArgument(0)).asString());
                            if (tag != null) {
                                String content = tag.getContent().toText().strip();
                                endpoint.addFormParam(new Parameter(call.getArgument(0).toString(), content.substring(content.indexOf(" "))));
                            } else {
                                endpoint.addFormParam(new Parameter(call.getArgument(0).toString(), ""));
                            }
                        }
                        break;
                    case "pathParam":
                        if (call.getArgument(0) instanceof StringLiteralExpr) {
                            JavadocBlockTag tag = getCommentTag(tags,"pathParam",((StringLiteralExpr) call.getArgument(0)).asString());
                            if (tag != null) {
                                String content = tag.getContent().toText().strip();
                                endpoint.addPathParam(new Parameter(call.getArgument(0).toString(), content.substring(content.indexOf(" "))));
                            } else {
                                endpoint.addPathParam(new Parameter(call.getArgument(0).toString(), ""));
                            }
                        }
                        break;
                    case "header":
                        if (call.getArguments().size() == 1) {
                            if (call.getArgument(0) instanceof StringLiteralExpr) {
                                JavadocBlockTag tag = getCommentTag(tags,"requestHeader",((StringLiteralExpr) call.getArgument(0)).asString());
                                if (tag != null) {
                                    String content = tag.getContent().toText().strip();
                                    endpoint.addHeaderParam(new Parameter(call.getArgument(0).toString(), content.substring(content.indexOf(" "))));
                                } else {
                                    endpoint.addHeaderParam(new Parameter(call.getArgument(0).toString(), ""));
                                }
                            }
                        } else {
                            if (call.getArgument(0) instanceof StringLiteralExpr) {
                                JavadocBlockTag tag = getCommentTag(tags,"responseHeader",((StringLiteralExpr) call.getArgument(0)).asString());
                                if (tag != null) {
                                    String content = tag.getContent().toText().strip();
                                    endpoint.addResponseHeader(new Parameter(call.getArgument(0).toString(), content.substring(content.indexOf(" "))));
                                } else {
                                    endpoint.addResponseHeader(new Parameter(call.getArgument(0).toString(), ""));
                                }
                            }
                        }
                        break;
                    case "queryParam":
                        if (call.getArgument(0) instanceof StringLiteralExpr) {
                            JavadocBlockTag tag = getCommentTag(tags,"queryParam",((StringLiteralExpr) call.getArgument(0)).asString());
                            if (tag != null) {
                                String content = tag.getContent().toText().strip();
                                endpoint.addQueryParam(new Parameter(call.getArgument(0).toString(), content.substring(content.indexOf(" "))));
                            } else {
                                endpoint.addQueryParam(new Parameter(call.getArgument(0).toString(), ""));
                            }
                        }
                        break;
                    case "html":
                        endpoint.setResponseType("html");
                        break;
                    case "status":
                        if (call.getArgument(0) instanceof IntegerLiteralExpr) {
                            JavadocBlockTag tag = getCommentTag(tags,"responseStatus",((IntegerLiteralExpr) call.getArgument(0)).toString());
                            if (tag != null) {
                                String content = tag.getContent().toText().strip();
                                endpoint.addResponseStatus(new Parameter(call.getArgument(0).toString(), content.substring(content.indexOf(" "))));
                            } else {
                                endpoint.addResponseStatus(new Parameter(call.getArgument(0).toString(), ""));
                            }
                        }
                        break;
                }
            }
        }
    }

    private JavadocBlockTag getCommentTag(List<JavadocBlockTag> tags, String type) {
        JavadocBlockTag returnTag = null;
        for (JavadocBlockTag tag : tags) {
            if (tag.getTagName().equals(type)) {
                
                returnTag = tag;
            }
        } 
        if (returnTag != null) {
            tags.remove(returnTag);
        }
        return returnTag;
    }

    private JavadocBlockTag getCommentTag(List<JavadocBlockTag> tags, String type, String param) {
        JavadocBlockTag returnTag = null;
        for (JavadocBlockTag tag : tags) {
            String content = tag.getContent().toText().strip();
            String tagParam = content.indexOf(" ") >= 0 ? content.substring(0, content.indexOf(" ")) : "";
            if (tag.getTagName().equals(type) && tagParam.equals(param)) {
                returnTag = tag;
            }
        }
        if (returnTag != null) {
            tags.remove(returnTag);
        } 
        return returnTag;
    }

    private Javadoc parseJavadoc(JavadocComment comment) {
        String comm = comment.toString();
        comm = comm.replace("/**", "");
        comm = comm.replace("*/", "");
        comm = comm.replace("*", "");
        Scanner scanner = new Scanner(comm);
        String word = " ";
        StringBuilder description = new StringBuilder();
        while(scanner.hasNext() && word.charAt(0) != '@') {
            word = scanner.next();
            if (word.charAt(0) != '@') {
                description.append(" " + word);
            }
        }
        JavadocDescription desc = new JavadocDescription();
        desc.addElement(new JavadocSnippet(description.toString()));
        Javadoc doc = new Javadoc(desc);
        if (word.charAt(0) == '@') {
            while (scanner.hasNext()) {
                String tag = word.substring(1);
                StringBuilder content = new StringBuilder();
                word = scanner.next();
                if (word.charAt(0) != '@') {
                    content.append(" " + word);
                }
                while(scanner.hasNext() && word.charAt(0) != '@') {
                    word = scanner.next();
                    if (word.charAt(0) != '@') {
                        content.append(" " + word);
                    }
                }
                doc.addBlockTag(tag, content.toString());
            }
        }
        scanner.close();;
        return doc;
    }

    static boolean isValidMethod(String name) {
        String[] vals = {"delete","get","head","options","patch", "post","put", "sse", "ws"};
        return Arrays.binarySearch(vals, name) >= 0;
    }

    static String getSymbol(NameExpr expr) {
        try {
            ResolvedValueDeclaration dec = expr.resolve();
            return dec.getType().describe();
        } catch (UnsolvedSymbolException exception) {
            return exception.toString();
        } catch (RuntimeException exception) {
            return "";
        }
     }
}
