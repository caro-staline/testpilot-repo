package com.testpilot.model;

/**
 * Data Transfer Object (DTO) for test case generation requests.
 */
public class TestCaseRequest {
    private String userStory;

    /**
     * Default constructor for Jackson deserialization.
     */
    public TestCaseRequest() {
    }

    /**
     * @return The user story text from the request.
     */
    public String getUserStory() {
        return userStory;
    }

    /**
     * @param userStory The user story text to set.
     */
    public void setUserStory(String userStory) {
        this.userStory = userStory;
    }
}
