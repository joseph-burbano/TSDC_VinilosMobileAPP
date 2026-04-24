Feature: Listado de artistas

  @user1 @mobile
  Scenario: Como usuario visitante navego el listado de artistas y veo el contenido
    Given I wait
    Then I tap on coordinates 678 2093
    Then I wait
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "Archivo de"
    Then I see the text "Artistas"
    Then I see the text "Rubén Blades Bellido de Luna"
    Then I see the text "Queen"
    Then I take a screenshot
