Feature: Handle user actions
  Scenario: Login
    Given the service is running
    When I retrieve the user 'user@email.com' with the password 'user123'
    Then I should get a '200' response
    And the response must contain 'jwt'

  Scenario: Login with wrong password
    Given the service is running
    When I retrieve the user 'user@email.com' with the password 'abc123'
    Then I should get a '402' response

  Scenario: Login non existing user
    Given the service is running
    When I retrieve the user 'me@email.com' with the password 'me123'
    Then I should get a '404' response

  Scenario: Logout
    Given the user 'user@email.com' is logged in with password 'user123'
    When I try to log out
    Then I should get a '200' response

  Scenario: Get shopping cart
    Given the user 'user@email.com' is logged in with password 'user123'
    When I try to get the shopping cart listing
    Then I should get a '200' response
    And the response must contain 'cart'

  Scenario: Add product to shopping cart
    Given the user 'user@email.com' is logged in with password 'user123'
    When I try to add '1' unit(s) of a product with the id '1' to shopping cart
    Then I should get a '200' response
    And the response must contain 'cart'

  Scenario: Add non existent product to shopping cart
    Given the user 'user@email.com' is logged in with password 'user123'
    When I try to add '1' unit(s) of a product with the id '99' to shopping cart
    Then I should get a '404' response

  Scenario: Remove product from shopping cart
    Given the user 'user@email.com' is logged in with password 'user123'
    And there's '1' unit(s) of a product with id '1' in the shopping cart
    When I try to remove a product with id '1' from the shopping cart
    Then I should get a '200' response
    And the response must contain 'cart'

  Scenario: Remove a non existent product from shopping cart
    Given the user 'user@email.com' is logged in with password 'user123'
    When I try to remove a product with id '99' from the shopping cart
    Then I should get a '404' response

  Scenario: Increase product quantity in shopping cart
    Given the user 'user@email.com' is logged in with password 'user123'
    And there's '1' unit(s) of a product with id '1' in the shopping cart
    When I try to increase '1' unit(s) of a product with id '1' in the shopping cart
    Then I should get a '200' response
    And the response must contain 'cart'

  Scenario: Increase a non existent product quantity in shopping cart
    Given the user 'user@email.com' is logged in with password 'user123'
    When I try to increase '1' unit(s) of a product with id '99' in the shopping cart
    Then I should get a '404' response

  Scenario: Decrease product quantity in shopping cart
    Given the user 'user@email.com' is logged in with password 'user123'
    And there's '1' unit(s) of a product with id '1' in the shopping cart
    When I try to decrease '1' unit(s) of a product with id '1' in the shopping cart
    Then I should get a '200' response
    And the response must contain 'cart'
    And the cart products field should be empty

  Scenario: Decrease more than cart amount from shopping cart
    Given the user 'user@email.com' is logged in with password 'user123'
    And there's '1' unit(s) of a product with id '1' in the shopping cart
    When I try to decrease '2' unit(s) of a product with id '1' in the shopping cart
    Then I should get a '200' response
    And the cart products field should be empty

  Scenario: Checkout from cart
    Given the user 'user@email.com' is logged in with password 'user123'
    And there's '10' unit(s) of a product with id '1' in stock
    And there's '3' unit(s) of a product with id '1' in the shopping cart
    When I try to checkout
    Then I should get a '200' response
    And the product with id '1' in stock should have '7' unit(s) remaining
    And the cart products field should be empty
