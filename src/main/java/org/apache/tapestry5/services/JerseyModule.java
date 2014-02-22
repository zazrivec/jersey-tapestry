package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Local;

public class JerseyModule {


    public static void bind(ServiceBinder binder)
    {
        binder.bind(HttpServletRequestFilter.class, RestApiRequestFilter.class).withSimpleId();
    }

    @Contribute(HttpServletRequestHandler.class)
    public void contributeHttpServletRequestHandler(
            OrderedConfiguration<HttpServletRequestFilter> configuration, @Local HttpServletRequestFilter restApiRequestFilter)
    {
        configuration.add("RestApiStoreIntoGlobals", restApiRequestFilter, "before:IgnoredPaths");
    }
}
