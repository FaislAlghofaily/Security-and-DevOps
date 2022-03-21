package com.example.demo.controllers;


import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.demo.model.persistence.User;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class CartControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<User> userJson;
    @Autowired
    private JacksonTester<CreateUserRequest> createJson;

    @Autowired
    private JacksonTester<ModifyCartRequest> cartRequest;




    @Test
    public void addItem() throws Exception {
        User user= createUser("addItem", "addItemPassword");
        String authKey=getAuthKeyForUser(user);


        ModifyCartRequest modifyCartRequest=new ModifyCartRequest();
        modifyCartRequest.setUsername(user.getUsername());
        modifyCartRequest.setItemId(1);
        modifyCartRequest.setQuantity(1);

        mvc.perform(
                        MockMvcRequestBuilders.post("/api/cart/addToCart").content(cartRequest.write(modifyCartRequest).getJson()).header("Authorization", authKey)
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

    }

    @Test
    public void addItemBadRequest() throws Exception {
        User user= createUser("addItemBadRequest", "addItemBadRequestPassword");
        String authKey=getAuthKeyForUser(user);


        ModifyCartRequest modifyCartRequest=new ModifyCartRequest();
        modifyCartRequest.setUsername(user.getUsername());
        modifyCartRequest.setItemId(421);//item not in the database , so its invalid
        modifyCartRequest.setQuantity(1);

        mvc.perform(
                        MockMvcRequestBuilders.post("/api/cart/addToCart").content(cartRequest.write(modifyCartRequest).getJson()).header("Authorization", authKey)
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn();
        //returns not found

        modifyCartRequest.setUsername("invalidUsername");//username does not exist
        modifyCartRequest.setItemId(2);
        modifyCartRequest.setQuantity(1);

        mvc.perform(
                        MockMvcRequestBuilders.post("/api/cart/addToCart").content(cartRequest.write(modifyCartRequest).getJson()).header("Authorization", authKey)
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn();
    }


    @Test
    public void removeItem() throws Exception {
        User user= createUser("removeItem", "removeItemPassword");
        String authKey=getAuthKeyForUser(user);


        ModifyCartRequest modifyCartRequest=new ModifyCartRequest();
        modifyCartRequest.setUsername(user.getUsername());
        modifyCartRequest.setItemId(1);
        modifyCartRequest.setQuantity(1);

        mvc.perform(
                        MockMvcRequestBuilders.post("/api/cart/addToCart").content(cartRequest.write(modifyCartRequest).getJson()).header("Authorization", authKey)
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

    }

    @Test
    public void removeItemBadRequest() throws Exception {
        User user= createUser("removeItemBadRequest", "removeItemBadRequestPassword");
        String authKey=getAuthKeyForUser(user);


        ModifyCartRequest modifyCartRequest=new ModifyCartRequest();
        modifyCartRequest.setUsername(user.getUsername());
        modifyCartRequest.setItemId(422);//item not in the database , so its invalid
        modifyCartRequest.setQuantity(1);

        mvc.perform(
                        MockMvcRequestBuilders.post("/api/cart/removeFromCart").content(cartRequest.write(modifyCartRequest).getJson()).header("Authorization", authKey)
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn();
        //returns not found

        modifyCartRequest.setUsername("invalidUsername");//username does not exist
        modifyCartRequest.setItemId(2);
        modifyCartRequest.setQuantity(1);

        mvc.perform(
                        MockMvcRequestBuilders.post("/api/cart/removeFromCart").content(cartRequest.write(modifyCartRequest).getJson()).header("Authorization", authKey)
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn();
    }



    public User createUser(String username, String password) throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(username);
        createUserRequest.setPassword(password);
        createUserRequest.setConfirmPassword(password);

        MvcResult result = mvc
                .perform(MockMvcRequestBuilders.post("/api/user/create")
                        .content(createJson.write(createUserRequest).getJson()).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username", is(username))).andReturn();
        MockHttpServletResponse response = result.getResponse();

        User user = new User();
        user.setId(Integer.parseInt(response.getContentAsString().replaceAll("[\\D]", "")));
        user.setUsername(username);
        user.setPassword(password);
        return user;

    }

    public String getAuthKeyForUser(User user) throws Exception {
        String body = "{\"username\":\"" + user.getUsername() + "\", \"password\":\"" + user.getPassword() + "\"}";

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/login").content(body)).andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        return response.getHeader("Authorization").toString();

    }

}
