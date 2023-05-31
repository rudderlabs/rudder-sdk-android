/*
 * Creator: Debanjan Chatterjee on 28/07/22, 1:56 PM Last modified: 28/07/22, 1:56 PM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.android.sampleapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rudderstack.android.sampleapp.ui.theme.RudderAndroidLibsTheme
import com.rudderstack.android.sampleapp.MainViewModel.Event
import com.rudderstack.android.sampleapp.models.LogData

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RudderAndroidLibsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    CreateButtonsTemplate(viewModel)
                }
            }
        }
    }

}
@Composable
fun CreateButtonsTemplate(viewModel: MainViewModel){
    Column(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        //each row with 3 elements
        CreateRowOfApis(names = arrayOf(Event.INITIALIZE,
        Event.SHUTDOWN, Event.CLEAR), weight = .3f, viewModel = viewModel)
        Spacer(modifier = Modifier.height(8.dp))
        CreateRowOfApis(names = arrayOf(Event.ALIAS,Event.TRACK, Event.SCREEN, ), weight = .3f, viewModel = viewModel)
        Spacer(modifier = Modifier.height(8.dp))
        CreateRowOfApis(names = arrayOf( Event.IDENTIFY,Event.GROUP), weight = .5f, viewModel = viewModel)
        Spacer(modifier = Modifier.height(8.dp))
        CreateRowOfApis(names = arrayOf( Event.OPT_IN,Event.FORCE_FLUSH), weight = .5f, viewModel = viewModel)
        Spacer(modifier = Modifier.height(8.dp))
        CreateRowOfApis(names = arrayOf( Event.SEND_ERROR), weight = .5f, viewModel = viewModel)
        Spacer(modifier = Modifier.height(8.dp))

        CreateLogcat(viewModel.logState.value)
    }
}
//state hoisting?
@Composable
fun CreateRowOfApis(vararg names: String, weight: Float, viewModel: MainViewModel){
    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        names.forEach {
            Button(modifier = Modifier.weight(weight= weight, fill = true), onClick = {
                viewModel.onEventClicked(it)
            }) {
                Text(text = it)
            }
        }

    }
}
@Composable
fun ColumnScope.CreateLogcat(logCatList : List<LogData>){
    LazyColumn( reverseLayout = true, modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
        .weight(1f)) {
            items(logCatList.size, null ){ index ->
                CreateRowData(logData = logCatList[index])
            }
    }
}
@Composable
fun CreateRowData(logData: LogData){
    Text(color = Color.Cyan, text = "${logData.time} - ${logData.log}")
}
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
   CreateButtonsTemplate(MainViewModel(LocalContext.current.applicationContext as Application))
}