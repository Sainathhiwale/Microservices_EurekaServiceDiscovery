package com.examen.productcatlog.repository;

import com.examen.productcatlog.domain.ProductMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMasterRepo extends JpaRepository<ProductMaster,Long> {
}
