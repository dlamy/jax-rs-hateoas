package com.jayway.jaxrs.hateoas;

/**
 * Created by IntelliJ IDEA.
 * User: kallestenflo
 * Date: 4/20/12
 * Time: 9:11 AM
 */
public class LinkableParameterInfo {

    private final String name;
    private final String defaultValue;

    public LinkableParameterInfo(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
