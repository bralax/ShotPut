package io.github.bralax.shotput.parser;

import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.NodeList;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.javadoc.JavadocBlockTag;

import io.github.bralax.shotput.endpoint.Endpoint;
import io.github.bralax.shotput.endpoint.Parameter;
import io.github.bralax.shotput.endpoint.Response;

/**
 * Helpers for parsing the body of a method into an endpoint.
 * @author Brandon Lax
 */
public class MethodParser {
    /**
     * Base method of this class. Responsible for parsing the method.
     * @param stmts The statements inside the method
     * @param endpoint The endpoint to add information to
     * @param tags The javadoc tags to intepret
     * @param ctx The name of the javalin context object
     */
    public static void parseMethodStatements(NodeList<Statement> stmts, Endpoint endpoint, List<JavadocBlockTag> tags, String ctx) {
        for(Statement stmt: stmts) {
            if (stmt instanceof ExpressionStmt) {
                ExpressionStmt stm = (ExpressionStmt) stmt;
                MethodParser.parseMethodExpressionStmt(endpoint, stm, tags, ctx);
            } else if (stmt instanceof NodeWithBody) {
                Statement bod = ((NodeWithBody)stmt).getBody();
                if (bod instanceof BlockStmt) {
                    NodeList<Statement> newStmts = ((BlockStmt) bod).getStatements();
                    MethodParser.parseMethodStatements(newStmts, endpoint, tags, ctx);
                }
            } else if (stmt instanceof IfStmt) {
                IfStmt stm = (IfStmt) stmt;
                if (stm.getThenStmt() instanceof BlockStmt) {
                    NodeList<Statement> newStmts = ((BlockStmt) stm.getThenStmt()).getStatements();
                    MethodParser.parseMethodStatements(newStmts, endpoint, tags, ctx);
                }
                if (stm.getElseStmt().isPresent() && stm.getElseStmt().get() instanceof BlockStmt) {
                    NodeList<Statement> newStmts = ((BlockStmt) stm.getElseStmt().get()).getStatements();
                    MethodParser.parseMethodStatements(newStmts, endpoint, tags, ctx);
                }
            }
        }
    }

    /**
     * Parses one line of a method.
     * @param endpoint The endpoint to add information to
     * @param stm The statement to parse
     * @param tags The javadoc tags to intepret
     * @param ctx The name of the javalin context object
     */
    public static void parseMethodExpressionStmt(Endpoint endpoint, ExpressionStmt stm, List<JavadocBlockTag> tags, String ctx ){
        if (stm.getExpression() instanceof MethodCallExpr) {
            MethodCallExpr call = (MethodCallExpr) stm.getExpression();
            MethodParser.parseMethodCall(endpoint, call, tags, ctx);
        } else if (stm.getExpression() instanceof VariableDeclarationExpr) {
            VariableDeclarationExpr declaration = (VariableDeclarationExpr) stm.getExpression();
            try {
                Optional<Expression> init = declaration.getVariable(0).getInitializer();
                if (init.isPresent() && init.get() instanceof MethodCallExpr) {
                    MethodCallExpr call = (MethodCallExpr) init.get();
                    MethodParser.parseMethodCall(endpoint, call, tags, ctx);
                }
            } catch (Exception except) {}
        }
    }

    /**
     * Parses a method call found in the method body. 
     * Looks for method calls that are called by the context and is one that
     * can be interpretted.
     * @param endpoint The endpoint to add information to
     * @param call The method call to parse
     * @param tags The javadoc tags to intepret
     * @param ctx The name of the javalin context object
     */
    public static void parseMethodCall(Endpoint endpoint, MethodCallExpr call, List<JavadocBlockTag> tags, String ctx) {
        Optional<Expression> exp = call.getScope();
        if (exp.isPresent() && exp.get() instanceof NameExpr) {
            if ((ParserHelpers.getSymbol((NameExpr) exp.get()).equals("? super io.javalin.http.Context")) || ((NameExpr) exp.get()).getNameAsString().equals(ctx)) {
                switch(call.getNameAsString()) {
                    case "json":
                        endpoint.setResponseType("json");
                        break;
                    case "formParam":
                        if (call.getArgument(0) instanceof StringLiteralExpr) {
                            JavadocBlockTag tag = ParserHelpers.getCommentTag(tags,"formParam",((StringLiteralExpr) call.getArgument(0)).asString());
                            if (tag != null) {
                                JavadocParser.parseParameterJavadoc(endpoint, tag);
                            } else {
                                endpoint.addFormParam(new Parameter(call.getArgument(0).toString().replace("\"", ""), ""));
                            }
                        }
                        break;
                    case "pathParam":
                        if (call.getArgument(0) instanceof StringLiteralExpr) {
                            JavadocBlockTag tag = ParserHelpers.getCommentTag(tags,"pathParam",((StringLiteralExpr) call.getArgument(0)).asString());
                            if (tag != null) {
                                JavadocParser.parseParameterJavadoc(endpoint, tag);
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
                                JavadocBlockTag tag = ParserHelpers.getCommentTag(tags,"requestHeader",((StringLiteralExpr) call.getArgument(0)).asString());
                                if (tag != null) {
                                    JavadocParser.parseParameterJavadoc(endpoint, tag);
                                } else {
                                    endpoint.addHeaderParam(new Parameter(call.getArgument(0).toString().replace("\"", ""), ""));
                                }
                            }
                        } else {
                            if (call.getArgument(0) instanceof StringLiteralExpr) {
                                JavadocBlockTag tag = ParserHelpers.getCommentTag(tags,"responseHeader",((StringLiteralExpr) call.getArgument(0)).asString());
                                if (tag != null) {
                                    JavadocParser.parseParameterJavadoc(endpoint, tag);
                                } else {
                                    endpoint.addResponseHeader(new Parameter(call.getArgument(0).toString().replace("\"", ""), ""));
                                }
                            }
                        }
                        break;
                    case "queryParam":
                        if (call.getArgument(0) instanceof StringLiteralExpr) {
                            JavadocBlockTag tag = ParserHelpers.getCommentTag(tags,"queryParam",((StringLiteralExpr) call.getArgument(0)).asString());
                            if (tag != null) {
                                JavadocParser.parseParameterJavadoc(endpoint, tag);
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
                            JavadocBlockTag tag = ParserHelpers.getCommentTag(tags,"responseStatus",((IntegerLiteralExpr) call.getArgument(0)).toString());
                            if (tag != null) {
                                JavadocParser.parseParameterJavadoc(endpoint, tag);
                            } else {
                                int status = call.getArgument(0).asIntegerLiteralExpr().asNumber().intValue();
                                endpoint.addExampleResponse(new Response(status, "", ""));
                            }
                        }
                        break;
                }
            }
        }
    }
}
