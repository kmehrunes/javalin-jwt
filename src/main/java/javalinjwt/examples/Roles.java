package javalinjwt.examples;

import io.javalin.core.security.Role;



enum Roles implements Role {
    ANYONE,
    USER,
    ADMIN
}
