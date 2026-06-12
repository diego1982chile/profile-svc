package cl.dsoto.profile.webservice.impl;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
@TestProfile(LocalMediaStorageProfile.class)
class LocalMediaStorageWebServiceTest {

    @Test
    void shouldUploadConfirmAndDownloadLocalMedia() {
        String userId = userId();
        createProfile(userId, "Provider Local Media");

        String storageKey = given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(Map.of(
                        "mediaType", "PHOTO",
                        "contentType", "image/jpeg",
                        "fileSize", 11
                ))
                .when()
                .post("/media/upload-url")
                .then()
                .statusCode(200)
                .body("storageKey", startsWith("profiles/"))
                .body("uploadUrl", startsWith("http://localhost:8081/media/files/profiles/"))
                .body("method", is("PUT"))
                .extract()
                .path("storageKey");
        byte[] bytes = "hello-media".getBytes();

        given()
                .contentType("image/jpeg")
                .body(bytes)
                .when()
                .put("/media/files/" + storageKey)
                .then()
                .statusCode(204);

        given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(Map.of(
                        "storageKey", storageKey,
                        "mediaType", "PHOTO",
                        "displayOrder", 1,
                        "primaryMedia", true
                ))
                .when()
                .post("/media/confirm")
                .then()
                .statusCode(201)
                .body("storageKey", is(storageKey))
                .body("url", is("http://localhost:8081/media/files/" + storageKey))
                .body("fileSize", is(bytes.length));

        given()
                .when()
                .get("/media/files/" + storageKey)
                .then()
                .statusCode(200)
                .contentType("image/jpeg")
                .body(is("hello-media"));
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
