package com.internet.shop.dao.jdbc;

import com.internet.shop.dao.interfaces.ProductDao;
import com.internet.shop.exceptions.DataBaseConnectionExchangeFailedException;
import com.internet.shop.library.Dao;
import com.internet.shop.model.Product;
import com.internet.shop.util.ConnectionUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Dao
public class ProductDaoJdbcImpl implements ProductDao {
    @Override
    public Product create(Product element) {
        try (Connection connection = ConnectionUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Product(Name, Price) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, element.getName());
            statement.setBigDecimal(2, element.getPrice());
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                element.setId(resultSet.getLong(1));
            }
            return element;
        } catch (SQLException exception) {
            throw new DataBaseConnectionExchangeFailedException("Failed to create the product: "
                    + element.getName(), exception);
        }
    }

    @Override
    public Optional<Product> get(Long id) {
        String query = "SELECT * FROM Product WHERE Deleted = false AND ID = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(extractValue(resultSet));
            }
        } catch (SQLException exception) {
            throw new DataBaseConnectionExchangeFailedException("Failed to get the product "
                    + "with id: "
                    + id, exception);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> getAll() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM Product WHERE Deleted = false";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Product product = extractValue(resultSet);
                products.add(product);
            }
        } catch (SQLException exception) {
            throw new DataBaseConnectionExchangeFailedException("Failed to get data", exception);
        }
        return products;
    }

    @Override
    public Product update(Product element) {
        String query = "UPDATE Product SET Name = ?, Price = ? WHERE ID = ? "
                + "AND Deleted = false";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, element.getName());
            statement.setString(2, String.valueOf(element.getPrice()));
            statement.setString(3, String.valueOf(element.getId()));
            statement.executeUpdate();
            return element;
        } catch (SQLException exception) {
            throw new DataBaseConnectionExchangeFailedException("Failed to update the product: "
                    + element.getName(), exception);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String query = "UPDATE Product SET Deleted = true WHERE ID = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(id));
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataBaseConnectionExchangeFailedException("Failed to delete the product"
                    + " with id: " + id, e);
        }
    }

    @Override
    public boolean delete(Product element) {
        return deleteById(element.getId());
    }

    private Product extractValue(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("ID");
        String name = resultSet.getString("Name");
        BigDecimal price = resultSet.getBigDecimal("Price");
        Product product = new Product(name, price);
        product.setId(id);
        return product;
    }
}
