/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crio.qeats.QEatsApplication;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.FixtureHelpers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Provider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import redis.embedded.RedisServer;

@SpringBootTest(classes = {QEatsApplication.class})
@DirtiesContext
@ActiveProfiles("test")
public class RestaurantRepositoryServiceTest {

  private static final String FIXTURES = "fixtures/exchanges";
  List<RestaurantEntity> allRestaurants = new ArrayList<>();
  @InjectMocks
  RestaurantRepositoryServiceImpl restaurantRepositoryServiceImpl;
  @Mock
  RestaurantRepositoryService restaurantRepositoryService;
  @Mock
  MongoTemplate mongoTemplate;
  @Mock
  ObjectMapper objectMapper;
  @Mock
  ModelMapper modelMapperProvider;

  @Mock
  RestaurantRepository restaurantRepository;

  @Value("${spring.redis.port}")
  private int redisPort;

  private RedisServer server = null;

  @BeforeEach
  void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    allRestaurants = listOfRestaurants();
    when(restaurantRepository.findAll()).thenReturn(allRestaurants);
  }

  @AfterEach
  void teardown() {
    mongoTemplate.dropCollection("restaurants");
  }

  @Test
void restaurantsCloseByAndOpenNow() {
    // Given
    List<RestaurantEntity> allRestaurants = new ArrayList<>();
    allRestaurants.add(createRestaurant("11", 20.0, 30.0, "17:00", "19:00"));
    allRestaurants.add(createRestaurant("12", 21.0, 31.0, "17:00", "19:00"));
    when(restaurantRepository.findAll()).thenReturn(allRestaurants);

    // When
    List<Restaurant> allRestaurantsCloseBy = restaurantRepositoryServiceImpl
            .findAllRestaurantsCloseBy(20.0, 30.0, LocalTime.of(18, 1), 3.0);

    // Then
    assertNotNull(allRestaurantsCloseBy); // Ensure result is not null
   
}


  private RestaurantEntity createRestaurant(String restaurantId, Double latitude, Double longitude,
          String opensAt, String closesAt) {
      RestaurantEntity restaurant = new RestaurantEntity();
      restaurant.setRestaurantId(restaurantId);
      restaurant.setLatitude(latitude);
      restaurant.setLongitude(longitude);
      restaurant.setOpensAt(opensAt);
      restaurant.setClosesAt(closesAt);
      return restaurant;
  }

  @Test
  void noRestaurantsNearBy() {
    assertNotNull(restaurantRepositoryService);

    List<Restaurant> allRestaurantsCloseBy = restaurantRepositoryService
        .findAllRestaurantsCloseBy(20.9, 30.0, LocalTime.of(18, 0), 3.0);

    assertEquals(0, allRestaurantsCloseBy.size());
  }

  @Test
  void tooEarlyNoRestaurantIsOpen() {
    assertNotNull(restaurantRepositoryService);

    List<Restaurant> allRestaurantsCloseBy = restaurantRepositoryService
        .findAllRestaurantsCloseBy(20.0, 30.0, LocalTime.of(7, 59), 3.0);

    assertEquals(0, allRestaurantsCloseBy.size());
  }

  @Test
  void tooLateNoRestaurantIsOpen() {
    assertNotNull(restaurantRepositoryService);

    List<Restaurant> allRestaurantsCloseBy = restaurantRepositoryService
        .findAllRestaurantsCloseBy(20.0, 30.0, LocalTime.of(23, 1), 3.0);

    assertEquals(0, allRestaurantsCloseBy.size());
  }

  private List<RestaurantEntity> listOfRestaurants() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/initial_data_set_restaurants.json");

    return objectMapper.readValue(fixture, new TypeReference<List<RestaurantEntity>>() {
    });
  }
}
