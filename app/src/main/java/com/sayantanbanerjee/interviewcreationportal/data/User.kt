package com.sayantanbanerjee.interviewcreationportal.data

/**
 * Generalized structure of a user.
 *
 * @param id: Unique ID of the user.
 * @param name: Name of the user.
 * @param email: Email of the user.
 */

data class User (
        val id: String,
        val name: String,
        val email: String
)
