/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package app;

import org.junit.jupiter.api.Test;

import app.client.App;
import app.client.View;
import app.client.Controller;
import app.client.Model;
import app.server.ChatGPTHandler;
import app.server.MyServer;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

class AppTest {
    // Tests whether the prompt we give chatgpt maintains the same provided ingredients as the original recipe

    @Test 
    void gptSameIngredientsTest() throws IOException {
        // MyServer.main(null);
        String user = "refreshUser";
        String pass = "pass";
        String mealType = "dinner";
        String ingredients = "steak, potatoes, butter";
        Model model = new Model();
        String prompt = "Make me a " + mealType + " recipe using " + ingredients + " presented in JSON format with the \"title\" as the first key with its value as one string, \"ingredients\" as another key with its value as one string, and \"instructions\" as the last key with its value as one string";
        String response = model.performRequest("POST", user, pass, prompt, null, "chatgpt");

        // API call should have successfully been made and returned thorugh model with the mealType and ingredients
        assertFalse(response.equals(""));
    }

    @Test
    void gptBddRefreshTest() throws IOException {
        // BDD TEST
        //MyServer.main(null);

        String user = "userBDD"; 

        // Scenario: I don't like the recipe generated
        String generatedText = "Scrambled eggs with bacon and toast, Step 1:... Step 2:...";
        // Given: I have chosen breakfast and listed bacon, eggs, and sausage
        // When: I am given a recipe for scrambled eggs with bacon and toast
        // And: I do not want this recipe
        String mealType = "breakfast";
        String ingredients = "bacon, eggs, sausage";
        // Then: when I press the refresh button it will generate another recipe like a bacon egg sandwich
        Model refreshTest = new Model();
        String prompt = "Make me a " + mealType + " recipe using " + ingredients + " presented in JSON format with the \"title\" as the first key with its value as one string, \"ingredients\" as another key with its value as one string, and \"instructions\" as the last key with its value as one string";
        String response = refreshTest.performRequest("POST", user, null, prompt, null, "chatgpt");
        assertNotEquals(response, generatedText);
    }

    // Tests signing up on a name thats taken already 
    @Test
    void signupTakenTest() throws IOException { 
        MyServer.main(null);
        Model loginTest = new Model();
        String response = loginTest.performRequest("POST", "Bob", "password12", null, null, "signup");
        assertEquals("NAME TAKEN", response);
    }

    // Tests a valid login
    // @Test
    // void loginValidTest() throws IOException { 
    //     //MyServer.main(null);
    //     Model loginTest = new Model();
    //     String response = loginTest.performRequest("POST", "Bob", "password12", null, null, "login");
    //     assertEquals("SUCCESS", response);
    // }

    // // Tests a invalid login password
    // @Test
    // void loginInvalidTest() throws IOException { 
    //     //MyServer.main(null);
    //     Model loginTest = new Model();
    //     String response = loginTest.performRequest("POST", "Bob", "wrongPassword", null, null, "login");
    //     assertEquals("PASSWORD FAILED", response);
    // }

    // // Tests a username that doesn't exist for login
    // @Test
    // void loginDoesntExistTest() throws IOException { 
    //     //MyServer.main(null);
    //     Model loginTest = new Model();
    //     String response = loginTest.performRequest("POST", "fakeName", "password12", null, null, "login");
    //     assertEquals("NAME FAILED", response);
    // }

    // @Test
    // void getMealTypeTest() throws IOException {
    //     //MyServer.main(null);
    //     Model mealtype = new Model();
    //     String response = mealtype.performRequest("GET", null, null, null, "breakfast", "mealtype");
    //     assertEquals("breakfast",response);
    // }

    // get URL of photo from google and use that to test dalle
    // mock file, to return fake url

    @Test
    void dalleLinkGenerationTest() throws IOException{
        //MyServer.main(null);
        Model dalleTest =  new Model();
        String recipeTitle = "Bacon Eggs and Ham";

        String url = "https://www.google.com/imgres?imgurl=https%3A%2F%2Fupload.wikimedia.org%2Fwikipedia%2Fcommons%2Fthumb%2Ff%2Ffa%2FHam_and_eggs_over_easy.jpg%2F1200px-Ham_and_eggs_over_easy.jpg&tbnid=jL-bcwE1AkYVvM&vet=12ahUKEwjm75GvxvSCAxWwJEQIHRB_BbYQMygBegQIARBW..i&imgrefurl=https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FHam_and_eggs&docid=2WM6ZYnDhyPs5M&w=1200&h=789&q=bacon%20eggs%20and%20ham&ved=2ahUKEwjm75GvxvSCAxWwJEQIHRB_BbYQMygBegQIARBW";

        String response = dalleTest.performRequest("POST", null, null, recipeTitle, null, "mockDalle");
        
        assertEquals(url, response);
    }

}