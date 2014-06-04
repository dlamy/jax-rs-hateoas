package com.jayway.jaxrs.hateoas.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.jayway.jaxrs.hateoas.HateoasInjectException;
import com.jayway.jaxrs.hateoas.HateoasLink;
import com.jayway.jaxrs.hateoas.HateoasLinkInjector;
import com.jayway.jaxrs.hateoas.HateoasLinkMapBean;
import com.jayway.jaxrs.hateoas.HateoasVerbosity;
import com.jayway.jaxrs.hateoas.LinkProducer;

/**
 * Created by IntelliJ IDEA.
 * User: davelamy
 */
public class HateoasLinkMapBeanLinkInjector implements HateoasLinkInjector<Object> {

    @Override
    public boolean canInject(Object entity) {
        return (entity instanceof HateoasLinkMapBean);
    }

    @Override
    public Object injectLinks(Object entity, LinkProducer<Object> linkProducer, final HateoasVerbosity verbosity) {

        HateoasLinkMapBean linkBean = (HateoasLinkMapBean) entity;

        Collection<Map<String, Object>> links = Collections2.transform(linkProducer.getLinks(entity),
                new Function<HateoasLink, Map<String, Object>>() {
                    @Override
                    public Map<String, Object> apply(HateoasLink from) {
                        return from.toMap(verbosity);
                    }
                }
        );

        Map<String, Map<String, Object>> linkMap = new HashMap<>();

        for (Map<String, Object> linkProperties : links) {
            if (linkProperties.size() > 0) {
                Object relValue = linkProperties.remove("rel");
                if (relValue == null) {
                    throw new HateoasInjectException(
                            "Invalid link properties '" + linkProperties +
                                    ":  Link properties require a rel entry."
                    );
                }

                linkMap.put(relValue.toString(), linkProperties);
            }
        }

        linkBean.setLinks(linkMap);

        return linkBean;
    }
}
