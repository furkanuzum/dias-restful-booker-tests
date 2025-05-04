package com.furkan.restfulbooker;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfulBookerTest {

    private static String token;
    private static int bookingId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";

        // Generate and store token
        Map<String, String> creds = new HashMap<>();
        creds.put("username", "admin");
        creds.put("password", "password123");

        token = given()
                .contentType(ContentType.JSON)
                .body(creds)
        .when()
                .post("/auth")
        .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    @Order(1)
    void testCreateBooking() {
        Map<String, Object> booking = new HashMap<>();
        booking.put("firstname", "Furkan");
        booking.put("lastname", "Uzum");
        booking.put("totalprice", 111);
        booking.put("depositpaid", true);
        booking.put("bookingdates", Map.of("checkin", "2024-01-01", "checkout", "2024-01-05"));
        booking.put("additionalneeds", "Breakfast");

        bookingId = given()
                .contentType(ContentType.JSON)
                .body(booking)
        .when()
                .post("/booking")
        .then()
                .statusCode(200)
                .body("bookingid", notNullValue())
                .extract()
                .path("bookingid");
    }

    @Test
    @Order(2)
    void testGetBookingIds() {
        given()
        .when()
            .get("/booking")
        .then()
            .statusCode(200)
            .body("bookingid", notNullValue())
            .body("bookingid", hasItem(bookingId));
    }

    @Test
    @Order(3)
    void testGetBookingById() {
        given()
            .pathParam("id", bookingId)
        .when()
            .get("/booking/{id}")
        .then()
            .statusCode(200)
            .body("firstname", equalTo("Furkan"))
            .body("lastname", equalTo("Uzum"));
    }

    @Test
    @Order(4)
    void testUpdateBooking() {
        Map<String, Object> booking = new HashMap<>();
        booking.put("firstname", "Gokhan");
        booking.put("lastname", "Uzum");
        booking.put("totalprice", 222);
        booking.put("depositpaid", false);
        booking.put("bookingdates", Map.of("checkin", "2024-01-10", "checkout", "2024-01-15"));
        booking.put("additionalneeds", "Lunch");

        given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .pathParam("id", bookingId)
                .body(booking)
        .when()
                .put("/booking/{id}")
        .then()
                .statusCode(200)
                .body("firstname", equalTo("Gokhan"));
    }

    @Test
    @Order(5)
    void testPartialUpdateBooking() {
        Map<String, String> update = new HashMap<>();
        update.put("firstname", "UpdatedName");

        given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .pathParam("id", bookingId)
                .body(update)
        .when()
                .patch("/booking/{id}")
        .then()
                .statusCode(200)
                .body("firstname", equalTo("UpdatedName"));
    }

    @Test
    @Order(6)
    void testDeleteBooking() {
        given()
                .cookie("token", token)
                .pathParam("id", bookingId)
        .when()
                .delete("/booking/{id}")
        .then()
                .statusCode(201);
    }

    @Test
    @Order(7)
    void testPingHealthCheck() {
        given()
        .when()
            .get("/ping")
        .then()
            .statusCode(201)
            .time(lessThan(20_000L));
    }
}
