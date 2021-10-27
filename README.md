# koin-extension
Moved to https://github.com/jdekim43/jext

* Extension functions
    * Koin property
    * Datasource
    * DB (exposed-extension)
    * Redis (lettuce-extension)

## Install
### Gradle Project
1. Add dependency
    ```
    build.gradle.kts
   
    implementation("kr.jadekim:koin-extension:1.0.0")
    ```

## How to use
### DataSource extension functions
* Use HikariCP
* Load from property
    * db.$name.driver=driverClass
    * db.$name.url=url
    * db.$name.username=username
    * db.$name.password=password
    * 만약 isReadOnly = true 이면 db.$name.readonly.~~~
```
build.gradle.kts

implementation("com.zaxxer:HikariCP:$hikaricpVersion") {
    exclude("org.slf4j", "slf4j-api")
}
implementation("kr.jadekim:common-api-server:$commonApiServerVersion")
```
```
DataSourceModule.kt

val DB_TEST = DBQualifier("test")

val dataSourceModule = module {
    dataSource(
        DB_TEST,
        "driverName",
        "url",
        "username",
        "password",
        isReadOnly = false,
        poolSize = 5
    ) {
        //configure HikariCP
    }

    //Load from property
    //In property
    //db.test.driver=driverClass
    //db.test.url=url
    //db.test.username=username
    //db.test.password=password
    dataSource(
        DB_TEST,
        isReadOnly = false,
        poolSize = 5
    ) {
         //configure HikariCP
    }
}
```
### DB extension functions (exposed-extension)
```
build.gradle.kts

implementation("com.zaxxer:HikariCP:$hikaricpVersion") {
    exclude("org.slf4j", "slf4j-api")
}
implementation("kr.jadekim:common-api-server:$commonApiServerVersion")
implementation("kr.jadekim:exposed-extension:$exposedExtensionVersion")
```
```
DBModule.kt

val dbModule = module {
    //crud
    db(DB_TEST)

    or

    db(
        DB_TEST,
        createDataSource = true, //동일한 DBQualifier 로 생성한 DataSource 가 있다면 false 로 설정
        withReadOnly = false, //read 용 db 가 나뉘어져 있다면 true
        readPoolSize = 5,
        crudPoolSize = 3
    ) {
        //configure HikariCP
    }

    //read only
    readDB(DB_TEST)

    or

    readDB(
        DB_TEST,
        createDataSource = true, //동일한 DBQualifier 로 생성한 DataSource 가 있다면 false 로 설정
        poolSize: Int = 5
    )
}
```
### Redis extension functions (lettuce-extension)
* Load from property
    * redis.$name.host=host
    * redis.$name.port=port
    * redis.$name.db=db
    * redis.$name.key.prefix=PRIFIX:
    * redis.$name.pool.size=4
```
build.gradle.kts

implementation("kr.jadekim:common-api-server:$commonApiServerVersion")
implementation("kr.jadekim:lettuce-extension:$lettuceExtensionVersion")
```
```
RedisModule.kt

val REDIS_TEST = RedisQualifier("test")

val redisModule = module {
    redis(
        REDIS_TEST,
        host = "host",
        port = 6379,
        db = 0,
        keyPrefix = "",
        poolSize = 4
    )

    //Load from property
    redis(REDIS_TEST)
}
```
