package com.school.manager.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Placeholder module. Bind repository interfaces to implementations here
 * as you add them in subsequent parts (e.g., AuthRepository, AttendanceRepository).
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule
