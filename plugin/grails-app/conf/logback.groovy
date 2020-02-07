//
// See http://logback.qos.ch/manual/groovy.html for details on configuration
//
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.core.util.FileSize
import grails.util.BuildSettings
import grails.util.Environment
import grails.util.Metadata
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset

statusListener(NopStatusListener)

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

scan("30 seconds")

def APP_NAME = Metadata.current.getApplicationName()
def APP_VERSION = Metadata.current.getApplicationVersion()
def LOG_HOME = System.getProperty("log.home") ?: "${BuildSettings.BASE_DIR}/logs"

appender('STDOUT', ConsoleAppender) {
    filter(ThresholdFilter) {
        level = INFO
    }
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')
        pattern =
            '%clr(%d{HH:mm:ss.SSS}){faint}: ' + // Date
                '%clr(%5p) - ' + // Log level
                '%clr(%-30.30logger{29}){cyan} %clr(:){faint} ' + // Logger
                '%m%n%wex' // Message
    }
}

def createFileAppender = { String name, String suffix, Level threshold, Integer history ->
    appender(name, RollingFileAppender) {
        file = "${LOG_HOME}/${APP_NAME}-${suffix}.log"
        encoder(PatternLayoutEncoder) {
            charset = Charset.forName('UTF-8')
            pattern = '%d{yyyy-MM-dd HH:mm:ss.SSS} ' + // Date
                '[%5p] ' + // Log level
                '%logger{29} : ' + // Logger
                '%m%n%wex' // Message
        }
        filter(ThresholdFilter) {
            level = threshold
        }
        rollingPolicy(TimeBasedRollingPolicy) {
            maxHistory = history
            fileNamePattern = "${LOG_HOME}/archive/${APP_NAME}-${suffix}.%d.log"
            if (Environment.isDevelopmentMode()) {
                totalSizeCap = FileSize.valueOf("500MB")
            }
        }
    }
}

createFileAppender("TF_INFO",  "info",  INFO,  30)
createFileAppender("TF_ERROR", "error", ERROR, 90)

// These loggers are enabled by default in all modes
def loggedPackages = ['groovy.grails', 'com.triu', 'common', 'trifleet', 'liquibase', 'com.agileorbit.schwartz.listener']

logger("org.hibernate.cache.ehcache.AbstractEhcacheRegionFactory", OFF)

if (Environment.current == Environment.PRODUCTION && !Boolean.getBoolean('add.common.config')) {
    loggedPackages.each {
        logger(it, INFO, ['STDOUT', 'TF_INFO', 'TF_ERROR'], false)
    }

    root(ERROR, ['STDOUT'])
}
else {
    createFileAppender("TF_SQL", "sql", TRACE, 5)
    createFileAppender("TF_DEBUG", "debug", DEBUG, 5)
    createFileAppender("TF_CONSOLE", "console", INFO, 5)

    loggedPackages.each {
        logger(it, DEBUG, ['STDOUT', 'TF_INFO', 'TF_ERROR', 'TF_DEBUG'], false)
    }

    // SQL Logging
    logger("org.hibernate.SQL", DEBUG, ["TF_SQL"], false)
    logger("org.hibernate.type.descriptor.sql.BasicBinder", TRACE, ["TF_SQL"], false)
    logger("org.hibernate.engine.internal", OFF)
    logger("org.hibernate.orm.deprecation", OFF)
    logger("org.hibernate.tool.schema.internal.ExceptionHandlerLoggedImpl", ERROR)

    root(INFO, ['STDOUT', 'TF_CONSOLE'])
}
