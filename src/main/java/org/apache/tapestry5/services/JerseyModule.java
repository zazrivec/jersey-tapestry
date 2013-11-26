package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.InjectService;

public class JerseyModule {


    private final RequestGlobals requestGlobals;

    public JerseyModule(RequestGlobals requestGlobals)
    {
        this.requestGlobals = requestGlobals;
    }

    public static void bind(ServiceBinder binder)
    {
        binder.bind(HttpServletRequestFilter.class, RestApiRequestFilter.class).withSimpleId();
    }

    public void contributeHttpServletRequestHandler(OrderedConfiguration<HttpServletRequestFilter> configuration,

                                                    @InjectService("RestApiRequestFilter")
                                                    HttpServletRequestFilter restApiRequestFilter)
    {
        configuration.add("RestApiStoreIntoGlobals", restApiRequestFilter, "before:IgnoredPaths");
    }
}
