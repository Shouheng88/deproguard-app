package me.shouheng.deproguard.data

class Event(
    val name: String,
    val data: Any? = null
) {
    companion object {
        const val EVENT_NAME_TO_PAGE = "__event_nav_to_page__"
    }
}