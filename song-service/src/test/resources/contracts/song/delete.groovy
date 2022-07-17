package contracts.song

import org.springframework.cloud.contract.spec.Contract;

Contract.make {
    request {
        method DELETE()
        url value(consumer(regex('/songs/\\d+(,\\d+)*')))
        headers {
            contentType('application/json')
        }
    }
    response {
        status CREATED()
        body(ids: 1)
        bodyMatchers {
            jsonPath('$.ids', byRegex('\\d+(,\\d+)*)'))
        }
        headers {
            contentType('application/json')
        }
    }
}
