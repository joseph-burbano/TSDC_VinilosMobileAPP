Feature: Listado de artistas (HU03)
  @user1 @mobile
  Scenario: Como usuario visitante navego el listado de artistas y veo el contenido
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_artistas"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "ARCHIVO DE"
    Then I see the text "Artistas"
    Then I see the text "Rubén Blades Bellido de Luna"
    Then I see the text "Queen"

  @user2 @mobile
  Scenario: Como usuario visitante busco un artista por nombre y veo solo el resultado filtrado
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_artistas"
    Then I wait
    Then I wait
    Then I wait
    Then I tap on element with accessibility id "artist_search"
    Then I type "Queen"
    Then I wait
    Then I see the text "Queen"
    Then I don't see the text "Rubén Blades Bellido de Luna"

  @user3 @mobile
  Scenario: Como usuario visitante busco un artista que no existe y veo la lista vacia
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_artistas"
    Then I wait
    Then I wait
    Then I wait
    Then I tap on element with accessibility id "artist_search"
    Then I type "zzz_no_existe"
    Then I wait
    Then I don't see the text "Rubén Blades Bellido de Luna"
    Then I don't see the text "Queen"
