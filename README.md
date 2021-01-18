❕说明：基于此 https://github.com/kungfoo/geohash-java 项目增加PolygonGeoHash

## 功能介绍
该工具提供了geohash相关操作的方法，主要包括：

1) 经纬度转GeoHash及GeoHash相关操作

2) 通过边界分形的手段将指定的地理围栏栅格化，返回geohash集合。

## 核心类介绍
### GeoHash
该类主要提供了按照指定的geohash长度(字符串长度或者bit位长度)，将经纬度转为对应的GeoHash对象，以及GeoHash相关的操作，具体操作方法见：com.getui.isg.geohash.sdk.GeoHash类。

### PolygonGeoHash
该类提供了四种算法，将指定的地理围栏栅格化，并返回GeoHash对象或GeoHash字符串，下面以返回GeoHash字符串为例，对四种算法进行说明

#### 算法简介
算法一：边界分形，非递归实现

实现方法：com.getui.isg.geohash.sdk.PolygonGeoHash#boundaryFractalEvenlySegmentation

算法二：边界分形，递归实现

实现方法：com.getui.isg.geohash.sdk.PolygonGeoHash#boundaryFractalSegmentation

算法三：瓦片思想，非边界分形

实现方法：com.getui.isg.geohash.sdk.PolygonGeoHash#tilesSegmentation

算法四：按照位偏移

实现方法：com.getui.isg.geohash.sdk.PolygonGeoHash#offsetStepSegmentation


#### 算法分析
+ 算法一和算法二的思想：

首先用粗精度的GeoHash对地理围栏进行栅格化，
接着判断当前精度下的GeoHash块是否在地理围栏内
如果在添加到GeoHash结果集合中，如果不存在，则用更细精度的GeoHahs进行栅格化，继续执行2操作
算法一和算法二的思想是一样的，只是实现方式不一样。算法一是非递归，算法二是递归，递归和非递归实现的差别是。

假设总的GeoHash数量设置为：1000，最小的GeoHash长度为：4，最大的GeoHash长度为：7

用递归实现可能会出现，geohash4、geohash5、geohash6 都是只有一个，而geohash7有997个，这样将导致结果偏差很大，但是用非递归就可以避免该问题

+ 算法三的思想：

首先获取地理围栏外界矩形边框
然后根据指定的GeoHash精度，计算从外界矩形边框的从西南角到东北角，经度和维度各自需要移动多少次
最后循环。
该算的思想很简单，也但是大部分应用开始使用的算法，它有一个缺点，那就是在计算前需要确定GeoHash的精度，但是对于未知的地理围栏，很难决定是用多少精度的GeoHash。



+ 算法四的思想：

首先获取地理围栏外界矩形边框
获取外界矩形边框的西南角和东北角的GeoHash对象，计算出两个GeoHash对应的二进制bit为步间距，
最后不断循环
算法三的问题，同样存在于算法四中，更糟糕的是，算法四的性能较差



### 最佳实践
推荐使用：com.getui.isg.geohash.sdk.PolygonGeoHash#boundaryFractalEvenlySegmentation，并根据应用情况指定GeoHash总数大小

