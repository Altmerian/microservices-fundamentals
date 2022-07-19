package contracts.song

import org.springframework.cloud.contract.spec.Contract;

Contract.make {
    request {
        method GET()
        url value(consumer(regex('/songs/\\d+')))
        headers {
            contentType('application/json')
        }
    }
    response {
        status OK()
        body(
            name: "We are the champions",
            artist: "Queen",
            album: "News of the world",
            length: "2:59",
            resourceId: 1,
            year: 1977
        )
        headers {
            contentType('application/json')
        }
    }
}
