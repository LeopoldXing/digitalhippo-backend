package com.leopoldhsing.digitalhippo.product.service.impl

import com.leopoldhsing.digitalhippo.common.exception.AuthenticationFailedException
import com.leopoldhsing.digitalhippo.common.exception.ResourceNotFoundException
import com.leopoldhsing.digitalhippo.common.mapper.product.ProductMapper
import com.leopoldhsing.digitalhippo.feign.search.ProductSearchingFeignClient
import com.leopoldhsing.digitalhippo.feign.stripe.StripeProductFeignClient
import com.leopoldhsing.digitalhippo.feign.user.UserFeignClient
import com.leopoldhsing.digitalhippo.model.dto.SearchingResultDto
import com.leopoldhsing.digitalhippo.model.dto.SearchingResultIndexDto
import com.leopoldhsing.digitalhippo.model.elasticsearch.ProductIndex
import com.leopoldhsing.digitalhippo.model.entity.Product
import com.leopoldhsing.digitalhippo.model.enumeration.UserRole
import com.leopoldhsing.digitalhippo.model.vo.ProductSearchingConditionVo
import com.leopoldhsing.digitalhippo.product.repository.ProductElasticsearchRepository
import com.leopoldhsing.digitalhippo.product.repository.ProductImageRepository
import com.leopoldhsing.digitalhippo.product.repository.ProductRepository
import com.leopoldhsing.digitalhippo.product.service.ProductCacheService
import com.leopoldhsing.digitalhippo.product.service.ProductService
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ObjectUtils

@Service
open class ProductServiceImpl @Autowired constructor(
    private val productRepository: ProductRepository,
    private val productImageRepository: ProductImageRepository,
    private val productElasticsearchRepository: ProductElasticsearchRepository,
    private val userFeignClient: UserFeignClient,
    private val productSearchingFeignClient: ProductSearchingFeignClient,
    private val stripeProductFeignClient: StripeProductFeignClient,
    private val productCacheService: ProductCacheService
) : ProductService {

    override fun conditionalSearchProducts(condition: ProductSearchingConditionVo): SearchingResultDto {
        // 1. use searching service to get product id list
        val searchResult: SearchingResultIndexDto = productSearchingFeignClient.searchProduct(condition)
        val searchResultIdList: List<Long> = searchResult.results.map { it.id }

        // 2. get product info
        val productList: List<Product> = searchResultIdList.map { id ->
            productRepository.findById(id).orElseThrow { throw ResourceNotFoundException("product", "id", id.toString()) }
        }

        // 3. return result
        val res: SearchingResultDto = SearchingResultDto()
        BeanUtils.copyProperties(searchResult, res)
        res.results = productList
        return res
    }

    override fun getProduct(productId: Long): Product {
        // 1. get product from cache
        val productFromCache = productCacheService.getProductFromCache(productId.toString())
        if (productFromCache != null) {
            // cache hit
            return productFromCache
        }

        // 2. cache miss
        // 2.1 verify this product existence in Product Bitmap
        var product: Product? = null
        if (productCacheService.hasProduct(productId)) {
            // Bitmap shows this product exists
            // 2.1.1 Query product in Postgres
            product = productRepository.findByIdOrNull(productId)
            // 2.1.2 Save this product into cache
            productCacheService.saveProductToCache(productId.toString(), product)
            if (product == null) {
                throw ResourceNotFoundException("product", "id", productId.toString())
            }
        } else {
            // Bitmap shows this product doesn't exist
            // save product data into cache, even the product doesn't exist, this will prevent null value attack
            productCacheService.saveProductToCache(productId.toString(), null)
            // someone is trying to access a product that doesn't exist
            throw ResourceNotFoundException("product", "id", productId.toString())
        }

        // 3. return result
        return product
    }

    @Transactional
    override fun createProduct(product: Product): Product {
        // 1. get user
        val currentUser = userFeignClient.getCurrentUser()
        product.user = currentUser

        // 2. create stripe product
        val stripeProduct = stripeProductFeignClient.createStripeProduct(product)
        product.priceId = stripeProduct.priceId
        product.stripeId = stripeProduct.stripeId

        // 3. save product to postgres
        productRepository.save(product)

        // 4. save product info to elasticsearch
        val productIndex: ProductIndex = ProductMapper.mapToProductIndex(product)
        productElasticsearchRepository.save(productIndex)

        // 5. update bitmap
        productCacheService.addProductToBitmap(product.id)

        return product
    }

    @Transactional
    override fun updateProduct(product: Product): Product {
        // 1. get user
        val currentUser = userFeignClient.currentUser

        // 2. find the product
        val originalProduct = productRepository.findProductByPayloadId(product.payloadId)
        if (originalProduct == null) {
            return createProduct(product)
        }

        // 3. determine if the user has the authority to update this product
        if (currentUser != null && (currentUser.role === UserRole.ADMIN || currentUser.id == originalProduct.user.id)) {
            // user has the authority
            // 4. update product
            product.id = originalProduct.id
            product.user = originalProduct.user
            product.stripeId = originalProduct.stripeId
            product.priceId = originalProduct.priceId

            // 4.1 update images
            productImageRepository.deleteProductImageByIdIn(product.productImages.map { image -> image.id })

            // 4.2 update stripe product
            val updatedProduct: Product = stripeProductFeignClient.updateStripeProduct(originalProduct)
            product.priceId = updatedProduct.priceId

            // 4.3 update postgres
            productRepository.save(product)

            // 4.4 update elasticsearch
            val productIndex: ProductIndex = ProductMapper.mapToProductIndex(product)
            productElasticsearchRepository.save(productIndex)
        } else {
            // user does not have the authority
            throw AuthenticationFailedException(currentUser.id.toString(), currentUser.email)
        }

        return Product()
    }

    @Transactional
    override fun deleteProduct(payloadId: String) {
        // 1. get user
        val currentUser = userFeignClient.getCurrentUser()

        // 2. find the product
        val product = productRepository.findProductByPayloadId(payloadId)

        if (product != null) {
            // 3. determine if the user has the authority to delete this product
            if (currentUser.role === UserRole.ADMIN || product.user?.id == currentUser.id) {
                // current user has authority
                // delete from postgres
                productRepository.delete(product)
                // delete from elasticsearch
                productElasticsearchRepository.deleteById(product.id)
                // update bitmap
                productCacheService.removeProductFromBitmap(product.id)
            } else {
                // current user does not have authority
                throw AuthenticationFailedException(currentUser.id.toString(), currentUser.email)
            }
        }
    }

    override fun getProducts(productIdList: List<Long>): List<Product> {
        val products = productRepository.findProductsByIdIn(productIdList)
        return products
    }
}