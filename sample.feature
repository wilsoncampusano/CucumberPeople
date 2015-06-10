Feature: This is a sample feature file

  #Scenario Tag ID is the test case id in TestRail,TestLink,ALM
  #  if the tag id is empty it will create this scenario test case in your testRail,TestLink or ALM,
  #  but if you specified the scenario tag id as the test case id,then it will just update the existing test case .
  #Scenario title is the Test Case name in TestRail,TestLink,ALM
  #Step sentence is the 'cucumber' field value in TestRail,TestLink,ALM

  @1333333
  Scenario: sample scenario title,you can put any title for it
     Given I open the url
     When I input my user name and password with "Alter" and "password"
     Then I should go to the home page
