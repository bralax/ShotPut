import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
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
                    System.out.println("YES");
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
                                System.out.println(javadoc.getBlockTags().size());
                                for (JavadocBlockTag tag : javadoc.getBlockTags()) {
                                    System.out.println("Tag:" + tag.getTagName() + "\t Name:" + tag.getName() + "\t Content:" + tag.getContent());
                                }
                                endpoint.setType(Endpoint.Type.valueOf(call.getNameAsString().toUpperCase()));
                                if (call.getArgument(0) instanceof StringLiteralExpr) {
                                    endpoint.setEndpoint(((StringLiteralExpr)call.getArgument(0)).asString());
                                }
                                if (call.getArgument(1) instanceof LambdaExpr) {
                                    parseLambdaExpression((LambdaExpr)call.getArgument(1), endpoint, javadoc.getBlockTags());
                                }
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

    private void parseLambdaExpression(LambdaExpr expr, Endpoint endpoint, List<JavadocBlockTag> tags) {
        Statement e = expr.getBody();
        System.out.println(e.getClass().getSimpleName());

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
