package com.kosiso.smartcount.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kosiso.smartcount.database.CountDao
import com.kosiso.smartcount.database.RoomDatabase
import com.kosiso.smartcount.repository.MainRepoImpl
import com.kosiso.smartcount.repository.MainRepository
import com.kosiso.smartcount.utils.Constants.ROOM_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton



@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRoomDatabase(
        @ApplicationContext app: Context
    ) =  Room.databaseBuilder(
        app,
        RoomDatabase::class.java,
        ROOM_DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideCountDao(db: RoomDatabase) =  db.countDao()

    @Singleton
    @Provides
    fun provideMainRepository(countDao: CountDao): MainRepository{
        return MainRepoImpl(countDao)
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

}