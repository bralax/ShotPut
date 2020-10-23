import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
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

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.github.javaparser.JavaParser;

public class ASTParser {
    private List<Endpoint> endpoints;
    private File file;

    private ASTParser(File file) {
        this.endpoints = new ArrayList<>();
        this.file = file;
        
    }

    private void start() throws IOException {
        CombinedTypeSolver solver = new CombinedTypeSolver();
        solver.add(new ReflectionTypeSolver());
        solver.add(new JarTypeSolver("src/main/resources/javalin-3.9.1.jar"));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(solver);
        JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);
        CompilationUnit unit =  JavaParser.parse(this.file.toPath());
        new VisitAll().visitPreOrder(unit);
        System.out.println(endpoints);
    }

    public static void main(String[] args) throws Exception {
        String filename = args[0];
        File file = new File(filename);
        new ASTParser(file).start();
        
    }

    private class VisitAll extends TreeVisitor {
        @Override
        public void process(Node node) {
            // TODO Auto-generated method stub
            if (node instanceof ExpressionStmt) {
                ExpressionStmt stmt = (ExpressionStmt) node;
                if (stmt.getExpression() instanceof MethodCallExpr) {
                    
                    MethodCallExpr call = (MethodCallExpr) stmt.getExpression();
                    Optional<Expression> exp = call.getScope();
                    if (exp.isPresent() && exp.get() instanceof NameExpr) {
                        //System.out.println("'" + getSymbol(((NameExpr) exp.get())) + "'");//.resolve().getType());
                        if (getSymbol((NameExpr) exp.get()).equals("io.javalin.Javalin") && isValidMethod(call.getNameAsString()) ) {
                            System.out.println("Is an endpoint!");
                            Endpoint endpoint = new Endpoint();
                            if (stmt.getComment().isPresent() && stmt.getComment().get() instanceof JavadocComment) {
                                JavadocComment comment = (JavadocComment) stmt.getComment().get();
                                Javadoc javadoc = parseJavadoc(comment);
                                List<JavadocBlockTag> tags = new ArrayList<>();
                                tags.addAll(javadoc.getBlockTags());
                                System.out.println(tags);
                                endpoint.setType(Endpoint.Type.valueOf(call.getNameAsString().toUpperCase()));
                                getCommentTag(tags, "endpointType");
                                if (call.getArgument(0) instanceof StringLiteralExpr) {
                                    endpoint.setEndpoint(((StringLiteralExpr)call.getArgument(0)).asString());
                                    getCommentTag(tags, "endpoint");
                                } else {
                                    JavadocBlockTag tag = getCommentTag(tags, "endoint");
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
                                        case "endpointType":
                                            endpoint.setType(Endpoint.Type.valueOf(content));
                                            break;
                                        case "endpointQueryParam":
                                            endpoint.addQueryParam(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                                            break;
                                        case "endpointPathParam":
                                            endpoint.addPathParam(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                                            break;
                                        case "endpointFormParam":
                                            endpoint.addFormParam(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                                            break;
                                        case "endpointRequestHeader":
                                            endpoint.addHeaderParam(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                                            break;
                                        case "endpointResponseHeader":
                                            endpoint.addResponseHeader(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                                            break;
                                        case "endpointStatus":
                                            endpoint.addResponseStatus(new Parameter(content.substring(0, content.indexOf(" ")), content.substring(content.indexOf(" "))));
                                            break;
                                    }
                                }
                                endpoint.setDescription(javadoc.getDescription().toText());
                            }
                            endpoints.add(endpoint);
                        }
    
                        //getSymbol(((NameExpr) exp.get())).equals('io.javalin.Javalin') -> is a endpoint
                    }
                    //call.getNameAsString() -> name of the method
                    //call.getArguments() -> the parameters passed to the method
                    //call.getScope() -> the caller
                    //
    
                }
            } /*else {
                System.out.println(node.getClass() + "\t" + node);
            }*/
            //System.out.println(node.getClass().getSimpleName() + "\t" + node);  
        } 
    }

    private void parseRemainingComment(List<JavadocBlockTag> tags) {
        for (JavadocBlockTag tag : tags) {
            switch
        }
    }

    private void parseLambdaExpression(LambdaExpr expr, Endpoint endpoint, List<JavadocBlockTag> tags) {
        Statement e = expr.getBody();
        if (e instanceof BlockStmt) {
            NodeList<Statement> stmts = ((BlockStmt) e).getStatements();
            for(Statement stmt: stmts) {
                if (stmt instanceof ExpressionStmt) {
                    ExpressionStmt stm = (ExpressionStmt) stmt;
                    if (stm.getExpression() instanceof MethodCallExpr) {
                        MethodCallExpr call = (MethodCallExpr) stm.getExpression();
                        Optional<Expression> exp = call.getScope();
                        if (exp.isPresent() && exp.get() instanceof NameExpr) {
                            //System.out.println(getSymbol((NameExpr) exp.get()));
                            if (getSymbol((NameExpr) exp.get()).equals("? super io.javalin.http.Context")) {
                                System.out.println(call.getNameAsString());
                                switch(call.getNameAsString()) {
                                    case "json":
                                        endpoint.setResponseType("json");
                                        break;
                                    case "formParam":
                                        if (call.getArgument(0) instanceof StringLiteralExpr) {
                                            JavadocBlockTag tag = getCommentTag(tags,"endpointFormParam",((StringLiteralExpr) call.getArgument(0)).asString());
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
                                            JavadocBlockTag tag = getCommentTag(tags,"endpointPathParam",((StringLiteralExpr) call.getArgument(0)).asString());
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
                                                JavadocBlockTag tag = getCommentTag(tags,"endpointRequestHeader",((StringLiteralExpr) call.getArgument(0)).asString());
                                                if (tag != null) {
                                                    String content = tag.getContent().toText().strip();
                                                    endpoint.addHeaderParam(new Parameter(call.getArgument(0).toString(), content.substring(content.indexOf(" "))));
                                                } else {
                                                    endpoint.addHeaderParam(new Parameter(call.getArgument(0).toString(), ""));
                                                }
                                            }
                                        } else {
                                            if (call.getArgument(0) instanceof StringLiteralExpr) {
                                                JavadocBlockTag tag = getCommentTag(tags,"endpointResponseHeader",((StringLiteralExpr) call.getArgument(0)).asString());
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
                                            JavadocBlockTag tag = getCommentTag(tags,"endpointQueryParam",((StringLiteralExpr) call.getArgument(0)).asString());
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
                                            JavadocBlockTag tag = getCommentTag(tags,"endpointStatus",((IntegerLiteralExpr) call.getArgument(0)).toString());
                                            if (tag != null) {
                                                String content = tag.getContent().toText().strip();
                                                endpoint.addResponseStatus(new Parameter(call.getArgument(0).toString(), content.substring(content.indexOf(" "))));
                                            } else {
                                                endpoint.addResponseStatus(new Parameter(call.getArgument(0).toString(), ""));
                                            }
                                        }
                                        System.out.println(call.getArgument(0).getClass().getSimpleName());
                                        break;
                                }
                            }
                        }
                    }
                }
                System.out.println(stmt.getClass().getSimpleName());
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
        }
    }
}
