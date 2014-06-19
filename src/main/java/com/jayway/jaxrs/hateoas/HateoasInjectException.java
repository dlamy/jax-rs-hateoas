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

/**
 * Exception thrown when something goes wrong injecting links into an entity.
 *
 * @author Mattias Hellborg Arthursson
 * @author Kalle Stenflo
 */
public class HateoasInjectException extends RuntimeException {

	private static final long serialVersionUID = -7586666921228435121L;

	public HateoasInjectException(String message, Throwable cause) {
		super(message, cause);
	}

	public HateoasInjectException(String message) {
        super(message);
    }

    public HateoasInjectException(Exception e) {
		super(e);
	}

}
