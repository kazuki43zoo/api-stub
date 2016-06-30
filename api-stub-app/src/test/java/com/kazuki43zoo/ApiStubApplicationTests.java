package com.kazuki43zoo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ApiStubApplication.class)
@WebIntegrationTest(randomPort = true)
public class ApiStubApplicationTests {

    @Test
    public void contextLoads() {
    }

}
