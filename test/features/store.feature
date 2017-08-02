Feature: Handle product listing
  Scenario: Get products
    Given the service is running
    When I try to retrieve all products
    Then I should get a '200' response
    And the response must contain 'products'

  Scenario: Get product
    Given the service is running
    When I try to retrieve a product with id '1'
    Then I should get a '200' response
    And the response must contain 'product'

  Scenario: Get a non existent product
    Given the service is running
    When I try to retrieve a product with id '99'
    Then I should get a '404' response
