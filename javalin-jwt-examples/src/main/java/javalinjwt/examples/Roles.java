package javalinjwt.examples;


import io.javalin.security.RouteRole;

enum Roles implements RouteRole {
    ANYONE,
    USER,
    ADMIN
}
