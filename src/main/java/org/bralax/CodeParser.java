package org.bralax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

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

import org.bralax.endpoint.Endpoint;
import org.bralax.endpoint.Parameter;

public class CodeParser extends TreeVisitor {

    private List<Endpoint> endpoints;
    public CodeParser() {
        this.endpoints = new ArrayList<>();
    }

    public List<Endpoint> getEndpoints() {
        return this.endpoints;
    }
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
                        this.endpoints.add(endpoint);
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
        parseJavadocTags(endpoint, tags);
    }

    private void parseJavadocTags(Endpoint endpoint, List<JavadocBlockTag> tags) {
        endpoint.setAuthenticated(false);
        for (JavadocBlockTag tag: tags) {
            String content = tag.getContent().toText().strip();
            String tagName = tag.getTagName();
            if (tagName.equals("endpoint")) {
                endpoint.setEndpoint(content);
            } else if (tagName.equals("type")) {
                endpoint.setType(Endpoint.Type.valueOf(content));
            } else if (tagName.equals("authenticated")) {
                endpoint.setAuthenticated(true);
            } else if (tagName.equals("group")) {
                endpoint.setGroup(content);
            } else if (tagName.equals(""))
            switch (tag.getTagName()) {
                case "endpoint":
                    endpoint.setEndpoint(content);
                    break;
                case "type":
                    endpoint.setType(Endpoint.Type.valueOf(content));
                    break;
                case "authenticated":
                    endpoint.setAuthenticated(true);
                case "group":
                    endpoint.setGroup(content);
                case "responseType":
                    endpoint.setResponseType(content);
                    break;
                default: 
                    parseParameterJavadoc(endpoint, tag);
            }
        }
    }

    private void parseParameterJavadoc(Endpoint endpoint, JavadocBlockTag tag) {
        String unparsedContent = tag.getContent().toText().strip();
        Scanner parser = new Scanner(unparsedContent);
        String paramName, paramType, content;
        boolean required = false;
        if (parser.hasNext()) {
            paramName = parser.next();
            if (parser.hasNext()) {
                String next = parser.next();
                if (isValidParameterType(next)) {
                    paramType = next;
                    if (parser.hasNext()) {
                        next = parser.next();
                        if (next.equals("required")) {
                            required = true;
                            content = getContent(parser);
                        } else {
                            content = next + " " +  getContent(parser);
                        }
                    } else {
                        content = "";
                    }
                } else if (next.equals("required")) {
                    paramType = "String";
                    required = true;
                    content = getContent(parser);
                } else {
                    paramType = "String";
                    content = next + " " + getContent(parser);
                }
            } else {
                paramType = "String";
                content = "";
            }
        } else {
            parser.close();
            return;
        }
        parser.close();
        Parameter newParam = new Parameter(paramName, content);
        newParam.setRequired(required);
        newParam.setType(paramType);
        switch (tag.getTagName()) {
            case "queryParam":
                    endpoint.addQueryParam(newParam);
                    break;
                case "pathParam":
                    endpoint.addPathParam(newParam);
                    break;
                case "bodyParam":
                    endpoint.addFormParam(newParam);
                    break;
                case "requestHeader":
                    endpoint.addHeaderParam(newParam);
                    break;
                case "responseHeader":
                    endpoint.addResponseHeader(newParam);
                    break;
                case "responseStatus":
                    endpoint.addResponseStatus(newParam);
                    break;
        }
    }

    private boolean isValidParameterType(String type) {
        if (type.endsWith("[]")) {
            type = type.substring(0, type.length()-2);
        }
        String[] types = {"Array", "Boolean", "File", "Float", "Int", "Object", "String"};
        return Arrays.binarySearch(types, type) >= -1;
    }

    private String getContent(Scanner scrn) {
        StringBuilder builder = new StringBuilder();
        while(scrn.hasNextLine()) {
            builder.append(scrn.nextLine()).append("\n");
        }
        return builder.toString();
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
                                parseParameterJavadoc(endpoint, tag);
                            } else {
                                endpoint.addFormParam(new Parameter(call.getArgument(0).toString().replace("\"", ""), ""));
                            }
                        }
                        break;
                    case "pathParam":
                        if (call.getArgument(0) instanceof StringLiteralExpr) {
                            JavadocBlockTag tag = getCommentTag(tags,"pathParam",((StringLiteralExpr) call.getArgument(0)).asString());
                            if (tag != null) {
                                parseParameterJavadoc(endpoint, tag);
                            } else {
                                Parameter param = new Parameter(call.getArgument(0).toString().replace("\"", ""), "");
                                param.setRequired(true);
                                endpoint.addPathParam(param);
                            }
                        }
                        break;
                    case "header":
                        if (call.getArguments().size() == 1) {
                            if (call.getArgument(0) instanceof StringLiteralExpr) {
                                JavadocBlockTag tag = getCommentTag(tags,"requestHeader",((StringLiteralExpr) call.getArgument(0)).asString());
                                if (tag != null) {
                                    parseParameterJavadoc(endpoint, tag);
                                } else {
                                    endpoint.addHeaderParam(new Parameter(call.getArgument(0).toString().replace("\"", ""), ""));
                                }
                            }
                        } else {
                            if (call.getArgument(0) instanceof StringLiteralExpr) {
                                JavadocBlockTag tag = getCommentTag(tags,"responseHeader",((StringLiteralExpr) call.getArgument(0)).asString());
                                if (tag != null) {
                                    parseParameterJavadoc(endpoint, tag);
                                } else {
                                    endpoint.addResponseHeader(new Parameter(call.getArgument(0).toString().replace("\"", ""), ""));
                                }
                            }
                        }
                        break;
                    case "queryParam":
                        if (call.getArgument(0) instanceof StringLiteralExpr) {
                            JavadocBlockTag tag = getCommentTag(tags,"queryParam",((StringLiteralExpr) call.getArgument(0)).asString());
                            if (tag != null) {
                                parseParameterJavadoc(endpoint, tag);
                            } else {
                                endpoint.addQueryParam(new Parameter(call.getArgument(0).toString().replace("\"", ""), ""));
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
                                parseParameterJavadoc(endpoint, tag);
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
        scanner.close();
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
