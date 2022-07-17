# The @txn tag enables a Transaction open-rollback around each Scenario,
# Preventing persisted data from leaking between Scenarios.
# Try removing the @txn tag and see what happens.
@txn
Feature: Store, track, and delete Mp3 files (Resources)

  Resource service allows user to perform CRD operations with mp3 files

  Scenario: Upload new resource - mp3 file
    When User uploads file "file_example_MP3_5MG.mp3"
    Then Application response status is 200
    And Response contains:
      """
      {"ids": 1}
      """
    And There are the following resources
      | fileName                 | id |
      | file_example_MP3_5MG.mp3 | 1  |


  Scenario: Get existing resource by id
    Given The following Resources exist in the system:
      | fileName                 | id | sizeInBytes |
      | file_example_MP3_5MG.mp3 | 2  | 5289384     |
    When User downloads resource with id=2
    Then Application response status is 200
    And Response content type is "audio/mpeg"
    And Response contains file with size in bytes 5289384

  Scenario: Delete existing resource by id
    Given The following Resources exist in the system:
      | fileName                 | id |
      | file_example_MP3_5MG.mp3 | 3  |
    When User deletes resource with id=3
    Then Application response status is 200
    And Response contains:
      """
      {"ids": 3}
      """