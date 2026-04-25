Feature: Detalle de artista (HU04)

  @user1 @mobile
  Scenario: Como usuario visitante accedo al detalle de un artista y veo su informacion
    Given I wait
    Then I tap on element with accessibility id "nav_artistas"
    Then I wait
    Then I wait
    Then I see the text "Artistas"
    Then I tap on element with accessibility id "artist_item_1"
    Then I wait
    Then I wait
    Then I see the text "ARTISTA DESTACADO"
    Then I see the text "LA COLECCIÓN"

  @user2 @mobile
  Scenario: Como usuario visitante puedo regresar desde el detalle al listado
    Given I wait
    Then I tap on element with accessibility id "nav_artistas"
    Then I wait
    Then I wait
    Then I tap on element with accessibility id "artist_item_1"
    Then I wait
    Then I wait
    Then I tap on element with accessibility id "artist_detail_back"
    Then I wait
    Then I see the text "Artistas"
