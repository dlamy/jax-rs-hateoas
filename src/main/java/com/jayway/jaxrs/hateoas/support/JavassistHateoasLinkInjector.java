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
package com.jayway.jaxrs.hateoas.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jaxrs.hateoas.HateoasInjectException;
import com.jayway.jaxrs.hateoas.HateoasLinkInjector;
import com.jayway.jaxrs.hateoas.HateoasVerbosity;
import com.jayway.jaxrs.hateoas.LinkProducer;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HateoasLinkInjector} implementation that uses javassist to dynamically add a field in the target entities
 * where the links can be injected. This enables usage of the framework without <b>any</b> impact on the actual DTOs.
 *
 * @author Mattias Hellborg Arthursson
 * @author Kalle Stenflo
 */
public class JavassistHateoasLinkInjector implements HateoasLinkInjector<Object> {

    private static final Logger log = LoggerFactory.getLogger(JavassistHateoasLinkInjector.class);

    private static final ClassPool CLASS_POOL = ClassPool.getDefault();

    private final static Map<String, Class<?>> TRANSFORMED_CLASSES = new HashMap<String, Class<?>>();

    static {
        CLASS_POOL.appendClassPath(new LoaderClassPath(
                JavassistHateoasLinkInjector.class.getClassLoader()));
    }

    private HateoasLinkInjector<Object> arrayInjector = new HateoasLinkBeanLinkInjector();

    private final boolean mapStructureEnabled;
    private HateoasLinkInjector<Object> mapInjector;

    public JavassistHateoasLinkInjector(boolean mapStructureEnabled) {
        this.mapStructureEnabled = mapStructureEnabled;
        mapInjector = new HateoasLinkMapBeanLinkInjector();
    }

    @Override
    public boolean canInject(Object entity) {
        return true;
    }

    @Override
    public Object injectLinks(Object entity, LinkProducer<Object> linkProducer,
                              final HateoasVerbosity verbosity) {

        if (entity == null) {
            return null;
        }

        String newClassName = entity.getClass().getPackage().getName() + "." + entity.getClass().getSimpleName() + "_generated";

        Class<?> clazz;
        if (!TRANSFORMED_CLASSES.containsKey(newClassName)) {
            synchronized (this) {
                try {
                    log.debug("Creating HATEOAS subclass for DTO : {}", entity.getClass());

	                // find the largest constructor
	                Constructor<?> largestConstructor = null;
	                for (Constructor<?> thisConstructor : entity.getClass().getDeclaredConstructors()) {
		                largestConstructor = largestConstructor == null ? thisConstructor : 
				                thisConstructor.getParameterTypes().length > largestConstructor.getParameterTypes().length ? thisConstructor : largestConstructor;
	                }

	                if (largestConstructor == null) {
		                throw new IllegalStateException("Unable to locate any valid constructors");
	                }

	                // save off the param types for the largest constructor
	                List<Class<?>> paramTypes = new ArrayList<>();
	                Collections.addAll(paramTypes, largestConstructor.getParameterTypes());
	                
                    CtClass newClass = CLASS_POOL.makeClass(newClassName);
                    newClass.setSuperclass(CLASS_POOL.get(entity.getClass().getName()));
                    CtConstructor ctConstructor = new CtConstructor(new CtClass[0], newClass);

	                // iterate through all the param types and construct a super() call
	                StringBuilder constructorBody = new StringBuilder("super(");
					for (int i = 0; i < paramTypes.size(); i++) {
						Class<?> paramType = paramTypes.get(i);
						if (paramType.isPrimitive()) {
							if (paramType.getName().equals("boolean")) {
								constructorBody.append("false");
							} else {
								constructorBody.append("(").append(paramType.getName()).append(")").append("0");
							}
						} else {
							constructorBody.append("null");
						}
						if (i < paramTypes.size() - 1) {
							constructorBody.append(", ");
						}
					}
	                constructorBody.append(");");
                    ctConstructor.setBody(constructorBody.toString());
                    newClass.addConstructor(ctConstructor);

                    if (mapStructureEnabled) {
                        addMapMembers(newClass);
                    }
                    else {
                        addCollectionMembers(newClass);
                    }

                    StringBuilder cloneMethodBody = new StringBuilder();

                    for (Field field : ReflectionUtils.getFieldsHierarchical(entity.getClass())) {
                        cloneMethodBody.append("com.jayway.jaxrs.hateoas.support.ReflectionUtils.setFieldHierarchical(this, \"" + field.getName() + "\", com.jayway.jaxrs.hateoas.support.ReflectionUtils.getFieldValueHierarchical(other, \"" + field.getName() + "\"));");
                    }
                    String method = "public void hateoasCopy(" + entity.getClass().getName() + " other ){ " + cloneMethodBody.toString() + "}";

                    CtMethod cloneMethod = CtMethod.make(method, newClass);
                    newClass.addMethod(cloneMethod);

                    //URLClassLoader classLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
                    //clazz = newClass.toClass(classLoader, this.getClass().getProtectionDomain());

                    URLClassLoader classLoader = new URLClassLoader(new URL[0], entity.getClass().getClassLoader());
                    clazz = newClass.toClass(classLoader, entity.getClass().getProtectionDomain());

                    TRANSFORMED_CLASSES.put(newClassName, clazz);
                }
                catch (Exception e) {
                    if (e instanceof HateoasInjectException) {
                        throw (HateoasInjectException) e;
                    }
                    throw new RuntimeException(e);
                }
            }
        }
        else {
            clazz = TRANSFORMED_CLASSES.get(newClassName);
        }

        Object newInstance;
        try {
            newInstance = clazz.newInstance();
            Method copyMethod = newInstance.getClass().getMethod("hateoasCopy", entity.getClass());
            copyMethod.invoke(newInstance, entity);
        } catch (Throwable e) {
	        throw new HateoasInjectException("could not create instance of " + clazz.getName(), e);
        }

        if (mapStructureEnabled) {
            return mapInjector.injectLinks(newInstance, linkProducer, verbosity);
        }
        else {
            return arrayInjector.injectLinks(newInstance, linkProducer, verbosity);
        }
    }

    private void addCollectionMembers(CtClass newClass) throws CannotCompileException, NotFoundException {
        CtField newField = CtField.make("public java.util.Collection links;", newClass);
        newClass.addField(newField);

        CtMethod linksGetterMethod = CtMethod.make("public java.util.Collection getLinks(){ return this.links; }", newClass);
        newClass.addMethod(linksGetterMethod);

        CtMethod linksSetterMethod = CtMethod.make("public void setLinks(java.util.Collection links){ this.links = links; }", newClass);
        newClass.addMethod(linksSetterMethod);

        newClass.addInterface(CLASS_POOL.get("com.jayway.jaxrs.hateoas.HateoasLinkBean"));
    }

    private void addMapMembers(CtClass newClass) throws CannotCompileException, NotFoundException {
        CtField newField = CtField.make("public java.util.Map links;", newClass);
        newClass.addField(newField);

        CtMethod linksGetterMethod = CtMethod.make("public java.util.Map getLinks(){ return this.links; }", newClass);
        newClass.addMethod(linksGetterMethod);

        CtMethod linksSetterMethod = CtMethod.make("public void setLinks(java.util.Map links){ this.links = links; }", newClass);
        newClass.addMethod(linksSetterMethod);

        newClass.addInterface(CLASS_POOL.get("com.jayway.jaxrs.hateoas.HateoasLinkMapBean"));
    }

}
