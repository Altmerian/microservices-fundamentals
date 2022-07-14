package com.pshakhlovich.microservices_fundamentals.resource.testcontainer;

import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
public abstract class ContainerBase {

    @Container
    public static final LocalStackContainer localstack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.14.5"))
                    .withServices(S3);
}
