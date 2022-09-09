package com.pshakhlovich.microservices_fundamentals.storage.client.controller;


import com.pshakhlovich.microservices_fundamentals.storage.client.model.StorageMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Controller
public class StoragesController {

    private final WebClient webClient;
    private final String storagesBaseUri;

    public StoragesController(WebClient webClient,
                              @Value("${storages.base-uri}") String storagesBaseUri) {
        this.webClient = webClient;
        this.storagesBaseUri = storagesBaseUri;
    }

    @GetMapping(value = "/storages", params = "grant_type=authorization_code")
    public String authorizationCodeGrant(Model model,
                                         @RegisteredOAuth2AuthorizedClient("storage-client-authorization-code")
                                         OAuth2AuthorizedClient authorizedClient) {

        List<StorageMetadata> storages = this.webClient
                .get()
                .uri(storagesBaseUri)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<StorageMetadata>>() {})
                .block();
        model.addAttribute("storages", storages);

        return "index";
    }

    @GetMapping(value = "/storages", params = "grant_type=client_credentials")
    public String clientCredentialsGrant(Model model) {

        List<StorageMetadata> storages = this.webClient
                .get()
                .uri(storagesBaseUri)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(clientRegistrationId("storage-client-client-credentials"))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<StorageMetadata>>() {})
                .block();
        model.addAttribute("storages", storages);

        return "index";
    }

    // '/authorized' is the registered 'redirect_uri' for authorization_code
    @GetMapping(value = "/authorized", params = OAuth2ParameterNames.ERROR)
    public String authorizationFailed(Model model, HttpServletRequest request) {
        String errorCode = request.getParameter(OAuth2ParameterNames.ERROR);
        if (StringUtils.hasText(errorCode)) {
            model.addAttribute("error",
                    new OAuth2Error(
                            errorCode,
                            request.getParameter(OAuth2ParameterNames.ERROR_DESCRIPTION),
                            request.getParameter(OAuth2ParameterNames.ERROR_URI))
            );
        }

        return "index";
    }
}
