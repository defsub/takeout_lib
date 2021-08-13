package com.defsub.takeout.client

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*

const val defaultEndpoint = "https://takeout.fm"
const val defaultTimeout = 30*1000L

class Client(endpoint: String = defaultEndpoint) {
    private val baseUrl: String = endpoint
    private var cookie: String? = null
    private var client: HttpClient = client()

    private fun client(timeout: Long = defaultTimeout): HttpClient {
        return HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                    allowSpecialFloatingPointValues = true
                    useArrayPolymorphism = false
                })
            }
            install(HttpTimeout) {
                timeout.let {
                    connectTimeoutMillis = it
                    requestTimeoutMillis = it
                    socketTimeoutMillis = it
                }
            }
        }
    }

    fun close() {
        client.close()
    }

    private suspend inline fun <reified T> get(uri: String, ttl: Int? = 0): T {
        return client.get("$baseUrl$uri") {
            accept(ContentType.Application.Json)
                cookie?.let { header(HttpHeaders.Cookie, "Takeout=$cookie") }
        }
    }

    fun loggedIn(): Boolean {
        return cookie != null
    }

    suspend fun login(user: String, pass: String): Boolean {
        cookie = null
        val client = client()
        val result: LoginResponse = client.post("$baseUrl/api/login") {
            contentType(ContentType.Application.Json)
            body = User(user, pass)
        }
        if (result.status == 200) {
            cookie = result.cookie
        }
        return cookie != null
    }

    suspend fun home(ttl: Int): HomeView {
        return get("/api/home", ttl)
    }

    suspend fun artists(ttl: Int): ArtistsView {
        return get("/api/artists", ttl)
    }

    suspend fun artist(id: Int, ttl: Int): ArtistView {
        return get("/api/artist/$id", ttl)
    }

    suspend fun artistSingles(id: Int, ttl: Int): SinglesView {
        return get("/api/artist/$id/singles", ttl)
    }

    suspend fun artistSinglesPlaylist(id: Int, ttl: Int): Spiff {
        return get("/api/artist/$id/singles/playlist", ttl)
    }

    suspend fun artistPopular(id: Int, ttl: Int): PopularView {
        return get("/api/artist/$id/popular", ttl)
    }

    suspend fun artistPopularPlaylist(id: Int, ttl: Int): Spiff {
        return get("/api/artist/$id/popular/playlist", ttl)
    }

    suspend fun artistPlaylist(id: Int, ttl: Int): Spiff {
        return get("/api/artist/$id/playlist", ttl)
    }

    suspend fun artistRadio(id: Int, ttl: Int): Spiff {
        return get("/api/artist/$id/radio", ttl)
    }

    suspend fun release(id: Int, ttl: Int): ReleaseView {
        return get("/api/releases/$id", ttl)
    }

    suspend fun releasePlaylist(id: Int, ttl: Int): Spiff {
        return get("/api/releases/$id/playlist", ttl)
    }

    suspend fun radio(ttl: Int): RadioView {
        return get("/api/radio", ttl)
    }

    suspend fun station(id: Int, ttl: Int): Spiff {
        return get("/api/radio/$id", ttl)
    }

    suspend fun movies(ttl: Int): MoviesView {
        return get("/api/movies", ttl)
    }

    suspend fun playlist(ttl: Int? = null): Spiff {
        return get("/api/playlist", ttl)
    }
}