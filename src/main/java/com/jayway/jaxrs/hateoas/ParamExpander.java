package com.jayway.jaxrs.hateoas;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Created by IntelliJ IDEA.
 * User: kallestenflo
 * Date: 4/26/12
 * Time: 8:39 AM
 */
public class ParamExpander {

    ParamExpander() {
    }

    /**
     * Creates a path parameter based on the field value of an entity (determined by context)
     * that can be used to populate the URI template
     *
     * @param fieldName fieldName to extract from entity
     * @return value of field
     */
    public static ParamExpander field(String fieldName) {
        return new ReflectionPathParamExpander(fieldName);
    }

    /**
     * Creates a path parameter with the given value that can be used to populate the URI template
     *
     * @param value the value to add to the path
     * @return the path parameter value
     */
    public static ParamExpander value(Object value) {
        return new PathParamExpander(value);
    }

    /**
     * Creates a query parameter
     *
     * @param name  parameter name
     * @param value parameter value
     * @return the query parameter
     */
    public static ParamExpander queryParam(String name, Object value) {
        return new QueryParamExpander(name, value);
    }


    public static class PathParamExpander extends ParamExpander {
        private final Object value;

        PathParamExpander(Object value) {
            notNull(value, "Path parameter value can not be null");
            this.value = value;
        }
        public Object getValue() {
            return value;
        }
    }
    public static class ReflectionPathParamExpander extends ParamExpander {
        private final String field;

        ReflectionPathParamExpander(String field) {
            notEmpty(field, "Reflection path parameter field can not be empty");
            this.field = field;
        }
        public String getField() {
            return field;
        }
    }
    public static class QueryParamExpander extends ParamExpander {
        private String name;
        private Object value;

        public QueryParamExpander(String name, Object value) {
            notEmpty(name, "Query parameter name can not be empty");
            notNull(value, "Query parameter value can not be null");
            this.name = name;
            this.value = value;
        }
        public String getName() {
            return name;
        }
        public Object getValue() {
            return value;
        }
        public static QueryParamExpander queryParam(String name, Object value) {
            return new QueryParamExpander(name, value);
        }
    }
}
