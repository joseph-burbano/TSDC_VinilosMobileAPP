Feature: Navegación principal

  @user1 @mobile
  Scenario: Como usuario verifico que la navbar tiene todas las secciones y son navegables
    Given I wait
    Then I see the text "Vinyl"
    Then I see the text "Álbumes"
    Then I see the text "Artists"
    Then I see the text "People"
    Then I tap on coordinates 678 2093
    Then I wait
    Then I wait
    Then I see the text "Artistas"
    Then I tap on coordinates 126 2093
    Then I wait
    Then I see the text "Vinyl"
    Then I tap on coordinates 402 2093
    Then I wait
    Then I see the text "Álbumes"
    Then I tap on coordinates 954 2093
    Then I wait
    Then I see the text "People"
