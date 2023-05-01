package com.example.indoornavigationapp.repository

import com.example.indoornavigationapp.model.Tag

interface TagRepository {

    fun findTagById(id: Int): Tag?
}