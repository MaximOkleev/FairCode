package com.team.antiplagiat.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.util.unit.DataSize

@Configuration
@ConfigurationProperties(prefix = "app.zip-import")
class ZipImportProperties {
    var maxFiles: Int = 1000
    var maxEntrySize: DataSize = DataSize.ofMegabytes(5)
}

