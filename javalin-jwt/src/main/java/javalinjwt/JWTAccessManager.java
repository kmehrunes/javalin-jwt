package javalinjwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;


import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JWTAccessManager implements AccessManager {
    private final String userRoleClaim;
    private final Map<String, RouteRole> rolesMapping;
    private final RouteRole defaultRole;

    public JWTAccessManager(String userRoleClaim, Map<String, RouteRole> rolesMapping, RouteRole defaultRole) {
        this.userRoleClaim = userRoleClaim;
        this.rolesMapping = rolesMapping;
        this.defaultRole = defaultRole;
    }

    private RouteRole extractRole(Context context) {
        if (!JavalinJWT.containsJWT(context)) {
            return defaultRole;
        }

        DecodedJWT jwt = JavalinJWT.getDecodedFromContext(context);
        String userLevel = jwt.getClaim(userRoleClaim).asString();

        return Optional.ofNullable(rolesMapping.get(userLevel)).orElse(defaultRole);
    }

    @Override
    public void manage(@NotNull Handler handler, @NotNull Context context, Set<? extends RouteRole> permittedRoles) throws Exception {
        RouteRole role = extractRole(context);

        if (permittedRoles.contains(role)) {
            handler.handle(context);
        } else {
            context.status(401).result("Unauthorized");
        }
    }
}