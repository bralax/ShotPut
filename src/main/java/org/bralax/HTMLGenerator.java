package org.bralax;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


import org.bralax.html.*;

public class HTMLGenerator {
    

    public static void generateHTML(File css, File out, List<Endpoint> endpoints) throws IOException {
        File htmlFolder = new File(out, "html/");
        htmlFolder.mkdir();
        InputStream source;
        if (!css.exists()) {
            source = HTMLGenerator.class.getResourceAsStream("/styles.css");
        } else {
            source = new FileInputStream(css);
        }
        FileOutputStream outputStream = new FileOutputStream(new File(htmlFolder, "styles.css"));
        copyStream(source, outputStream);
        HtmlTree tree = new HtmlTree(TagName.HTML);
        tree.add(writeHead());
        tree.add(writeBody(endpoints));
        FileWriter writer = new FileWriter(new File(htmlFolder, "index.html"));
        tree.write(writer, false);
        writer.close();
    }

    private static HtmlTree writeHead() {
        HtmlTree tree = new HtmlTree(TagName.HEAD);
        tree.add(new HtmlTree(TagName.TITLE).add("Endpoint Documentation"));
        tree.add(new HtmlTree(TagName.LINK).put(HtmlAttr.HREF, "./styles.css").put(HtmlAttr.REL, "stylesheet").put(HtmlAttr.TYPE, "text/css"));
        return tree;
    }

    private static HtmlTree writeBody(List<Endpoint> endpoints) {
        HtmlTree tree = new HtmlTree(TagName.BODY);
        return tree;
    }


    private static void copyStream(InputStream source, OutputStream outputStream) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) > 0) {
            outputStream.write(buf, 0, length);
        }
        source.close();
        outputStream.close();
    }
}
