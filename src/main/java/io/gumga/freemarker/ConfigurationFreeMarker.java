package io.gumga.freemarker;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.IOException;

public class ConfigurationFreeMarker {

    private Configuration config;

    public ConfigurationFreeMarker() {
        this.config = new Configuration(Configuration.VERSION_2_3_23);
        config.setClassForTemplateLoading(this.getClass(), "/template");
        this.config.setDefaultEncoding("UTF-8");
        this.config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public Configuration getConfiguration() {
        return this.config;
    }

}
