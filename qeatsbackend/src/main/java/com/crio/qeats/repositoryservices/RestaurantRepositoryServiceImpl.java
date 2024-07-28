/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Provider;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


@Service
@Primary
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {


  @Autowired
  private RestaurantRepository mongoTemplate;

  @Autowired
  private ModelMapper modelMapper;

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }


 @Override
public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
    Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

  // Fetch all restaurants from the database
  List<RestaurantEntity> allRestaurants = mongoTemplate.findAll();

  // Filter restaurants based on whether they are open now and within the serving radius
  List<Restaurant> openAndCloseByRestaurants = allRestaurants.stream()
      .filter(restaurant -> isRestaurantCloseByAndOpen(restaurant, currentTime, latitude, longitude, servingRadiusInKms))
      .map(this::mapEntityToDto) // Mapping using mapEntityToDto method
      .collect(Collectors.toList());

  return openAndCloseByRestaurants;
}

  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude,
          restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
          < servingRadiusInKms;
    }

    return false;
  }


  public Restaurant mapEntityToDto(RestaurantEntity restaurantEntity) {
    // Configure ModelMapper to map only the required fields
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

    // Perform the mapping
    Restaurant restaurantDto = modelMapper.map(restaurantEntity, Restaurant.class);

    // Clean the restaurant name by removing special characters
    String cleanedName = restaurantDto.getName().replaceAll("[^a-zA-Z0-9 ]", "");
    restaurantDto.setName(cleanedName);

    return restaurantDto;
  }

}

