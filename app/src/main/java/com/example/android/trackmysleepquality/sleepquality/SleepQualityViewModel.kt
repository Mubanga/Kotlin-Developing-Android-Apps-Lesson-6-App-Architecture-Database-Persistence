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

package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import kotlinx.coroutines.*

class SleepQualityViewModel(private val sleepNightKey: Long = 0L, val database: SleepDatabaseDao) : ViewModel() {
    /** SETTING UP COROUTINES
     *  For Setting Up You Coroutine You Need To Define The Following
     *  1) A Job To Execute The Tasks
     *  2) The Scope Of The Coroutine
     *  2.1) If This Is A ViewModel Then It Will Be Coming From The Main Thread + ViewModelJob( "SleepQuality_VM_Job" )
     *  3) ***N.B*** Don't Forget To Terminate Or Clear The Coroutine (OVERRIDE onCleared() ViewModelJob.cancel() )
     */
    private val _SleepQuality_VM_Job = Job()
    private val _UIScope = CoroutineScope(Dispatchers.Main + _SleepQuality_VM_Job)
    private val _navigateToSleepTracker = MutableLiveData<Boolean?>()

    val navigateToSleepTracker: LiveData<Boolean?>
    get() = _navigateToSleepTracker


    fun doneNavigating()
    {
        _navigateToSleepTracker.value = false
    }

    fun onSetSleepQuality(sleepQuality:Int)
    {
        _UIScope.launch {
            withContext(Dispatchers.IO)
            {
                /** Basically the Elvis Operator " ?: " Here Means That If No Key Is Found i.e. sleepNightKey = null
                 * Then We Will Be Returning Immediately From The Coroutine, However If We Are Dealing With The
                 * Situation Where We Find An Entry Proceed As Normal
                 */
                val tonight = database.get(sleepNightKey) ?: return@withContext
                tonight.SleepQuality = sleepQuality
                database.update(tonight)
            }
            _navigateToSleepTracker.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        _SleepQuality_VM_Job.cancel()
    }
}

