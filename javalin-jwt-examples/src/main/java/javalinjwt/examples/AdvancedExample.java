package javalinjwt.examples;

import com.auth0.jwt.interfaces.DecodedJWT;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.security.RouteRole;

import javalinjwt.*;

import java.util.*;

public class AdvancedExample {

    public static void main(String[] args) {
        // create the provider
        JWTProvider<MockUser> provider = ProviderExample.createHMAC512();

        // create the access manager
        Map<String, RouteRole> rolesMapping = new HashMap<>() {{
            put("user", Roles.USER);
            put("admin", Roles.ADMIN);
        }};

        JWTAccessManager accessManager = new JWTAccessManager("level", rolesMapping, Roles.ANYONE);

        // create the app
        Javalin app = Javalin.create(config -> config.accessManager(accessManager))
                .start(4000);

        /*
         * A decode handler which captures the value of a JWT from an
         * authorization header in the form of "Bearer {jwt}". The handler
         * decodes and verifies the JWT then puts the decoded object as
         * a context attribute for future handlers to access directly.
         */
        Handler decodeHandler = JavalinJWT.createHeaderDecodeHandler(provider);

        Handler generateHandler = context -> {
            // a mock user as an examples
            MockUser mockUser = new MockUser("Mocky McMockface", "user");

            // generate a token for the user
            String token = provider.generateToken(mockUser);

            // send the JWT response
            context.json(new JWTResponse(token));
        };

        Handler validateHandler = context -> {
            DecodedJWT decodedJWT = JavalinJWT.getDecodedFromContext(context);
            context.result("Hi " + decodedJWT.getClaim("name").asString());
        };

        // set the paths
        app.before(decodeHandler);

        app.get("/generate",  generateHandler, Roles.ANYONE);
        app.get("/validate", validateHandler, Roles.USER, Roles.ADMIN);
        app.get("/adminslounge", validateHandler, Roles.ADMIN);
    }
}
