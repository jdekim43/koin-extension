package kr.jadekim.di.koin

import org.koin.core.qualifier.Qualifier

class DSQualifier(val name: String, val isReadOnly: Boolean = false) : Qualifier {
    override fun toString(): String = if (isReadOnly) {
        "$name-readonly-datasource"
    } else {
        "$name-datasource"
    }
}

class DBQualifier(val name: String) : Qualifier {
    val dsQualifier: DSQualifier by lazy { DSQualifier(name) }
    val readOnlyDSQualifier: DSQualifier by lazy { DSQualifier(name, true) }

    override fun toString(): String = "$name-db"
}

class RedisQualifier(val name: String) : Qualifier {
    override fun toString(): String = "$name-redis"
}