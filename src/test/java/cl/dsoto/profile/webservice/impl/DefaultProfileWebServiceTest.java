package cl.dsoto.profile.webservice.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class DefaultProfileWebServiceTest {

    @Test
    void shouldCreateAndFetchOwnProfile() {
        String userId = userId();

        given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(profilePayload("Provider One"))
                .when()
                .post("/profiles")
                .then()
                .statusCode(201)
                .body("userId", is(userId))
                .body("displayName", is("Provider One"))
                .body("publicationStatus", is("DRAFT"))
                .body("ageVerificationStatus", is("NOT_STARTED"))
                .body("services", hasSize(1))
                .body("rates", hasSize(1));

        given()
                .header("X-User-Id", userId)
                .when()
                .get("/profiles/me")
                .then()
                .statusCode(200)
                .body("userId", is(userId))
                .body("displayName", is("Provider One"));
    }

    @Test
    void shouldRejectDuplicateProfileForSameUser() {
        String userId = userId();

        createProfile(userId, "Provider Duplicate");

        given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(profilePayload("Provider Duplicate"))
                .when()
                .post("/profiles")
                .then()
                .statusCode(409);
    }

    @Test
    void shouldUpdateOwnProfileAggregate() {
        String userId = userId();
        createProfile(userId, "Provider Before");

        given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(Map.of(
                        "displayName", "Provider After",
                        "description", "Updated description",
                        "age", 31,
                        "location", "Valparaiso",
                        "services", List.of(
                                Map.of("name", "Massage", "description", "Relax", "active", true),
                                Map.of("name", "Video Call", "description", "Remote", "active", true)
                        ),
                        "rates", List.of(
                                Map.of(
                                        "label", "1 Hour",
                                        "amount", new BigDecimal("90000"),
                                        "currency", "CLP",
                                        "durationAmount", 1,
                                        "durationUnit", "HOURS",
                                        "displayOrder", 1,
                                        "active", true
                                )
                        )
                ))
                .when()
                .put("/profiles/me")
                .then()
                .statusCode(200)
                .body("displayName", is("Provider After"))
                .body("description", is("Updated description"))
                .body("location", is("Valparaiso"))
                .body("services", hasSize(2))
                .body("rates", hasSize(1));
    }

    @Test
    void shouldNotExposeDraftProfilePublicly() {
        String userId = userId();
        String profileId = createProfile(userId, "Provider Draft");

        given()
                .when()
                .get("/public/profiles/" + profileId)
                .then()
                .statusCode(404);
    }

    private String createProfile(String userId, String displayName) {
        return given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(profilePayload(displayName))
                .when()
                .post("/profiles")
                .then()
                .statusCode(201)
                .extract()
                .path("profileId");
    }

    private Map<String, Object> profilePayload(String displayName) {
        return Map.of(
                "displayName", displayName,
                "description", "Profile description",
                "age", 29,
                "location", "Santiago",
                "services", List.of(
                        Map.of("name", "Massage", "description", "Relax", "active", true)
                ),
                "rates", List.of(
                        Map.of(
                                "label", "30 Minutes",
                                "amount", new BigDecimal("50000"),
                                "currency", "CLP",
                                "durationAmount", 30,
                                "durationUnit", "MINUTES",
                                "displayOrder", 1,
                                "active", true
                        )
                )
        );
    }

    private String userId() {
        return "user-" + UUID.randomUUID();
    }
}
