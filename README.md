Provides support for Tapestry5 Web applications.

Tapestry5 support is enabled by referencing the class TapestryContainer in the web.xml. For example:

```xml
   <web-app>
       <context-param>
           <param-name>tapestry.app-package</param-name>
           <param-value>com.example.application</param-value>
       </context-param>
       <filter>
           <filter-name>app</filter-name>
           <filter-class>org.apache.tapestry5.TapestryFilter</filter-class>
       </filter>
       <filter-mapping>
           <filter-name>app</filter-name>
           <url-pattern>/*</url-pattern>
       </filter-mapping>
    <servlet>
        <servlet-name>Jersey Tapestry Servlet</servlet-name>
        <servlet-class>com.sun.jersey.spi.tapestry.container.servlet.TapestryContainer</servlet-class>
        <init-param>
            <param-name>javax.ws.rs.Application</param-name>
            <param-value>com.example.application.Application</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>Jersey Tapestry Servlet</servlet-name>
        <url-pattern>/api/v1/*</url-pattern>
    </servlet-mapping>
   </web-app>
```