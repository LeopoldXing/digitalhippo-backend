package com.leopoldhsing.digitalhippo.product.repository

import com.leopoldhsing.digitalhippo.model.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
}