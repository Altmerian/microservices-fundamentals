package contracts.song

import org.springframework.cloud.contract.spec.Contract;

Contract.make {
    request {
        method POST()
        url '/songs'
        body(
            name: "We are the champions",
            artist: "Queen",
            album: "News of the world",
            length: "2:49",
            resourceId: 7,
            year: 1977
        )
        headers {
            contentType('application/json')
        }
    }
    response {
        status CREATED()
        body(id: 1)
        headers {
            contentType('application/json')
        }
    }
}
