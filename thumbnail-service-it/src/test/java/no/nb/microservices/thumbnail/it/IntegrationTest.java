package no.nb.microservices.thumbnail.it;

import no.nb.microservices.thumbnail.*;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

        import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, RibbonClientConfiguration.class})
@WebIntegrationTest("server.port: 0")
public class IntegrationTest {

    @Value("${local.server.port}")
    int port;

    @Autowired
    ILoadBalancer loadBalancer;

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;
    MockWebServer mockWebServer;
    RestTemplate rest;

    @Before
    public void setup() throws Exception {
        rest = new TestRestTemplate();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockWebServer = new MockWebServer();

        // Read mock data
        String mock1 = IOUtils.toString(new ClassPathResource("mock1.json").getInputStream());

        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/service1/method1")) {
                    return new MockResponse().setBody(mock1).setHeader("Content-Type", "application/hal+json; charset=utf-8");
                }
                else if (request.getPath().equals("/service1/method2")) {
                    return new MockResponse().setBody(mock1).setHeader("Content-Type", "application/hal+json; charset=utf-8");
                }
                else if (request.getPath().equals("/service2/method1")) {
                    return new MockResponse().setBody(mock1).setHeader("Content-Type", "application/hal+json; charset=utf-8");
                }
                else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        };
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start();

        BaseLoadBalancer baseLoadBalancer = (BaseLoadBalancer) loadBalancer;
        baseLoadBalancer.setServersList(Arrays.asList(new Server(mockWebServer.getHostName(), mockWebServer.getPort())));
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void helloWorldTest() throws URISyntaxException {
        URI uri = new URI("http://localhost:" + port + "/");
        ResponseEntity<String> response = rest.getForEntity(uri, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private MockResponse getMockResponse(String path) {
        Buffer buffer = new Buffer();
        try {
            InputStream inputStream = new ClassPathResource(path).getInputStream();
            ByteArrayResource byteArrayResource = new ByteArrayResource(IOUtils.toByteArray(inputStream));
            buffer.write(byteArrayResource.getByteArray());
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return new MockResponse().setBody(buffer);
    }
}

@Configuration
class RibbonClientConfiguration {
    @Bean
    public ILoadBalancer ribbonLoadBalancer() {
        return new BaseLoadBalancer();
    }
}