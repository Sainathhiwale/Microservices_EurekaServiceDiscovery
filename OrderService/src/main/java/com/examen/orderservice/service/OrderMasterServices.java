package com.examen.orderservice.service;


import com.examen.orderservice.domain.OrderMaster;
import com.examen.orderservice.dto.ApiResponseDto;
import com.examen.orderservice.dto.ProductStockUpdateDto;
import com.examen.orderservice.exception.ProductUnAvailableException;
import com.examen.orderservice.repository.OrderMasterRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class OrderMasterServices {
    private OrderMasterRepo repo;
    private RestTemplate restTemplate;

    public OrderMaster save(OrderMaster orderMaster){
        int availableQut=getProductAvl(orderMaster.getProductId());
        if (availableQut>0) {
            updateStock(orderMaster.getProductId(),orderMaster.getQuantity());
            return repo.save(orderMaster);
        }
        else throw new ProductUnAvailableException("Out of stock");
    }
     Integer getProductAvl(Long id){
         ResponseEntity<ApiResponseDto> response
                 = restTemplate.getForEntity( "http://ProductCatlog/api/v1/product/stock/"+id, ApiResponseDto.class);
         if (response.getStatusCode().value()==200){
             try {
                 ApiResponseDto apiResponseDto =  response.getBody();
                 assert apiResponseDto != null;
                 return (Integer) apiResponseDto.getData();
             }catch (Exception e){
                 e.printStackTrace();
                 throw new ProductUnAvailableException(e.getMessage());
             }
         }
         return -1;
     }

     boolean updateStock(Long productId,Integer qty){
         HttpEntity<ProductStockUpdateDto> entity=new HttpEntity<>(ProductStockUpdateDto.builder()
                 .productId(productId)
                 .qty(qty).build());
         ResponseEntity<ApiResponseDto> response
                 = restTemplate.exchange( "http://ProductCatlog/api/v1/product/stock", HttpMethod.POST,entity,ApiResponseDto.class);
         return response.getStatusCode().value() == 200;
     }

}
