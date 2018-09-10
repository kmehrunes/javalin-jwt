package javalinjwt;

import com.auth0.jwt.algorithms.Algorithm;

@FunctionalInterface
public interface JWTGenerator {
    String generate(Object obj, Algorithm algorithm);
}
