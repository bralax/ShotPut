package io.github.bralax.shotput.markdown;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.*;

public class CopyVisitor extends AbstractVisitor {
    private Node document;
    public CopyVisitor(Node document) {
        this.document = document;
    }
    @Override
    public void visit(BlockQuote blockQuote) {
       this.document.appendChild(blockQuote);
    }
    @Override
    public void visit(BulletList bulletList) {
        this.document.appendChild(bulletList);
    }
    @Override
    public void visit(Code code) {
        this.document.appendChild(code);
    }
    @Override
    public void visit(CustomBlock customBlock) {
        this.document.appendChild(customBlock);
    }
    @Override
    public void visit(CustomNode customNode) {
        this.document.appendChild(customNode);
    }
    @Override
    public void visit(Document document) {
        this.visitChildren(document);
    }
    @Override
    public void visit(Emphasis emphasis) {
        this.document.appendChild(emphasis);
    }
    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        this.document.appendChild(fencedCodeBlock);
    }
    @Override
    public void visit(HardLineBreak hardLineBreak) {
        this.document.appendChild(hardLineBreak);
    }
    @Override
    public void visit(Heading heading) {
        this.document.appendChild(heading);
    }
    @Override
    public void visit(HtmlBlock htmlBlock) {
        this.document.appendChild(htmlBlock);
    }
    @Override
    public void visit(HtmlInline htmlInline) {
        this.document.appendChild(htmlInline);
    }
    @Override
    public void visit(Image image) {
        this.document.appendChild(image);
    }
    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        this.document.appendChild(indentedCodeBlock);
    }
    @Override
    public void visit(Link link) {
        this.document.appendChild(link);
    }
    @Override
    public void visit(LinkReferenceDefinition linkReferenceDefinition) {
        this.document.appendChild(linkReferenceDefinition);
    }
    @Override
    public void visit(ListItem listItem) {
        this.document.appendChild(listItem);
    }
    @Override
    public void visit(OrderedList orderedList) {
        this.document.appendChild(orderedList);
    }
    @Override
    public void visit(Paragraph paragraph) {
        this.document.appendChild(paragraph);
    }
    @Override
    public void visit(SoftLineBreak softLineBreak) {
        this.document.appendChild(softLineBreak);
    }
    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        this.document.appendChild(strongEmphasis);
    }
    @Override
    public void visit(Text text) {
        this.document.appendChild(text);
    }
    @Override
    public void visit(ThematicBreak thematicBreak) {
        this.document.appendChild(thematicBreak);
    }
}
