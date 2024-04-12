package com.plcoding.stockmarketapp.di

import android.app.Application
import androidx.room.Room
import com.plcoding.stockmarketapp.data.local.StockDatabase
import com.plcoding.stockmarketapp.data.remote.StockApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesStockApi() =
        Retrofit.Builder()
            .baseUrl(StockApi.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(StockApi::class.java)

    @Provides
    @Singleton
    fun providesStockDatabase(app: Application) =
        Room.databaseBuilder(app, StockDatabase::class.java, "stock_db")
            .fallbackToDestructiveMigration()
            .build()

}