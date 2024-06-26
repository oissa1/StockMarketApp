package com.plcoding.stockmarketapp.data.mapper

import com.plcoding.stockmarketapp.data.local.CompanyInfoEntity
import com.plcoding.stockmarketapp.data.local.CompanyListingEntity
import com.plcoding.stockmarketapp.data.remote.dto.CompanyInfoDto
import com.plcoding.stockmarketapp.domain.model.CompanyInfo
import com.plcoding.stockmarketapp.domain.model.CompanyListing

fun CompanyListingEntity.toCompanyListing() =
    CompanyListing(
        name = name,
        symbol = symbol,
        exchange = exchange
    )

fun CompanyListing.toCompanyListingEntity() =
    CompanyListingEntity(
        name = name,
        symbol = symbol,
        exchange = exchange
    )

fun CompanyInfoDto.toCompanyInfo() =
    CompanyInfo(
        symbol = symbol ?: "",
        description = description ?: "",
        name = name ?: "",
        country = country ?: "",
        industry = industry ?: ""
    )

fun CompanyInfoEntity.toCompanyInfo() =
    CompanyInfo(
        symbol = symbol,
        description = description,
        name = name,
        country = country,
        industry = industry
    )

fun CompanyInfo.toCompanyInfoEntity() =
    CompanyInfoEntity(
        symbol = symbol,
        description = description,
        name = name,
        country = country,
        industry = industry
    )