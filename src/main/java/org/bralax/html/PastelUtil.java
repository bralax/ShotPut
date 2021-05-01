package org.bralax.html;

public class PastelUtil {
    public String getCSSLinkTag(String name, String media) {
        media = media == null ? "" : media;
        return "<link rel=\"stylesheet\" href=\"css/"+name+".css\" media=\""+media+"\" />";
    }

    public String getJSScriptTag(String name) {
        return "<script src=\"js/"+name+".js\"></script>";
    }

    public String getImageTag(String path, String className) {
        className = className == null ? "" : className;
        return "<img src=\""+path+"\" alt=\""+className+"-image\" class=\""+className+"\"/>";
    }

    public String arrayToString(String[] arr) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < arr.length; i++) {
            builder.append('"')
                   .append(arr[i])
                   .append('"');
            if (i != arr.length - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public String dollarSign() {
        return "$";
    }
}
