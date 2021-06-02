package vivid.money

import com.google.gson.Gson

object CacheConfig {

    @Volatile
    var gson = Gson()
}