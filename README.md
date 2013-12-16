Provides support for Tapestry5 Web applications.

First add maven dependency and repository to your project pom.xml

```xml
...
    <dependency>
        <groupId>com.sun.jersey.contribs</groupId>
        <artifactId>jersey-tapestry</artifactId>
        <version>1.18</version>
        <exclusions>
            <exclusion>
                <artifactId>jersey-server</artifactId>
                <groupId>com.sun.jersey</groupId>
            </exclusion>
        </exclusions>
    </dependency>
...
	<repositories>
        <repository>
            <id>zazrivec-releases</id>
            <url>http://zazrivec.github.io/maven/releases</url>
        </repository>
	</repositories>
...
```

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
            <param-value>com.example.application.MyApplication</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>Jersey Tapestry Servlet</servlet-name>
        <url-pattern>/api/v1/*</url-pattern>
    </servlet-mapping>
   </web-app>
```

Example JAX-RS Application class:

```java
    public class MyApplication extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            return new HashSet<Class<?>>(Arrays.asList(TapestryJsonProvider.class, GuideResource.class));
        }

    }
```

Example JAX-RS Resource class:

```java
    @Path("/guide")
    public class GuideResource {

        // we can inject tapestry service into jax-rs resource
        @Inject
        private ProgramMetadataService programMetadataService;

        @Inject
        private ProjectService projectService;

        // we can process tapestry JSONObject class as result and input parameter
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("/review/{projectId}")
        public JSONObject getReviewGuide(@PathParam("projectId") String projectId) {
            Project project = projectService.getProject(new ObjectId(projectId));
            return programMetadataService.getGuide(project.getProgram().getReviewDefinition().getGuideSections());
        }

        // and of course, we can make all the jax-rs apu things
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("/reviewerReview/{projectId}")
        public Response getProjectReview(@PathParam("projectId") String projectId) {
            try {
                Project project = projectService.getProject(new ObjectId(projectId));
                ReviewDefinition definition = project.getProgram().getReviewDefinition();
                ReviewPart reviewPart = ModelUtils.toClient(reviewService.getReviewPartForLoggedInUser(project), definition);
                ReviewerMetadataAndData result = new ReviewerMetadataAndData(project, EnumUtils.create(ProjectReviewRating.class), definition,
                        reviewPart);

                return Response.ok(result).build();
            }
            catch (IllegalAccessException e) {
                return Response.status(Status.FORBIDDEN).build();
            }
        }

    }
```