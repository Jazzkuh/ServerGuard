package com.jazzkuh.serverguard.framework;

import lombok.Data;

import java.util.List;

@Data
public class PluginInformation {
    private String name;
    private List<String> authors;
    private String reason;
    private PluginStatus status;

    public PluginInformation(String name, List<String> authors, String reason, PluginStatus status) {
        this.name = name;
        this.authors = authors;
        this.reason = reason;
        this.status = status;
    }
}
