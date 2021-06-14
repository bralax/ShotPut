package io.github.bralax.shotput.parser;

import java.util.List;

import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * Class containing a number of helpers used throughout the code parsing.
 * @author Brandon Lax
 */
public class ParserHelpers {
    /**
     * Method to get a tag from the list by type.
     * @param tags The list of javadoc tags
     * @param type The type of the tag to find
     * @return The javadoc tag, if found
     */
    public static JavadocBlockTag getCommentTag(List<JavadocBlockTag> tags, String type) {
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

    /**
     * Method to get a tag from the list by type and parameter.
     * @param tags The list of javadoc tags
     * @param type The type of the tag to find
     * @param param The parameter attached to the tag
     * @return The javadoc tag, if found
     */
    public static JavadocBlockTag getCommentTag(List<JavadocBlockTag> tags, String type, String param) {
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

    /**
     * Small helper to resolve a symbol.
     * Mostly used to verify if a value is of type context.
     * @param expr The expression to verify the type of.
     * @return The type of the expression, if found 
     */
    public static String getSymbol(NameExpr expr) {
        try {
            ResolvedType type = expr.calculateResolvedType();
            return type.describe();
        } catch (UnsolvedSymbolException exception) {
            return exception.toString();
        } catch (RuntimeException exception) {
            return "";
        }
    }
}
