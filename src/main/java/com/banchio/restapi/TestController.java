package com.banchio.restapi;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import com.azure.security.keyvault.secrets.SecretClient;

@CrossOrigin
@RestController
@RequestMapping("/test")
public class TestController {
    
    private final SecretClient secretClient;
    
    public TestController(SecretClient secretClient) {
        this.secretClient = secretClient;
    }


    @GetMapping(value = "/getsecret", produces = MediaType.TEXT_PLAIN_VALUE)
    public String GetSecret() throws Exception {
        String mySecretValue = "";
        mySecretValue = secretClient.getSecret("springsecret").getValue();
        return mySecretValue;
    }

}
