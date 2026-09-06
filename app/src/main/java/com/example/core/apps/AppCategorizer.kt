package com.example.core.apps

import com.example.core.model.AppCategory
import java.util.Locale

/**
 * Deterministic local app categorization.
 *
 * v0.1 intentionally uses lightweight package/label heuristics.
 * The categorizer is isolated so a richer local classifier can replace
 * it later without changing AppManager or the drawer UI.
 */
object AppCategorizer {

    fun categorize(
        packageName: String,
        label: String
    ): AppCategory {

        val pkg = packageName.lowercase(Locale.ROOT)
        val name = label.lowercase(Locale.ROOT)

        return when {

            pkg.contains("dialer") ||
                pkg.contains("phone") ||
                pkg.contains("contacts") ||
                pkg.contains("message") ||
                pkg.contains("sms") ||
                pkg.contains("whatsapp") ||
                pkg.contains("telegram") ||
                pkg.contains("signal") ||
                pkg.contains("messenger") ||
                pkg.contains("chat") ||
                name.contains("phone") ||
                name.contains("contacts") ||
                name.contains("messages") ||
                name.contains("whatsapp") ||
                name.contains("chat") ->
                AppCategory.COMMUNICATION

            pkg.contains("instagram") ||
                pkg.contains("tiktok") ||
                pkg.contains("twitter") ||
                pkg.contains("facebook") ||
                pkg.contains("reddit") ||
                pkg.contains("snapchat") ||
                pkg.contains("linkedin") ||
                pkg.contains("threads") ||
                pkg.contains("bereal") ||
                pkg.contains("pinterest") ||
                name.contains("instagram") ||
                name.contains("tiktok") ||
                name.contains("reddit") ||
                name.contains("twitter") ||
                name.contains("facebook") ->
                AppCategory.SOCIAL

            pkg.contains("spotify") ||
                pkg.contains("music") ||
                pkg.contains("youtube") ||
                pkg.contains("video") ||
                pkg.contains("player") ||
                pkg.contains("netflix") ||
                pkg.contains("camera") ||
                pkg.contains("gallery") ||
                pkg.contains("photo") ||
                pkg.contains("twitch") ||
                pkg.contains("podcast") ||
                pkg.contains("sound") ||
                name.contains("camera") ||
                name.contains("gallery") ||
                name.contains("music") ||
                name.contains("photos") ||
                name.contains("youtube") ||
                name.contains("spotify") ->
                AppCategory.MEDIA

            pkg.contains("docs") ||
                pkg.contains("sheets") ||
                pkg.contains("slides") ||
                pkg.contains("drive") ||
                pkg.contains("keep") ||
                pkg.contains("note") ||
                pkg.contains("word") ||
                pkg.contains("excel") ||
                pkg.contains("pdf") ||
                pkg.contains("office") ||
                pkg.contains("mail") ||
                pkg.contains("gmail") ||
                pkg.contains("outlook") ||
                pkg.contains("calendar") ||
                pkg.contains("notion") ||
                pkg.contains("trello") ||
                pkg.contains("zoom") ||
                pkg.contains("teams") ||
                name.contains("notes") ||
                name.contains("calendar") ||
                name.contains("mail") ||
                name.contains("gmail") ||
                name.contains("clock") ||
                name.contains("drive") ->
                AppCategory.WORK

            pkg.contains("game") ||
                pkg.contains("arcade") ||
                pkg.contains("puzzle") ||
                pkg.contains("craft") ||
                pkg.contains("roblox") ||
                pkg.contains("chess") ||
                name.contains("game") ->
                AppCategory.GAMES

            pkg.contains("calc") ||
                pkg.contains("browser") ||
                pkg.contains("chrome") ||
                pkg.contains("firefox") ||
                pkg.contains("maps") ||
                pkg.contains("weather") ||
                pkg.contains("vending") ||
                pkg.contains("store") ||
                pkg.contains("clock") ||
                pkg.contains("recorder") ||
                pkg.contains("file") ||
                pkg.contains("settings") ||
                name.contains("calculator") ||
                name.contains("chrome") ||
                name.contains("browser") ||
                name.contains("maps") ||
                name.contains("weather") ||
                name.contains("files") ||
                name.contains("settings") ->
                AppCategory.TOOLS

            else ->
                AppCategory.SYSTEM
        }
    }
}
