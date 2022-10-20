# JavalinJWT
### A basic JWT extension for Javalin which uses Auth0 Java JWT library available [here](https://github.com/auth0/java-jwt)

**It's a good idea to familiarize yourself with Auth0 library first before using this extension**

## Dependency
### 0.7.0+
For versions 0.7.0+, use GitHub Packages repository

```xml
<repositories>
    <repository>
        <id>github-pacakges-javalin-jwt</id>
        <name>javalin-jwt packages</name>
        <url>https://maven.pkg.github.com/kmehrunes/javalin-jwt</url>
    </repository>
</repositories>
```

And then add the dependency
```xml
<dependencies>
    <dependency>
        <groupId>com.github.kmehrunes</groupId>
        <artifactId>javalin-jwt</artifactId>
        <version>[version]</version>
    </dependency>
</dependencies>
```

### Versions older than 0.7.0
Add the JitPack repository
```xml
<repositories>
 	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```

And then add the dependency
```xml
<dependencies>
	<dependency>
	    <groupId>com.github.kmehrunes</groupId>
	    <artifactId>javalin-jwt</artifactId>
	    <version>[version]</version>
	</dependency>
</dependencies>
```
Require the java module with: `require com.github.kmehrunes.javalinjwt;`

## Features
- Helper functions for Javalin Context to make working with JWTs easier, includes: extracting tokens from authorization headers, adding/getting tokens to/from cookies, and adding decoded JWT objects to contexts for future handlers to use
- Decode helpers which take care of extracting, validating, and adding decoded objects to the context for you
- A simple access manager

## Usage Examples

We will walk through example which cover every aspect of the extension. You can, however, refer to the two examples available in the repository for basic and slightly more advanced use (with a decode handler and an access manager).

All the examples below use a MockUser class which is simply:
```java
class MockUser {
    String name;
    String level;

    MockUser(String name, String level) {
        this.name = name;
        this.level = level;
    }
}
```

### Creating a JWT Provider
In order to use any functionality available here, you need first to create a JWT provider (for lack of a better word). A provider is a somewhat convient way of working with JWT which wraps a generator and a verifier. You need a generator which implements the functional interfaceJWTGeneratr, and a verifier which is the normal Auth0 JWTVerifier. Additionally, both of them require an Auth0 Algorithm to work on. For the sake of example, we are going to use HMAC256.

To create an HMAC256 instance you can use the snippet below, with your own secret of coure:
```java
Algorithm algorithm = Algorithm.HMAC256("very_secret");
```

A JWT generator needs to implement the function `generate()`. It takes an object of a generic type and generates the user 
claims based on it, and an algorithm to sign the token:
```java
JWTGenerator<MockUser> generator = (user, alg) -> {
            JWTCreator.Builder token = JWT.create()
                    .withClaim("name", user.name)
                    .withClaim("level", user.level);
            return token.sign(alg);
        };
```


A verifier does the opposite job of a generator and can be created as follows:
```java
JWTVerifier verifier = JWT.require(algorithm).build();
```
FYI, Auth0 JWT provides a lot of options for creating a verifier, check the documentation to know more.


Finally a provider is created using the algorithm, the generator, and the verifier:
```java
JWTProvider provider = new JWTProvider(algorithm, generator, verifier);
```

### Using the Provider in Handlers
Now that the provider is ready, we can go ahead and use it in a Javalin handler. Here we will have two handler: one to generate the toke and one to verify it. In the generate handler we send a JWT as a JSON { "jwt": String }. This is just an example, you can add whatever you want like a renewal token etc.

```java
app.get("/generate",  context -> {
      // a mock user as an examples
      MockUser mockUser = new MockUser("Mocky McMockface", "admin");

      // generate a token for the user
      String token = provider.generateToken(mockUser);

      // send the JWT response
      context.json(new JWTResponse(token));
});

app.get("/validate", context -> {
      Optional<DecodedJWT> decodedJWT = JavalinJWT.getTokenFromHeader(context)
                                                    .flatMap(provider::validateToken);

      if (!decodedJWT.isPresent()) {
          context.status(401).result("Missing or invalid token");
      }
      else {
          context.result("Hi " + decodedJWT.get().getClaim("name").asString());
      }
});
```

Now if you visit `/generate`, you'll get a JWT for the created user. Then you need to put that token in an authorization header with "Bearer" scheme and issue a request to `/validate`.

### Using a Decode Handler and an Access Manager
What we did for `/validate` in the previous example needs to be done in every handler we have. This is why we have decode handlers and access managers. A decode handler takes care of the first part (extracting, decoding, and validating a JWT) while an access manager takes care of the second part (handling authorization).

#### Decode Handler
There are two decode handler: one for reading the token from an authorization and one to read the token from a cookie. Pick whichever you like. A decode handler is simply created using a helper function as follows:
```java
Handler decodeHandler = JavalinJWT.createHeaderDecodeHandler(provider);
```
And should be added as a *before* handler, whether globally or to certain paths. In this example we set it globally:
```java
app.before(decodeHandler);
```

#### Access Manager
An access manager requires the name of a JWT claim which declares the user's level, a mapping between users' levels and roles, and a default role for when no token is available. For the sake of this example, here are the available roles and their mapping:
```java
enum Roles implements Role {
    ANYONE,
    USER,
    ADMIN
}

Map<String, Role> rolesMapping = new HashMap<String, Role>() {{
    put("user", Roles.USER);
    put("admin", Roles.ADMIN);
}};
```
Creating an access manager is a simple one-line:
```java
JWTAccessManager accessManager = new JWTAccessManager("level", rolesMapping, Roles.ANYONE);
```
And can be added directly to the app:
```java
app.accessManager(accessManager);
```
Notice that the user's level claim must match what was specified in the generator.

#### Putting it to Work
Now that we have the decode handler and the access manager all set up, we can got ahead and put them to good use. We will first define the handlers:
```java
Handler generateHandler = context -> {
    MockUser mockUser = new MockUser("Mocky McMockface", "user");
    String token = provider.generateToken(mockUser);
    context.json(new JWTResponse(token));
};

Handler validateHandler = context -> {
    DecodedJWT decodedJWT = JavalinJWT.getDecodedFromContext(context);
    context.result("Hi " + decodedJWT.getClaim("name").asString());
};
```

Finally, now you can protect the paths using roles:
```java
app.get("/generate",  generateHandler, roles(Roles.ANYONE));
app.get("/validate", validateHandler, roles(Roles.USER, Roles.ADMIN));
app.get("/adminslounge", validateHandler, roles(Roles.ADMIN));
```

Although `generateHandler` remains unchanged (except for changing the level to *user* instead of *admin*) from the previous example, `validateHandler` is now more concise and focused. You no longer need to do user authorization in the handlers. To highlight that, the example shows that both `/validate` and `/adminlounge` have the same handler but different access roles.
