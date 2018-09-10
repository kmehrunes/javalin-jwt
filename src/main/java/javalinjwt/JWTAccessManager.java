package javalinjwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.security.AccessManager;
import io.javalin.security.Role;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JWTAccessManager implements AccessManager {
    private String userRoleClaim;
    private Map<String, Role> rolesMapping;
    private Role defaultRole;

    public JWTAccessManager(String userRoleClaim, Map<String, Role> rolesMapping, Role defaultRole) {
        this.userRoleClaim = userRoleClaim;
        this.rolesMapping = rolesMapping;
        this.defaultRole = defaultRole;
    }

    private Role extractRole(Context context) {
        if (!JavalinJWT.containsJWT(context)) {
            return defaultRole;
        }

        DecodedJWT jwt = JavalinJWT.getDecodedFromContext(context);
        String userLevel = jwt.getClaim(userRoleClaim).asString();

        return Optional.ofNullable(rolesMapping.get(userLevel)).orElse(defaultRole);
    }

    @Override
    public void manage(Handler handler, Context context, Set<Role> permittedRoles) throws Exception {
        Role role = extractRole(context);

        if (permittedRoles.contains(role)) {
            handler.handle(context);
        } else {
            context.status(401).result("Unauthorized");
        }
    }
}