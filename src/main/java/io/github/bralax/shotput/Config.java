package io.github.bralax.shotput;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the config generated from the config file.
 * @author Brandon Lax
 */
public class Config {

    /** The example languages to support. */
    public List<String> languages;

    /** The title of the documentation. */
    public String title;

    /** The html to insert at the top of the documentation. */
    public String intro;

    /** The base url of the documentation. */
    public String baseUrl;

    /** A short description of the documentation. */
    public String description;

    /** Boolean indicating whether the documentation should be interactive. */
    public boolean interactive;

    /** The path to the documentation's log. */
    public String logo;

    public boolean disableMethodParsing;

    /** Basic constructor for a config.
     * 
     * By default languages is an empty list, interactive is false 
     * and everything else is null.
     */
    public Config() {
        this.languages = new ArrayList<>();
    }

    public static Config defaultValue() {
        Config config = new Config();
        config.title = "Documentation";
        config.baseUrl = "http://localhost:7000";
        config.intro = "";
        config.description = "";
        config.interactive = true;
        config.disableMethodParsing = false;
        config.logo = null;
        return config;
    }

    /**
     * Gets the list of languages.
     * @return The list of languages
     */
    public List<String> getLanguages() {
        return this.languages;
    }

    /**
     * Gets the documentation title.
     * @return The documentation title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the documentation intro text.
     * @return The documentation intro text
     */
    public String getIntro() {
        return intro;
    }
    
    /**
     * Gets the api base url.
     * @return The api base url
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the documentation description.
     * @return The documentation description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets whether the documentation is interactive.
     * @return whether the documentation is interactive
     */
    public boolean getInteractive() {
        return interactive;
    }

    /**
     * Gets the logo path.
     * @return the logo path
     */
    public String getLogo() {
        return logo;
    }

    public boolean getDisableMethodParsing() {
        return disableMethodParsing;
    }
}
