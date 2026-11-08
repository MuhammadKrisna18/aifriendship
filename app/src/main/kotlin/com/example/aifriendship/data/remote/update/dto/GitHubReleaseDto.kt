package com.example.aifriendship.data.remote.update.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubAssetDto(
    val name: String,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String
)

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name")
    val tagName: String,
    val name: String? = null,
    val body: String? = null,
    val assets: List<GitHubAssetDto> = emptyList()
)

