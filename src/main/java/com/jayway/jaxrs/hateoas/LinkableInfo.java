/*
 * Copyright 2011 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jayway.jaxrs.hateoas;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Holds all data regarding a Linkable method. Not intended for external use.
 *
 * @author Mattias Hellborg Arthursson
 * @author Kalle Stenflo
 */
public final class LinkableInfo {
	private final String id;
	private final String methodPath;
	private final String[] produces;
	private final String httpMethod;
	private final String description;
	private final Class<?> templateClass;
	private final String[] consumes;
	private final String label;
    private final LinkableParameterInfo[] parameterInfo;

    public LinkableInfo(String id, String methodPath,
                           String httpMethod, String[] consumes, String[] produces,
                           String label, String description, Class<?> templateClass) {
        this(id, methodPath, httpMethod, consumes, produces, label, description, templateClass, new LinkableParameterInfo[0]);

    }

	public LinkableInfo(String id, String methodPath,
                        String httpMethod, String[] consumes, String[] produces,
                        String label, String description, Class<?> templateClass, LinkableParameterInfo[] parameterInfo) {
		this.id = id;
		this.methodPath = methodPath;
		this.httpMethod = httpMethod;
		this.consumes = consumes;
		this.produces = produces;
		this.label = label;
		this.description = description;
		this.templateClass = templateClass;
        this.parameterInfo = parameterInfo;
	}

	public Class<?> getTemplateClass() {
		return templateClass;
	}

	public String getDescription() {
		return description;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public String[] getConsumes() {
		return consumes;
	}

	public String[] getProduces() {
		return produces;
	}

	public String getMethodPath() {
		return methodPath;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

    public LinkableParameterInfo[] getParameterInfo() {
        return parameterInfo;
    }

    @Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}