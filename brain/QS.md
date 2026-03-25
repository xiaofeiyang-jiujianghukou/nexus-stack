## 1. ClassCastException
Caused by: java.lang.ClassCastException: class java.math.BigInteger cannot be cast to class java.lang.Long (java.math.BigInteger and java.lang.Long are in module java.base of loader 'bootstrap')
问题产生原因:
1) 字段没有对齐
2) kafka传输的数据和接受的表结构不一致
3) mysql的表结构和flink的表结构不一致
4) mysql的对应字段id设为unsigned了,flink默认转为Long了