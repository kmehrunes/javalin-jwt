package javalinjwt;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Optional;

public class JWTProvider<T> {
    private final Algorithm algorithm;
    private final JWTGenerator<T> generator;
    private final JWTVerifier verifier;

    public JWTProvider(Algorithm algorithm, JWTGenerator<T> generator, JWTVerifier verifier) {
        this.algorithm = algorithm;
        this.generator = generator;
        this.verifier = verifier;
    }

    public String generateToken(T obj) {
        return generator.generate(obj, algorithm);
    }

    public Optional<DecodedJWT> validateToken(String token) {
        try {
            return Optional.of(verifier.verify(token));
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }
    }
}
