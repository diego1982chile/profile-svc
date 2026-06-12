package cl.dsoto.profile.webservice.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class DefaultLocationWebServiceTest {

    @Test
    void shouldListChileRegions() {
        given()
                .when()
                .get("/locations/regions")
                .then()
                .statusCode(200)
                .body("", hasSize(16))
                .body("regionCode", hasItem("13"))
                .body("name", hasItem("Metropolitana de Santiago"));
    }

    @Test
    void shouldListCommunesByRegion() {
        given()
                .when()
                .get("/locations/regions/13/communes")
                .then()
                .statusCode(200)
                .body("", hasSize(52))
                .body("communeCode", hasItem("13123"))
                .body("name", hasItem("Providencia"));
    }

    @Test
    void shouldRejectUnknownRegionForCommunes() {
        given()
                .when()
                .get("/locations/regions/99/communes")
                .then()
                .statusCode(400)
                .body("message", is("region not found"));
    }
}
