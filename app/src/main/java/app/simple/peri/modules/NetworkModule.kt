package app.simple.peri.modules

import app.simple.peri.interfaces.WallhavenApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://wallhaven.cc/api/v1/"

    @Provides
    @javax.inject.Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @javax.inject.Singleton
    fun provideWallhavenApi(retrofit: Retrofit): WallhavenApi {
        return retrofit.create(WallhavenApi::class.java)
    }
}
