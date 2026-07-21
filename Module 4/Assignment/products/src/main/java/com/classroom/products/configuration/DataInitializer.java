package com.classroom.products.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.classroom.products.enums.Category;
import com.classroom.products.enums.Role;
import com.classroom.products.model.AppUser;
import com.classroom.products.model.Customer;
import com.classroom.products.model.Product;
import com.classroom.products.repository.ProductRepository;
import com.classroom.products.repository.UserRepository;

/**
 * Seeds default users and sample products on startup if they are not already
 * present. Credentials are externalized so production environments can supply
 * their own via environment variables. Seeding is idempotent.
 */
@Component
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final PasswordEncoder passwordEncoder;

	private final String adminUsername;
	private final String adminPassword;
	private final String userUsername;
	private final String userPassword;

	public DataInitializer(UserRepository userRepository,
			ProductRepository productRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.seed.admin.username:admin}") String adminUsername,
			@Value("${app.seed.admin.password:password}") String adminPassword,
			@Value("${app.seed.user.username:user}") String userUsername,
			@Value("${app.seed.user.password:password}") String userPassword) {
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.passwordEncoder = passwordEncoder;
		this.adminUsername = adminUsername;
		this.adminPassword = adminPassword;
		this.userUsername = userUsername;
		this.userPassword = userPassword;
	}

	@Override
	public void run(String... args) {
		seedUser(adminUsername, adminPassword, Role.ADMIN);
		seedUser(userUsername, userPassword, Role.USER);
		seedProducts();
	}

	private void seedUser(String username, String rawPassword, Role role) {
		AppUser user = userRepository.findByUsername(username).orElseGet(() -> {
			AppUser created = new AppUser();
			created.setUsername(username);
			created.setPassword(passwordEncoder.encode(rawPassword));
			created.setRole(role);
			created.setEnabled(true);
			return created;
		});

		boolean changed = user.getId() == null;

		// Keep the stored credentials in sync with the configured seed values so
		// changing the seed password takes effect on the next startup, even for
		// users created by an earlier run (data persists via ddl-auto=update).
		if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
			user.setPassword(passwordEncoder.encode(rawPassword));
			changed = true;
		}
		if (user.getRole() != role) {
			user.setRole(role);
			changed = true;
		}
		if (!user.isEnabled()) {
			user.setEnabled(true);
			changed = true;
		}

		// Shoppers (USER role) get a linked Customer profile so they can place
		// orders. This also backfills existing users created before this feature.
		if (role == Role.USER && user.getCustomer() == null) {
			Customer customer = new Customer();
			customer.setName(username);
			customer.setEmail(username + "@example.com");
			user.setCustomer(customer);
			changed = true;
		}

		if (changed) {
			userRepository.save(user);
		}
	}

	private void seedProducts() {
		if (productRepository.count() > 0) {
			return;
		}
		// Laptops
		productRepository.save(newProduct(Category.Laptop, "ThinkPad X1 Carbon", "Lenovo", 1499.00));
		productRepository.save(newProduct(Category.Laptop, "MacBook Air M3", "Apple", 1299.00));
		productRepository.save(newProduct(Category.Laptop, "MacBook Pro 16", "Apple", 2499.00));
		productRepository.save(newProduct(Category.Laptop, "XPS 15", "Dell", 1799.00));
		productRepository.save(newProduct(Category.Laptop, "Spectre x360", "HP", 1399.00));
		productRepository.save(newProduct(Category.Laptop, "ZenBook 14", "Asus", 1099.00));

		// Mobiles
		productRepository.save(newProduct(Category.Mobile, "Galaxy S24", "Samsung", 899.00));
		productRepository.save(newProduct(Category.Mobile, "iPhone 15", "Apple", 999.00));
		productRepository.save(newProduct(Category.Mobile, "iPhone 15 Pro Max", "Apple", 1199.00));
		productRepository.save(newProduct(Category.Mobile, "Pixel 8 Pro", "Google", 999.00));
		productRepository.save(newProduct(Category.Mobile, "OnePlus 12", "OnePlus", 799.00));
		productRepository.save(newProduct(Category.Mobile, "Xperia 1 VI", "Sony", 1099.00));

		// Tablets
		productRepository.save(newProduct(Category.Tablet, "iPad Air", "Apple", 599.00));
		productRepository.save(newProduct(Category.Tablet, "Galaxy Tab S9", "Samsung", 749.00));
		productRepository.save(newProduct(Category.Tablet, "iPad Pro 12.9", "Apple", 1099.00));
		productRepository.save(newProduct(Category.Tablet, "Surface Pro 9", "Microsoft", 999.00));
		productRepository.save(newProduct(Category.Tablet, "Tab P12", "Lenovo", 449.00));
		productRepository.save(newProduct(Category.Tablet, "MatePad Pro", "Huawei", 649.00));
	}

	private Product newProduct(Category category, String productName, String brand, double price) {
		Product product = new Product();
		product.setCategory(category);
		product.setProductName(productName);
		product.setBrand(brand);
		product.setPrice(price);
		return product;
	}
}
