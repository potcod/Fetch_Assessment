package com.example.fetchassessment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ScrollView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fetchassessment.ui.theme.FetchAssessmentTheme
import com.example.fetchassessment.ui.theme.ItemCard
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import retrofit2.Callback
import retrofit2.Response

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable


class MainActivity : ComponentActivity() {

    private val TAG: String = "CHECK_RESPONSE" // Tag to check  through LogCat
    private val url = "https://fetch-hiring.s3.amazonaws.com/"
    private var itemState = mutableStateOf<List<Item>>(emptyList())
    var itemDataMap : HashMap<Int, Int > = HashMap()

    private fun getData() { //Function to retrieve data from URL and add to itemState variable.

        //Start retrofit with URL, give it json path in .create()
        val api = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())//Convert from JSON to kotlin readable
            .build()
            .create(MyApi::class.java) //json path

        api.getItems().enqueue(object : Callback<List<Item>>{
            override fun onResponse(
                call: Call<List<Item>?>,
                response: Response<List<Item>?>
            ) {

                if(response.isSuccessful){
                    Log.i(TAG, "Data Retrieved Successfully")
                    response.body()?.let{

                        // Add list to list state
                        for(item in it){
                            if(!item.name.isNullOrBlank()){
                                itemState.value = itemState.value + item;
                                if(itemDataMap.containsKey(item.listId)){
                                    itemDataMap[item.listId] = itemDataMap.getOrDefault(item.listId, 0) + 1
                                } else {
                                    itemDataMap.put(item.listId, 1)
                                }
                            }
                        }
                    }
                    Log.i("HASH", itemDataMap.toString() )
                }

                //Sort item state by "listId" first, then by "names"
                itemState.value = itemState.value.sortedWith(compareBy ({it.listId}, {it.name.substring(5,it.name.length).toInt()}) )
                Log.i(TAG, itemState.value.toString())

            }
            override fun onFailure(
                call: Call<List<Item>?>,
                t: Throwable
            ) {
                Log.e(TAG, "onFailure")
            }
        })
    }
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        getData()
        setContent {
            FetchAssessmentTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "screen_a"
                ){
                    composable("screen_a"){
                        Scaffold(modifier = Modifier.fillMaxSize()) {
                            AppBar(navController)
                            ItemTable(
                                items = itemState.value
                            )
                        }
                    }
                    composable("screen_b"){
                        ScreenB(navController, itemDataMap)
                    }
                }

            }
        }
    }
}

@Composable
fun ItemTable(items: List<Item>) { //Function to sort and display the data from itemState var
    Column(modifier = Modifier.padding(top = 10.dp)) {
        //Header row
        Row(modifier = Modifier.padding(top = 75.dp).background(Color(0xFF300D38))) {
            Text("ID", modifier = Modifier.weight(1f).padding(start = 8.dp), color = Color(0xFFFFD700))
            Text("List ID", modifier = Modifier.weight(1f), color = Color(0xFFFFD700))
            Text("Name", modifier = Modifier.weight(1f), color = Color(0xFFFFD700))
        }

        //Data rows
        LazyColumn( modifier = Modifier.padding(top = 0.dp)) {
            items(items) { item ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(item.id.toString(), modifier = Modifier.weight(1f).padding(start = 8.dp))
                    Text(item.listId.toString(), modifier = Modifier.weight(1f))
                    Text(item.name, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar( navController: NavController //AppBar of main screen A
){
    TopAppBar(title = { Text(text = "Fetch Assessment", fontFamily = FontFamily.Serif, fontSize = 24.sp)},
        navigationIcon = {
            Icon(painter = painterResource(id = R.drawable.images), contentDescription = "FetchIcon",
                tint = Color.Unspecified, modifier = Modifier.padding(bottom = 8.dp, top = 8.dp))

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFFD700),
        ), actions = {
            IconButton(onClick = {navController.navigate("screen_b")}) {
                Icon(Icons.Default.Info, contentDescription = "InfoIcon", tint = Color.Unspecified,)
            }
        }

    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenB(navController: NavController, dataMap : HashMap<Int, Int>){ //Data analytics page
    TopAppBar(
        title = { Text(text = "Fetch Data Analytics", fontFamily = FontFamily.Serif, fontSize = 24.sp) },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("screen_a") }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFFD700)
        )
    )
    Column(modifier = Modifier.padding(top = 90.dp, start = 10.dp)) {
        Text("Item Count by List ID", fontSize = 20.sp, color = Color.Black, )

        var totalItemCount = 0
        dataMap.forEach { (listId, count) ->
            Text(text = "List ID $listId: $count items")
            totalItemCount += count
        }
        Text("Total Amount of Items: $totalItemCount", fontSize = 23.sp)
    }

}


//Deprecated way of showing data
@Composable
fun itemListComposable(items: List<Item> ){
    //AppBar()
    LazyColumn(
        modifier = Modifier.padding(top = 100.dp)
    ) {
        items(items)  { item ->
            ItemCard(item.id, item.listId, item.name)
        }
    }
}