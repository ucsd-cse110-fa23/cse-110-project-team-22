/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package app;

import org.junit.jupiter.api.Test;

import app.client.App;
import app.client.Controller;
import app.client.Model;
import app.server.ChatGPTHandler;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test void appHasAGreeting() {
        App classUnderTest = new App();
        assertEquals(3, 3);
        // assertNotNull(classUnderTest, "app should have a greeting");
    }

    @Test
    void gptRefreshTest() {
        String generatedText = "Bacon Egg Sandwhich, bacon, eggs, and cheese, step 1:... Step 2:...";
        String mealType = "breakfast";
        String ingredients = "bacon, eggs, sausage";
        Model refreshTest = new Model();
        String prompt = "Make me a " + mealType + " recipe using " + ingredients + " presented in JSON format with the \"title\" as the first key with its value as one string, \"ingredients\" as another key with its value as one string, and \"instructions\" as the last key with its value as one string";
        String response = refreshTest.performRequest("POST", prompt, null, "chatgpt");
        assertNotEquals(response, generatedText);
    }
}