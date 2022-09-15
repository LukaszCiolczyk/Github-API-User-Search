package com.company;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

class GUITest {
    @Test
    public void shouldSendGetRequest(){
        try{

            HttpResponse<String> actualResponse = GUI.sendHttpGet("https://api.github.com/search/repositories?q=user:as");
            int expected = 200;
            Assertions.assertEquals(expected,actualResponse.statusCode());
        }catch (Exception e){
        e.printStackTrace();
        }
    }
}