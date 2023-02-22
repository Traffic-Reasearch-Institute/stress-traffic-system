package com.project.stress_traffic_system.product.repository;

import com.project.stress_traffic_system.product.model.Category;
import com.project.stress_traffic_system.product.model.SubCategory;
import com.project.stress_traffic_system.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.parameters.P;

import javax.persistence.LockModeType;
import java.util.List;


public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Product findByIdWithPessimisticLock(Long id);
}
