package org.apache.tapestry5.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.RequestImpl;
import org.apache.tapestry5.internal.services.ResponseImpl;
import org.apache.tapestry5.internal.services.TapestrySessionFactory;
import org.apache.tapestry5.ioc.annotations.Symbol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

public class RestApiRequestFilter implements HttpServletRequestFilter {

    private final RequestGlobals requestGlobals;
    private final Pattern[] restApiPatterns;
    private final String applicationCharset;
    private final TapestrySessionFactory sessionFactory;

    public RestApiRequestFilter(Collection<String> configuration, RequestGlobals requestGlobals,
            @Symbol(SymbolConstants.CHARSET) String applicationCharset, TapestrySessionFactory sessionFactory) {
        this.requestGlobals = requestGlobals;
        this.applicationCharset = applicationCharset;
        this.sessionFactory = sessionFactory;

        restApiPatterns = new Pattern[configuration.size()];

        int i = 0;

        for (String regexp : configuration) {
            Pattern p = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);

            restApiPatterns[i++] = p;
        }
    }

    public boolean service(HttpServletRequest servletRequest, HttpServletResponse servletResponse, HttpServletRequestHandler handler)
            throws IOException {
        String path = servletRequest.getServletPath();
        String pathInfo = servletRequest.getPathInfo();

        if (pathInfo != null) {
            path += pathInfo;
        }

        for (Pattern p : restApiPatterns) {
            if (p.matcher(path).matches()) {
                requestGlobals.storeServletRequestResponse(servletRequest, servletResponse);

                Request request = new RequestImpl(servletRequest, applicationCharset, sessionFactory);
                Response response = new ResponseImpl(servletRequest, servletResponse);

                requestGlobals.storeRequestResponse(request, response);
            }
        }

        return handler.service(servletRequest, servletResponse);
    }
}
