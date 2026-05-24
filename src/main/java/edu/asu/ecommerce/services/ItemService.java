package edu.asu.ecommerce.services;

import edu.asu.ecommerce.dataaccess.Item_DAO;
import edu.asu.ecommerce.dataaccess.models.Item;
import edu.asu.ecommerce.dataaccess.models.Profile;
import edu.asu.ecommerce.dataaccess.models.User;
import edu.asu.ecommerce.dataaccess.models.User_Info;

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
						  int categoryId, int brandId, String sellerId) throws Exception {
		Item item = new Item(itemName, description, price, quantity, categoryId, brandId, sellerId);

		boolean itemInserted = itemDao.insertItem(item);
		if (!itemInserted) {
			throw new SQLException("item insert failed");
		}

		return item.getId();
	}

	public Item getItemById(String id) throws SQLException {
		if (id == null || id.isEmpty()) {
			return null;
		}

		Item item = itemDao.getItemById(id);
		if (item == null) {
			return null;
		}

		return item;
	}

	public void deleteItem(String itemId) throws SQLException {
		itemDao.deleteItem(itemId);
	}
}
