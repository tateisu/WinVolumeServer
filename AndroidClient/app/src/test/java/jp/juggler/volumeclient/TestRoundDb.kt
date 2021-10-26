package jp.juggler.volumeclient


import jp.juggler.volumeclient.MainActivityViewModelImpl.Companion.minDb
import jp.juggler.volumeclient.MainActivityViewModelImpl.Companion.roundDb
import org.junit.Test

import org.junit.Assert.*
import kotlin.math.abs

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestRoundDb {
    @Test
    fun test1() {
        assertEquals("clip low",-minDb, (-minDb*2).roundDb())
        assertEquals("clip high",0f, 10f.roundDb())
        for( i in 0 .. minDb.toInt().times(2) ){
            val inDb = i.toFloat().div(2f).times(-1f)
            val outDb = inDb.roundDb()
            assertTrue("round $inDb=>$outDb",abs(inDb-outDb)<0.1f)
        }
    }
}