package com.products.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.products.dao.ProductDAO;
import com.products.model.Product;
import com.products.util.DBManager;

public class ProductDAOImpl implements ProductDAO {

    private final Product mapToProduct(ResultSet rs) {

        try {

            Product product = new Product();

            product.setId(rs.getInt("id"));
            product.setName(rs.getString("name"));
            product.setPrice(rs.getDouble("price"));
            product.setQuantity(rs.getInt("quantity"));

            return product;

        } catch (SQLException e) {

            throw new RuntimeException(
                "Unable to map ResultSet to Product", e);

        }

    }
    
    @Override
    public int save(Product product) {

        String sql = "INSERT INTO product(name, price, quantity) VALUES (?, ?, ?)";

        try(

            Connection connection = DBManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);

        ) {

            statement.setString(1, product.getName());
            statement.setDouble(2, product.getPrice());
            statement.setInt(3, product.getQuantity());

            return statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException("Unable to save product", e);

        }
    }

    @Override
    public Product findById(int id) {

        String sql = "SELECT * FROM product WHERE id = ?";

        try(

            Connection connection = DBManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);

        ) {

            statement.setInt(1, id);

            try(ResultSet rs = statement.executeQuery()) {

                if(rs.next()) {
                    return mapToProduct(rs);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "Unable to fetch the product", e);

        }

        return null;
    }

    @Override
    public List<Product> findAll() {

        List<Product> products = new ArrayList<Product>();
        String sql = "SELECT * FROM product";

        try(

            Connection connection = DBManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

        ) {

            while(rs.next()) {
                products.add(mapToProduct(rs));
            }

        } catch (SQLException e) {

            throw new RuntimeException("Unable to fetch products", e);
        }

        return products;
    }

    @Override
    public int update(Product product) {

        String sql = 
                "UPDATE product " + 
                "SET name = ?, price = ?, quantity = ? " +
                "WHERE id = ?";

        try(
            
            Connection connection = DBManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);

        ) {

            statement.setString(1, product.getName());
            statement.setDouble(2, product.getPrice());
            statement.setInt(3, product.getQuantity());
            statement.setInt(4, product.getId());

            return statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException("Unable to update the product", e);

        }

    }

    @Override
    public int deleteById(int id) {

        String sql = "DELETE FROM product WHERE id = ?";

        try(

            Connection connection = DBManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);

        ) {

            statement.setInt(1, id);

            return statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException("Unable to delete the product", e);

        }
    }

}
