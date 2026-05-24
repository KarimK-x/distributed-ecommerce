package edu.asu.ecommerce.services;

import edu.asu.ecommerce.dataaccess.Item_DAO;
import edu.asu.ecommerce.dataaccess.UserInventory_DAO;
import edu.asu.ecommerce.dataaccess.models.Item;
import edu.asu.ecommerce.dataaccess.models.Profile;
import edu.asu.ecommerce.dataaccess.models.User;
import edu.asu.ecommerce.dataaccess.models.User_Info;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

	// public Item getItemById(String itemId) throws SQLException {
	// 	return itemDao.getItemById(itemId);
	// }

	public void updateItem(String itemId, String itemName, String description, double price, int quantity,
						   int categoryId, int brandId) throws Exception {
		Item existing = itemDao.getItemById(itemId);
		if (existing == null) {
			throw new Exception("item not found");
		}
		existing.setItemName(itemName);
		existing.setDescription(description);
		existing.setPrice(price);
		existing.setQuantity(quantity);
		existing.setCategoryId(categoryId);
		existing.setBrandId(brandId);
		boolean updated = itemDao.updateItem(existing);
		if (!updated) {
			throw new SQLException("item update failed");
		}
	}

	public List<Item> searchAvailableItems(String nameQuery, String brandQuery,
										   Connection conNorth, Connection conSouth) throws SQLException {
		List<Item> candidates = itemDao.searchByNameAndBrand(nameQuery, brandQuery);
		UserInventory_DAO northDao = new UserInventory_DAO(conNorth);
		UserInventory_DAO southDao = new UserInventory_DAO(conSouth);
		List<Item> availableItems = new ArrayList<>();
		for (Item item : candidates) {
			if (northDao.isItemListedAsAvailable(item.getId()) || southDao.isItemListedAsAvailable(item.getId())) {
				availableItems.add(item);
			}
		}
		return availableItems;
	}
}
