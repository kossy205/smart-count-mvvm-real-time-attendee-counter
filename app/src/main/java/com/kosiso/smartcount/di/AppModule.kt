package com.kosiso.smartcount.di

import android.content.Context
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kosiso.foodshare.repository.LocationRepository
import com.kosiso.foodshare.repository.LocationRepositoryImplementation
import org.imperiumlabs.geofirestore.GeoFirestore
import com.kosiso.smartcount.database.CountDao
import com.kosiso.smartcount.database.RoomDatabase
import com.kosiso.smartcount.database.UserDao
import com.kosiso.smartcount.repository.MainRepoImpl
import com.kosiso.smartcount.repository.MainRepository
import com.kosiso.smartcount.utils.Constants
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
    fun provideUserDao(db: RoomDatabase) =  db.userDao()

    @Singleton
    @Provides
    fun provideMainRepository(countDao: CountDao,
                              userDao: UserDao,
                              firebaseAuth: FirebaseAuth,
                              firestore: FirebaseFirestore,
                              geoFirestore: GeoFirestore
    ): MainRepository{
        return MainRepoImpl(
            countDao,
            userDao,
            firebaseAuth,
            firestore,
            geoFirestore
        )
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Singleton
    @Provides
    fun provideGeoFirestore(
        firestore: FirebaseFirestore
    ): GeoFirestore{
        Log.i("GeoFirestore", "Providing GeoFirestore instance")
        return GeoFirestore(firestore.collection(Constants.AVAILABLE_USERS))
    }



    @Singleton
    @Provides
    fun provideLocationRepository(fusedLocationProviderClient: FusedLocationProviderClient,
                                  locationRequest: LocationRequest): LocationRepository {
        return LocationRepositoryImplementation(fusedLocationProviderClient, locationRequest)
    }
    @Singleton
    @Provides
    fun provideFusedLocationClient(@ApplicationContext app: Context):FusedLocationProviderClient{
        return LocationServices.getFusedLocationProviderClient(app)
    }
    @Singleton
    @Provides
    fun providelocationRequest():LocationRequest{
        return LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000 // 5 seconds
        }
    }

}