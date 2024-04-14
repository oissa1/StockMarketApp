package com.plcoding.stockmarketapp.data.repository

import com.plcoding.stockmarketapp.data.csv.CSVParser
import com.plcoding.stockmarketapp.data.local.StockDatabase
import com.plcoding.stockmarketapp.data.mapper.toCompanyInfo
import com.plcoding.stockmarketapp.data.mapper.toCompanyInfoEntity
import com.plcoding.stockmarketapp.data.mapper.toCompanyListing
import com.plcoding.stockmarketapp.data.mapper.toCompanyListingEntity
import com.plcoding.stockmarketapp.data.remote.StockApi
import com.plcoding.stockmarketapp.domain.model.CompanyInfo
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import com.plcoding.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    db: StockDatabase,
    private val companyListingsParser: CSVParser<CompanyListing>,
    private val intradayInfoParser: CSVParser<IntradayInfo>
) : StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean, query: String
    ): Flow<Resource<List<CompanyListing>>> = flow {
        emit(Resource.Loading(isLoading = true))
        val localListings = dao.searchCompanyListings(query)
        emit(Resource.Success(data = localListings.map { it.toCompanyListing() }))

        val isDbEmpty = localListings.isEmpty() && query.isBlank()
        val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote

        if (shouldJustLoadFromCache) {
            emit(Resource.Loading(false))
            return@flow
        }
        val remoteListings = try {
            val response = api.getListings()
            companyListingsParser.parse(response.byteStream())
        } catch (e: IOException) {
            e.printStackTrace()
            emit(Resource.Error("Couldn't load data"))
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            emit(Resource.Error("Couldn't load data"))
            null
        }

        remoteListings?.let { listings ->
            dao.clearCompanyListings()
            dao.insertCompanyListings(listings.map { it.toCompanyListingEntity() })
            emit(
                Resource.Success(
                    data = dao.searchCompanyListings("")
                        .map { it.toCompanyListing() })
            )
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getCompanyInfo(symbol: String, fetchFromRemote: Boolean): Flow<Resource<CompanyInfo>> = flow {
        emit(Resource.Loading(isLoading = true))
        val localInfo = dao.getCompanyInfo(symbol)
        if (localInfo != null) {
            emit(Resource.Success(data = localInfo.toCompanyInfo()))
        }

        val shouldJustLoadFromCache = localInfo != null && !fetchFromRemote
        if (shouldJustLoadFromCache) {
            emit(Resource.Loading(false))
            println("OMAARR Just loaded from cache")
            return@flow
        }
        val remoteInfo = try {
            val response = api.getCompanyInfo(symbol)
            response.toCompanyInfo()
        } catch (e: IOException) {
            e.printStackTrace()
            emit(Resource.Error("Couldn't load company info"))
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            emit(Resource.Error("Couldn't load company info"))
            null
        }
        remoteInfo?.let {
            dao.insertCompanyInfo(it.toCompanyInfoEntity())
            emit(Resource.Success(data = it))
            emit(Resource.Loading(false))
            println("OMAARR Just loaded from network")
        }

    }

    override suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try {
            val response = api.getIntradayInfo(symbol)
            val results = intradayInfoParser.parse(response.byteStream())
            Resource.Success(data = results)
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error("Couldn't load intraday info")
        } catch (e: HttpException) {
            e.printStackTrace()
            Resource.Error("Couldn't load intraday info")
        }
    }
}