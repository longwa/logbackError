package test

import grails.gorm.transactions.Transactional

@Transactional
class TestService {
    def serviceMethod() {
        log.info("Hello")
    }
}
