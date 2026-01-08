package com.testpilot.model;

import java.util.List;

public class TestCase {
    public String id;
    public String scenario;
    public String title;
    public List<String> preconditions;
    public List<String> steps;
    public String expectedResult;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
   
}
