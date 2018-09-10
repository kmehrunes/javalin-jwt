package javalinjwt.examples;

import io.javalin.security.Role;

enum Roles implements Role {
    ANYONE,
    USER,
    ADMIN
}
