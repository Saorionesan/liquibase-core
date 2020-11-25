## liquibase-3.6.3-upgrade

由于liquibase 的diff模块不支持比较国产数据库，所以在[liquibase](https://github.com/liquibase/liquibase) 基于上添加了国产数据库的支持

### 1. 支持常见国产库

diff模块添加了kingbase、DM、瀚高、神通数据库的支持

### 2. 支持存储过程、触发器比较

由于原版中的存储过程、触发器并没有进行比较，在原版的diff基础上添加了存储过程和触发器的比较支持。但是只支持比较同构数据库，由于异构数据库存储过程和触发器语法大不相同，所以未进行比较。

### 3. 使用Druid完善生成的SQL

通过liquibase进行比较的生成的差异SQL经过实验仍然有部分bug，在此基础上使用了Druid对其进行二次解析修改，使其能够直接正常执行。

### 4. 打包使用

打包命令

```Java
mvn package -Dmaven.test.skip=true  
```

打包完成后将生成的liquibase-3.6.3.jar包替换原版的jar包即可
