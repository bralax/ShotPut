package org.bralax.parser;

import java.util.List;

import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;

public class ParserHelpers {
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
