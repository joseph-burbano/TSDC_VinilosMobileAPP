Feature: Listado de artistas

  @user1 @mobile
  Scenario: Como usuario visitante navego el listado de artistas y veo el contenido
    Given I wait
    Then I tap on element with accessibility id "nav_artistas"
    Then I wait
    Then I wait
    Then I see the text "ARCHIVO DE"
    Then I see the text "Artistas"
    Then I see the text "Rubén Blades Bellido de Luna"
    Then I see the text "Queen"
