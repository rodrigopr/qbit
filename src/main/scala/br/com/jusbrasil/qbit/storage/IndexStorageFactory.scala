package br.com.jusbrasil.qbit.storage

import br.com.jusbrasil.qbit.bijections.SparseBitmapBijection
import br.com.jusbrasil.qbit.bitmap.SparseBitmap
import com.twitter.bijection.Injection
import com.twitter.bijection.Injection.connect
import com.twitter.bijection.netty._
import com.twitter.finagle.redis
import com.twitter.storehaus.cache.MutableTTLCache
import com.twitter.storehaus.redis.RedisStore
import com.twitter.storehaus.{ConvertedStore, Store}
import com.twitter.util.{Duration, Future}
import org.jboss.netty.buffer.ChannelBuffer

object IndexStorageFactory {
  private def redisStore = RedisStore(redis.Client("127.0.0.1:6379"))

  def store: Store[String, SparseBitmap] = {
    implicit val channelBufferCodec = ChannelBufferBijection.inverse

    // Create a chain to convert: String -> Array[Byte] -> ChannelBuffer
    val keyBijection = connect[String, Array[Byte], ChannelBuffer]
    val valueInjection = Injection.fromBijection(SparseBitmapBijection)

    new ConvertedStore(redisStore)(keyBijection)(valueInjection)
  }


  def cachedStore = {
    val underlineCache = MutableTTLCache[String, Future[Option[SparseBitmap]]](Duration.fromSeconds(60), 10000)
    store.withCache(underlineCache)
  }
}
