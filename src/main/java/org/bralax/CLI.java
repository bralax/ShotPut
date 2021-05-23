package org.bralax;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CLI {
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        //options.addOption(Option.builder("s").longOpt("css").desc("The css file to be used on the html").hasArg().required(false).build());
        options.addOption(Option.builder("cp").longOpt("classpath").desc("The files to parse (Required)").hasArg().required(true).build());
        options.addOption(Option.builder("c").longOpt("config").desc("A Configuration file path").hasArg().required(false).build());
        options.addOption(Option.builder("o").longOpt("outdir").desc("The place to put the generated files (Required)").hasArg().required(true).build());
        options.addOption(Option.builder("x").longOpt("excel").desc("Flag to generate excel file. If this flag and -h are not set both will be generated").required(false).build());
        options.addOption(Option.builder("s").longOpt("swagger").desc("Flag to generate swagger/openapi documentation.").required(false).build());
        options.addOption(Option.builder("h").longOpt("html").desc("Flag to generate html file. If this flag and -h are not set both will be generated").required(false).build());
        options.addOption(Option.builder("?").longOpt("help").desc("Print the Help Menu").required(false).build());
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("javalin-doc",options);
            } else {
                String outDir = commandLine.getOptionValue("o");
                String classPath = commandLine.getOptionValue("cp");
                String config = commandLine.getOptionValue("c", null);
                Config configuration;
                if (config == null) {
                    configuration = Config.defaultValue();
                } else {
                    configuration = ConfigParser.parseConfig(config);
                }
                //String css = commandLine.getOptionValue("s", "test.css");
                boolean excel = commandLine.hasOption("x");
                boolean html = commandLine.hasOption("h");
                boolean swagger = commandLine.hasOption("s");
                File file = new File(classPath);
                File out = new File(outDir);
                if (file.exists() && out.exists()) {
                    if (out.isDirectory()) {
                        new Shotput(configuration, file, excel, html, swagger, out).start();
                    } else {
                        System.out.println("The output directory must be a folder");
                    }
                }
                else {
                    System.out.println("The classpath and output directory have to be valid locations");
                }
            }
        } catch (org.apache.commons.cli.ParseException exception) {
            
            System.err.println(exception.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("javalin-doc",options);
        }
        
        
    }
}
