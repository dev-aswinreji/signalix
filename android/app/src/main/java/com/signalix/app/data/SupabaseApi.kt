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

    fun sendMessage(sender: String, receiver: String, body: String): Boolean {
        val c = conn("${Supabase.URL}/rest/v1/messages")
        c.requestMethod = "POST"
        c.doOutput = true
        c.outputStream.use { it.write("{\"sender\":\"$sender\",\"receiver\":\"$receiver\",\"body\":\"$body\"}".toByteArray()) }
        return c.responseCode in 200..299
    }

    fun listMessages(user: String, peer: String): String {
        val c = conn("${Supabase.URL}/rest/v1/messages?or=(sender.eq.$user,receiver.eq.$user)&or=(sender.eq.$peer,receiver.eq.$peer)")
        return c.inputStream.bufferedReader().readText()
    }
}
