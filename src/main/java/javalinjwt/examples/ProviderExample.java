package javalinjwt.examples;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import javalinjwt.JWTProvider;
import javalinjwt.JWTGenerator;

class ProviderExample {
    static JWTProvider createHMAC512() {
        JWTGenerator<MockUser> generator = (user, alg) -> {
            JWTCreator.Builder token = JWT.create()
                    .withClaim("name", user.name)
                    .withClaim("level", user.level);
            return token.sign(alg);
        };

        Algorithm algorithm = Algorithm.HMAC256("very_secret");
        JWTVerifier verifier = JWT.require(algorithm).build();

        return new JWTProvider(algorithm, generator, verifier);
    }
}
