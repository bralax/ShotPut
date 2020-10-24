package org.bralax;

import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import javax.lang.model.element.Element;
import javax.lang.model.SourceVersion;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import com.sun.source.util.DocTrees;
import javax.lang.model.element.ElementKind;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import javax.lang.model.util.ElementScanner9;
import java.io.PrintStream;
import com.sun.source.util.DocTreeScanner;
/**
 * This doclet generates HTML-formatted documentation for the specified modules,
 * packages and types.
 *
 * <h2><a id="user-defined-taglets">User-Defined Taglets</a></h2>
 *
 * The standard doclet supports user-defined {@link Taglet taglets},
 * which can be used to generate customized output for user-defined tags
 * in documentation comments.
 *
 * Taglets invoked by the standard doclet must return strings from
 * {@link Taglet#toString(List,Element) Taglet.toString} as follows:
 *
 * <dl>
 * <dt> <i>Inline Tags</i>
 * <dd> The returned string must be
 *      <a href="https://www.w3.org/TR/html52/dom.html#flow-content">flow content</a>,
 *      or any valid fragment of HTML code that may appear in the body of a document.
 *      There may be additional constraints, depending on how the tag is to be
 *      used in a documentation comment: for example, if the tag may be used
 *      within an inline element such as {@code <b>} or {@code <i>}, the taglet
 *      must not return a string containing block tags, like {@code <h3>} or
 *      {@code <p>}.
 * <dt> <i>Block Tags</i>
 * <dd> The returned string must be suitable content for a definition list,
 *      or {@code <dl>} element. It will typically be a series of pairs
 *      of {@code <dt>} and {@code <dd>} elements.
 * </dl>
 * @endpoint
 * @see <a href="{@docRoot}/../specs/javadoc/doc-comment-spec.html">
 *      Documentation Comment Specification for the Standard Doclet</a>
 */
public class JavalinDoc implements Doclet {

   private DocTrees treeUtils;
   private List<Endpoint> endpoints;

   //private final HtmlDoclet htmlDoclet;

    /**
     * Creates an instance of the standard doclet, used to generate HTML-formatted
     * documentation.
     */
    public JavalinDoc() {
       //htmlDoclet = new HtmlDoclet(this);
    }

    @Override
    public void init(Locale locale, Reporter reporter) {
       //htmlDoclet.init(locale, reporter);
    }

    @Override
    public String getName() {
        return "Javalin";
    }

    @Override
    public Set<? extends Doclet.Option> getSupportedOptions() {
       return new HashSet<>();
       //return htmlDoclet.getSupportedOptions();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
       return SourceVersion.RELEASE_9;
       //return htmlDoclet.getSupportedSourceVersion();
    }

    @Override
    public boolean run(DocletEnvironment docEnv) {
      treeUtils = docEnv.getDocTrees();
      this.endpoints = new ArrayList<>();
      ShowElements se = new ShowElements(System.out);
      se.show(docEnv.getIncludedElements());
      System.out.println(endpoints);
       /*DocTrees trees = docEnv.getDocTrees();
       Elements elements = docEnv.getElementUtils();
       Set<? extends Element> elems = docEnv.getSpecifiedElements();
       for(Element elem: elems) {
         if (elem.getKind().equals(ElementKind.CLASS)) {
            ClassTree tree = (ClassTree) trees.getTree(elem);
            for (Tree member: tree.getMembers()) {
               System.out.println(tree.getKind());
            }
            
            /*DocCommentTree tree = trees.getDocCommentTree(elem);
            List<? extends DocTree> blocks = tree.getBlockTags();
            for (DocTree block : blocks) {
               if (block.getKind().equals(DocTree.Kind.UNKNOWN_BLOCK_TAG)) {
                  UnknownBlockTagTree unknown = (UnknownBlockTagTree) block;
                  System.out.println(unknown.getKind() + "\t" + unknown.getTagName());

               }
            }/
         } else {
            System.out.println(elem.getSimpleName() + "\t'" + elem.getKind());
         }
       }*/
       return true;
       //return htmlDoclet.run(docEnv);
    }
    
     /**
     * A scanner to display the structure of a series of elements
     * and their documentation comments.
     */
    class ShowElements extends ElementScanner9<Void, Integer> {
        final PrintStream out;
 
        ShowElements(PrintStream out) {
            this.out = out;
        }
 
        void show(Set<? extends Element> elements) {
            scan(elements, 0);
        }
 
        @Override
        public Void scan(Element e, Integer depth) {
            DocCommentTree dcTree = treeUtils.getDocCommentTree(e);
            if (dcTree != null && e.getKind().equals(ElementKind.METHOD)) {
                List<? extends DocTree> blocks = dcTree.getBlockTags();
                if (this.isEndpoint(blocks)) {
                  Endpoint endpoint = new Endpoint();
                  this.processEndpoint(blocks, endpoint);
                  if (!endpoints.contains(endpoint)) {
                     endpoints.add(endpoint);
                  }
                  //out.println(e.getKind() + "\t" + e.getKind().equals(ElementKind.METHOD));
                }
            }
            return super.scan(e, depth + 1);
        }
        
        private boolean isEndpoint(List<? extends DocTree> blocks) {
            for (DocTree tree: blocks) {
               if (tree.getKind().equals(DocTree.Kind.UNKNOWN_BLOCK_TAG)) {
                  UnknownBlockTagTree unknown = (UnknownBlockTagTree) tree;
                  if (unknown.getTagName().equals("endpoint")) {
                     return true;
                  }
               }
            }
            return false;
        }
        
        private void processEndpoint(List<? extends DocTree> blocks, Endpoint endpoint) {
            for (DocTree tree: blocks) {
               if (tree.getKind().equals(DocTree.Kind.UNKNOWN_BLOCK_TAG)) {
                  UnknownBlockTagTree unknown = (UnknownBlockTagTree) tree;
                  String content = unknown.getContent().toString();
                  Parameter param;
                  switch(unknown.getTagName().toLowerCase()) {
                     case "endpointtype":
                        endpoint.setType(content);
                        break;
                     case "endpoint":
                        endpoint.setEndpoint(content);
                        break;
                     case "endpointpathparam":
                        param = new Parameter(content.substring(0,content.indexOf(" ")), content.substring(content.indexOf(" ")));
                        endpoint.addPathParam(param);
                        break;
                     case "endpointqueryparam":
                        param = new Parameter(content.substring(0,content.indexOf(" ")), content.substring(content.indexOf(" ")));
                        endpoint.addQueryParam(param);
                        break; 
                     case "endpointresponsetype":
                        endpoint.setResponseType(content);
                        break;
                     case "endpointrequestheader":
                        param = new Parameter(content.substring(0,content.indexOf(" ")), content.substring(content.indexOf(" ")));
                        endpoint.addHeaderParam(param);
                        break; 
                     case "endpointresponsestatus":
                        break;          
                  }
               }
            }
        }
    }
 
    /**
     * A scanner to display the structure of a documentation comment.
     **/
    class ShowDocTrees extends DocTreeScanner<Void, Integer> {
        final PrintStream out;
 
        ShowDocTrees(PrintStream out) {
            this.out = out;
        }
 
        @Override
        public Void scan(DocTree t, Integer depth) {
            //if (t.getKind().equals(ElementKind.METHOD)) {
               out.println(t.getKind() + "\t" + t);
               return super.scan(t, depth + 1);
            //}
        }
    }
}
