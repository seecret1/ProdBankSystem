package com.github.seecret1.transaction_service.service.impl;

import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
import com.github.seecret1.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor //TODO: реализовать
public class TransactionServiceImpl implements TransactionService {

    @Override
    public void process(TransactionMessage message) {

    }
}
