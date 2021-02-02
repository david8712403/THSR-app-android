# 高鐵資訊及餐廳資訊App
> 北科大MMS實驗室 - Android homework實作紀錄

## 2021/2/1 功能、頁面整理
### **THSR App功能**
1. 站點地圖資訊
    * 所有高鐵站地標
    * 移動至自己座標
    * 點擊地標後顯示context menu
    * 起終點交換
    ![](https://i.imgur.com/mlKw9uR.jpg)

2. 站點搜尋
    * 點擊站點搜尋，跳轉至高鐵站列表
    * 高鐵站列表顯示站名、地址，搜尋輸入欄
    * 點擊列表上任一站時，返回地圖並移動視角
    ![](https://i.imgur.com/FRxeNqG.jpg)

3. 路線規劃搜尋
    * 顯示車次列表，內容包含車次/發車時間/車程/到站時間
    ![](https://i.imgur.com/lzUBIOv.jpg)

4. 附近餐廳
    * 由context menu點擊附近餐廳，跳轉頁面顯示車站附近2km內的15個餐廳
    ![](https://i.imgur.com/Ba7hOhO.jpg)

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
![](https://i.imgur.com/eWX8FxI.png)

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

### **交通部API申請：**




###### tags: `Android` `MMSLab` `GoogleMap` `實作紀錄`