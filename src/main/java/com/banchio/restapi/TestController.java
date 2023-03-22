package com.banchio.restapi;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import com.azure.security.keyvault.secrets.SecretClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.function.Supplier;



@CrossOrigin
@RestController
@RequestMapping("/test")
public class TestController {
  

    private final SecretClient secretClient;
    private static final Sinks.Many<Message<String>> many = Sinks.many().unicast().onBackpressureBuffer();
    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    public TestController(SecretClient secretClient) {
        this.secretClient = secretClient;
    }


    @GetMapping(value = "/getsecret", produces = MediaType.TEXT_PLAIN_VALUE)
    public String GetSecret() throws Exception {
        String mySecretValue = "";
        mySecretValue = secretClient.getSecret("springsecret").getValue();
        return mySecretValue;
    }

    @PostMapping(value="/sendmessage")
    public String send(@RequestParam("message") String message) {
        var m = MessageBuilder.withPayload(message);
        many.emitNext(m.build(), Sinks.EmitFailureHandler.FAIL_FAST);
        return message;
    }

    @Bean
    public Supplier<Flux<Message<String>>> supply() {
        return ()->many.asFlux()
                       .doOnNext(m->LOGGER.info("Manually sending message {}", m))
                       .doOnError(t->LOGGER.error("Error encountered", t));
    }

}
