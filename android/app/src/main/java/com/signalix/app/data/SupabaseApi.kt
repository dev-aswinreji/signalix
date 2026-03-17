package com.signalix.app.data

import java.net.HttpURLConnection
import java.net.URL

object SupabaseApi {
    private fun conn(url: String): HttpURLConnection {
        val c = URL(url).openConnection() as HttpURLConnection
        c.setRequestProperty("apikey", Supabase.ANON)
        c.setRequestProperty("Authorization", "Bearer ${Supabase.ANON}")
        c.setRequestProperty("Content-Type", "application/json")
        return c
    }

    fun findUser(username: String): String? {
        val c = conn("${Supabase.URL}/rest/v1/users?username=eq.$username")
        val body = c.inputStream.bufferedReader().readText()
        return if (body.contains("\"username\"")) body else null
    }

    fun searchUsers(prefix: String): String {
        val c = conn("${Supabase.URL}/rest/v1/users?username=ilike.${prefix}%")
        return c.inputStream.bufferedReader().readText()
    }

    fun addContact(owner: String, contact: String): Boolean {
        val c = conn("${Supabase.URL}/rest/v1/contacts")
        c.requestMethod = "POST"
        c.doOutput = true
        c.outputStream.use { it.write("{\"owner\":\"$owner\",\"contact\":\"$contact\"}".toByteArray()) }
        return c.responseCode in 200..299
    }

    fun listContacts(owner: String): String {
        val c = conn("${Supabase.URL}/rest/v1/contacts?owner=eq.$owner")
        return c.inputStream.bufferedReader().readText()
    }

    fun sendRequest(sender: String, receiver: String): Boolean {
        val c = conn("${Supabase.URL}/rest/v1/requests")
        c.requestMethod = "POST"
        c.doOutput = true
        c.outputStream.use { it.write("{\"sender\":\"$sender\",\"receiver\":\"$receiver\",\"status\":\"pending\"}".toByteArray()) }
        return c.responseCode in 200..299
    }

    fun listRequests(user: String): String {
        val c = conn("${Supabase.URL}/rest/v1/requests?receiver=eq.$user&status=eq.pending")
        return c.inputStream.bufferedReader().readText()
    }

    fun acceptRequest(id: String): Boolean {
        val c = conn("${Supabase.URL}/rest/v1/requests?id=eq.$id")
        c.requestMethod = "PATCH"
        c.doOutput = true
        c.outputStream.use { it.write("{\"status\":\"accepted\"}".toByteArray()) }
        return c.responseCode in 200..299
    }

    fun sendMessage(sender: String, receiver: String, body: String): Boolean {
        val c = conn("${Supabase.URL}/rest/v1/messages")
        c.requestMethod = "POST"
        c.doOutput = true
        c.outputStream.use { it.write("{\"sender\":\"$sender\",\"receiver\":\"$receiver\",\"body\":\"$body\"}".toByteArray()) }
        return c.responseCode in 200..299
    }

    fun uploadKeyBundle(username: String, identity: String, preKey: String, signedPreKey: String): Boolean {
        val c = conn("${Supabase.URL}/rest/v1/key_bundles")
        c.requestMethod = "POST"
        c.doOutput = true
        c.outputStream.use {
            it.write("{\"username\":\"$username\",\"identity\":\"$identity\",\"prekey\":\"$preKey\",\"signed_prekey\":\"$signedPreKey\"}".toByteArray())
        }
        return c.responseCode in 200..299
    }

    fun getKeyBundle(username: String): String {
        val c = conn("${Supabase.URL}/rest/v1/key_bundles?username=eq.$username")
        return c.inputStream.bufferedReader().readText()
    }

    fun listMessages(user: String, peer: String): String {
        val c = conn("${Supabase.URL}/rest/v1/messages?or=(sender.eq.$user,receiver.eq.$user)&or=(sender.eq.$peer,receiver.eq.$peer)")
        return c.inputStream.bufferedReader().readText()
    }
}
