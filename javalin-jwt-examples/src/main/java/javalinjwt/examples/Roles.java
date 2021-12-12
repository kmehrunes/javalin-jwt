package javalinjwt.examples;


import io.javalin.core.security.RouteRole;

enum Roles implements RouteRole {
    ANYONE,
    USER,
    ADMIN
}
