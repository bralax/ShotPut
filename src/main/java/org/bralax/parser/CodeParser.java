package org.bralax.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.TreeVisitor;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import com.github.javaparser.javadoc.description.JavadocSnippet;

import org.bralax.endpoint.Endpoint;

public class CodeParser extends TreeVisitor {

    private Set<Endpoint> endpoints;
    public CodeParser() {
        this.endpoints = new HashSet<>();
    }

    public List<Endpoint> getEndpoints() {
        return Arrays.asList(this.endpoints.toArray(new Endpoint[this.endpoints.size()]));
    }
    @Override
    public void process(Node node) {
        if (node instanceof ExpressionStmt) {
            ExpressionStmt stmt = (ExpressionStmt) node;
            if (stmt.getExpression() instanceof MethodCallExpr) {
                MethodCallExpr call = (MethodCallExpr) stmt.getExpression();
                Optional<Expression> exp = call.getScope();
                if (exp.isPresent() && exp.get() instanceof NameExpr) {
                    if (ParserHelpers.getSymbol((NameExpr) exp.get()).equals("io.javalin.Javalin") && isValidMethod(call.getNameAsString()) ) {
                        //Is an endpoint
                        Endpoint endpoint = new Endpoint();
                        if (stmt.getComment().isPresent() && stmt.getComment().get() instanceof JavadocComment) {
                            JavadocComment comment = (JavadocComment) stmt.getComment().get();
                            Javadoc javadoc = parseJavadoc(comment);
                            List<JavadocBlockTag> tags = new ArrayList<>();
                            tags.addAll(javadoc.getBlockTags());
                            JavadocDescription desc = javadoc.getDescription();
                            List<JavadocDescriptionElement> elements = desc.getElements();
                            if (elements.size() == 1) {
                                endpoint.setDescription(elements.get(0).toText());
                            } else if (elements.size() > 1) {
                                endpoint.setTitle(elements.get(0).toText());
                                endpoint.setDescription(elements.get(1).toText());
                            }
                            parseEndpoint(endpoint, tags, call);
                            if (this.endpoints.contains(endpoint)) {
                                this.endpoints.remove(endpoint);
                            }
                            this.endpoints.add(endpoint);
                        } else {
                            parseEndpoint(endpoint, new ArrayList<>(), call);
                            if (!this.endpoints.contains(endpoint)) {
                                this.endpoints.add(endpoint);
                            }
                        }
                        
                    }

                    
                }
                //NOTE:
                //call.getNameAsString() -> name of the method
                //call.getArguments() -> the parameters passed to the method
                //call.getScope() -> the caller
                //getSymbol(((NameExpr) exp.get())).equals('io.javalin.Javalin') -> is a endpoint
            }
        } else if (node instanceof MethodDeclaration) {
            MethodDeclaration decl = (MethodDeclaration) node;
            NodeList<com.github.javaparser.ast.body.Parameter> parameters = decl.getParameters();
            if (parameters.size() == 1) {
                com.github.javaparser.ast.body.Parameter parameter = parameters.get(0);
                Type paramType = parameter.getType();
                if (paramType instanceof ClassOrInterfaceType && ((ClassOrInterfaceType) paramType).getNameAsString().endsWith("Context")) {
                    Endpoint endpoint = new Endpoint();
                    if (decl.getComment().isPresent() && decl.getComment().get() instanceof JavadocComment) {
                        JavadocComment comment = (JavadocComment) decl.getComment().get();
                        Javadoc javadoc = parseJavadoc(comment);
                        List<JavadocBlockTag> tags = new ArrayList<>();
                        tags.addAll(javadoc.getBlockTags());
                        JavadocDescription desc = javadoc.getDescription();
                        List<JavadocDescriptionElement> elements = desc.getElements();
                        if (elements.size() == 1) {
                            endpoint.setDescription(elements.get(0).toText());
                        } else if (elements.size() > 1) {
                            endpoint.setTitle(elements.get(0).toText());
                            endpoint.setDescription(elements.get(1).toText());
                        }
                        JavadocBlockTag typeTag = ParserHelpers.getCommentTag(tags, "type");
                        if (typeTag != null) {
                            endpoint.setType(typeTag.getContent().toText().strip());
                        } else {
                            return;
                        }
                        JavadocBlockTag endpointTag = ParserHelpers.getCommentTag(tags, "endpoint");
                        if (endpointTag != null) {
                            endpoint.setEndpoint(endpointTag.getContent().toText().strip());
                        } else {
                            return;
                        }
                        parseMethodDeclaration(decl, endpoint, tags);
                        JavadocParser.parseJavadocTags(endpoint, tags);
                        System.out.println(endpoint);
                        if (this.endpoints.contains(endpoint)) {
                            this.endpoints.remove(endpoint);
                        }
                        this.endpoints.add(endpoint);
                    }
                }
            }
        }
    } 


    private void parseEndpoint(Endpoint endpoint, List<JavadocBlockTag> tags, MethodCallExpr call) {
        endpoint.setType(Endpoint.Type.valueOf(call.getNameAsString().toUpperCase()));
        ParserHelpers.getCommentTag(tags, "type");
        
        if (call.getArgument(0) instanceof StringLiteralExpr) {
            endpoint.setEndpoint(((StringLiteralExpr)call.getArgument(0)).asString());
            ParserHelpers.getCommentTag(tags, "endpoint");
        } else {
            JavadocBlockTag tag = ParserHelpers.getCommentTag(tags, "endpoint");
            if (tag != null) {
                endpoint.setEndpoint(tag.getContent().toText().strip());
            }
        }
        if (call.getArgument(1) instanceof LambdaExpr) {
            parseLambdaExpression((LambdaExpr)call.getArgument(1), endpoint, tags);
        } else if (call.getArgument(1) instanceof MethodReferenceExpr){
            parseMethodReference(call.getArgument(1).asMethodReferenceExpr(), endpoint, tags);
        }
        JavadocParser.parseJavadocTags(endpoint, tags);
    }

    private void parseMethodReference(MethodReferenceExpr expr, Endpoint endpoint, List<JavadocBlockTag> tags ) {
        /*System.out.println(expr.asMethodReferenceExpr().resolve());
        ResolvedMethodDeclaration type = expr.resolve();
        System.out.println(type);*/
    }

    private void parseLambdaExpression(LambdaExpr expr, Endpoint endpoint, List<JavadocBlockTag> tags) {
        String ctx = expr.getParameter(0).getNameAsString();
        Statement e = expr.getBody();
        if (e instanceof BlockStmt) {
            NodeList<Statement> stmts = ((BlockStmt) e).getStatements();
            MethodParser.parseMethodStatements(stmts, endpoint, tags, ctx);
        }

    }

    private void parseMethodDeclaration(MethodDeclaration expr, Endpoint endpoint, List<JavadocBlockTag> tags) {
        String ctx = expr.getParameter(0).getNameAsString();
        Optional<BlockStmt> e = expr.getBody();
        if (e.isPresent()) {
            NodeList<Statement> stmts = e.get().getStatements();
            MethodParser.parseMethodStatements(stmts, endpoint, tags, ctx);
        }
    }

    

   

    

    private Javadoc parseJavadoc(JavadocComment comment) {
        String comm = comment.toString();
        comm = comm.replace("/**", "");
        comm = comm.replace("*/", "");
        comm = comm.replace("*", "");
        Scanner scanner = new Scanner(comm.trim());
        String word = " ";
        StringBuilder description = new StringBuilder();
        JavadocDescription desc = new JavadocDescription();
        if (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.length() > 0 && line.charAt(0) == '@') {
                word = line;
            } else {
                desc.addElement(new JavadocSnippet(line));
                while(scanner.hasNext() && word.charAt(0) != '@') {
                    word = scanner.next();
                    if (word.charAt(0) != '@') {
                        description.append(" " + word);
                    }
                }
                desc.addElement(new JavadocSnippet(description.toString()));
            }
        }
        /*while(scanner.hasNext() && word.charAt(0) != '@') {
            word = scanner.next();
            if (word.charAt(0) != '@') {
                description.append(" " + word);
            }
        }
        JavadocDescription desc = new JavadocDescription();
        desc.addElement(new JavadocSnippet(description.toString()));*/
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
                doc.addBlockTag(tag, content.toString().trim());
            }
        }
        scanner.close();
        return doc;
    }

    static boolean isValidMethod(String name) {
        String[] vals = {"delete","get","head","options","patch", "post","put", "sse", "ws"};
        return Arrays.binarySearch(vals, name) >= 0;
    }

    
}
