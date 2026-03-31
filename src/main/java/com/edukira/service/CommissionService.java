package com.edukira.service;

import com.edukira.dto.response.CommissionResponse;
import com.edukira.dto.response.CommissionSummaryResponse;
import com.edukira.entity.Commission;
import com.edukira.entity.MarketplaceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommissionService {

    /** Regista comissão após uma venda completada */
    Commission record(MarketplaceOrder order);

    /** Resumo total de comissões (admin) */
    CommissionSummaryResponse summary();

    /** Listagem paginada de comissões (admin) */
    Page<CommissionResponse> listAll(Pageable pageable);

    /** Comissões do vendedor autenticado */
    Page<CommissionResponse> mySeller(Pageable pageable);
}
