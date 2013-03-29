package com.sun.jersey.spi.tapestry.container.servlet;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import com.sun.jersey.spi.tapestry.container.TapestryComponentProviderFactory;
import org.apache.tapestry5.TapestryFilter;
import org.apache.tapestry5.ioc.Registry;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import java.util.Map;
import java.util.logging.Logger;

public class TapestryContainer extends ServletContainer {

    private static final long serialVersionUID = -5446319872884544020L;

    private static final Logger LOGGER = Logger.getLogger(TapestryContainer.class.getName());

    private WebApplication webapp;

    @Override
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props,
                                                      WebConfig webConfig) throws ServletException {
        return new DefaultResourceConfig();
    }

    @Override
    protected void initiate(ResourceConfig config, WebApplication webapp) {
        this.webapp = webapp;
        webapp.initiate(config, new TapestryComponentProviderFactory(config, getRegistry()));
    }

    protected Registry getRegistry() {
        return (Registry)getServletContext().getAttribute(TapestryFilter.REGISTRY_CONTEXT_NAME);
    }

    public WebApplication getWebApplication() {
        return webapp;
    }
}
