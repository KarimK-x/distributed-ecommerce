package edu.asu.ecommerce.services;

import edu.asu.ecommerce.dataaccess.Item_DAO;
import edu.asu.ecommerce.dataaccess.models.Item;

import java.sql.Connection;
import java.sql.SQLException;

public class ItemService {
	private final Connection conGlobal;

	private final Item_DAO itemDao;

	public ItemService(Connection conGlobal) {
		this.conGlobal = conGlobal;

		this.itemDao = new Item_DAO(conGlobal);
	}

	public String addItem(String itemName, String description, double price, int quantity,
						  int categoryId, int brandId) throws Exception {
		Item item = new Item(itemName, description, price, quantity, categoryId, brandId);

		boolean itemInserted = itemDao.insertItem(item);
		if (!itemInserted) {
			throw new SQLException("item insert failed");
		}

		return item.getId();
	}

	public void deleteItem(String itemId) throws SQLException {
		itemDao.deleteItem(itemId);
	}
}
