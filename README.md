# JavalinJWT
### A basic JWT extension for Javalin which uses Auth0 Java JWT library available [here](https://github.com/auth0/java-jwt)

**It's probably a good idea to familiarize yourself with Auth0 library first before using this extension**

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

### Creating a JWT provider
In order to use any functionality available here, you need first to create a JWT provider (for lack of a better word). A provider is a somewhat convient way of working with JWT which wraps a generator and a verifier. You need a generator which implements the functional interfaceJWTGeneratr, and a verifier which is the normal Auth0 JWTVerifier. Additionally, both of them require an Auth0 Algorithm to work on. For the sake of example, we are going to use HMAC256.

To create an HMAC256 instance you can use the snippet below, with your own secret of coure:
```java
Algorithm algorithm = Algorithm.HMAC256("very_secret");
```

A JWT generator needs to implement the function *generate()*. It takes an object of a generic type and generates the user 
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
JWTProvider provider = JWTProvider(algorithm, generator, verifier);
```
