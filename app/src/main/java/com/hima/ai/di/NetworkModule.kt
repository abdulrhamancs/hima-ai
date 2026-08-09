package com.hima.ai.di

import com.hima.ai.BuildConfig
import com.hima.ai.data.remote.backend.HimaBackendApi
import com.hima.ai.data.remote.supabase.SupabaseAuthApi
import com.hima.ai.data.remote.supabase.SupabaseRestApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/** The Supabase-project Retrofit — auth and the profiles table. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SupabaseRetrofit

/** Our own Express backend's Retrofit — only `/analyze` today. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            // Bodies carry credentials/tokens; keep this to headers even in a
            // debug build, and never on release.
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.HEADERS
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    /** Supabase requires its anon key on every request, REST or auth. */
    @Provides
    @Singleton
    fun provideSupabaseApiKeyInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    @SupabaseRetrofit
    fun provideSupabaseRetrofit(
        moshi: Moshi,
        logging: HttpLoggingInterceptor,
        apiKeyInterceptor: Interceptor,
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL.ifBlank { "https://placeholder.supabase.co/" })
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @BackendRetrofit
    fun provideBackendRetrofit(moshi: Moshi, logging: HttpLoggingInterceptor): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            // The vision call can genuinely take a while; give it real room
            // rather than a generic short timeout that reads as "broken".
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideSupabaseAuthApi(@SupabaseRetrofit retrofit: Retrofit): SupabaseAuthApi =
        retrofit.create(SupabaseAuthApi::class.java)

    @Provides
    @Singleton
    fun provideSupabaseRestApi(@SupabaseRetrofit retrofit: Retrofit): SupabaseRestApi =
        retrofit.create(SupabaseRestApi::class.java)

    @Provides
    @Singleton
    fun provideHimaBackendApi(@BackendRetrofit retrofit: Retrofit): HimaBackendApi =
        retrofit.create(HimaBackendApi::class.java)
}
