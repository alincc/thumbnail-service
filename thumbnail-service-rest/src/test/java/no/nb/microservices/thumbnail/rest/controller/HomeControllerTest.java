package no.nb.microservices.thumbnail.rest.controller;

import no.nb.microservices.thumbnail.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class HomeControllerTest {

    private MockMvc mockMvc;

    private HomeController homeController;

    @Before
    public void setup() {
        homeController = new HomeController();
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    }

    @Test
    public void helloWorldTest() throws Exception{
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

}
