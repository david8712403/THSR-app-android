# 高鐵資訊及餐廳資訊App
> 北科大MMS實驗室 - Android homework實作紀錄

## 2021/2/1 功能、頁面整理
### **THSR App功能**
1. 站點地圖資訊
    * 所有高鐵站地標
    * 移動至自己座標
    * 點擊地標後顯示context menu
    * 起終點交換
    
    ![](https://i.imgur.com/mlKw9uR.jpg =80%x)

2. 站點搜尋
    * 點擊站點搜尋，跳轉至高鐵站列表
    * 高鐵站列表顯示站名、地址，搜尋輸入欄
    * 點擊列表上任一站時，返回地圖並移動視角
    
    ![](https://i.imgur.com/FRxeNqG.jpg =80%x)

3. 路線規劃搜尋
    * 顯示車次列表，內容包含車次/發車時間/車程/到站時間
    
    ![](https://i.imgur.com/lzUBIOv.jpg =80%x)

4. 附近餐廳
    * 由context menu點擊附近餐廳，跳轉頁面顯示車站附近2km內的15個餐廳
    
    ![](https://i.imgur.com/Ba7hOhO.jpg =80%x)

### **主要頁面歸納**
1. 地圖頁面
2. 高鐵站列表頁面
3. 車次列表頁面
4. 車次詳細內容頁面（時刻表）
5. 附近餐廳列表頁面

## 2021/2/2 公共運輸整合資訊流通服務平臺(PTX)會員申請

### PTX帳號申請
先至 [PTX官網](https://https://ptx.transportdata.tw/PTX/Management/AccountApply) 申請帳號，需填寫申請原因、用途等資料，約三個工作天審核。

經實測，約2天左右收到認證申請通知信：

![](https://i.imgur.com/eWX8FxI.png =50%x)

### API測試
將APP ID及APP Key填入 [官方API文件網站](https://ptx.transportdata.tw/MOTC?t=Rail&v=2#/) 上方產生包含簽章的指令：
![](https://i.imgur.com/yzLQ3Ls.png)

選擇測試的Api，並複製產生出來的Curl指令：
![](https://i.imgur.com/0tgvrpd.png)

在terminal輸入Curl指令：
![](https://i.imgur.com/URktzfi.png)

成功取得所有高鐵站資料：
![](https://i.imgur.com/epqtgcR.png)



## 2021/2/2 地圖頁面實作
### **高鐵取得車站基本資料API：**
>[MOTC Transport API V2](https://ptx.transportdata.tw/MOTC?t=Rail&v=2#/)

取得車站基本資料:
```
https://ptx.transportdata.tw/MOTC/v2/Rail/THSR/Station?$top=30&$format=JSON
```
Response Body:
```
[
  {
    "StationUID": "THSR-0990",
    "StationID": "0990",
    "StationCode": "NAK",
    "StationName": {
      "Zh_tw": "南港",
      "En": "Nangang"
    },
    "StationAddress": "台北市南港區南港路一段313號",
    "OperatorID": "THSR",
    "UpdateTime": "2017-04-11T11:05:00+08:00",
    "VersionID": 1,
    "StationPosition": {
      "PositionLat": 25.05318832397461,
      "PositionLon": 121.60706329345703,
      "GeoHash": "wsqqx0z93"
    },
    "LocationCity": "臺北市",
    "LocationCityCode": "TPE",
    "LocationTown": "南港區",
    "LocationTownCode": "63000090"
  },
  ...
]
```

## **2021/2/3 地圖頁面座標標記：**

設計Station類別：
```
class Station : Serializable{
    lateinit var StationName: Name
    lateinit var StationID: String
    lateinit var StationAddress: String
    lateinit var StationPosition: Position

    class Name {
        val Zh_tw: String = ""
        val En: String = ""
    }

    class Position {
        val PositionLat = 0.0
        val PositionLon = 0.0
    }
}
```
並使用Gson套件將http response轉為Station資料：
```
stations = Gson().fromJson(json, Array<Station>::class.java)
```
在地圖中加入`stations`中所有站點：
```
for (s in stations) {
    Log.d("[StationInfo]", s.toString())
    val marker = MarkerOptions()
    marker.apply {
        position(s.getLatLng())
        title(s.StationName.Zh_tw)
    }
    runOnUiThread {
        val m = mMap.addMarker(marker).apply {
            tag = s.StationID
        }
        mapMarkers.add(m)
    }
}
```
![](https://i.imgur.com/PYou3x1.jpg =30%x)

點擊座標後打萬顯示的dialog，預計參考:
[Bottom Sheet Dialog Fragment in Android](https://androidwave.com/bottom-sheet-dialog-fragment-in-android/)

## **2021/2/4 實作高鐵列表Fragment**
### 高鐵列表顯示
`onCreateView()`接收stations資料
```
stations = arguments?.getSerializable("stations") as Array<Station>
```
新增自訂並繼承`RecyclerView.Adapter`的Adapter類別
```
class StationAdapter(private val stations: Array<Station>): 
    RecyclerView.Adapter<StationAdapter.ViewHolder>() {
    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val tvName = v.findViewById<TextView>(R.id.tv_station_name)
        val tvAddr = v.findViewById<TextView>(R.id.tv_station_address)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int ): ViewHolder {
        ... 
    }

    override fun onBindViewHolder(holder: StationAdapter.ViewHolder, position: Int) {
        ...
    }

    override fun getItemCount(): Int {
        ...
    }
}
```
列表顯示效果：

![](https://i.imgur.com/MbIUHzt.png =50%x)

### RecyclerView item點擊切換fragment issue
App有設計「點擊高鐵站item時，返回地圖頁面並顯示該站地點」的功能。

以往可以在`onCreateView`直接設定`OnItemClickListener`並實作`fragment`之間的處理，但問題是：

**`RecyclerView`沒有`setOnItemClickListener`的設計**

只能在自訂的Adapter內的`onBindViewHolder`個別設定每個item的點擊事件，像這樣：
```
override fun onBindViewHolder(holder: StationAdapter.ViewHolder, position: Int) {
    holder.tvName.text = stations[position].StationName.Zh_tw
    holder.tvAddr.text = stations[position].StationAddress
    holder.itemView.setOnClickListener {
        ...
    }
}
```

這樣會沒有辦法在`StationFragment`內取得`fragmentManager`物件，就沒辦法處理fragment之間轉換與資料傳遞。

為了解決這個問題，解決方法步驟如下：
1. 自訂一個`interface`，點擊時傳回該高鐵站的`stationId`，像這樣：
    ```
    interface StationAdapterCallback {
        fun onClick(stationId: String)
    }
    ```
2. 並在自訂的`StationAdapter`中加入member
    ```
    var callback: StationAdapterCallback? = null
    ```
3. 在自訂`StationAdapter`中的`onBindViewHolder`中呼叫callback function
    ```
    override fun onBindViewHolder(holder: StationAdapter.ViewHolder, position: Int) {
        holder.tvName.text = stations[position].StationName.Zh_tw
        holder.tvAddr.text = stations[position].StationAddress
        holder.itemView.setOnClickListener {
            callback?.onClick(stations[position].StationID)
        }
    }
    
    ```
4. 在`StationFragment`的`onCreateView`中，new一個自訂義的interface，並實作`onClick`事件
    ```
    adapter = StationAdapter(stations)
    adapter.callback = object : StationAdapterCallback {
        override fun onClick(stationId: String) {
            ...
        }
    }
    ```
如此一來就可以達到在`RecyclerView`中的item點擊時，處理跳轉fragment並傳遞資料。處理步驟4. `onClick`觸發時fragment跳轉：
```
adapter.callback = object : StationAdapterCallback {
    override fun onClick(stationId: String) {
        val b = Bundle()
        b.putString("stationId", stationId)
        parentFragmentManager.setFragmentResult(REQUEST_KEY, b)
        parentFragmentManager.popBackStack()
    }
}
```


###### tags: `Android` `MMSLab` `GoogleMap` `實作紀錄`