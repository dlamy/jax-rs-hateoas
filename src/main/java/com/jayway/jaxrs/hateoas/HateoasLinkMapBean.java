package com.jayway.jaxrs.hateoas;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: davelamy
 */
public interface HateoasLinkMapBean {

    Map<String, Map<String, Object>> getLinks();

    void setLinks(Map<String, Map<String, Object>> links);

}
