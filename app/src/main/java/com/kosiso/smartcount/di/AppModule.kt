package com.kosiso.smartcount.di

import com.kosiso.smartcount.repository.MainRepoImpl
import com.kosiso.smartcount.repository.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMainRepository(): MainRepository{
        return MainRepoImpl()
    }
}