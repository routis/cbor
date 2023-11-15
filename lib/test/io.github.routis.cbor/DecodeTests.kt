package io.github.routis.cbor

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals


class DecodeTests {


    @Test
    fun `Major 0 single byte`() {
        for (i in 0..23) {
            val expected = DataItem.PositiveInteger(i.toULong())
            val decoded = decode(byteArrayOf(i.toByte()))
            assertEquals(expected, decoded)
        }
    }

    @Test
    fun `Major 0 0b000_01010 is uint 10`() {
        val bytes = byteArrayOf(0b000_01010.toByte())
        val decoded = decode(bytes)
        assertEquals(DataItem.PositiveInteger(10uL), decoded)
    }

    @Test
    fun `0b000_11001 followed by 0x01F4 is uint 500`() {
        val bytes = byteArrayOf(
                0b000_11001.toByte(),
                0x01.toByte(),
                0xF4.toByte(),
        )
        val decoded = decode(bytes)
        assertEquals(DataItem.PositiveInteger(500uL), decoded)
    }

    @Test
    fun `-500`() {
        val bytes = byteArrayOf(
                0b001_11001.toByte(),
                0x01.toByte(),
                0xF3.toByte(),
        )
        val decoded = decode(bytes)
        assertEquals(DataItem.NegativeInteger(-500L), decoded)
    }

    @Test
    fun `vp_token should be decoded`() {
        decodeBase64UrlSafe(vpToken).also { cbor ->
            toJson(cbor)!!.also { println(jsonSupport.encodeToString(it)) }
        }
    }

    @Test
    fun `babis should be decoded`() {
        decodeBase64UrlSafe(babis).also { cbor ->
            toJson(cbor)!!.also { println(jsonSupport.encodeToString(it)) }
        }
    }

    private val jsonSupport = Json { prettyPrint = true }
    private val vpToken = """
    o2d2ZXJzaW9uYzEuMGlkb2N1bWVudHOBo2dkb2NUeXBleBhldS5ldXJvcGEuZWMuZXVkaXcucGlkLjFsaXNzdWVyU2lnbmVkompuYW1lU3BhY2VzoXgYZXUuZXVyb3BhLmVjLmV1ZGl3LnBpZC4xkNgYWFikaGRpZ2VzdElEE2ZyYW5kb21QSmzzPZyCDv1T17NLxFK-onFlbGVtZW50SWRlbnRpZmllcmtmYW1pbHlfbmFtZWxlbGVtZW50VmFsdWVpQU5ERVJTU09O2BhYUaRoZGlnZXN0SUQEZnJhbmRvbVDqmfP2aA8nfl5TSHowY8m9cWVsZW1lbnRJZGVudGlmaWVyamdpdmVuX25hbWVsZWxlbWVudFZhbHVlY0pBTtgYWFykaGRpZ2VzdElEGBhmcmFuZG9tUJ1dU9F63fDU9XpbZ5IjMq5xZWxlbWVudElkZW50aWZpZXJqYmlydGhfZGF0ZWxlbGVtZW50VmFsdWXZA-xqMTk4NS0wMy0zMNgYWF6kaGRpZ2VzdElEDmZyYW5kb21QCU2SXHh-buWPbU6RMHcY73FlbGVtZW50SWRlbnRpZmllcnFmYW1pbHlfbmFtZV9iaXJ0aGxlbGVtZW50VmFsdWVpQU5ERVJTU09O2BhYV6RoZGlnZXN0SUQLZnJhbmRvbVDE-UnG_R9fklw8tMfXIQXOcWVsZW1lbnRJZGVudGlmaWVycGdpdmVuX25hbWVfYmlydGhsZWxlbWVudFZhbHVlY0pBTtgYWFWkaGRpZ2VzdElECmZyYW5kb21Q7aHFdl7agjf38513RbhjzHFlbGVtZW50SWRlbnRpZmllcmtiaXJ0aF9wbGFjZWxlbGVtZW50VmFsdWVmU1dFREVO2BhYZKRoZGlnZXN0SUQYGWZyYW5kb21QGVDZsN0X6o47yYU938bdR3FlbGVtZW50SWRlbnRpZmllcnByZXNpZGVudF9hZGRyZXNzbGVsZW1lbnRWYWx1ZW9GT1JUVU5BR0FUQU4gMTXYGFhcpGhkaWdlc3RJRAVmcmFuZG9tUDkVyImEeK9h7opQdhbeSDxxZWxlbWVudElkZW50aWZpZXJtcmVzaWRlbnRfY2l0eWxlbGVtZW50VmFsdWVrS0FUUklORUhPTE3YGFhdpGhkaWdlc3RJRBJmcmFuZG9tUF8F2eAL4JrvyQ0A5tBdfXpxZWxlbWVudElkZW50aWZpZXJ0cmVzaWRlbnRfcG9zdGFsX2NvZGVsZWxlbWVudFZhbHVlZTY0MTMz2BhYVKRoZGlnZXN0SUQMZnJhbmRvbVDm5aPBwr7FsyZX5k1nj2gRcWVsZW1lbnRJZGVudGlmaWVybnJlc2lkZW50X3N0YXRlbGVsZW1lbnRWYWx1ZWJTRdgYWFakaGRpZ2VzdElEAGZyYW5kb21Qi0eHxS49YJdz_yxPWTx3lnFlbGVtZW50SWRlbnRpZmllcnByZXNpZGVudF9jb3VudHJ5bGVsZW1lbnRWYWx1ZWJTRdgYWEqkaGRpZ2VzdElEA2ZyYW5kb21QDwYENdGWt-XUq7gi1iPEbHFlbGVtZW50SWRlbnRpZmllcmZnZW5kZXJsZWxlbWVudFZhbHVlAdgYWFGkaGRpZ2VzdElED2ZyYW5kb21Q4NMx_u8EloVTQkzjZ7GShXFlbGVtZW50SWRlbnRpZmllcmtuYXRpb25hbGl0eWxlbGVtZW50VmFsdWViU0XYGFhPpGhkaWdlc3RJRAJmcmFuZG9tUFE9zNuMOez7t6LQ5jnQVRBxZWxlbWVudElkZW50aWZpZXJrYWdlX292ZXJfMThsZWxlbWVudFZhbHVl9dgYWFGkaGRpZ2VzdElEFmZyYW5kb21Qa0Mdsk6zlTuKvl_ifh-rGXFlbGVtZW50SWRlbnRpZmllcmxhZ2VfaW5feWVhcnNsZWxlbWVudFZhbHVlGCbYGFhUpGhkaWdlc3RJRA1mcmFuZG9tUHVeV2RpUa1pvkam8EBOMHdxZWxlbWVudElkZW50aWZpZXJuYWdlX2JpcnRoX3llYXJsZWxlbWVudFZhbHVlGQfBamlzc3VlckF1dGiEQ6EBJqEYIVkChTCCAoEwggImoAMCAQICCRZK5ZkC3AUQZDAKBggqhkjOPQQDAjBYMQswCQYDVQQGEwJCRTEcMBoGA1UEChMTRXVyb3BlYW4gQ29tbWlzc2lvbjErMCkGA1UEAxMiRVUgRGlnaXRhbCBJZGVudGl0eSBXYWxsZXQgVGVzdCBDQTAeFw0yMzA1MzAxMjMwMDBaFw0yNDA1MjkxMjMwMDBaMGUxCzAJBgNVBAYTAkJFMRwwGgYDVQQKExNFdXJvcGVhbiBDb21taXNzaW9uMTgwNgYDVQQDEy9FVSBEaWdpdGFsIElkZW50aXR5IFdhbGxldCBUZXN0IERvY3VtZW50IFNpZ25lcjBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABHyTE_TBpKpOsLPraBGkmU5Z3meZZDHC864IjrehBhy2WL2MORJsGVl6yQ35nQeNPvORO6NL2yy8aYfQJ-mvnfyjgcswgcgwHQYDVR0OBBYEFNGksSQ5MvtFcnKZSPJSfZVYp00tMB8GA1UdIwQYMBaAFDKR6w4cAR0UDnZPbE_qTJY42vsEMA4GA1UdDwEB_wQEAwIHgDASBgNVHSUECzAJBgcogYxdBQECMB8GA1UdEgQYMBaGFGh0dHA6Ly93d3cuZXVkaXcuZGV2MEEGA1UdHwQ6MDgwNqA0oDKGMGh0dHBzOi8vc3RhdGljLmV1ZGl3LmRldi9wa2kvY3JsL2lzbzE4MDEzLWRzLmNybDAKBggqhkjOPQQDAgNJADBGAiEA3l-Y5x72V1ISa_LEuE_e34HSQ8pXsVvTGKq58evrP30CIQD-Ivcya0tXWP8W_obTOo2NKYghadoEm1peLIBqsUcISFkE1tgYWQTRpmd2ZXJzaW9uYzEuMG9kaWdlc3RBbGdvcml0aG1nU0hBLTI1Nmdkb2NUeXBleBhldS5ldXJvcGEuZWMuZXVkaXcucGlkLjFsdmFsdWVEaWdlc3RzoXgYZXUuZXVyb3BhLmVjLmV1ZGl3LnBpZC4xuBoAWCAxMjgJkpvspLKPMN-Pnty4oh4maG2wtTFoemKpj_m5lgFYIH1Zh6Dbdoa5uXkKnqkxeq9h6djxynaGjSNEiqjsfbqnAlggTPupXMvLYNCuUxRiJH6g5qXfcWJIZ3ksZdcBA3pRwVUDWCB09uPPCqgMuIAJjQn2YRgmBl_tT9_3E9e0Jt3wburrMQRYIKyhscjEnmppDBFnWXIexzJ6VA3qO_0O_28WUVZ7Do73BVggTnDM3Imck9Hyok1Bmq4AZppNorJD00rpxc5BcXL5DJIGWCAo2rB9bGnbf9Xk5SYW22eaR2gxf1fj8a5rvDrpg66_jgdYIGo9ozvjL4bMgj7DuocMDAW5625WzDTj_BYNkHWFtgWOCFgg33OYzqyvNX-DNFMfs2ylTqBLJLnd8Yrm-ifEwMKPOhcJWCC31ffovu9YfCMFuUG7bdA3aqLaAEslQSq3whIl2pftugpYIC871wTY0U5kzcdUgKT0-HU2vOQGK_R7xTYzMeHdNW-nC1ggOWtn_Y2bEeQPO-5c0ULn9SbuLqx3et9VPV_97W_4ak0MWCCfycCaJs0xNbn_hWILpaZFvND6lWlm0oWRXTr_fA3BeA1YIKh7sCAEOwZJgTTy8o1xKD3B3x377CyYJX60oNthxPOtDlggwlmw_3oWKQZuk5zZ6J3WqWs8CPUKBgqQ_-4iiQNi-Z8PWCDrLdImBO7FEZy6AOWUKiDZqCgLy3OIAbhL3r2saF_VQhBYIKfGo-N144iz-NMxZKBHRITm8nE0ehQLPYDjvyRZ9l4eEVggU--gmFBCsGFt4k9ok9OYq1x5yRudR2EI5aoypDqdo5gSWCDWNI6q2jc9agynYGJAcI2iNFmoBYtErVC0_M-gKmrGSBNYIJ7xfKUNEwKhWfOX7XepB-wbgTRbZvBaGWXLYNb3Gf9AFFgg5C3mvEIKHsvvRRew19IErl0H7LcjBuozBqiUAqgpkWUVWCDCgU5RvDr9vu213R4u8rPTZ7h8CiVBDGTnvwH4GOe7RhZYIPOVFTWGsQyS2tAEWHFmpBX0qA33YuedXUdCM_w4El-XF1ggpWkSSNUczHH0KlnfVMYGF91Nh1k4QWNpGBAJKWISthUYGFgg6QCJ07heChHShYpVlRM2H-FmFVTEH_TQPQn4ie44ZGMYGVggDxlSeoNAWC2gPtQRoGXudIsumxrSkV6vi9ePrJYLN_NtZGV2aWNlS2V5SW5mb6FpZGV2aWNlS2V5pAECIAEhWCBUnC7AtGRRpb0PMV3aoCWymw28qjrFbRp6khJkvPK5TSJYIIzHjsTwaoV-LlDUVbScnLjsn1Nqfs6iSqo5-J2FKtPxbHZhbGlkaXR5SW5mb6Nmc2lnbmVkwHQyMDIzLTA3LTEzVDE2OjA4OjQ1Wml2YWxpZEZyb23AdDIwMjMtMDctMTNUMTY6MDg6NDVaanZhbGlkVW50aWzAdDIwMjQtMDctMTNUMTY6MDg6NDVaWEAtug-r916DygJtBmo-uBWynnJDQXy0N7zre1jnDf4qnaRq8OZENCiEfG4Jiw9saECd84Y7BKXZpTK4j1J14dvObGRldmljZVNpZ25lZKJqbmFtZVNwYWNlc9gYQaBqZGV2aWNlQXV0aKFvZGV2aWNlU2lnbmF0dXJlhEOhASag9lhA_5-pTyCo_U85BsOkGlYkKtYJjWfWz0myI6Q6lRWI-qunvUb1Bd3aWYxod5Fx6_NLDJjqs4k0SCKHwLZDhBkIHmZzdGF0dXMA
    """.trimIndent().replace("\n", "")
    private val babis = """
    o2ZzdGF0dXMAZ3ZlcnNpb25jMS4waWRvY3VtZW50c4GiZ2RvY1R5cGV4GGV1LmV1cm9wYS5lYy
    5ldWRpdy5waWQuMWxpc3N1ZXJTaWduZWSiamlzc3VlckF1dGiEQ6EBJqEYIVkC6DCCAuQwggJq
    oAMCAQICFHIybfZjCJp7UA-MPyamhcvCwtLKMAoGCCqGSM49BAMCMFwxHjAcBgNVBAMMFVBJRC
    BJc3N1ZXIgQ0EgLSBVVCAwMTEtMCsGA1UECgwkRVVESSBXYWxsZXQgUmVmZXJlbmNlIEltcGxl
    bWVudGF0aW9uMQswCQYDVQQGEwJVVDAeFw0yMzA5MDIxNzQyNTFaFw0yNDExMjUxNzQyNTBaMF
    QxFjAUBgNVBAMMDVBJRCBEUyAtIDAwMDExLTArBgNVBAoMJEVVREkgV2FsbGV0IFJlZmVyZW5j
    ZSBJbXBsZW1lbnRhdGlvbjELMAkGA1UEBhMCVVQwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAA
    RJBHzUHC0bpmqOtZBhZbDmk94bHOWvem1civd-0j3esn8q8L1MCColNqQkCPadXjJAYsmXS3D4
    -HB9scOshixYo4IBEDCCAQwwHwYDVR0jBBgwFoAUs2y4kRcc16QaZjGHQuGLwEDMlRswFgYDVR
    0lAQH_BAwwCgYIK4ECAgAAAQIwQwYDVR0fBDwwOjA4oDagNIYyaHR0cHM6Ly9wcmVwcm9kLnBr
    aS5ldWRpdy5kZXYvY3JsL3BpZF9DQV9VVF8wMS5jcmwwHQYDVR0OBBYEFIHv9JxcgwpQpka-91
    B4WlM-P9ibMA4GA1UdDwEB_wQEAwIHgDBdBgNVHRIEVjBUhlJodHRwczovL2dpdGh1Yi5jb20v
    ZXUtZGlnaXRhbC1pZGVudGl0eS13YWxsZXQvYXJjaGl0ZWN0dXJlLWFuZC1yZWZlcmVuY2UtZn
    JhbWV3b3JrMAoGCCqGSM49BAMCA2gAMGUCMEX62qLvLZVT67SIRNhkGtAqnjqOSit32uL0Hnlf
    Ly2QmwPygQmUa04tkoOtf8GhhQIxAJueTu1QEJ9fDrcALM-Ys_7kEUB-Ze4w-wEEvtZzguqD3h
    9cxIjmEBdkATInQ0BNClkCgNgYWQJ7pmdkb2NUeXBleBhldS5ldXJvcGEuZWMuZXVkaXcucGlk
    LjFndmVyc2lvbmMxLjBsdmFsaWRpdHlJbmZvo2ZzaWduZWTAdDIwMjMtMTEtMDlUMTQ6NTI6Mz
    laaXZhbGlkRnJvbcB0MjAyMy0xMS0wOVQxNDo1MjozOVpqdmFsaWRVbnRpbMB0MjAyNC0wMi0x
    N1QwMDowMDowMFpsdmFsdWVEaWdlc3RzoXgYZXUuZXVyb3BhLmVjLmV1ZGl3LnBpZC4xqQBYID
    5sw3SZR5gy-dq2Hti1SpQwPIqmPLBYzhyERS_szsLjAVgglDXLX4pQBIV3H-RIT2FYLBECnQUP
    UuS6krTINwXgnAECWCCUJ-qaB1TSmJWD7nxa3i9ZU4iOyaBbZkUGXlHj0Vw3bQNYINLsiLYiwL
    I_7LvGVRIEKkaD17tl7no0hq92FX4SQ6tEBFggKPg879SPzr9iWo6vi_2wdFbep33D-izjCrqc
    jFDesSQFWCA2SH-DH52oGL1D4NcVRgaJqWN6You9h8ZH85cr_-AhNQZYIHBtgMUXuj0ifnFect
    PWHgA1XBWTcPpysjfDyaKoy9t_B1ggZeACDTM7d6fCW5Mc8er4_I0EqhLzzaTvFDf7nSVJ3R4I
    WCDXbFUlmVhTr88PcHI67KL3YOzWQd20-T8TkJqoZ3Cx221kZXZpY2VLZXlJbmZvoWlkZXZpY2
    VLZXmkAQIgASFYIGnFtLlPlN2AJ80EKW3PKKa8kROJpSM4x1xABck19Q-lIlgg32KKtkQwHBmc
    rvVxoJZdmFHY4hXRakJrCkl4nSuPTjFvZGlnZXN0QWxnb3JpdGhtZ1NIQS0yNTZYQG4p4t9PWK
    REh0_nGgxWiYV9Oq3rOqTXhE6CYtRAR0ies_BpSVSkUTbZniLcAp-nT0IvI-_le8hWC3iWSChd
    WWpqbmFtZVNwYWNlc6F4GGV1LmV1cm9wYS5lYy5ldWRpdy5waWQuMYnYGFiIpGZyYW5kb21YIM
    O_HzYnttnHb0yHd20C61HPPbnVvhjZ-JAiwzQg_chSaGRpZ2VzdElEAGxlbGVtZW50VmFsdWV4
    ISBGb28gYmF0IGFkbWluaXN0cmF0aXZlIGF1dGhvcml0eXFlbGVtZW50SWRlbnRpZmllcnFpc3
    N1aW5nX2F1dGhvcml0edgYWIOkZnJhbmRvbVgg2RsmtUKTY2LLmr62OuR9XZAmqMtMP1PhpgbB
    yveNO_JoZGlnZXN0SUQBbGVsZW1lbnRWYWx1ZXgkNTc3MTFjMjYtZjBkNC00NmFkLTliYjQtYW
    RkNTE5MDI1YjNkcWVsZW1lbnRJZGVudGlmaWVyaXVuaXF1ZV9pZNgYWGSkZnJhbmRvbVggNh1c
    mH2nCaczqnmqcnamE-2Z9lx3ntu0OmUsXxF0ZVloZGlnZXN0SUQCbGVsZW1lbnRWYWx1ZWVCYW
    Jpc3FlbGVtZW50SWRlbnRpZmllcmpnaXZlbl9uYW1l2BhYbaRmcmFuZG9tWCBC9Pl6hc0JLisV
    2J9IQUfs9ZTXVhCoe85FSFXBK8-qdmhkaWdlc3RJRANsZWxlbWVudFZhbHVl2QPsajIwMjQtMD
    ItMTdxZWxlbWVudElkZW50aWZpZXJrZXhwaXJ5X2RhdGXYGFhspGZyYW5kb21YID_I8pi1wFu6
    o0UN0R7VGHP_ho-AACUBDVnth_UNJ-QRaGRpZ2VzdElEBGxlbGVtZW50VmFsdWXZA-xqMjAyMy
    0xMS0wOXFlbGVtZW50SWRlbnRpZmllcmpiaXJ0aF9kYXRl2BhYX6RmcmFuZG9tWCC_AmIAH-mv
    urJOAnIUGsOy3G7sl8PdFMt3lLSB_fjRO2hkaWdlc3RJRAVsZWxlbWVudFZhbHVl9XFlbGVtZW
    50SWRlbnRpZmllcmppc19vdmVyXzE42BhYZqRmcmFuZG9tWCBqznUqqbCSDDPMzbilMxNZ0JKp
    OCk32u2FBNEDv0vpFWhkaWdlc3RJRAZsZWxlbWVudFZhbHVlZlJvdXRpc3FlbGVtZW50SWRlbn
    RpZmllcmtmYW1pbHlfbmFtZdgYWGakZnJhbmRvbVggLHJ8WFXncKMwTD3LRagOfLkRPbOzHYNZ
    T4wCBPcJAFFoZGlnZXN0SUQHbGVsZW1lbnRWYWx1ZWJGQ3FlbGVtZW50SWRlbnRpZmllcm9pc3
    N1aW5nX2NvdW50cnnYGFhvpGZyYW5kb21YIEp-BnPVwHaFuxEwOOvm5rWfgj1RZRi1yPr86MxX
    to20aGRpZ2VzdElECGxlbGVtZW50VmFsdWXZA-xqMjAyMy0xMS0wOXFlbGVtZW50SWRlbnRpZm
    llcm1pc3N1YW5jZV9kYXRl
    """.trimIndent().replace("\n", "")
}