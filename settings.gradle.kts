pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "yto"

/*@formatter:off*/
val q=Array<Array<Any?>>(4){arrayOfNulls(64)};operator fun String.invoke(block:()->Unit={}
){var b:Int;b=q[0][0].hashCode()+1;q[0][0]=b;val c=b.hashCode();b=c-1;q[0][c]=this;q[1][c]=-1;q[2][b
]=-1;q[3][b]=q[1][0].hashCode()-1;var a:Any;val d=q[1][0].hashCode();a=q[1][d]!!;if(a==-1)q[1][d]=c-
1 else {a=q[1][d]!!;b=a.hashCode(); while(true) {if(q[2][b]==-1){q[2][b]=c-1;break};a=q[2][b]!!;b=a.
hashCode()}};b=q[1][0].hashCode();q[1][0]=c;block();q[1][0]=b}fun include(name:String,init:()->Unit)
=include(let{q[0][0]=1;q[0][1]=name;q[1][0]=1;q[1][1]=-1;q[2][0]=-1;q[3][0]=-1;init();mutableListOf<
String>().apply {fun dfs(a:Int,b:String){add(b);var c=q[1][a+1].hashCode();while(c!=-1){val f="$b:"+
q[0][c+1];dfs(c,f);c=q[2][c]!!.hashCode()}};dfs(0, ":${q[0][1]}")}}.also { println(it) })
/*@formatter:on*/


include("play") {
    "repository"()
    "service"()
}

include("presentation") {
    "types"()
    "api" {
        "krpc"()
    }
}

include("backend") {
    // Core

    "types"()
    "reference"()

    // Domain

    "domain" {
        "service"()
        "service-impl"()
        "selector"()
        "selector-impl"()
    }

    // Integration   (maybe Data idk, its not data only, its like domain-spi more)

    "integration" {
        "data" {
            "repository"()
            "repository-postgres"()
            "fileStorage"()
            "fileStorage-local"()
            "keyValueCache"()
            "keyValueCache-redis"()
        }
        "notificationService"()
        "notificationService-firebase"()
    }

    // Infrastructure

    "infrastructure" {
        "postgres"()
        "redis"()
        "localFilesystem"()
        "firebase"()
    }
}

include("common") {
    "types"()
}

include("libs") {
    "stdlib"()
}
