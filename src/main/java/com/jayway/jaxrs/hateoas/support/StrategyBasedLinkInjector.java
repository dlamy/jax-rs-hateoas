package com.jayway.jaxrs.hateoas.support;

import com.google.common.collect.Lists;
import com.jayway.jaxrs.hateoas.HateoasInjectException;
import com.jayway.jaxrs.hateoas.HateoasLinkInjector;
import com.jayway.jaxrs.hateoas.HateoasVerbosity;
import com.jayway.jaxrs.hateoas.LinkProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy based link injector that tries to inject the links with all configured {@link HateoasLinkInjector}.
 *
 * @author Kalle Stenflo
 */
public class StrategyBasedLinkInjector implements HateoasLinkInjector<Object> {

    private static final Logger log = LoggerFactory.getLogger(StrategyBasedLinkInjector.class);

    private List<HateoasLinkInjector<Object>> strategies;

    private static final Map<Class<?>, HateoasLinkInjector<Object>> INJECTOR_MAPPING = new HashMap<Class<?>, HateoasLinkInjector<Object>>();
    
    public StrategyBasedLinkInjector() {
        strategies = Lists.newArrayList();
        strategies.add(new MapBasedHateoasLinkInjector());
        strategies.add(new HateoasLinkBeanLinkInjector());
        strategies.add(new ReflectionBasedHateoasLinkInjector());
        strategies.add(new JavassistHateoasLinkInjector());
    }

    @Override
    public boolean canInject(Object entity) {
        return true;
    }

    @Override
    public Object injectLinks(Object entity, LinkProducer<Object> objectLinkProducer, HateoasVerbosity verbosity) {
        if(!INJECTOR_MAPPING.containsKey(entity.getClass())){
            synchronized (this) {
                for (HateoasLinkInjector<Object> strategy : strategies) {
                    log.debug("Trying link injector strategy : " + strategy.getClass().getName() );

                    if(strategy.canInject(entity)){

                        log.debug("Caching injector strategy {} for class {}", strategy.getClass().getSimpleName(), entity.getClass().getSimpleName());

                        INJECTOR_MAPPING.put(entity.getClass(), strategy);
                        break;
                    }
                }
            }
        }

        HateoasLinkInjector<Object> injector = INJECTOR_MAPPING.get(entity.getClass());

        if(injector == null){
            throw new HateoasInjectException("No suitable injector found for " + entity.getClass());
        }

        return injector.injectLinks(entity, objectLinkProducer, verbosity);
    }
}
