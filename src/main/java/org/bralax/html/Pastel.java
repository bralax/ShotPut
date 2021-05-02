package org.bralax.html;

import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Map.entry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.bralax.markdown.CopyVisitor;
import org.commonmark.Extension;
import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
 

public class Pastel {
    public static Map<String, Object> defaultMetadata = Map.ofEntries(
        entry("title", "API Documentation"),
        entry("language_tabs", new ArrayList<String>()),
        entry("toc_footers", new ArrayList<String>()),
        entry("logo", Boolean.valueOf(false)),
        entry("includes", new ArrayList<String>()),
        entry("last_updated", "")
        );
    
/**
     * Generate the API documentation using the markdown and include files
     */
    public void generate(String sourceFolder, String destinationFolder, Map<String, Object> metadataOverrides) {
      
        String sourceMarkdownFilePath;
        File source = new File(sourceFolder);
        if (sourceFolder.endsWith(".md")) {
            // We're given just the path to a file, we'll use default assets
            sourceMarkdownFilePath = source.getAbsolutePath();
            sourceFolder = source.getParent();///dirname($sourceMarkdownFilePath);
        } else {
            if (!source.isDirectory()) {
                throw new IllegalArgumentException("Source folder sourceFolder is not a directory.");
            }

            // Valid source directory
            sourceMarkdownFilePath = sourceFolder + "/index.md";
        }

        if (destinationFolder == null) {
            // If no destination is supplied, place it in the source folder
            destinationFolder = sourceFolder;
        }
        try {
            String content = Files.readString(Path.of(sourceMarkdownFilePath));
            List<Extension> extensions = Arrays.asList(YamlFrontMatterExtension.create());
            Parser parser = Parser.builder().extensions(extensions).build();
            Node document = parser.parse(content);
            Node firstChild = document.getFirstChild();
            Map<String, Object> frontMatterData = new HashMap<>();
            if (firstChild instanceof YamlFrontMatterBlock) {
                YamlFrontMatterBlock frontmatter = (YamlFrontMatterBlock) firstChild;
                //handle the frontmatter
                YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
                frontmatter.accept(visitor);
                Map<String, List<String>> frontData = visitor.getData();
                List<String> filePathsToInclude = new ArrayList<>();
                for (Entry<String, List<String>> item: frontData.entrySet()) {
                    List<String> values = item.getValue();
                    if (values.size() == 1) {
                        frontMatterData.put(item.getKey(), values.get(0));
                    } else {
                        frontMatterData.put(item.getKey(), values);
                    }
                }
                if (frontData.containsKey("includes")) {//isset($frontmatter['includes'])) {
                    for (String include: frontData.get("includes")) {
                        filePathsToInclude.add(sourceFolder.trim() + "/" + include.trim());
                    }
                    for (String filename: filePathsToInclude) {
                        Path path = Path.of(filename);
                        if (!path.toFile().exists()) {
                            System.out.println("Include file " + filename + " not found.");
                        } else if (path.toFile().isDirectory()) {
                            for( File f: path.toFile().listFiles()) {
                                if (f.isFile() && f.getName().endsWith(".md")) {
                                    Node item = parser.parse(Files.readString(f.toPath()));
                                    item.accept(new CopyVisitor(document));
                                } 
                            }
                        } else {
                            Node item = parser.parse(Files.readString(path));
                            item.accept(new CopyVisitor(document));
                        }
                    }
                }
                if (!frontData.containsKey("last_updated")) {
                    // Set last_updated to most recent time main or include files was modified
                    long time = 0l;
                    for (String file: filePathsToInclude) {
                        Path path = Path.of(file);
                        long timeModified = path.toFile().lastModified();
                        if (timeModified > time) {
                            time = timeModified;
                        }
                    }
                    long mTime = Path.of(sourceMarkdownFilePath).toFile().lastModified(); 
                    time = mTime > time ? mTime : time;
                    Date date = new Date(time);
                    SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
                    String dateText = df2.format(date);
                    frontMatterData.put("last_updated", dateText);
                }
            }
            HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
            String htmlContent = renderer.render(document);
            Map<String, Object> metadata = this.getPageMetadata(frontMatterData, metadataOverrides);
            System.out.println(metadata);
            VelocityEngine engine = new VelocityEngine();
            engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
            engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            engine.init();
            Template t = engine.getTemplate("views/index.vtl");
            VelocityContext context = new VelocityContext();
            PastelUtil utils = new PastelUtil();
            context.put("page", metadata);
            context.put("util", utils);
            context.put("content", htmlContent);
            StringWriter writer = new StringWriter();
            t.merge(context, writer);
            File dest = new File(destinationFolder);
            if (!dest.exists()) {
                dest.mkdirs();
            }
            BufferedWriter indexWriter = new BufferedWriter(new FileWriter(dest.getAbsolutePath() + "/index.html"));
            indexWriter.write(writer.toString());
            
            indexWriter.close();
            copyRecursively("css/", destinationFolder);
            copyRecursively("js/", destinationFolder);
            copyRecursively("fonts/", destinationFolder);
            copyRecursively("images/", destinationFolder);
        } catch (IOException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            System.out.println("Failed to generate Documentation");
        }
    }



    protected Map<String, Object> getPageMetadata(Map<String, Object> frontmatter, Map<String, Object> metadataOverrides)
    {
        Map<String, Object> metadata = new HashMap<>(Pastel.defaultMetadata);//Pastel.defaultMetadata.clone();//Pastel::$defaultMetadata;

        for (Entry<String, Object> row : metadata.entrySet()) {
             // Override default with values from front matter
            String key = row.getKey();
            if (frontmatter != null && frontmatter.containsKey(key)) {
                metadata.put(key, frontmatter.get(key));
            }
            // And override that with values from config
            if (metadataOverrides != null && metadataOverrides.containsKey(key)) {
                metadata.put(key, metadataOverrides.get(key));
            }
        }
        return metadata;
    }

    private boolean copyRecursively(String src, String dest) {
        Path destinationDir = Paths.get(dest);
        URL dirURL = getClass().getClassLoader().getResource(src);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            // Traverse the file tree and copy each file/directory.
            try {
                for (File f: new File(dirURL.toURI()).listFiles()) {
                    if (f.isFile()) {
                        Path targetPath = destinationDir.resolve(src + f.getName()).toAbsolutePath();
                        targetPath.toFile().mkdirs();
                        Files.copy(new FileInputStream(f), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        copyRecursively(src + f.getName(), dest);
                    }
                }
            } catch (IOException | URISyntaxException ex) {
                return false;
            }
            return true;
        } 
  
        if (dirURL == null) {
          /* 
           * In case of a jar file, we can't actually find a directory.
           * Have to assume the same jar as clazz.
           */
          String me = getClass().getName().replace(".", "/")+".class";
          dirURL = getClass().getClassLoader().getResource(me);
        }
        
        if (dirURL.getProtocol().equals("jar")) {
          /* A JAR path */
            try {
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
                JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
                Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                while(entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String name = jarEntry.getName();
                    if (name.startsWith(src)) { //filter according to the path
                        Path targetPath = destinationDir.resolve(name).toAbsolutePath();
                        if (name.equals(src)) {
                            targetPath.toFile().mkdirs();
                        } else {
                            jar.getInputStream(jarEntry).transferTo(new FileOutputStream(targetPath.toFile()));
                        }
                    }
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        } 
        return false;
        
    }

    public static void main(String[] args) {
        Pastel pastel = new Pastel();
        pastel.generate("index.md", "docs/", null);
    }
}