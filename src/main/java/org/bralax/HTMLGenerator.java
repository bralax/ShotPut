package org.bralax;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import org.bralax.html.*;


public class HTMLGenerator {
    

    public static void generateHTML(File css, File out, List<Endpoint> endpoints) throws IOException {
        endpoints.sort(new EndpointComparator());
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
        writeSummary(endpoints, tree);
        writeDetail(endpoints, tree);
        return tree;
    }

    private static void writeSummary(List<Endpoint> endpoints, HtmlTree tree) {
        HtmlTree summary = new HtmlTree(TagName.DIV).setStyle(HtmlStyle.summary);
        HtmlTree blockList = new HtmlTree(TagName.DIV).setStyle(HtmlStyle.block);
        blockList.add(new HtmlTree(TagName.H3).add("Endpoint Summary"));
        blockList.add(generateSummaryTable(endpoints));
        summary.add(new HtmlTree(TagName.DIV).setStyle(HtmlStyle.blockList).add(blockList));
        tree.add(summary);
    }

    private static HtmlTree generateSummaryTable(List<Endpoint> endpoints) {
        HtmlTree table = new HtmlTree(TagName.TABLE).addStyle(HtmlStyle.summaryTable).put(HtmlAttr.SUMMARY, "List all the endpoints in this application");
        HtmlTree body = new HtmlTree(TagName.TBODY);
        boolean even = false;
        for (Endpoint endpoint : endpoints) {
            HtmlTree row = new HtmlTree(TagName.TR).addStyle(even ? HtmlStyle.evenRowColor : HtmlStyle.oddRowColor);
            row.add(HtmlTree.TD(HtmlStyle.colFirst, new HtmlTree(TagName.A).put(HtmlAttr.HREF, "#" + endpoint.getType().toLowerCase() + "-" + endpoint.getEndpoint()).add(new StringContent(endpoint.getType()))));
            row.add(HtmlTree.TD(HtmlStyle.colSecond, new HtmlTree(TagName.A).put(HtmlAttr.HREF, "#" + endpoint.getType().toLowerCase() + "-" + endpoint.getEndpoint()).add(new StringContent(endpoint.getEndpoint()))));
            even = !even;
            body.add(row);
        }
        table.add(body);
        return table;
    }

    private static void writeDetail(List<Endpoint> endpoints, HtmlTree tree) {
        HtmlTree details = HtmlTree.DIV(HtmlStyle.details);
        HtmlTree blockList = new HtmlTree(TagName.DIV).setStyle(HtmlStyle.block);
        blockList.add(new HtmlTree(TagName.H3).add("Endpoint Details"));
        for (Endpoint endpoint: endpoints) {
            String link = endpoint.getType().toLowerCase() + "-" + endpoint.getEndpoint();
            HtmlTree a = new HtmlTree(TagName.A).put(HtmlAttr.NAME, link).put(HtmlAttr.ID, link);
            blockList.add(a);
            blockList.add(generateEndpointDetails(endpoint));
        }
        details.add(new HtmlTree(TagName.DIV).setStyle(HtmlStyle.blockList).add(blockList));
        tree.add(details);
    }  

    private static HtmlTree generateEndpointDetails(Endpoint endpoint) {
        HtmlTree endpointBox = HtmlTree.DIV(HtmlStyle.endpointBlock);
        endpointBox.add(new HtmlTree(TagName.H4).add(endpoint.getEndpoint()));
        endpointBox.add(new HtmlTree(TagName.PRE).add(endpoint.getType() + " " + endpoint.getEndpoint()));
        HtmlTree description = new HtmlTree(TagName.DIV).setStyle(HtmlStyle.description);
        Scanner scn = new Scanner(endpoint.getDescription());
        while(scn.hasNextLine()) {
            description.add(HtmlTree.P(new StringContent(scn.nextLine())));
        }
        endpointBox.add(description);
        HtmlTree descriptionList = HtmlTree.DL(HtmlStyle.detailsList);
        descriptionList.add(HtmlTree.DT(new StringContent("Response Type:")));
        descriptionList.add(HtmlTree.DD(new StringContent(endpoint.getResponseType())));
        if (endpoint.pathParamLength() > 0) {
            descriptionList.add(HtmlTree.DT(new StringContent("Path Parameters:")));
            for (int i = 0; i < endpoint.pathParamLength(); i++) {
                Parameter param = endpoint.pathParam(i);
                descriptionList.add(HtmlTree.DD(new HtmlTree(TagName.CODE).add(param.getName())).add(" - " + param.getDescription()));
            }
        }
        if (endpoint.queryParamLength() > 0) {
            descriptionList.add(HtmlTree.DT(new StringContent("Query Parameters:")));
            for (int i = 0; i < endpoint.queryParamLength(); i++) {
                Parameter param = endpoint.queryParam(i);
                descriptionList.add(HtmlTree.DD(new HtmlTree(TagName.CODE).add(param.getName())).add(" - " + param.getDescription()));
            }
        }
        if (endpoint.formParamLength() > 0) {
            descriptionList.add(HtmlTree.DT(new StringContent("Form Parameters:")));
            for (int i = 0; i < endpoint.formParamLength(); i++) {
                Parameter param = endpoint.formParam(i);
                descriptionList.add(HtmlTree.DD(new HtmlTree(TagName.CODE).add(param.getName())).add(" - " + param.getDescription()));
            }
        }
        if (endpoint.headerParamLength() > 0) {
            descriptionList.add(HtmlTree.DT(new StringContent("Header Parameters:")));
            for (int i = 0; i < endpoint.headerParamLength(); i++) {
                Parameter param = endpoint.headerParam(i);
                descriptionList.add(HtmlTree.DD(new HtmlTree(TagName.CODE).add(param.getName())).add(" - " + param.getDescription()));
            }
        }

        if (endpoint.responseHeaderLength() > 0) {
            descriptionList.add(HtmlTree.DT(new StringContent("Response Headers:")));
            for (int i = 0; i < endpoint.responseHeaderLength(); i++) {
                Parameter param = endpoint.responseHeader(i);
                descriptionList.add(HtmlTree.DD(new HtmlTree(TagName.CODE).add(param.getName())).add(" - " + param.getDescription()));
            }
        }

        if (endpoint.responseStatusLength() > 0) {
            descriptionList.add(HtmlTree.DT(new StringContent("Response Statuses:")));
            for (int i = 0; i < endpoint.responseStatusLength(); i++) {
                Parameter param = endpoint.responseStatus(i);
                descriptionList.add(HtmlTree.DD(new HtmlTree(TagName.CODE).add(param.getName())).add(" - " + param.getDescription()));
            }
        }


        endpointBox.add(descriptionList);
        return endpointBox;
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

    private static class EndpointComparator implements Comparator<Endpoint> {
        @Override
        public int compare(Endpoint a, Endpoint b) {
            return a.getEndpoint().compareTo(b.getEndpoint());
        }
    }
}
