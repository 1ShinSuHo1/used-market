package com.wonsu.used_market.transaction.repository;

import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.transaction.domain.Transaction;
import com.wonsu.used_market.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository  extends JpaRepository<Transaction, Long>, TransactionRepositoryCustom {


}
