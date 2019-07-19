/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 *  Creating Room Databases Is Done With A Singleton Design Pattern Because You Only Ever Really
 *  Want To Have One Instance Present Throughout The Entirety Of The Application At Any One Time
 */

@Database(entities = [SleepNight::class],version = 1,exportSchema = false)
abstract class SleepDatabase :RoomDatabase(){

    abstract val sleepDatabaseDao: SleepDatabaseDao

    companion object{

        // Necessary To Make The Instance Volatile So It Is Updated Regularly And Not Cached
        //
        @Volatile
        private var _INSTANCE:SleepDatabase? = null

        fun getInstance(context: Context) : SleepDatabase{
            /** ***N.B*** Very Important To Synchronize The Database So Multiple Threads Don't Attempt
             *  To Access The Same Database Instance At The Same Time...Hence Synchronized
             */
            synchronized(this)
            {
                var Instance = _INSTANCE
                if(Instance==null)
                {
                    Instance = Room.databaseBuilder(context.applicationContext,
                            SleepDatabase::class.java,
                            "sleep_history_database")
                            .fallbackToDestructiveMigration()
                            .build()
                    // This Might Look Weird But You Are Taking Advantage Of Kotlin's Smart Casting
                    _INSTANCE = Instance
                }

                // This Should Never Be Null Though
                return _INSTANCE!!

            }


        }

    }



}