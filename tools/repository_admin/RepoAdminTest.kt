package com.lomipe.devhub.core
import org.junit.Test
import org.junit.Assert.*
class RepoAdminTest {
    @Test fun topicsNormalized(){assertEquals(listOf("android","kotlin"),RepoAdmin.topicNames("Android, kotlin android"))}
    @Test fun rejectsInvalidTopic(){assertThrows(IllegalArgumentException::class.java){RepoAdmin.topicNames("bad/topic")}}
    @Test fun rejectsUnsafePath(){assertThrows(IllegalArgumentException::class.java){RepoAdmin.part("a/b")};assertThrows(IllegalArgumentException::class.java){RepoAdmin.part("..")}}
    @Test fun checksWebsite(){RepoAdmin.website("https://example.com");assertThrows(IllegalArgumentException::class.java){RepoAdmin.website("http://example.com")};assertThrows(IllegalArgumentException::class.java){RepoAdmin.website("https://user@example.com")}}
    @Test fun encodesRef(){assertEquals("feature%2Fone",RepoAdmin.ref("feature/one"))}
}
