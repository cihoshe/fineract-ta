/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanproduct.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Setter
@Entity
@Table(name = "m_loan_product_payment_allocation_rule", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "loan_product_id", "transaction_type" }, name = "uq_m_loan_product_payment_allocation_rule") })
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanProductPaymentAllocationRule extends AbstractAuditableWithUTCDateTimeCustom {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentAllocationTransactionType transactionType;

    @Convert(converter = AllocationTypeListConverter.class)
    @Column(name = "allocation_types", nullable = false)
    private List<PaymentAllocationType> allocationTypes;

    @Enumerated(EnumType.STRING)
    @Column(name = "future_installment_allocation_rule", nullable = false)
    private FutureInstallmentAllocationRule futureInstallmentAllocationRule;
}
