package com.yan.xpay.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yan.xpay.enums.BlockchainStatus;
import com.yan.xpay.enums.Chain;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Transaction Info
 */
@Data
public class Transaction {

    /**
     * Chain
     */
    private Chain chain;
    /**
     *Symbol
     */
    private String symbol;
    /**
     *Block Number
     */
    private Long blockNum;
    /**
     *txId
     */
    private String txid;
    /**
     *Contract Address
     */
    private String contractAddress;
    /**
     *from
     */
    private String from;
    /**
     *to
     */
    private String to;
    /**
     *Transfer Amount
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;
    /**
     * decimals
     */
    private Integer decimals;
    /**
     *Timestamp
     */
    private Long timestamp;
    /**
     *Blockchain transaction fee
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal txGas;
    /**
     *Confirmed Number
     */
    private Integer confirmedNum = 0;
    /**
     *Status EX. PENDING,SUCCESS,FAILED
     */
    private BlockchainStatus status;

    public static Transaction getTransaction(PaymentOrder order, TxRecord txRecord, int decimals){
        Transaction transaction = new Transaction();
        if(order != null) {
            transaction.setTo(order.getReceiveAddress());
            transaction.setFrom(order.getPayAddress());
            transaction.setAmount(order.getAmount());
            transaction.setChain(order.getChain());
            transaction.setSymbol(order.getSymbol());
            transaction.setDecimals(decimals);
        }
        if(txRecord != null){
            transaction.setTo(txRecord.getToAddress());
            transaction.setStatus(txRecord.getStatus());
            transaction.setFrom(txRecord.getFromAddress());
            transaction.setSymbol(txRecord.getSymbol());
            transaction.setConfirmedNum(txRecord.getConfirmedNum());
            if(txRecord.getAmount() != null)
                transaction.setAmount(txRecord.getAmount());
            transaction.setChain(txRecord.getChain());
            transaction.setTxid(txRecord.getTxId());
            transaction.setContractAddress(txRecord.getContractAddress());
            transaction.setBlockNum(txRecord.getBlockNumber());
            if(txRecord.getTxFee() != null)
                transaction.setTxGas(txRecord.getTxFee());
            transaction.setTimestamp(txRecord.getBlockTime());
            transaction.setDecimals(decimals);
        }
        return transaction;
    }
    public static Transaction getTransaction(CollectRecord collectRecord, int decimals){
        if(collectRecord == null) return null;
        Transaction transaction = new Transaction();
        transaction.setTo(collectRecord.getToAddress());
        transaction.setStatus(collectRecord.getStatus());
        transaction.setFrom(collectRecord.getFromAddress());
        transaction.setSymbol(collectRecord.getSymbol());
        transaction.setConfirmedNum(collectRecord.getConfirmedNum());
        if(collectRecord.getAmount() != null)
            transaction.setAmount(collectRecord.getAmount());
        transaction.setChain(collectRecord.getChain());
        transaction.setTxid(collectRecord.getTxId());
        transaction.setContractAddress(collectRecord.getContractAddress());
        transaction.setBlockNum(collectRecord.getBlockNumber());
        if(collectRecord.getTxFee() != null)
            transaction.setTxGas(collectRecord.getTxFee());
        transaction.setTimestamp(collectRecord.getBlockTime());
        transaction.setDecimals(decimals);
        return transaction;
    }
}
