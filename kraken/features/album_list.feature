Feature: Listado de álbumes (HU01)

  @user1 @mobile
  Scenario: Como usuario visitante navego al catálogo de álbumes y veo el header
    Given I wait
    Then I tap on element with accessibility id "nav_álbumes"
    Then I wait
    Then I wait
    Then I see the text "SELECCIÓN DE"
    Then I see the text "Álbumes"
    Then I see the text "encontrados"
    Then I see the text "Buscando América"
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
