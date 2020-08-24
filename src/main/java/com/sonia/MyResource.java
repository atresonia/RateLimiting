package com.sonia;

import com.sonia.ratelimiter.RateLimiter;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {
    @Inject
    private RateLimiter rateLimiter;

    private static class Person {
        private final String firstName;
        private final String lastName;
        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }

    @GET
    @Path("person")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPerson(@QueryParam("guid") String guid) {
        if (guid == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                    .entity("guid parameter is mandatory")
                    .build());
        }

        if (!rateLimiter.isAllowed(guid)) {
            return Response.status(Response.Status.TOO_MANY_REQUESTS).build();
        }

        rateLimiter.increment(guid);

        Person person = new Person("Sonia", "Atre");
        return Response.ok(person).build();
    }

}
