package com.rpn.mosquetime.domain

interface Mapper<Entity, Domain> {
    fun mapToDomain(entity: Entity): Domain
    fun mapToEntity(domain: Domain): Entity
}