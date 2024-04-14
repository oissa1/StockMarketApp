package com.plcoding.stockmarketapp.data.csv

import com.opencsv.CSVReader
import com.plcoding.stockmarketapp.data.mapper.toIntraDayInfo
import com.plcoding.stockmarketapp.data.remote.dto.IntradayInfoDto
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntradayInfoParser @Inject constructor() : CSVParser<IntradayInfo> {
    override suspend fun parse(stream: InputStream): List<IntradayInfo> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            csvReader
                .readAll()
                .drop(1)
                .mapNotNull { line ->
                    val timestamp = line.getOrNull(0) ?: return@mapNotNull null
                    val close = line.getOrNull(4) ?: return@mapNotNull null
                    IntradayInfoDto(
                        timestamp = timestamp,
                        close = close.toDouble()
                    ).toIntraDayInfo()
                }
                .filter {
                    var yesterday = LocalDateTime.now().minusDays(1)
                    var dayOfWeek = yesterday.dayOfWeek
                    while (dayOfWeek.value == 6 || dayOfWeek.value == 7) {
                        yesterday = yesterday.minusDays(1)
                        dayOfWeek = yesterday.dayOfWeek
                    }
                    it.date.dayOfMonth == yesterday.dayOfMonth
                }
                .sortedBy {
                    it.date.hour
                }
                .also {
                    csvReader.close()
                }
        }
    }
}