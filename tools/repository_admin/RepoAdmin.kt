package com.lomipe.devhub.core

import java.net.URI

class RepoAdmin(private val http: GitHubHttp, repo: String) {
    private val full = checkRepo(repo).also { require(it.split('/').none { p -> p == "." || p == ".." }) }
    private val root = "/repos/" + full
    private suspend fun request(method: String, suffix: String, data: JObj? = null, account: Boolean = false): Any? {
        val bytes = data?.let { Json.write(it).toByteArray(Charsets.UTF_8) }
        val result = http.send(method, http.policy.apiBase + (if (account) "" else root) + suffix,
            contentType = if (bytes == null) null else "application/json",
            length = bytes?.size?.toLong() ?: -1,
            body = bytes?.let { value -> { out, check -> check(); out.write(value) } },
            maxBytes = 4 * 1024 * 1024)
        require(result.code in 200..299) { "Unexpected GitHub response" }
        return if (result.body.isEmpty()) null else result.json()
    }
    private suspend fun obj(path: String) = request("GET", path).asObj() ?: error("Expected object")
    private suspend fun rows(path: String, key: String? = null): List<JObj> {
        val data = request("GET", path)
        return (if (key == null) data.asList() else data.asObj()?.get(key).asList()).mapNotNull { it.asObj() }
    }
    suspend fun about() = obj("")
    suspend fun topics() = obj("/topics")["names"].asList().mapNotNull { it as? String }
    suspend fun updateAbout(desc: String, site: String) {
        require(desc.length <= 350); website(site)
        request("PATCH", "", mapOf("description" to desc, "homepage" to site))
    }
    suspend fun updateTopics(input: String) = request("PUT", "/topics", mapOf("names" to topicNames(input)))
    suspend fun profile() = request("GET", "/user", account = true).asObj() ?: error("Expected profile")
    suspend fun saveProfile(name: String, bio: String, site: String) {
        require(name.length <= 100 && bio.length <= 160); website(site)
        request("PATCH", "/user", mapOf("name" to name, "bio" to bio, "blog" to site), true)
    }
    suspend fun branches() = rows("/branches?per_page=100")
    suspend fun compare(base: String, head: String) = obj("/compare/" + ref(base) + "..." + ref(head))
    suspend fun createBranch(base: String, name: String) {
        val sha = obj("/branches/" + ref(base)).o("commit")?.s("sha") ?: error("Missing SHA")
        require(sha.matches(Regex("[0-9a-fA-F]{40,64}")))
        request("POST", "/git/refs", mapOf("ref" to "refs/heads/" + validateRepoPath(name), "sha" to sha))
    }
    suspend fun deleteBranch(name: String) {
        require(about().s("default_branch") != name)
        require(obj("/branches/" + ref(name))["protected"] == false) { "Protected branch" }
        request("DELETE", "/git/refs/heads/" + encodeRepoPath(name))
    }
    suspend fun mergeRequest(base: String, head: String, title: String) {
        require(base != head && title.isNotBlank())
        request("POST", "/pulls", mapOf("base" to validateRepoPath(base), "head" to validateRepoPath(head), "title" to title))
    }
    suspend fun protection(branch: String) = obj("/branches/" + ref(branch) + "/protection")
    suspend fun pages() = obj("/pages")
    suspend fun savePages(branch: String, folder: String, create: Boolean) {
        require(folder in listOf("/", "/docs"))
        request(if (create) "POST" else "PUT", "/pages", mapOf("source" to mapOf("branch" to validateRepoPath(branch), "path" to folder), "build_type" to "legacy"))
    }
    suspend fun collaborators() = rows("/collaborators?per_page=100")
    suspend fun invite(login: String, permission: String) {
        require(permission in listOf("pull","triage","push","maintain","admin"))
        request("PUT", "/collaborators/" + user(login), mapOf("permission" to permission))
    }
    suspend fun remove(login: String) = request("DELETE", "/collaborators/" + user(login))
    private fun env(name: String) = "/environments/" + part(name)
    private fun vars(name: String) = if (name.isBlank()) "/actions/variables" else env(name) + "/variables"
    private fun secret(name: String) = if (name.isBlank()) "/actions/secrets" else env(name) + "/secrets"
    suspend fun variables(environment: String) = rows(vars(environment) + "?per_page=100", "variables")
    suspend fun saveVariable(environment: String, name: String, value: String, update: Boolean) {
        require(name.matches(Regex("[A-Za-z_][A-Za-z0-9_]{0,127}")) && value.length <= 48000)
        request(if (update) "PATCH" else "POST", vars(environment) + if (update) "/" + part(name) else "", mapOf("name" to name,"value" to value))
    }
    suspend fun deleteVariable(environment: String, name: String) = request("DELETE", vars(environment) + "/" + part(name))
    suspend fun secrets(environment: String) = rows(secret(environment) + "?per_page=100", "secrets")
    suspend fun environments() = rows("/environments?per_page=100", "environments")
    suspend fun environment(name: String) = obj(env(name))
    suspend fun releases() = rows("/releases?per_page=100")
    suspend fun publishDraft(id: Long, tag: String) {
        require(id > 0)
        val draft = obj("/releases/" + id)
        require(draft.b("draft") && draft.s("tag_name") == tag) { "Draft changed; reload" }
        request("PATCH", "/releases/" + id, mapOf("draft" to false))
    }
    suspend fun syncFork(branch: String) {
        require(about().b("fork"))
        request("POST", "/merge-upstream", mapOf("branch" to validateRepoPath(branch)))
    }
    private suspend fun graph(query: String, variables: JObj): JObj {
        val response = request("POST", "/graphql", mapOf("query" to query, "variables" to variables), true).asObj() ?: error("Bad response")
        require(response["errors"].asList().isEmpty()) { "Discussion request failed" }
        return response.o("data") ?: error("Missing data")
    }
    suspend fun discussions() = graph("query(\$owner:String!,\$name:String!){repository(owner:\$owner,name:\$name){id hasDiscussionsEnabled discussionCategories(first:30){nodes{id name}} discussions(first:30){nodes{id number title}}}}",
        mapOf("owner" to full.substringBefore('/'), "name" to full.substringAfter('/'))).o("repository") ?: error("Unavailable")
    suspend fun createDiscussion(repoId: String, categoryId: String, title: String, body: String) =
        graph("mutation(\$input:CreateDiscussionInput!){createDiscussion(input:\$input){discussion{id number}}}", mapOf("input" to mapOf("repositoryId" to repoId,"categoryId" to categoryId,"title" to title,"body" to body)))
    suspend fun commentDiscussion(id: String, body: String) =
        graph("mutation(\$input:AddDiscussionCommentInput!){addDiscussionComment(input:\$input){comment{id}}}", mapOf("input" to mapOf("discussionId" to id,"body" to body)))
    companion object {
        fun part(s: String): String { require(s.isNotBlank() && s.length <= 255 && s !in listOf(".","..") && s.none { it.code < 32 || it == '/' || it == '\\' }); return encodeSegment(s) }
        fun ref(s: String) = encodeSegment(validateRepoPath(s))
        fun user(s: String) = s.trim().also { require(it.matches(Regex("[A-Za-z0-9][A-Za-z0-9-]{0,38}"))) }
        fun website(s: String) { if (s.isNotBlank()) { val u = URI(s); require(u.scheme == "https" && !u.host.isNullOrBlank() && u.userInfo == null) } }
        fun topicNames(input: String) = input.lowercase(java.util.Locale.ROOT).split(Regex("[,\\s]+")).filter { it.isNotBlank() }.distinct().also { require(it.size <= 20 && it.all { t -> t.matches(Regex("[a-z0-9][a-z0-9-]{0,49}")) }) }
    }
}
