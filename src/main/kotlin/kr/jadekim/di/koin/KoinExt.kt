package kr.jadekim.di.koin

import com.zaxxer.hikari.HikariDataSource
import io.lettuce.core.support.BoundedPoolConfig
import kr.jadekim.db.exposed.CrudDB
import kr.jadekim.db.exposed.ReadDB
import kr.jadekim.redis.lettuce.Redis
import kr.jadekim.redis.lettuce.ReplicaRedis
import kr.jadekim.redis.lettuce.StandaloneRedis
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.onClose
import java.time.Duration
import javax.sql.DataSource

fun Scope.getInt(key: String): Int? {
    return getKoin().getProperty<Int>(key)
}

fun Scope.getInt(key: String, defaultValue: Int): Int {
    return getKoin().getProperty<Int>(key, defaultValue)
}

fun Scope.getBoolean(key: String): Boolean? {
    return getKoin().getProperty<String>(key)?.toBoolean()
}

fun Scope.getBoolean(key: String, defaultValue: Boolean): Boolean {
    return getKoin().getProperty<String>(key)?.toBoolean() ?: defaultValue
}

fun Scope.getString(key: String): String? {
    return getKoin().getProperty(key)
}

fun Scope.getString(key: String, defaultValue: String): String {
    return getKoin().getProperty(key, defaultValue)
}

fun Module.dataSource(
        qualifier: DSQualifier,
        driver: String,
        url: String,
        username: String,
        password: String,
        isReadOnly: Boolean = false,
        name: String = qualifier.name,
        poolSize: Int = 5,
        configure: HikariDataSource.() -> Unit = {}
) {
    single(qualifier) {
        HikariDataSource().apply {
            this.driverClassName = driver
            this.jdbcUrl = url
            this.username = username
            this.password = password
            this.poolName = name
            this.isReadOnly = isReadOnly
            this.maximumPoolSize = poolSize
            connectionTimeout = Duration.ofSeconds(5).toMillis()
            configure()

            //initial with startup
            connection.close()
        }
    }.onClose { it?.close() }
}

fun Module.dataSource(
        qualifier: DSQualifier,
        isReadOnly: Boolean = false,
        name: String = qualifier.name,
        poolSize: Int = 5,
        propertyPrefix: String = if (isReadOnly) "db.$name.readonly." else "db.$name.",
        configure: HikariDataSource.() -> Unit = {}
) {
    single(qualifier) {
        HikariDataSource().apply {
            driverClassName = getString(propertyPrefix + "driver")
            jdbcUrl = getString(propertyPrefix + "url")
            username = getString(propertyPrefix + "username")
            password = getString(propertyPrefix + "password")
            poolName = name
            this.maximumPoolSize = poolSize
            this.isReadOnly = isReadOnly
            connectionTimeout = Duration.ofSeconds(5).toMillis()
            configure()

            //initial with startup
            connection.close()
        }
    }.onClose { it?.close() }
}

fun HikariDataSource.configureMssql() {
    connectionTestQuery = "SELECT 1"
}

fun HikariDataSource.configureMysql() {
    connectionTestQuery = "SELECT 1"
    addDataSourceProperty("useUnicode", "true")
    addDataSourceProperty("characterEncoding", "utf8")
}

fun Module.db(
        qualifier: DBQualifier,
        createDataSource: Boolean = true,
        withReadOnly: Boolean = false,
        readPoolSize: Int = 5,
        crudPoolSize: Int = if (withReadOnly) 3 else readPoolSize,
        configureDataSource: HikariDataSource.() -> Unit = {}
) {
    if (createDataSource) {
        dataSource(qualifier.dsQualifier, poolSize = crudPoolSize, configure = configureDataSource)

        if (withReadOnly) {
            dataSource(
                    qualifier.readOnlyDSQualifier,
                    isReadOnly = true,
                    poolSize = readPoolSize,
                    configure = configureDataSource
            )
        } else {
            single(qualifier.readOnlyDSQualifier) { get<DataSource>(qualifier.dsQualifier) }
        }
    }

    single(qualifier) { CrudDB(get(qualifier.dsQualifier), get(qualifier.readOnlyDSQualifier)) }
}

fun Module.readDB(
        qualifier: DBQualifier,
        createDataSource: Boolean = true,
        poolSize: Int = 5,
        configureDataSource: HikariDataSource.() -> Unit = {}
) {
    if (createDataSource) {
        dataSource(
                qualifier.readOnlyDSQualifier,
                isReadOnly = true,
                poolSize = poolSize,
                configure = configureDataSource
        )
    }

    single(qualifier) { ReadDB(get(qualifier.readOnlyDSQualifier)) }
}

fun Module.standaloneStringRedis(
        qualifier: RedisQualifier,
        host: String,
        port: Int = 6379,
        db: Int = 0,
        password: String? = null,
        keyPrefix: String = "",
        poolSize: Int = BoundedPoolConfig.DEFAULT_MAX_TOTAL
) {
    single(qualifier) { StandaloneRedis.stringCodec(host, port, db, password, keyPrefix, poolSize) }
            .bind(Redis::class)
            .onClose { it?.close() }
}

fun Module.standaloneStringRedis(
        qualifier: RedisQualifier,
        name: String = qualifier.name,
        propertyPrefix: String = "redis.$name."
) {
    single(qualifier) {
        StandaloneRedis.stringCodec(
                getString(propertyPrefix + "host")!!,
                getInt(propertyPrefix + "port", 6379),
                getInt(propertyPrefix + "db", 0),
                getString(propertyPrefix + "password"),
                getString(propertyPrefix + "key.prefix", ""),
                getInt(propertyPrefix + "pool.size", BoundedPoolConfig.DEFAULT_MAX_TOTAL)
        )
    }
            .bind(Redis::class)
            .onClose { it?.close() }
}

fun Module.replicaStringRedis(
        qualifier: RedisQualifier,
        host: String,
        port: Int = 6379,
        db: Int = 0,
        password: String? = null,
        keyPrefix: String = "",
        poolSize: Int = BoundedPoolConfig.DEFAULT_MAX_TOTAL
) {
    single(qualifier) { ReplicaRedis.stringCodec(host, port, db, password, keyPrefix, poolSize) }
            .bind(Redis::class)
            .onClose { it?.close() }
}

fun Module.replicaStringRedis(
        qualifier: RedisQualifier,
        name: String = qualifier.name,
        propertyPrefix: String = "redis.$name."
) {
    single(qualifier) {
        ReplicaRedis.stringCodec(
                getString(propertyPrefix + "host")!!,
                getInt(propertyPrefix + "port", 6379),
                getInt(propertyPrefix + "db", 0),
                getString(propertyPrefix + "password"),
                getString(propertyPrefix + "key.prefix", ""),
                getInt(propertyPrefix + "pool.size", BoundedPoolConfig.DEFAULT_MAX_TOTAL)
        )
    }
            .bind(Redis::class)
            .onClose { it?.close() }
}