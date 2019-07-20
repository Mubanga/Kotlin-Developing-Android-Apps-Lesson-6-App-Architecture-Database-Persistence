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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    // For Coroutines Everything Initiates Or Emanates From The Job
    private var _SleepTracker_VM_Job = Job()

    /** Definition of UI Scope. Dispatcher.Main = UI Thread This Is Necessary Because The Coroutine
     *  Will Be Fetching Data From Dao and The Database That Will Eventually Be Returned To The
     *  UI Thread....Hence The UIScope
     */
    private val _UIScope = CoroutineScope(Dispatchers.Main + _SleepTracker_VM_Job)

    private var _tonight = MutableLiveData<SleepNight?>()

    private val _nights = database.getAllNights()

    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent:LiveData<Boolean>
    get() = _showSnackbarEvent

    val startButtonVisible = Transformations.map(_tonight) {
        null == it
    }
    val stopButtonVisible = Transformations.map(_tonight) {
        null != it
    }
    val clearButtonVisible = Transformations.map(_nights) {
        it?.isNotEmpty()
    }

    val navigateToSleepQuality:LiveData<SleepNight>
    get() = _navigateToSleepQuality

    val nightsString = Transformations.map(_nights){ Nights->
        formatNights(Nights,application.resources)
    }

    init {
        initialiseTonight()
    }

    private fun initialiseTonight() {
        _UIScope.launch {
            _tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {

        /**
         *  So Here Is Where We Essentially Change Hands. We Make Sure The DAO and Database Interactions
         *  Occur On A Different Thread "IO" Which Is Optimised For Such Calls/Interactions.
         *  Upon Completion The "night" Variable Is Returned From The IO Context Back To The UI Context
         */
        return withContext(Dispatchers.IO){
            var night = database.getTonight()

            if(night?.EndTimeMilli != night?.StartTimeMilli)
            {
                night = null
            }
            night
        }
    }

    /**
     *  Start Button onClickListner
     */
    fun onStartTracking()
    {
        _UIScope.launch {
            val newNight = SleepNight()
            // You cannot insert directly into the database here because you are operating on the UI
            // Thread. So we need to create a suspend function to launch a coroutine for us to do insert
            // the database entry.
            insertEntry(newNight)
        }
    }

    /**
     *  Stop Button onClickListner
     */
    fun onStopTracking()
    {
        _UIScope.launch {
        val oldNight = _tonight.value ?: return@launch
            oldNight.EndTimeMilli = System.currentTimeMillis()
            updateEntry(oldNight)
            _navigateToSleepQuality.value = oldNight
        }
    }

    /**
     *  Clear Button onClickListner
     */
    fun onClearTracking()
    {
        _UIScope.launch {
            clearDatabaseEntries()
            _tonight.value = null // Invalidate The Tonight Variable
        }
    }

    private suspend fun clearDatabaseEntries() {
        withContext(Dispatchers.IO)
        {
            database.clear()
        }
        _showSnackbarEvent.value = true
    }

    private suspend fun updateEntry(oldNight: SleepNight) {

        withContext(Dispatchers.IO)
        {
            database.update(oldNight)
        }

    }

    private suspend fun insertEntry(newNight: SleepNight) {
       withContext(Dispatchers.IO)
       {
           database.insert(newNight)
       }
    }

    fun doneNavigating()
    {
        // If No SleepQuality Rating Was Selected Then No Reason To Navigate Anywhere
        _navigateToSleepQuality.value = null
    }

    fun doneShowingSnackbar()
    {
        _showSnackbarEvent.value = null
    }

    override fun onCleared() {
        super.onCleared()
        _SleepTracker_VM_Job.cancel()
    }


}

