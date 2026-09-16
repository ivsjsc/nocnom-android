package vn.ivsjsc.nocnom.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import vn.ivsjsc.nocnom.data.HybridUserStateRepository
import vn.ivsjsc.nocnom.data.UserStateRepository
import vn.ivsjsc.nocnom.data.auth.AuthRepository
import vn.ivsjsc.nocnom.data.auth.FirebaseAuthRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindUserStateRepository(
        repository: HybridUserStateRepository,
    ): UserStateRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        repository: FirebaseAuthRepository,
    ): AuthRepository
}
