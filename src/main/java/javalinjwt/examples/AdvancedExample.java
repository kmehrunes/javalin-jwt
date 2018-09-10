package javalinjwt.examples;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.Handler;
import io.javalin.Javalin;
import io.javalin.security.Role;
import javalinjwt.*;

import java.util.*;

public class AdvancedExample {

    public static void main(String[] args) {
        // create the provider
        JWTProvider provider = ProviderExample.createHMAC512();

        // create the access manager
        Map<String, Role> rolesMapping = new HashMap<String, Role>() {{
            put("user", Roles.USER);
            put("admin", Roles.ADMIN);
        }};

        JWTAccessManager accessManager = new JWTAccessManager("level", rolesMapping, Roles.ANYONE);

        // create the app
        Javalin app = Javalin.create()
                .port(4000)
                .accessManager(accessManager) // THE ACCESS MANAGER IS SET HERE
                .start();

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
            Optional<DecodedJWT> decodedJWT = JavalinJWT.getTokenFromHeader(context)
                    .flatMap(provider::validateToken);

            if (!decodedJWT.isPresent()) {
                context.status(401);
            }
            else {
                context.result("Hi " + decodedJWT.get().getClaim("name").asString());
            }
        };

        // set the paths
        app.before(decodeHandler);

        app.get("/generate",  generateHandler, Collections.singleton(Roles.ANYONE));
        app.get("/validate", validateHandler, new HashSet<>(Arrays.asList(Roles.USER, Roles.ADMIN)));
        app.get("/adminslounge", validateHandler, Collections.singleton(Roles.ADMIN));
    }
}
