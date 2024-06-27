create database dairy;
use dairy;
-- Table: signup
CREATE TABLE signup (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    signup_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: admin
CREATE TABLE admin (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL
);

-- Table: login
CREATE TABLE login (
    login_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES signup(user_id)
);

-- Table: products
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL
);

-- Table: order
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES signup(user_id)
);

-- Table: delivery
CREATE TABLE delivery (
    delivery_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    delivery_status VARCHAR(50) NOT NULL,
    delivery_date TIMESTAMP,
    tracking_number VARCHAR(100),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
CREATE TABLE order_details (
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
INSERT INTO products (product_name, description, price, stock_quantity)
VALUES 
    ('Milk', 'Fresh whole milk, 1 liter', 2.99, 100),
    ('Cheese', 'Cheddar cheese, 200g block', 4.50, 50),
    ('Yogurt', 'Greek yogurt, 500g tub', 3.25, 75),
    ('Butter', 'Salted butter, 250g pack', 3.99, 80),
    ('Cream', 'Whipping cream, 1 pint', 5.25, 60),
    ('Cottage Cheese', 'Low-fat cottage cheese, 400g tub', 2.75, 70),
    ('Sour Cream', 'Sour cream, 200g tub', 2.50, 90),
    ('Mozzarella', 'Mozzarella cheese, 250g pack', 3.75, 55),
    ('Condensed Milk', 'Sweetened condensed milk, 400g can', 4.99, 45),
    ('Feta Cheese', 'Feta cheese, 150g block', 6.50, 40);
