-- Sales data table creation
-- Purpose: Store actual sales transaction data
-- Dependencies: users and file_uploads tables must exist

USE salesdata;

CREATE TABLE sales_data(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    file_upload_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_price DECIMAL(10,2) NOT NULL,
    sale_location VARCHAR(255),
    sale_date DATE NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (file_upload_id) REFERENCES file_uploads(id) ON DELETE CASCADE
);

-- Add indexes for common queries
CREATE INDEX idx_sales_data_user_id ON sales_data(user_id);
CREATE INDEX idx_sales_data_file_upload_id ON sales_data(file_upload_id);
CREATE INDEX idx_sales_data_sale_date ON sales_data(sale_date);
CREATE INDEX idx_sales_data_product_name ON sales_data(product_name);