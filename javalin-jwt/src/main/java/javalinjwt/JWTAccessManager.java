package javalinjwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JWTAccessManager implements Handler {
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
    public void handle(@NotNull Context context) {
        RouteRole role = extractRole(context);
        Set<RouteRole> permittedRoles = context.routeRoles();
        if (!permittedRoles.contains(role)) {
            throw new UnauthorizedResponse();
        }
    }
}