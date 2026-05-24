package edu.asu.ecommerce.services;

import edu.asu.ecommerce.dataaccess.LedgerEntry_DAO;
import edu.asu.ecommerce.dataaccess.LedgerEntry_DAO;
import edu.asu.ecommerce.dataaccess.Order_DAO;
import edu.asu.ecommerce.dataaccess.models.LedgerEntry;
import edu.asu.ecommerce.dataaccess.models.Order; // Updated import

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class OrderService {
    private final Connection conSecure;
    private final Order_DAO orderDao;
    private final LedgerEntry_DAO ledgerDao;

    public OrderService(Connection conSecure) {
        this.conSecure = conSecure;
        this.orderDao = new Order_DAO(conSecure);
        this.ledgerDao = new LedgerEntry_DAO(conSecure);
    }

    public int addOrder(String buyerId, String sellerId, String itemId, double totalPrice, String buyerType) throws Exception {
        // Updated instantiation to use your new secondary constructor
        Order order = new Order(buyerId, sellerId, itemId, totalPrice, buyerType);

        int generatedOrderId = orderDao.insertOrder(order);
        if (generatedOrderId <= 0) {
            throw new SQLException("Order insert failed");
        }
        return generatedOrderId;
    }

    public void addLedgerEntry(String userId, double amount, String transactionType, int orderId) throws Exception {
        LedgerEntry entry = new LedgerEntry(userId, amount, transactionType, orderId, java.time.LocalDate.now());

        boolean inserted = ledgerDao.insertEntry(entry);
        if (!inserted) {
            throw new SQLException("Ledger entry insert failed");
        }
    }

    // public List<LedgerEntry> getUserLedger(String userId) throws SQLException {
    //     return ledgerDao.getLedgerEntriesByUserId(userId);
    // }

    public void processFullOrderTransaction(String buyerId, String sellerId, String itemId, double totalPrice, String buyerType) throws Exception {
        int newOrderId = addOrder(buyerId, sellerId, itemId, totalPrice, buyerType);
        //check for buyer type to choose to generate 1 or 2 ledger entries
        addLedgerEntry(buyerId, totalPrice, "PURCHASE", newOrderId);
        addLedgerEntry(sellerId, totalPrice, "SALE", newOrderId);
    }
}