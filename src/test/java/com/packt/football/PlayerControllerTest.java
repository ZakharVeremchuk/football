package com.packt.football;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.packt.football.exceptions.NotFoundException;
import com.packt.football.model.Player;
import com.packt.football.service.FootballService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/***
 * The @WebMvcTest annotation is used to create MVC (or more specifically controller) related tests.
 * It can also be configured to test for a specific controller.
 * It mainly loads and makes testing of the web layer easy.
 *
 * The @SpringBootTest annotation is used to create a test environment by loading a full application context (like classes annotated with @Component and @Service, DB connections, etc).
 * It looks for the main class (which has the @SpringBootApplication annotation) and uses it to start the application context.
 *
 * Both of these annotations were introduced in Spring Boot 1.4.
 *
 * The @WebMvcTest annotation is located in the org.springframework.boot.test.autoconfigure.web.servlet package, whereas @SpringBootTest is located in org.springframework.boot.test.context.
 * Spring Boot, by default, adds the necessary dependencies to our project assuming that we plan to test our application.
 *
 * https://www.baeldung.com/spring-mockmvc-vs-webmvctest
 */

@WebMvcTest(value = PlayerController.class)
public class PlayerControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FootballService footballService;

    @Test
    public void testPlayers() throws Exception {
        Player player1 = new Player("1884823", 5, "Ivana ANDRES", "Defender", LocalDate.of(1994, 7, 13));
        Player player2 = new Player("325636", 11, "Alexia PUTELLAS", "Midfielder", LocalDate.of(1994, 2, 4));

        List<Player> players = List.of(player1, player2);
        given(footballService.listPlayers()).willReturn(players);

        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/players").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2))).andReturn();

        String json = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        List<Player> returnedPlayers = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Player.class));
        assertArrayEquals(players.toArray(), returnedPlayers.toArray());
    }

    @Test
    public void testReadPlayer_doesnt_exist() throws Exception {
        String id = "1884823";
        given(footballService.getPlayer(id)).willThrow(new NotFoundException("PLayer not found"));
        mvc.perform(MockMvcRequestBuilders.get("/players/" + id)).andExpect(status().isNotFound());
    }

    @Test
    public void testCreatePlayer() throws Exception {
        Player player3 = new Player("325643", 10, "Alexia TestTest", "Midfielder", LocalDate.of(1995, 2, 4));

        // {"id":"325643","jerseyNumber":10,"name":"Alexia TestTest","position":"Midfielder","dateOfBirth":"1995-02-04"}
        String playerJson = objectMapper.writeValueAsString(player3);

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(playerJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    public void testDeletePlayer() throws Exception {
        String id = "1";
        doNothing().when(footballService).deletePlayer(id);
        mvc.perform(MockMvcRequestBuilders.delete("/players/" + id).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
    }

}
