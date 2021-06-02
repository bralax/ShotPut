package io.github.bralax.shotput;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public List<String> languages;
    public String title;
    public String intro;
    public String baseUrl;
    public String description;
    public boolean interactive;
    public String logo;

    public Config() {
        this.languages = new ArrayList<>();
    }

    public static Config defaultValue() {
        Config config = new Config();
        config.title = "Documentation";
        config.baseUrl = "http://localhost:3000";
        config.intro = "";
        config.description = "";
        config.interactive = true;
        config.logo = null;
        return config;
    }

    public List<String> getLanguages() {
        return this.languages;
    }

    public String getTitle() {
        return title;
    }

    public String getIntro() {
        return intro;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }

    public String getDescription() {
        return description;
    }

    public boolean getInteractive() {
        return interactive;
    }

    public String getLogo() {
        return logo;
    }
}
