package com.examen.orderservice.repository;

import com.examen.orderservice.domain.OrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderMasterRepo extends JpaRepository<OrderMaster,Long> {

}
