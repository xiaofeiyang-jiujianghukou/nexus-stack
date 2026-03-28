## 1. ClassCastException
Caused by: java.lang.ClassCastException: class java.math.BigInteger cannot be cast to class java.lang.Long (java.math.BigInteger and java.lang.Long are in module java.base of loader 'bootstrap')
问题产生原因:
1) 字段没有对齐
2) kafka传输的数据和接受的表结构不一致
3) mysql的表结构和flink的表结构不一致
4) mysql的对应字段id设为unsigned了,flink默认转为Long了

## 2.网络缓冲区不足
Caused by: java.io.IOException: Insufficient number of network buffers: required 25, but only 5 available. The total number of network buffers is currently set to 2048 of 32768 bytes each. You can increase this number by setting the configuration keys 'taskmanager.memory.network.fraction', 'taskmanager.memory.network.min', and 'taskmanager.memory.network.max'.
问题:buffer不够
解决:
1)代码层面tableEnv.getConfig().getConfiguration().setString("parallelism.default", "1");
2)flink镜像配置层面升级


## 4.CANNOT_UPDATE_COLUMN
java.sql.BatchUpdateException: Code: 420. DB::Exception: Cannot UPDATE key column `ts`. (CANNOT_UPDATE_COLUMN) (version 23.8.16.16 (official build))
解决办法:
1)在sink文件(insert_ck_wide_orders.sql)中加入'sink.update-strategy' = 'insert'


## 3.部署到容器中报错ExecutionConfig
Caused by: java.lang.ClassNotFoundException: org.apache.flink.api.common.ExecutionConfig
