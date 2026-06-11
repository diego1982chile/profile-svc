package cl.dsoto.profile;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ServiceLivenessCheckTest {

    @Test
    void shouldExposeLivenessCheck() {
        given()
                .when()
                .get("/q/health/live")
                .then()
                .statusCode(200)
                .body("status", is("UP"));
    }
}
