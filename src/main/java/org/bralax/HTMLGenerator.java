package org.bralax;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        FileOutputStream outputStream = new FileOutputStream(new File(htmlFolder, "style.css"));
        copyStream(source, outputStream);
        HtmlTree tree = new HtmlTree(TagName.HTML);
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
