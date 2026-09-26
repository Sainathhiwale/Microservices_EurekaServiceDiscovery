package com.examen.productcatlog.service;

import com.examen.productcatlog.domain.ProductMaster;
import com.examen.productcatlog.dto.ProductStockUpdateDto;
import com.examen.productcatlog.repository.ProductMasterRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductMasterServices {
    private ProductMasterRepo masterRepo;
    public ProductMaster save(ProductMaster productMaster){

        return masterRepo.save(productMaster);
    }

    public ProductMaster getById(Long id) {
        return masterRepo.findById(id).orElseThrow(()-> new IllegalArgumentException("Id Not found"));
    }
    public Integer getProductStockByID(Long id) {
        return masterRepo.findById(id).orElseThrow(()-> new IllegalArgumentException("Id Not found")).getQuantity();
    }

    public Boolean updateStock(ProductStockUpdateDto productStockUpdateDto) {
        ProductMaster product= masterRepo.findById(productStockUpdateDto.getProductId()).orElseThrow(()-> new IllegalArgumentException("Id Not found"));
        product.setQuantity(product.getQuantity()-productStockUpdateDto.getQty());  //10-1=9
        masterRepo.save(product);
        return true;
    }
}

