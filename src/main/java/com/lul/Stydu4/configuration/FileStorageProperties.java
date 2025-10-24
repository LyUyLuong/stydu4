package com.lul.Stydu4.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "file")
@Data
public class FileStorageProperties {

    private Storage storage = new Storage();
    private Upload upload = new Upload();

    @Data
    public static class Storage {
        private String location = "D:/toeic-storage";
    }

    @Data
    public static class Upload {
        private FileConfig images = new FileConfig();
        private FileConfig audio = new FileConfig();
    }

    @Data
    public static class FileConfig {
        private List<String> allowedExtensions;
        private String maxSize;
    }
}