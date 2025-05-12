package com.example.fetchassessment.ui.theme

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.fetchassessment.Item
import com.example.fetchassessment.MyApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ItemViewModel: ViewModel() {
    private val TAG: String = "CHECK_RESPONSE" // Tag to check  through LogCat
    private val url = "https://fetch-hiring.s3.amazonaws.com/"
    var itemState = mutableStateOf<List<Item>>(emptyList())
    var itemDataMap : HashMap<Int, Int > = HashMap()
    var sortedItemState = mutableStateOf<List<Item>>(emptyList())

    init{
        getData()
    }
    private fun getData() { //Function to retrieve data from URL and add to itemState variable.

        //Start retrofit with URL, give it json path in .create()
        val api = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())//Convert from JSON to kotlin readable
            .build()
            .create(MyApi::class.java) //json path

        api.getItems().enqueue(object : Callback<List<Item>> {
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
}