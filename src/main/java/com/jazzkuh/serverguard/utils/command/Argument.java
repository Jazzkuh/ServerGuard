package com.jazzkuh.serverguard.utils.command;

import lombok.Getter;

public class Argument {
    private final @Getter String arguments;
    private final @Getter String description;
    private final @Getter String permission;

    public Argument(String arguments, String description, String permission) {
        this.arguments = arguments;
        this.description = description;
        this.permission = permission;
    }

    public Argument(String arguments, String description) {
        this.arguments = arguments;
        this.description = description;
        this.permission = null;
    }
}