module com.github.kmehrunes.javalinjwt {
    exports javalinjwt; // will not include the examples package

    requires io.javalin;
    requires com.auth0.jwt;
    // The org.jetbrains.annotations package only includes annotations for code analysis tools.
    // static will prevent the runtime from checking if the package is in the class path.
    requires static org.jetbrains.annotations;
}