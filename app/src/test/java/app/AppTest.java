/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package app;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import app.Mock.ShareLinkMock;
import app.client.App;
import app.client.views.*;
import app.client.controllers.*;
import app.client.Model;
import app.server.ServerChecker;
import app.server.MyServer;
import java.net.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;


class AppTest {
    // Tests whether the prompt we give chatgpt maintains the same provided ingredients as the original recipe
    
    private final String MONGOURI =  "mongodb+srv://bryancho:73a48JL4@cluster0.jpmyzqg.mongodb.net/?retryWrites=true&w=majority";
    public String IPHOST = "192.168.1.173";

    @Test 
    void testGptSameIngredients() throws IOException {
        MyServer.main(null);
        String mealType = "dinner";
        String ingredients = "steak, potatoes, butter";
        Model model = new Model();
        String prompt = "Make me a " + mealType + " recipe using " + ingredients + " presented in JSON format with the \"title\" as the first key with its value as one string, \"ingredients\" as another key with its value as one string, and \"instructions\" as the last key with its value as one string";
        String response = model.performRequest("POST", null, null, prompt, null, "mockGPT");

        // API call should have successfully been made and returned thorugh model with the mealType and ingredients
        assertFalse(response.equals(""));
        MyServer.stop();
    }

    @Test
    void testGptBddRefresh() throws IOException {
        MyServer.main(null);
        // BDD TEST
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
        String response = refreshTest.performRequest("POST", user, null, prompt, null, "mockGPT");
        assertNotEquals(response, generatedText);
        MyServer.stop();
    }

    // Tests successful sign up
    @Test
    void testValidSignup() throws IOException {
        MyServer.main(null);
        Model model = new Model();
        String newUser = Long.toHexString(System.currentTimeMillis());
        String password = Long.toHexString(System.currentTimeMillis() + 3);
        String response = model.performRequest("POST", newUser, password, null, null, "signup");
        assertTrue(response.equals("NEW USER CREATED"));
        MyServer.stop();
    }

    // Tests signing up on a name thats taken already 
    @Test
    void testSignupUsernameTaken() throws IOException { 
        MyServer.main(null);
        Model loginTest = new Model();


        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("credentials");
            
            Document user = new Document("_id", new ObjectId());
            user.append("user", "Bob");
            user.append("password","password12");

            collection.insertOne(user);

            String response = loginTest.performRequest("POST", "Bob", "password12", null, null, "signup");
            assertEquals("USERNAME TAKEN", response);
            Bson filter = eq("user","Bob");
            collection.deleteMany(filter);
        }
        MyServer.stop();
    }

    // Tests a valid login
    @Test
    void testValidLoginValid() throws IOException { 
        MyServer.main(null);
        Model loginTest = new Model();
        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("credentials");
            
            Document user = new Document("_id", new ObjectId());
            user.append("user", "Bob");
            user.append("password","password12");

            collection.insertOne(user);

            String response = loginTest.performRequest("POST", "Bob", "password12", null, null, "login");
            assertEquals("SUCCESS", response);
            Bson filter = eq("user","Bob");
            collection.deleteMany(filter);
        }

        MyServer.stop();
    }

    // Tests a invalid login password
    @Test
    void testInvalidLoginCredentials() throws IOException { 
        MyServer.main(null);
        Model loginTest = new Model();

        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("credentials");
            
            Document user = new Document("_id", new ObjectId());
            user.append("user", "Bob");
            user.append("password","password12");

            collection.insertOne(user);

            String response = loginTest.performRequest("POST", "Bob", "wrongPassword", null, null, "login");
            assertEquals("INCORRECT CREDENTIALS", response);
            Bson filter = eq("user","Bob");
            collection.deleteMany(filter);
        }

        MyServer.stop();
    }

    // Tests a username that doesn't exist for login
    @Test
    void testLoginDoesntExist() throws IOException { 
        MyServer.main(null);
        Model loginTest = new Model();
        String response = loginTest.performRequest("POST", "fakeName", "password12", null, null, "login");
        assertEquals("USER NOT FOUND", response);
        MyServer.stop();
    }

    // Test /mealtype route to filter breakfast recipes belonging to "testGetMealType" account
    @Test
    void dalleLinkGenerationTest() throws IOException{
        MyServer.main(null);
        Model dalleTest =  new Model();
        String recipeTitle = "Bacon Eggs and Ham";

        String url = "https://www.google.com/imgres?imgurl=https%3A%2F%2Fupload.wikimedia.org%2Fwikipedia%2Fcommons%2Fthumb%2Ff%2Ffa%2FHam_and_eggs_over_easy.jpg%2F1200px-Ham_and_eggs_over_easy.jpg&tbnid=jL-bcwE1AkYVvM&vet=12ahUKEwjm75GvxvSCAxWwJEQIHRB_BbYQMygBegQIARBW..i&imgrefurl=https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FHam_and_eggs&docid=2WM6ZYnDhyPs5M&w=1200&h=789&q=bacon%20eggs%20and%20ham&ved=2ahUKEwjm75GvxvSCAxWwJEQIHRB_BbYQMygBegQIARBW";

        String response = dalleTest.performRequest("POST", null, null, recipeTitle, null, "mockDalle");
        
        assertEquals(url, response);
        MyServer.stop();

    }

    @Test
    void testGetMealType() throws IOException {
        MyServer.main(null);
        String user = "testGetMealType";
        Model mealtype = new Model();

        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("recipes");
            
            Document doc = new Document("_id", new ObjectId());
            doc.append("title", "Egg Bacon and Ham Breakfast Recipe");
            doc.append("mealtype","breakfast");
            doc.append("user",user);

            collection.insertOne(doc);
            String response = mealtype.performRequest("GET", user, null, null, "breakfast", "mealtype");
            // Account with username "testGetMealType" has ONE breakfast recipe named "Egg Bacon and Ham Breakfast Recipe"
            assertEquals("Egg Bacon and Ham Breakfast Recipe+breakfast", response);

        }
        MyServer.stop();
    }

    // Test /mealtype route to filter lunch recipes that have not been saved
    @Test
    void testGetNoLunchRecipe() throws IOException {
        MyServer.main(null);
        String user = "testGetMealType";
        Model mealtype = new Model();
        String response = mealtype.performRequest("GET", user, null, null, "lunch", "mealtype");
        // Account with username "testGetMealType" has NO lunch recipes
        assertEquals(null, response);
        MyServer.stop();
    }

    // Test /mealtype route to filter the two dinner recipes belonging to "testGetMealType" account
    // 
    @Test
    void testGetMultipleDinnerRecipes() throws IOException {
        MyServer.main(null);
        Model mealtype = new Model();
        // Account with username "testGetMealType" has TWO dinner recipes
        String t = "testTitle";
        String i = "testIngredients";
        String ins = "testInstructinos";
        String user = "testGetMealType";
        String m = "dinner";
        String method = "GET";
        
        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("recipes");
            
            for(int x = 1; x < 3;x++){
                Document recipe = new Document("_id", new ObjectId());
                String abc = t + x;
                recipe.append("title", abc);
                recipe.append("ingredients", i);
                recipe.append("instructions", ins);
                recipe.append("user", user);
                recipe.append("mealtype", m);
                
                collection.insertOne(recipe);
            }
            String response = mealtype.performRequest("GET", user, null, null, "dinner", "mealtype");
            // assertEquals("testTitle1+dinner_testTitle2+dinner",response);
            Bson filter = eq("user",user);
            collection.deleteMany(filter);
        }



        // assertEquals("Cheesy Vegetable Tortellini Bake+dinner+Savory Stuffed Pancakes+dinner", response);
        MyServer.stop();
    }

    @Test
    void testServerNotRunning() throws IOException{
        boolean status = ServerChecker.isServerRunning(IPHOST, 8100);
        assertEquals(false, status);
    }
    
    @Test
    void testServerRunning() throws IOException{
        MyServer.main(null);
        boolean status = ServerChecker.isServerRunning(IPHOST, 8100);
        assertEquals(true, status);
        MyServer.stop();
    }

    // UNIT TEST
    @Test
    void testGetShareLink() throws IOException{
        // given user has a recipe already
        Mock m = new Mock();
        ShareLinkMock mock = m.new ShareLinkMock("Bryan", "steak and eggs");
        // want to test the share functionality as a unit test
        String web = mock.getWebString();
        assertNotEquals("", web);
        assertTrue(web.contains("Bryan"));
        assertTrue(web.contains("steak and eggs"));
    }

    // Integration Test with model and server
    @Test 
    void shareIntegrationTest() throws IOException{
        MyServer.main(null);
        Model shareTest =  new Model();
        // have a recipe in the database already
        String recipeTitle = "Steak and Egg Skillet";
        String user = "Bryan";
        String error = "The recipe you have selected cannont be found by the server";
        
        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("recipes");
            
            Document doc = new Document("_id", new ObjectId());
            doc.append("title", recipeTitle);
            doc.append("mealtype","breakfast");
            doc.append("user",user);

            collection.insertOne(doc);
            String response = shareTest.performRequest("GET", user, null, null, recipeTitle, "share");

            assertTrue(response.contains(recipeTitle));
            assertFalse(response.contains(error));

            Bson filter = eq("title",recipeTitle);
            collection.deleteMany(filter);
        }

        
        MyServer.stop();
    }

    // // just testing server request handler method,  GET METHOD
    // // USER+TITLE+INGREDIENTS+INSTRUCTIONS+MEALTYPE
    // // UNIT TEST
    // @Test
    // void GETrequestHandlerUnitTest() throws IOException, URISyntaxException{
    //     MyServer.main(null);
    //     // have a recipe in the database already


    //     String t = "testTitle";
    //     String i = "testIngredients";
    //     String ins = "testInstructinos";
    //     String u = "testUser";
    //     String m = "testMealtype";
    //     String method = "GET";

    //     try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
    //         MongoDatabase database = mongoClient.getDatabase("PantryPal");
    //         MongoCollection<Document> collection = database.getCollection("recipes");
            
    //         Document recipe = new Document("_id", new ObjectId());
    //         recipe.append("title", t);
    //         recipe.append("user", u);
    //         recipe.append("mealtype", m);
    //         recipe.append("ingredients", i);
    //         recipe.append("instructions",ins);            
    //         recipe.append("content", i+ins);

    //         collection.insertOne(recipe);

    //         String query = URLEncoder.encode("u=" + u + "&q=" + t, "UTF-8");
    //         String urlString = "http://IPHOST:8100/?" + query;
    //         URL url = new URI(urlString).toURL();
    //         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    //         conn.setRequestMethod(method);
    //         conn.setDoOutput(true);
    //         BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    //         String response = in.readLine();    
    //         in.close();

    //         assertNotEquals("", response);;
    //         assertTrue(response.contains(t));
    //         assertTrue(response.contains(i));
    //         assertTrue(response.contains(ins));


    //         Bson filter = Filters.and(Filters.eq("title",t),Filters.eq("user", u));
    //         collection.findOneAndDelete(filter);

    //     }
    //     MyServer.stop();
    // }


    /**
     * UNIT TEST
     * Test for just the server handler method to post the corret data
     * 
     * removes added data at the end to make sure to not change user recipes
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void POSTrequestHandlerTest() throws IOException, URISyntaxException{
        MyServer.main(null);


        // have a recipe in the database already
        String recipeTitle = "pancakes with maple syrup";
        String user = "Bryan";
        String ingred = "flour,eggs,sugar,milk";
        String instructions = "mix ingredients to make batter and then pour into hot pan";
        String mealtype = "breakfast";
        String img = "test-img";
        String method = "POST";

        //String query = URLEncoder.encode("u=" + user + "&q=" + recipeTitle, "UTF-8");
        String urlString = "http://"+IPHOST+":8100/";
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);


        // writing to the body of the request
        String reqBody = user + "+" + recipeTitle + "+" + ingred + "+" + instructions + "+" + mealtype + "+" + img;
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(URLEncoder.encode(reqBody, "UTF-8"));
        out.flush();
        out.close();


        // reading the input
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.readLine();    
        in.close();
        assertNotEquals("invalid post", response);

        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("recipes");
      
            Bson filter = eq("title", recipeTitle);
            Bson filter2 = eq("user",user);
            filter = combine(filter,filter2);

            // checkign that post method correctly added to database
            Document recipe = collection.find(filter).first();
            assertEquals(recipeTitle, recipe.getString("title"));
            assertTrue(recipe.getString("instructions").contains(instructions));
            assertEquals(user,recipe.getString("user"));
            assertEquals(mealtype, recipe.getString("mealtype"));


            // removing newly added recipe 
            collection.findOneAndDelete(filter);
            recipe = collection.find(filter).first();
            assertNull(recipe);
        }
        
        MyServer.stop();
    }


    
    @Test
    void PUTrequestHandlerTest() throws IOException, URISyntaxException{
        MyServer.main(null);
        // have a recipe in the database already channging the ingredients and the instructions
        String recipeTitle = "pancakes";
        String user = "Bryan";
        int random = (int)(Math.random() * 100);
        String ingred = "flour,eggs,sugar,milk," + random + "bacons(number of bacon is random)";
        String instructions = "mix ingredients to make batter and then pour into hot pan with lots of bacon";
        String mealtype = "breakfast";
        String method = "PUT";

        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("recipes");

            Document recipe = new Document("_id", new ObjectId());
            recipe.append("title", recipeTitle);
            recipe.append("user", user);
            recipe.append("content", "no inged or instrucitons");
            recipe.append("mealtype", mealtype);

            collection.insertOne(recipe);

            String urlString = "http://"+IPHOST+":8100/";
            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setDoOutput(true);


            // writing to the body of the request
            String reqBody = user + "+" + recipeTitle + "+" + ingred + "+" +instructions;
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(URLEncoder.encode(reqBody, "UTF-8"));
            out.flush();
            out.close();

            // reading the input
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.readLine();    
            in.close();

            assertEquals("valid put", response);


      
            Bson filter = eq("title", recipeTitle);
            Bson filter2 = eq("user",user);
            filter = combine(filter,filter2);

            // checkign that post method correctly added to database
            Document rec = collection.find(filter).first();
            assertEquals(recipeTitle, rec.getString("title"));
            //assertTrue(rec.getString("content").contains(instructions));
            assertEquals(user,rec.getString("user"));
            assertEquals(mealtype, rec.getString("mealtype"));

        }
        
        MyServer.stop();
    }

    @Test
    void DELETErequestHandlerTest() throws IOException, URISyntaxException{
        MyServer.main(null);

        // setting up a fake recipe to test the DELETE endpoint for requesthandler route
        String t = "testTitle";
        String i = "testIngredients";
        String ins = "testInstructinos";
        String u = "testUser";
        String m = "testMealtype";

        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("recipes");
            
            Document recipe = new Document("_id", new ObjectId());
            recipe.append("title", t);
            recipe.append("ingredients", i);
            recipe.append("instructions", ins);
            recipe.append("user", u);
            recipe.append("mealtype", m);

            collection.insertOne(recipe);
        }

        // starting the delete request
        String method = "DELETE";
        String query = URLEncoder.encode("u=" + u + "&q=" + t, "UTF-8");
        String urlString = "http://"+IPHOST+":8100/?" + query;
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);

        // reading the output
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.readLine();  
        assertEquals("valid delete", response);
        
        in.close();

        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("recipes");

            Bson filter = Filters.and(Filters.eq("title",t),Filters.eq("user", u));
            Document recipe = collection.find(filter).first();
            assertNull(recipe);
        }

        MyServer.stop();
    }

    // just testing Share request handler method,  GET METHOD
    // USER+TITLE+INGREDIENTS+INSTRUCTIONS+MEALTYPE
    // UNIT TEST
    // @Test
    // void GETShareHandlerUnitTest() throws IOException, URISyntaxException{
    //     MyServer.main(null);

    //     String t = "testTitle";
    //     String u = "testUser12";
    //     String m = "testMealtype";
    //     String c = "Testcontent";
    //     String method = "GET";

    //     try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
    //         MongoDatabase database = mongoClient.getDatabase("PantryPal");
    //         MongoCollection<Document> collection = database.getCollection("recipes");
            
    //         Document recipe = new Document("_id", new ObjectId());
    //         recipe.append("title", t);
    //         recipe.append("user", u);
    //         recipe.append("mealtype", m);
    //         recipe.append("instructions", "instructoins");
    //         recipe.append("ingredients", "helo ingridients");
    //         recipe.append("content", c);

    //         collection.insertOne(recipe);

    //         String query = URLEncoder.encode("u=" + u + "&q=" + t, "UTF-8");
    //         String urlString = "http://IPHOST:8100/?" + query;
    //         URL url = new URI(urlString).toURL();
    //         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    //         conn.setRequestMethod(method);
    //         conn.setDoOutput(true);
    //         BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    //         String response = in.readLine();
    //         in.close();

    //         String combined = t + "+" + "helo ingridients" + "+" + "instructoins";
    //         assertEquals(response, combined);

    //         Bson filter = Filters.and(Filters.eq("title",t),Filters.eq("user", u));
    //         collection.findOneAndDelete(filter);

    //     }

    //     // have a recipe in the database already

        
    //     MyServer.stop();
    // }

    // UNIT TEST
    /*
     * Mocks the whisper prompt
     * Takes the mealtype, ingredients and makes it into a prompt
     * Pushes the prompt through Model to check if the Whisper gets the same information
     * compares the prompts
     */
    @Test
    void WhisperPromptTest() throws IOException, URISyntaxException{
        MyServer.main(null);
        String mealType = "lunch";
        String ingredients = "Bacon, Eggs and ham";
        String prompt = "Make me a " + mealType + " recipe with " + ingredients;

        Model model = new Model();
        String response = model.performRequest("POST", mealType, ingredients, null, null, "mockwhisper");
        
        System.out.println(response);

        assertEquals(response, prompt);
        MyServer.stop();
    }

    @Test
    void sortAlphabeticallyTest() throws IOException { 
        Model sortModelTest = new Model();

        String recipe1 = "B";
        String recipe2 = "A";
        String recipe3 = "C";
        String input = recipe1 + "_" + recipe2 + "_" + recipe3;
        String temp = sortModelTest.sortAlphabetically(input);  
        
        String sorted = recipe2 + "_" + recipe1 + "_" + recipe3;
        assertEquals(sorted, temp);
    }

    @Test
    void sortNothingAlphabeticallyTest() throws IOException { 
        Model sortModelTest = new Model();

        String recipe = null;
        String temp = sortModelTest.sortAlphabetically(recipe);  
        assertEquals(null, temp);
    }

    @Test
    void sortAlphabeticallyBDDTest() throws IOException { 
        // Scenario: I have selected alphabetically sort
        Model sortModelTest = new Model();
        String recipe1 = "Grilled Cheese";
        String recipe2 = "Scrambled Eggs";
        String recipe3 = "Nutella French Toast";
        String input = recipe1 + "_" + recipe2 + "_" + recipe3;
        // Given: I have an account and recipes saved and I want to see recipes in alphabetical order
        // When: I press the sort button and click on alphabetical order
        String sorted = sortModelTest.sortAlphabetically(input);  
        // Then: I should see my recipes sorted alphabetically by the title
        String expected = recipe1 + "_" + recipe3 + "_" + recipe2;
        assertEquals(expected, sorted);
    }

    @Test
    void sortRAlphabeticallyTest() throws IOException { 
        Model sortModelTest = new Model();

        String recipe1 = "B";
        String recipe2 = "A";
        String recipe3 = "C";
        String input = recipe1 + "_" + recipe2 + "_" + recipe3;
        String temp = sortModelTest.sortRAlphabetically(input);  
        
        String sorted = recipe3 + "_" + recipe1 + "_" + recipe2;
        assertEquals(sorted, temp);
    }

    @Test
    void sortNothingRAlphabeticallyTest() throws IOException { 
        Model sortModelTest = new Model();

        String recipe = null;
        String temp = sortModelTest.sortRAlphabetically(recipe);  
        assertEquals(null, temp);
    }

    @Test
    void sortRAlphabeticallyBDDTest() throws IOException { 
        // Scenario: I have selected reverse alphabetically sort
        Model sortModelTest = new Model();
        String recipe1 = "Grilled Cheese";
        String recipe2 = "Scrambled Eggs";
        String recipe3 = "Nutella French Toast";
        String input = recipe1 + "_" + recipe2 + "_" + recipe3;
        // Given: I have an account and recipes saved and I want to see recipes in reverse alphabetical order
        // When: I press the sort button and click on alphabetical order
        String sorted = sortModelTest.sortRAlphabetically(input);  
        // Then: I should see my recipes sorted alphabetically by the title
        String expected = recipe2 + "_" + recipe3 + "_" + recipe1;
        assertEquals(expected, sorted);
    }

    @Test
    void sortChronologicalTest() throws IOException { 
        Model sortModelTest = new Model();

        String recipe1 = "B";
        String recipe2 = "A";
        String recipe3 = "C";
        String input = recipe1 + "_" + recipe2 + "_" + recipe3;
        String temp = sortModelTest.sortChronological(input);  
        
        String sorted = recipe1 + "_" + recipe2 + "_" + recipe3;
        assertEquals(sorted, temp);
    }

    @Test
    void sortNothingChronologicallyTest() throws IOException { 
        Model sortModelTest = new Model();

        String recipe = null;
        String temp = sortModelTest.sortChronological(recipe);  
        assertEquals(null, temp);
    }

    @Test
    void sortChronologicalBDDTest() throws IOException { 
        // Scenario: I want newest recipes first
        Model sortModelTest = new Model();
        String recipe1 = "Grilled Cheese";
        String recipe2 = "Scrambled Eggs";
        String recipe3 = "Nutella French Toast";
        String input = recipe1 + "_" + recipe2 + "_" + recipe3;
        // Given: I have an account and recipes saved and I want to see recipes in newest to oldest
        // When: I press the sort button and click on chronological order
        String sorted = sortModelTest.sortChronological(input);
        // Then: I should see my recipes that I have created the latest at the top going down to my oldest created recipe
        String expected = recipe1 + "_" + recipe2 + "_" + recipe3;
        assertEquals(expected, sorted);
    }

    @Test
    void sortRChronologicalTest() throws IOException { 
        Model sortModelTest = new Model();

        String recipe1 = "B";
        String recipe2 = "A";
        String recipe3 = "C";
        String input = recipe1 + "_" + recipe2 + "_" + recipe3;
        String temp = sortModelTest.sortRChronological(input);  
        
        String sorted = recipe3 + "_" + recipe2 + "_" + recipe1;
        assertEquals(sorted, temp);
    }

    @Test
    void sortNothingRChronologicallyTest() throws IOException { 
        Model sortModelTest = new Model();

        String recipe = null;
        String temp = sortModelTest.sortRChronological(recipe);  
        assertEquals(null, temp);
    }

    @Test
    void sortRChronologicalBDDTest() throws IOException { 
        // Scenario: I want the oldest recipe first
        Model sortModelTest = new Model();
        String recipe1 = "Grilled Cheese";
        String recipe2 = "Scrambled Eggs";
        String recipe3 = "Nutella French Toast";
        String input = recipe1 + "_" + recipe2 + "_" + recipe3;
        // Given: I have an account and recipes saved and I want to see the recipes in oldest to newest
        // When: I press the sort button and click on reverse chronological order
        String sorted = sortModelTest.sortRChronological(input);
        // Then: I should see my recipes that I have created the oldest at the top going down to my recent created recipe
        String expected = recipe3 + "_" + recipe2 + "_" + recipe1;
        assertEquals(expected, sorted);
    }

    @Test
    void mealTypeBreakfastTest() throws IOException {
        Model mealTypeTest = new Model();

        String expected = "breakfast";
        String testBreakfast = "I want a breakfast meal";

        assertEquals(expected, mealTypeTest.transcribeMealType(testBreakfast));
    }

    // Scenario: I do not provide one the the 3 listed meal types
    // Given: I have white bread, eggs, bacon, and orange
    // And: I want to make a brunch recipe
    // And: I am prompted to choose either breakfast, lunch, or dinner
    // When I answer with “brunch”
    // Then Then I should be informed that “brunch” is not a valid answer
    // And: I should be prompted again to answer with the provided choices

    @Test
    void mealTypeBreakfastBDDTest() throws IOException {
        // Scenario: I select breakfast as the meal type
        Model mealTypeTest = new Model();
        // Given: I have logged in and am at the home frame
        // And: I have clicked the new Recipe button
        // And: I am prompted to choose either breakfast, lunch, or dinner
        // When: I press record and state "I want a breakfast meal"
        String testBreakfast = "I want a breakfast meal";
        String result = mealTypeTest.transcribeMealType(testBreakfast);
        // Then: The whisper AI should transcribe my message and select breakfast
        String expected = "breakfast";
        assertEquals(expected, result);
    }

    @Test
    void mealTypeLunchTest() throws IOException {
        Model mealTypeTest = new Model();

        String expected = "lunch";
        String testLunch = "A lunch is the meal I want";

        assertEquals(expected, mealTypeTest.transcribeMealType(testLunch));
    }

    @Test
    void mealTypeLunchBDDTest() throws IOException {
        // Scenario: I select lunch as the meal type
        Model mealTypeTest = new Model();
        // Given: I have logged in and am at the home frame
        // And: I have clicked the new Recipe button
        // And: I am prompted to choose either breakfast, lunch, or dinner
        // When: I press record and state "I am craving lunch"
        String testBreakfast = "I am craving lunch";
        String result = mealTypeTest.transcribeMealType(testBreakfast);
        // Then: The whisper AI should transcribe my message and select lunch
        String expected = "lunch";
        assertEquals(expected, result);
    }

    @Test
    void mealTypeDinnerTest() throws IOException {
        Model mealTypeTest = new Model();

        String expected = "dinner";
        String testDinner = "My meal I am asking for is dinner";

        assertEquals(expected, mealTypeTest.transcribeMealType(testDinner));
    }

    @Test
    void mealTypeDinnerBDDTest() throws IOException {
        // Scenario: I select dinner as the meal type
        Model mealTypeTest = new Model();
        // Given: I have logged in and am at the home frame
        // And: I have clicked the new Recipe button
        // And: I am prompted to choose either breakfast, lunch, or dinner
        // When: I press record and state "I am wondering what I should have for dinner"
        String testBreakfast = "I am wondering what I should have for dinner";
        String result = mealTypeTest.transcribeMealType(testBreakfast);
        // Then: The whisper AI should transcribe my message and select lunch
        String expected = "dinner";
        assertEquals(expected, result);
    }

    @Test
    void mealTypeCapitalLetterTest() throws IOException {
        Model mealTypeTest = new Model();

        String expected = "dinner";
        String testCapital = "I WANT DINNER!";

        assertEquals(expected, mealTypeTest.transcribeMealType(testCapital));
    }

    @Test
    void mealTypeMultipleMealTypesTest() throws IOException {
        Model mealTypeTest = new Model();

        String expected = "breakfast";
        String testMultiple = "I want breakfast, lunch, and dinner";

        assertEquals(expected, mealTypeTest.transcribeMealType(testMultiple));
    }

    @Test
    void mealTypeNoMealTest() throws IOException {
        Model mealTypeTest = new Model();

        String expected = "";
        String testNone = "I do not know what I want";

        assertEquals(expected, mealTypeTest.transcribeMealType(testNone));
    }

    @Test
    void mealTypeNoMealBDDTest() throws IOException {
        // Scenario: I do not select a meal type
        Model mealTypeTest = new Model();
        // Given: I have logged in and am at the home frame
        // And: I have clicked the new Recipe button
        // And: I am prompted to choose either breakfast, lunch, or dinner
        // When: I press record and state "I want some food"
        String testBreakfast = "I want some food";
        String result = mealTypeTest.transcribeMealType(testBreakfast);
        // Then: The whisper AI should transcribe my message and return an empty string
        String expected = "";
        assertEquals(expected, result);
    }
}