
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crio.qeats.QEatsApplication;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import com.crio.qeats.utils.FixtureHelpers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
//  Pass all the RestaurantService test cases.
// Objectives:
// 1. Make modifications to the tests if necessary so that all test cases pass
// 2. Test RestaurantService Api by mocking RestaurantRepositoryService.


@SpringBootTest(classes = {QEatsApplication.class})
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DirtiesContext
@ActiveProfiles("test")
class RestaurantServiceTest {

  private static final String FIXTURES = "fixtures/exchanges";
  @InjectMocks
  private RestaurantServiceImpl restaurantService;
  @Mock
  RestaurantRepositoryService restaurantRepositoryServiceMock;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    MockitoAnnotations.initMocks(this);

    objectMapper = new ObjectMapper();
  }

  private String getServingRadius(List<Restaurant> restaurants, LocalTime timeOfService) {
    when(restaurantRepositoryServiceMock
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            any(Double.class)))
        .thenReturn(restaurants);

    GetRestaurantsResponse allRestaurantsCloseBy = restaurantService
        .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0, 30.0),
            timeOfService); //LocalTime.of(19,00));

    assertEquals(2, allRestaurantsCloseBy.getRestaurants().size());
    assertEquals("11", allRestaurantsCloseBy.getRestaurants().get(0).getRestaurantId());
    assertEquals("12", allRestaurantsCloseBy.getRestaurants().get(1).getRestaurantId());

    ArgumentCaptor<Double> servingRadiusInKms = ArgumentCaptor.forClass(Double.class);
    verify(restaurantRepositoryServiceMock, times(1))
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            servingRadiusInKms.capture());

    return servingRadiusInKms.getValue().toString();
  }

  @Test
  void peakHourServingRadiusOf3KmsAt7Pm() throws IOException {
    assertEquals(getServingRadius(loadRestaurantsDuringPeakHours(), LocalTime.of(19, 0)), "3.0");
  }


  @Test
  void normalHourServingRadiusIs5Kms() throws IOException {
    // Load restaurants for normal hours
    List<Restaurant> restaurants = loadRestaurantsDuringNormalHours();
    
    // Mock the repository method to return the loaded restaurants
    when(restaurantRepositoryServiceMock
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            any(Double.class)))
        .thenReturn(restaurants);
    
    // Call the service method
    GetRestaurantsResponse response = restaurantService
        .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0, 30.0),
            LocalTime.of(15, 0)); // Normal hour

    // Verify the number of restaurants returned
    assertEquals(restaurants.size(), response.getRestaurants().size());
    assertEquals(restaurants.get(0).getRestaurantId(), response.getRestaurants().get(0).getRestaurantId());

    // Verify that the repository method was called with the correct arguments
    ArgumentCaptor<Double> servingRadiusInKms = ArgumentCaptor.forClass(Double.class);
    verify(restaurantRepositoryServiceMock, times(1))
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            servingRadiusInKms.capture());

    // Check that the serving radius was 5.0 km during normal hours
    assertEquals("5.0", servingRadiusInKms.getValue().toString());
  }



  
  private List<Restaurant> loadRestaurantsDuringNormalHours() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/normal_hours_list_of_restaurants.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }

  private List<Restaurant> loadRestaurantsSearchedByAttributes() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/list_restaurants_searchedby_attributes.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }

  private List<Restaurant> loadRestaurantsDuringPeakHours() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/peak_hours_list_of_restaurants.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }
  @Test
  void peakHourServingRadiusOf3KmsAt9Pm() throws IOException {
    // Load restaurants for peak hours
    List<Restaurant> restaurants = loadRestaurantsDuringPeakHours();

    // Mock the repository method to return the loaded restaurants
    when(restaurantRepositoryServiceMock
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            any(Double.class)))
        .thenReturn(restaurants);

    // Call the service method
    GetRestaurantsResponse response = restaurantService
        .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0, 30.0),
            LocalTime.of(21, 0)); // Peak hour

    // Verify the number of restaurants returned
    assertEquals(restaurants.size(), response.getRestaurants().size());
    assertEquals(restaurants.get(0).getRestaurantId(), response.getRestaurants().get(0).getRestaurantId());

    // Verify that the repository method was called with the correct arguments
    ArgumentCaptor<Double> servingRadiusInKms = ArgumentCaptor.forClass(Double.class);
    verify(restaurantRepositoryServiceMock, times(1))
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            servingRadiusInKms.capture());

    // Check that the serving radius was 3.0 km during peak hours
    assertEquals("3.0", servingRadiusInKms.getValue().toString());
  }
  @Test
  void peakHourServingRadiusOf3KmsAt8Pm() throws IOException {
    assertEquals(getServingRadius(loadRestaurantsDuringPeakHours(), LocalTime.of(20, 0)), "3.0");
  }

  @Test
  void peakHourServingRadiusOf3KmsAt1Pm() throws IOException {
    assertEquals(getServingRadius(loadRestaurantsDuringPeakHours(), LocalTime.of(13, 0)), "3.0");
  }

  @Test
  void peakHourServingRadiusOf3KmsAt10Am() throws IOException {
    assertEquals(getServingRadius(loadRestaurantsDuringPeakHours(), LocalTime.of(10, 0)), "3.0");
  }

  @Test
  void peakHourServingRadiusOf3KmsAt9Am() throws IOException {
    assertEquals(getServingRadius(loadRestaurantsDuringPeakHours(), LocalTime.of(9, 0)), "3.0");
  }

  @Test
  void peakHourServingRadiusOf3KmsAt8Am() throws IOException {
    assertEquals(getServingRadius(loadRestaurantsDuringPeakHours(), LocalTime.of(8, 0)), "3.0");
  }

  @Test
  void peakHourServingRadiusOf3KmsAt2Pm() throws IOException {
    assertEquals(getServingRadius(loadRestaurantsDuringPeakHours(), LocalTime.of(14, 0)), "3.0");
  }
}
