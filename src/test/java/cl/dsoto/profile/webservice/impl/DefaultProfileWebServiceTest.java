package cl.dsoto.profile.webservice.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.util.Map.entry;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
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
                        "countryCode", "CL",
                        "regionCode", "05",
                        "communeCode", "05101",
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
                .body("commune.countryCode", is("CL"))
                .body("commune.region.regionCode", is("05"))
                .body("commune.region.name", is("Valparaíso"))
                .body("commune.communeCode", is("05101"))
                .body("commune.name", is("Valparaíso"))
                .body("services", hasSize(2))
                .body("rates", hasSize(1));
    }

    @Test
    void shouldUpdateExtendedPublicProfileData() {
        String userId = userId();
        createProfile(userId, "Provider Extended Before");

        given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(Map.ofEntries(
                        entry("displayName", "Provider Extended"),
                        entry("description", "Extended profile description"),
                        entry("age", 28),
                        entry("countryCode", "CL"),
                        entry("regionCode", "13"),
                        entry("communeCode", "13101"),
                        entry("details", Map.ofEntries(
                                entry("contactPhone", "+56912345678"),
                                entry("whatsappEnabled", true),
                                entry("shortTitle", "Atención elegante y discreta"),
                                entry("experience", "5 años de experiencia"),
                                entry("rules", "Reservas con anticipación"),
                                entry("heightCm", 168),
                                entry("weightKg", 58),
                                entry("measurements", "90-60-90"),
                                entry("bodyType", "Delgada"),
                                entry("hairColor", "Castaño"),
                                entry("eyeColor", "Café"),
                                entry("smokes", false),
                                entry("tattoos", true),
                                entry("piercings", false),
                                entry("grooming", "Depilada"),
                                entry("languages", "Español, Inglés")
                        )),
                        entry("availability", List.of(
                                Map.of(
                                        "dayOfWeek", "MONDAY",
                                        "startTime", "10:00",
                                        "endTime", "22:00",
                                        "available", true,
                                        "displayOrder", 1
                                )
                        )),
                        entry("modalities", List.of("OWN_PLACE", "HOTEL", "OUTCALL")),
                        entry("tags", List.of("Masajes", " Elegante ", "DISCRETA")),
                        entry("services", List.of(
                                Map.of("name", "Massage", "description", "Relax", "active", true)
                        )),
                        entry("rates", List.of(
                                Map.of(
                                        "label", "1 Hour",
                                        "amount", new BigDecimal("90000"),
                                        "currency", "CLP",
                                        "durationAmount", 1,
                                        "durationUnit", "HOURS",
                                        "displayOrder", 1,
                                        "active", true
                                )
                        ))
                ))
                .when()
                .put("/profiles/me")
                .then()
                .statusCode(200)
                .body("details.contactPhone", is("+56912345678"))
                .body("details.whatsappEnabled", is(true))
                .body("details.heightCm", is(168))
                .body("availability", hasSize(1))
                .body("availability[0].dayOfWeek", is("MONDAY"))
                .body("availability[0].startTime", is("10:00:00"))
                .body("modalities", hasSize(3))
                .body("modalities", hasItem("HOTEL"))
                .body("tags", hasSize(3))
                .body("tags", hasItem("elegante"))
                .body("completion.complete", is(false))
                .body("completion.missingFields", hasItem("photo"));
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
                "countryCode", "CL",
                "regionCode", "13",
                "communeCode", "13101",
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
